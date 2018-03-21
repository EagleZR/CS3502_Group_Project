package yeezus.cpu;

import yeezus.DuplicateIDException;
import yeezus.memory.*;
import yeezus.pcb.PCB;

import java.util.ArrayList;

/**
 * This class emulates some of the CPU's actions in the {@link yeezus} Operating System. Once it has been assigned a
 * {@link PCB}, it runs until the associated process has been terminated. The CPU fetches instructions from the {@link
 * MMU}, decodes them, and executes them. Changes to the process data are reflected in the RAM.
 */
public class CPU implements Runnable {

	private static final ArrayList<Integer> cpuids = new ArrayList<>();
	private final int cpuid;
	private final Memory registers;
	private final Memory cache;
	private DMAChannel dmaChannel;
	private PCB pcb;
	private int pc;
	private ExecutableInstruction previousInstruction;

	/**
	 * Constructs a new CPU from the given parameters.
	 *
	 * @param cpuid        The ID of the new CPU. <b>NOTE: This must be a unique value.</b>
	 * @param mmu          The MMU that manages this system's RAM.
	 * @param registerSize The amount of registers to be used by this CPU.
	 * @param cacheSize    The size of the cache to be used by this CPU.
	 * @throws DuplicateIDException Thrown if the given CPU ID is not unique.
	 */
	public CPU( int cpuid, MMU mmu, int registerSize, int cacheSize )
			throws DuplicateIDException, InvalidWordException {
		if ( cpuids.contains( cpuid ) ) {
			throw new DuplicateIDException( "The CPU ID " + cpuid + " already exists in this system." );
		}
		// System.out.println( "Creating CPU " + cpuid );
		this.cpuid = cpuid;
		cpuids.add( cpuid );

		this.registers = new Memory( registerSize );
		this.cache = new Memory( cacheSize );
		this.dmaChannel = new DMAChannel( mmu, registers );
	}

	/**
	 * Used to reset the state of the CPU in testing.
	 */
	public static void reset() {
		cpuids.clear();
		// TODO Reset everything
	}

	/**
	 * Retrieves the CPU ID for this CPU instance.
	 *
	 * @return The CPU ID associated with this CPU.
	 */
	public int getCPUID() {
		return this.cpuid;
	}

	/**
	 * Retrieves the {@link PCB} of the process currently associated with this CPU.
	 *
	 * @return The {@link PCB} of the process associated with this CPU.
	 */
	public PCB getProcess() {
		return this.pcb;
	}

	/**
	 * Sets a new process for this CPU.
	 *
	 * @param pcb The {@link PCB} of the new process to be run by this CPU.
	 */
	public void setProcess( PCB pcb ) {
		this.pcb = pcb;
		this.pcb.setStatus( PCB.Status.RUNNING );
		this.pc = 0;
	}

	/**
	 * Executes the process that is currently associated with this CPU. The process will continue to execute until it
	 * has completed.
	 *
	 * @throws InvalidInstructionException Thrown if the fetched Instruction could not be successfully decoded.
	 * @throws InvalidWordException        Thrown if there was an issue with storing new data in the execution of the
	 *                                     instruction.
	 * @throws ExecutionException          Thrown if the CPU attempts to execute an I/O instruction (the DMA-Channel
	 *                                     should handle that).
	 * @throws InvalidAddressException     Thrown if an instruction tries to access an invalid address in memory.
	 */
	public void run() {
		if ( this.pcb == null ) {
			// Do nothing
			// System.out.println( "CPU " + this.cpuid + " has nothing to do." );
			return;
		}
		// System.out.println( "CPU " + this.cpuid + " is executing." );
		while ( true ) {
			// Fetch
			if ( pc >= 100 ) {
				StringBuilder errorReport = new StringBuilder(
						"PC " + pc + " is invalid.\nPID: " + pcb.getPID() + "\nInstruction Count: " + pcb
								.getExecutionCount() + "\nPrevious Instruction: " + this.previousInstruction
								+ "\nRegisters: " );
				for ( int i = 0; i < this.registers.getCapacity(); i++ ) {
					errorReport.append( "\n\t" + this.registers.read( i ) );
				}
				System.err.println( errorReport );
				pcb.setStatus( PCB.Status.TERMINATED );
				return;
			}
			Word instruction = this.cache.read( this.pc++ );

			// Decode
			ExecutableInstruction executableInstruction = decode( instruction );

			// Execute
			this.pcb.setExecutionCount( this.pcb.getExecutionCount() + 1 );

			if ( executableInstruction.type == InstructionSet.HLT ) {
				this.pcb.setStatus( PCB.Status.TERMINATED );
				return;
			}

			if ( executableInstruction.getClass() == ExecutableInstruction.IOExecutableInstruction.class ) {
				this.dmaChannel
						.handle( (ExecutableInstruction.IOExecutableInstruction) executableInstruction, this.pcb );
			} else {
				executableInstruction.run();
			}

			this.previousInstruction = executableInstruction;
		}
	}

	// For testing
	public void debugRun() {
		if ( this.pcb == null ) {
			// Do nothing
			return;
		}
		// Fetch
		Word instruction = this.cache.read( this.pc++ );

		// Decode
		ExecutableInstruction executableInstruction = decode( instruction );

		// Execute
		if ( executableInstruction.type == InstructionSet.HLT ) {
			this.pcb.setStatus( PCB.Status.TERMINATED );
			return;
		}

		if ( executableInstruction.getClass() == ExecutableInstruction.IOExecutableInstruction.class ) {
			this.dmaChannel.handle( (ExecutableInstruction.IOExecutableInstruction) executableInstruction, this.pcb );
		} else {
			executableInstruction.run();
		}
	}

	/**
	 * Retrieves the registers used by this CPU.
	 *
	 * @return The registers used by this CPU.
	 */
	public Memory getRegisters() {
		return registers;
	}

	/**
	 * Retrieves the cache used by this CPU.
	 *
	 * @return The cache used by this CPU.
	 */
	public Memory getCache() {
		return cache;
	}

	/**
	 * Decodes the given {@link Word} into an {@link ExecutableInstruction} that the CPU can then execute.
	 *
	 * @param word The data to be decoded.
	 * @return An {@link ExecutableInstruction} containing the details of the decoded data in a form that's ready to be
	 * executed by the CPU.
	 * @throws InvalidInstructionException Thrown if the given {@link Word} cannot be successfully decoded into a known
	 *                                     instruction type.
	 */
	ExecutableInstruction decode( Word word ) throws InvalidInstructionException {
		long signature = word.getData() & 0xC0000000;
		if ( signature == 0x00000000 ) {
			return new ExecutableInstruction.ArithmeticExecutableInstruction( word, this.registers );
		} else if ( signature == 0x40000000 ) {
			return new ExecutableInstruction.ConditionalExecutableInstruction( word, this.registers, this.cache,
					( Integer value ) -> {
						if ( value != -1 ) {
							this.pc = value;
						}
					} );
		} else if ( signature == 0x80000000 ) {
			return new ExecutableInstruction.UnconditionalJumpExecutableInstruction( word, this.registers,
					( Integer value ) -> {
						if ( value != -1 ) {
							this.pc = value;
						}
					} );
		} else {
			return new ExecutableInstruction.IOExecutableInstruction( word, this.registers );
		}
	}
}
