package yeezus.pcb;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import yeezus.memory.Cache;
import yeezus.memory.MMU;
import yeezus.memory.Memory;

import java.util.Iterator;

/**
 * A class to hold various information relating to the processes run by the Yeezus Operating System. The data held by
 * these instances are to be used by the {@link yeezus.driver.Driver} and related classes for their interactions with
 * the processes stored in {@link yeezus.memory.Memory}.
 *
 * @author Mark Zeagler
 * @author Jessica Brummel
 * @version 2.0
 */
public class PCB {

	private final int pid, startDiskAddress, instructionsLength, inputBufferLength, outputBufferLength, tempBufferLength, priority;
	private int cpuID = -1;
	private int pc;
	private int executionCount;
	private int numIO = 0;
	private int ramUsed;
	private long clock, elapsedWaitTime, elapsedRunTime;
	private Status status;
	private Memory cache, registers;
	private PageTable pageTable = null;
	/**
	 * Constructs a PCB with the given characteristics.
	 *
	 * @param pid                The PCB ID of the new PCB.
	 * @param startDiskAddress   The start address of the Instructions on the disk.
	 * @param instructionsLength The the amount of Instructions on the disk.
	 * @param inputBufferLength  The size of the Input Buffer on the disk.
	 * @param outputBufferLength The size of the Output Buffer on the disk.
	 * @param tempBufferLength   The size of the Temp Buffer on the disk.
	 * @param priority           The given priority of the PCB.
	 */
	PCB( int pid, int startDiskAddress, int instructionsLength, int inputBufferLength, int outputBufferLength,
			int tempBufferLength, int priority ) {
		this.clock = System.nanoTime();
		this.elapsedWaitTime = 0;
		this.elapsedRunTime = 0;
		this.status = Status.NEW;
		this.pid = pid;
		this.startDiskAddress = startDiskAddress;
		this.instructionsLength = instructionsLength;
		this.inputBufferLength = inputBufferLength;
		this.outputBufferLength = outputBufferLength;
		this.tempBufferLength = tempBufferLength;
		this.priority = priority;
	}

	public int getRAMUsed() {
		return this.ramUsed;
	}

	public void setRAMUsed( int ramUsed ) {
		this.ramUsed = ramUsed;
	}

	/**
	 * Retrieves the saved Program Counter of this process. This is only to be used in swapping.
	 *
	 * @return The saved Program Counter for this process.
	 */
	public int getPC() {
		return this.pc;
	}

	/**
	 * Saves the Program Counter for this process.
	 *
	 * @param pc The Program Counter to be saved.
	 */
	public void setPC( int pc ) {
		this.pc = pc;
	}

	/**
	 * Retrieves the saved cache of this process. This is only to be used in swapping.
	 *
	 * @return The saved cache for this process.
	 */
	public Memory getCache() {
		return this.cache;
	}

	/**
	 * Saves the cache for this process. This is only to be used in swapping.
	 *
	 * @param cache The cache to be saved.
	 */
	public void setCache( @Nullable Cache cache ) {
		if ( cache == null ) {
			this.cache = null;
			return;
		}
		this.cache = new Memory( cache.getCapacity() - ( cache.getWritablePagesCount() * MMU.FRAME_SIZE ) );
		for ( int i = 0; i < this.getTempBufferLength(); i++ ) {
			try {
				this.cache.write( i, cache.read( this, getTempBufferDiskAddress() - getStartDiskAddress() + i ) );
			} catch ( MMU.PageFault pageFault ) {
				System.err.println( "This shouldn't be happening when we're only accessing the temp data..." );
				pageFault.printStackTrace();
			}
		}
	}

	/**
	 * Retrieves the saved Registers of this process. This is only to be used in swapping.
	 *
	 * @return The saved Registers for this process.
	 */
	public Memory getRegisters() {
		return this.registers;
	}

	/**
	 * <p>Saves the registers for this process. This is only to be used in swapping.</p><p><b>NOTE:</b> This method
	 * makes a copy of the given registers so the source registers can be changed without corrupting the saved
	 * data.</p>
	 *
	 * @param registers The registers to be saved.
	 */
	public void setRegisters( @Nullable Memory registers ) {
		if ( registers == null ) {
			this.registers = null;
			return;
		}
		this.registers = new Memory( registers.getCapacity() );
		for ( int i = 0; i < registers.getCapacity(); i++ ) {
			this.registers.write( i, registers.read( i ) );
		}
	}

