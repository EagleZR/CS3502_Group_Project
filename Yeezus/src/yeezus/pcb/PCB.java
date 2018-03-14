package yeezus.pcb;

import yeezus.memory.Memory;

/**
 * A class to hold various information relating to the processes run by the Yeezus Operating System. The data held by
 * these instances are to be used by the {@link yeezus.driver.Driver} and related classes for their interactions with
 * the processes stored in {@link yeezus.memory.Memory}.
 */
public class PCB {

	private final int pid, startDiskAddress, instructionsLength, inputBufferLength, outputBufferLength, tempBufferLength, priority;
	private int cpuid, pc;
	private long clock;
	private long elapsedWaitTime;
	private long elapsedRunTime;
	private Status status;

	public int getPC() {
		return pc;
	}

	public void setPC( int pc ) {
		this.pc = pc;
	}

	public Memory getCache() {
		return cache;
	}

	public void setCache( Memory cache ) {
		this.cache = cache;
	}

	public Memory getRegisters() {
		return registers;
	}

	public void setRegisters( Memory registers ) {
		this.registers = registers;
	}

	private Memory cache, registers;

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
		this.clock = System.currentTimeMillis();
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

	/**
	 * Retrieves the CPUID of the CPU that this process is running on.
	 *
	 * @return The CPUID of the CPU that this process is running on.
	 */
	public synchronized int getCPUID() {
		return this.cpuid;
	}

	/**
	 * Sets the CPU ID to the given value to indicate that the process is running on that CPU. A negative value
	 * indicates that the process is not on the CPU.
	 *
	 * @param cpuid The CPU that the process is running on.
	 */
	public synchronized void setCPUID( int cpuid ) {
		this.cpuid = cpuid;
	}

	/**
	 * The amount of instructions for this process.
	 *
	 * @return The number of instructions in this process.
	 */
	public int getInstructionsLength() {
		return this.instructionsLength;
	}

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

	public int getTempBufferDiskAddress() {
		return getOutputBufferDiskAddress() + this.outputBufferLength;
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
	public synchronized void setStatus( Status status ) {
		long timestamp = System.currentTimeMillis();
		long elapsedTime = timestamp - this.clock;
		if ( status == Status.TERMINATED ) {
			// TODO Throw Exception about zombies or reincarnation something
		}
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
				System.currentTimeMillis() - this.clock :
				0 );
	}

	/**
	 * Retrieves the elapsed amount of time that this PCB has been running on the CPU.
	 *
	 * @return the elapsed amount of time this PCB has been running on the CPU.
	 */
	public synchronized long getElapsedRunTime() {
		return this.elapsedRunTime + ( this.status == Status.RUNNING ? System.currentTimeMillis() - this.clock : 0 );
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
}
