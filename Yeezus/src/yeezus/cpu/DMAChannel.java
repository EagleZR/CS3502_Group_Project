package yeezus.cpu;

import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A component for handling Input and Output instructions within the {@link yeezus} operating system, meant to run in a
 * separate thread, parallel to the Driver thread and the CPU threads.
 *
 * @author Jessica Brummel
 * @author Mark Zeagler
 * @version 2.0
 */
public class DMAChannel implements Runnable {

	private final Queue<Job> jobQueue;
	private final InputCache inputCache;
	private MMU mmu;
	private boolean keepRunning;

	/**
	 * Creates a new DMA Channel. If it is to be run in a separate thread, the {@code continuous} parameter should be
	 * set to {@link true}.
	 *
	 * @param mmu        The MMU which manages the RAM this will input/output from.
	 * @param iCacheSize The size of the input cache to be maintained by the DMA Channel.
	 * @param continuous Sets whether this runs continuously or only one time per call. Set to {@link true} if this is
	 *                   meant to be run in a separate thread, or {@code false} if it will be run in the main thread.
	 */
	public DMAChannel( MMU mmu, int iCacheSize, boolean continuous ) {
		this.mmu = mmu;
		this.jobQueue = new LinkedList<>();
		this.inputCache = new InputCache( iCacheSize );
		this.keepRunning = continuous;
	}

	/**
	 * Provides a new instruction for the DMA Channel to execute.
	 *
	 * @param instruction The instruction to be executed by the DMA Channel.
	 * @param pcb         The PCB of the process whose instruction is to be executed.
	 * @param registers   The registers which contain the process's data.
	 */
	void handle( ExecutableInstruction.IOExecutableInstruction instruction, PCB pcb, Memory registers ) {
		this.jobQueue.add( ( instruction.type == InstructionSet.RD ?
				new IJob( pcb, instruction, registers ) :
				new OJob( pcb, instruction, registers ) ) );
	}

	/**
	 * Retrieves input from the DMA Channel's input cache to the given registers, according to the PID of the given PCB
	 * and the register address specified by the given instruction.
	 *
	 * @param instruction The instruction which specifies this input operation.
	 * @param pcb         The PCB associated with the process whose instruction is being executed.
	 * @param registers   The registers into which the input will be loaded.
	 * @return {@code true} if the data was successfully written to the registers.
	 */
	boolean retrieveInput( ExecutableInstruction.IOExecutableInstruction instruction, PCB pcb, Memory registers ) {
		// This method doesn't need to be synchronized because the cache, the only shared data, is synchronized.
		if ( instruction.type == InstructionSet.RD ) {
			Word data = this.inputCache.read( pcb );
			if ( data != null ) {
				//reading address into reg1
				if ( instruction.reg2 == 0 && instruction.address != 0 ) {
					registers.write( instruction.reg1, data );
					return true;
				}
				//reading reg2 into reg1
				else {
					registers.write( instruction.reg1, data );
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Notifies the DMA Channel to stop running so its thread can be joined.
	 */
	public synchronized void scheduleShutdown() {
		this.keepRunning = false;
	}

	private synchronized boolean KeepRunning() {
		return this.keepRunning;
	}

	@Override public void run() {
		while ( KeepRunning() ) {
			if ( !this.jobQueue.isEmpty() ) {
				Job job = this.jobQueue.peek(); // Shouldn't be null cause we checked if it was empty, right?
				PCB pcb = job.pcb;
				ExecutableInstruction.IOExecutableInstruction instruction = job.instruction;
				boolean success = false;

				try {
					// RD Operation
					if ( instruction.type == InstructionSet.RD ) {
						IJob iJob = (IJob) job;
						//reading address into reg1
						if ( instruction.reg2 == 0 && instruction.address != 0 ) {
							success = this.inputCache.write( pcb, this.mmu.read( pcb, instruction.address / 4 ) );
						}
						//reading reg2 into reg1
						else {
							success = this.inputCache.write( pcb, this.mmu.read( pcb, iJob.offset ) );
						}
					}
					//WR operation
					else if ( instruction.type == InstructionSet.WR ) {
						OJob oJob = (OJob) job;
						//writing register 1 to address
						if ( instruction.reg2 == 0 && instruction.address != 0 ) {
							this.mmu.write( pcb, instruction.address / 4, oJob.data );
						}
						//writing register 1 to register 2
						else {
							this.mmu.write( pcb, oJob.offset, oJob.data );
						}
						success = true;
					}
				} catch ( MMU.PageFault pageFault ) {
					// TODO Handle page faults in DMA Channel
				}

				// Remove jobs if they were successfully completed
				if ( success ) {
					pcb.incNumIO();
					pcb.setStatus( PCB.Status.READY );
					TaskManager.INSTANCE.getReadyQueue().add( pcb );
					this.jobQueue.remove( job );
				}
			}
		}
	}

	/**
	 * A simple storage structure made to store data about input and output jobs.
	 */
	private abstract class Job {
		final int offset;
		private final PCB pcb;
		private final ExecutableInstruction.IOExecutableInstruction instruction;

		private Job( PCB pcb, ExecutableInstruction.IOExecutableInstruction instruction, Memory registers ) {
			this.pcb = pcb;
			this.instruction = instruction;
			this.offset = (int) registers.read( instruction.reg2 ).getData() / 4;
		}
	}

	/**
	 * An extension of the {@link Job} class, specifically made to store data about output jobs.
	 */
	private class OJob extends Job {
		private final Word data;

		private OJob( PCB pcb, ExecutableInstruction.IOExecutableInstruction instruction, Memory registers ) {
			super( pcb, instruction, registers );
			this.data = registers.read( instruction.reg1 );
		}
	}

	/**
	 * An extension of the {@link Job} class, specifically made to store data about input jobs.
	 */
	private class IJob extends Job {

		private IJob( PCB pcb, ExecutableInstruction.IOExecutableInstruction instruction, Memory registers ) {
			super( pcb, instruction, registers );
		}
	}

	/**
	 * A wrapper class for a {@link Memory} instance used to represent an input cache for the DMA Channel. It provides
	 * synchronized access to the stored input data, and associates each input with the process it is for.
	 */
	private class InputCache {
		private final Memory iCache;
		private final int[] iCacheID; // The PID associated with the contents of a certain iCache location

		private InputCache( int iCacheSize ) {
			this.iCache = new Memory( iCacheSize );
			this.iCacheID = new int[iCacheSize];
			for ( int i = 0; i < iCacheSize; i++ ) {
				this.iCacheID[i] = -1;
			}
		}

		private synchronized boolean write( PCB pcb, Word data ) {
			for ( int i = 0; i < this.iCacheID.length; i++ ) {
				if ( this.iCacheID[i] == -1 ) {
					this.iCacheID[i] = pcb.getPID();
					this.iCache.write( i, data );
					return true;
				}
			}
			return false;
		}

		private synchronized Word read( PCB pcb ) {
			for ( int i = 0; i < this.iCacheID.length; i++ ) {
				if ( this.iCacheID[i] == pcb.getPID() ) {
					this.iCacheID[i] = -1;
					return this.iCache.read( i );
				}
			}
			return null;
		}
	}
}