	/**
	 * Retrieves the CPUID of the CPU that this process is running on.
	 *
	 * @return The CPUID of the CPU that this process is running on.
	 */
	public synchronized int getCPUID() {
		return this.cpuID;
	}

	/**
	 * Sets the CPU ID to the given value to indicate that the process is running on that CPU. A negative value
	 * indicates that the process is not on the CPU.
	 *
	 * @param cpuid The CPU that the process is running on.
	 */
	public synchronized void setCPUID( int cpuid ) {
		this.cpuID = cpuid;
	}

	/**
	 * The amount of IO operations for this process.
	 *
	 * @return The number of IO operations in this process.
	 */
	public int getNumIO() {
		return this.numIO;
	}

	/**
	 * Increases the count for I/O operations by 1.
	 */
	public void incNumIO() {
		++this.numIO;
	}

	/**
	 * The amount of instructions for this process.
	 *
	 * @return The number of instructions in this process.
	 */
	public int getInstructionsLength() {
		return this.instructionsLength;
	}

	/**
	 * Retrieves the starting address on the Disk for the instructions of this process.
	 *
	 * @return The starting address on the Disk of this process's instructions.
	 */
	public int getInstructionDiskAddress() {
		return this.startDiskAddress;
	}

	/**
	 * The size of the input buffer for this process.
	 *
	 * @return The size of the input buffer for this process.
	 */
	public int getInputBufferLength() {
		return this.inputBufferLength;
	}

	/**
	 * Retrieves the starting address on the Disk for the input buffer of this process.
	 *
	 * @return The starting address on the Disk of this process's input buffer.
	 */
	public int getInputBufferDiskAddress() {
		return this.startDiskAddress + this.instructionsLength;
	}

	/**
	 * The size of the output buffer for this process.
	 *
	 * @return The size of the output buffer for this process.
	 */
	public int getOutputBufferLength() {
		return this.outputBufferLength;
	}

	/**
	 * Retrieves the starting address on the Disk for the output buffer of this process.
	 *
	 * @return The starting address on the Disk of this process's output buffer.
	 */
	public int getOutputBufferDiskAddress() {
		return getInputBufferDiskAddress() + this.inputBufferLength;
	}

	/**
	 * The size of the temp buffer for this process.
	 *
	 * @return The size of the temp buffer for this process.
	 */
	public int getTempBufferLength() {
		return this.tempBufferLength;
	}

	/**
	 * Retrieves the starting address on the Disk for the temp buffer of this process.
	 *
	 * @return The starting address on the Disk of this process's temp buffer.
	 */
	public int getTempBufferDiskAddress() {
		return getOutputBufferDiskAddress() + this.outputBufferLength;
	}

	public int getTempBufferLogicalAddress() {
		return this.instructionsLength + this.inputBufferLength + this.outputBufferLength;
	}

	/**
	 * Retrieves the total size that this process requires in memory. This is a summation of the instruction, input
	 * buffer, output buffer, and temp buffer lengths.
	 *
	 * @return The total amount of memory required for this process.
	 */
	public int getTotalSize() {
		return this.instructionsLength + this.inputBufferLength + this.outputBufferLength + this.tempBufferLength;
	}

	/**
	 * Retrieves the PID of this PCB.
	 *
	 * @return The PID associated with this PCB.
	 */
	public int getPID() {
		return this.pid;
	}

	/**
	 * Retrieves the start address of this PCB's instructions on the disk.
	 *
	 * @return The start address of this PCB's instructions on the disk.
	 */
	public int getStartDiskAddress() {
		return this.startDiskAddress;
	}

	/**
	 * Retrieves the priority of this PCB.
	 *
	 * @return The priority of this PCB.
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * Retrieves the current status of the PCB.
	 *
	 * @return The current status of the PCB.
	 */
	public synchronized Status getStatus() {
		return this.status;
	}

	/**
	 * Sets the new status of the PCB.
	 *
	 * @param status The new status of the PCB.
	 */
	public synchronized void setStatus( @NotNull Status status ) {
		if ( this.status == Status.TERMINATED ) {
			return;
		}
		// System.out.println( "Process " + pid + " status set to " + status );
		long timestamp = System.nanoTime();
		long elapsedTime = timestamp - this.clock;
		if ( this.status == Status.RUNNING ) {
			this.elapsedRunTime += elapsedTime;
		} else {
			this.elapsedWaitTime += elapsedTime;
		}
		this.status = status;
		this.clock = timestamp;
	}

	/**
	 * Retrieves the elapsed amount of time that this PCB has been waiting to be run on the CPU.
	 *
	 * @return the elapsed amount of time this PCB has been waiting on the CPU.
	 */
	public synchronized long getElapsedWaitTime() {
		return this.elapsedWaitTime + ( this.status != Status.RUNNING && this.status != Status.TERMINATED ?
				System.nanoTime() - this.clock :
				0 );
	}

	/**
	 * Retrieves the elapsed amount of time that this PCB has been running on the CPU.
	 *
	 * @return the elapsed amount of time this PCB has been running on the CPU.
	 */
	public synchronized long getElapsedRunTime() {
		return this.elapsedRunTime + ( this.status == Status.RUNNING ? System.nanoTime() - this.clock : 0 );
	}

	/**
	 * Retrieves the count of instructions that have been executed for this process.
	 *
	 * @return How many times the instructions of this process have been executed.
	 */
	public int getExecutionCount() {
		return this.executionCount;
	}

	/**
	 * Increases the execution count by 1.
	 */
	public void incExecutionCount() {
		++this.executionCount;
	}

	/**
	 * Generates a new page table, replacing the old one if it exists.
	 *
	 * @param pageSize The page size of the memory, which determines the number of pages this process will allocate.
	 */
	public void generatePageTable( int pageSize ) {
		this.pageTable = new PageTable( (int) Math.ceil( (double) getTotalSize() / pageSize ) );
	}

	/**
	 * Retrieves the current page table, if it exists.
	 *
	 * @return The current page table, or {@code null} if it has not yet been generated.
	 */
	public PageTable getPageTable() {
		return this.pageTable;
	}

	/**
	 * <p>An enumeration of the different statuses that this process will set as. </p> <p>{@link Status#NEW}: Indicates
	 * that the process has been created, but is not yet ready to be run.</p><p>{@link Status#READY}: Indicates that the
	 * process has been loaded into RAM and is ready to be run.</p><p>{@link Status#RUNNING}: Indicates that the process
	 * is being executed by a CPU.</p><p>{@link Status#WAITING}: Indicates that the process required an I/O event, and
	 * is currently waiting on it to be completed.</p><p>{@link Status#TERMINATED}: Indicates that the process has
	 * completed its execution.</p>
	 */
	public enum Status {
		/**
		 * Indicates that the process has been created, but is not yet ready to be run.
		 */
		NEW, /**
		 * Indicates that the process is being executed by a CPU.
		 */
		RUNNING, /**
		 * Indicates that the process required an I/O event, and is currently waiting on it to be completed.
		 */
		WAITING, /**
		 * Indicates that the process has been loaded into RAM and is ready to be run.
		 */
		READY, /**
		 * Indicates that the process has completed its execution.
		 */
		TERMINATED
	}

	public static class PageTable implements Iterable<Integer> {
		Page[] pages;

		private PageTable( int size ) {
			this.pages = new Page[size];
			for ( int i = 0; i < size; i++ ) {
				this.pages[i] = new Page();
			}
		}

		public synchronized int getAddress( int pageNumber ) {
			Page page = this.pages[pageNumber];
			return ( page.isValid ? page.physicalAddress : -1 );
		}

		public synchronized void setAddress( int pageNumber, int address ) {
			Page page = this.pages[pageNumber];
			page.physicalAddress = address;
			page.isValid = true;
		}

		public synchronized void clearAddress( int pageNumber ) {
			this.pages[pageNumber].isValid = false;
		}

		@Override public Iterator<Integer> iterator() {
			return new Iterator<Integer>() {
				int index = 0;

				@Override public boolean hasNext() {
					return this.index < PageTable.this.pages.length - 1;
				}

				@Override public Integer next() {
					return getAddress( this.index++ );
				}

				@Override public void remove() {
					PageTable.this.pages[this.index - 1].isValid = false;
				}

				public int getIndex() {
					return this.index;
				}
			};
		}
	}

	private static class Page {
		private int physicalAddress;
		private boolean isValid = false;
	}
}
