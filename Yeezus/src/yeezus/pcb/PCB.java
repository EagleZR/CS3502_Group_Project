package yeezus.pcb;

/**
 * A class to hold various information relating to the processes run by the Yeezus Operating System. The data held by
 * these instances are to be used by the {@link yeezus.driver.Driver} and related classes for their interactions with
 * the processes stored in {@link yeezus.memory.Memory}.
 */
public class PCB {

	private final int pid, startDiskInstructionAddress, instructionsLength, startDiskInputBufferAddress, inputBufferLength, startDiskOutputBufferAddress, outputBufferLength, startDiskTempBufferAddress, tempBufferLength, priority;
	private int cpuid, startRAMInstructionAddress, startRAMInputBufferAddress, startRAMOutputBufferAddress, startRAMTempBufferAddress;
	private long clock;
	private long elapsedWaitTime;
	private long elapsedRunTime;
	private Status status;

	/**
	 * Constructs a PCB with the given characteristics.
	 *
	 * @param pid                          The PCB ID of the new PCB.
	 * @param startDiskInstructionAddress  The start address of the Instructions on the disk.
	 * @param instructionsLength           The the amount of Instructions on the disk.
	 * @param startDiskInputBufferAddress  The start address of the Input Buffer on the disk.
	 * @param inputBufferLength            The size of the Input Buffer on the disk.
	 * @param startDiskOutputBufferAddress The start address of the Output Buffer on the disk.
	 * @param outputBufferLength           The size of the Output Buffer on the disk.
	 * @param startDiskTempBufferAddress   The start address of the Temp Buffer on the disk.
	 * @param tempBufferLength             The size of the Temp Buffer on the disk.
	 * @param priority                     The given priority of the PCB.
	 */
	PCB( int pid, int startDiskInstructionAddress, int instructionsLength, int startDiskInputBufferAddress,
			int inputBufferLength, int startDiskOutputBufferAddress, int outputBufferLength,
			int startDiskTempBufferAddress, int tempBufferLength, int priority ) {
		this.clock = System.currentTimeMillis();
		this.elapsedWaitTime = 0;
		this.elapsedRunTime = 0;
		this.status = Status.NEW;
		this.pid = pid;
		this.startDiskInstructionAddress = startDiskInstructionAddress;
		this.instructionsLength = instructionsLength;
		this.startDiskInputBufferAddress = startDiskInputBufferAddress;
		this.inputBufferLength = inputBufferLength;
		this.startDiskOutputBufferAddress = startDiskOutputBufferAddress;
		this.outputBufferLength = outputBufferLength;
		this.startDiskTempBufferAddress = startDiskTempBufferAddress;
		this.tempBufferLength = tempBufferLength;
		this.priority = priority;
	}

	/**
	 * @return
	 */
	public int getCPUID() {
		return this.cpuid;
	}

	/**
	 * Sets the CPU ID to the given value to indicate that the process is running on that CPU. A negative value
	 * indicates that the process is not on the CPU.
	 *
	 * @param cpuid The CPU that the process is running on.
	 */
	public void setCPUID( int cpuid ) {
		this.cpuid = cpuid;
	}

	public int getStartRAMInstructionAddress() {
		return this.startRAMInstructionAddress;
	}

	public void setStartRAMInstructionAddress( int startRAMInstructionAddress ) {
		this.startRAMInstructionAddress = startRAMInstructionAddress;
	}

	public int getStartRAMInputBufferAddress() {
		return this.startRAMInputBufferAddress;
	}

	public void setStartRAMInputBufferAddress( int startRAMInputBufferAddress ) {
		this.startRAMInputBufferAddress = startRAMInputBufferAddress;
	}

	public int getStartRAMOutputBufferAddress() {
		return this.startRAMOutputBufferAddress;
	}

	public void setStartRAMOutputBufferAddress( int startRAMOutputBufferAddress ) {
		this.startRAMOutputBufferAddress = startRAMOutputBufferAddress;
	}

	public int getStartRAMTempBufferAddress() {
		return this.startRAMTempBufferAddress;
	}

	public void setStartRAMTempBufferAddress( int startRAMTempBufferAddress ) {
		this.startRAMTempBufferAddress = startRAMTempBufferAddress;
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
	 * The size of the input buffer for this process.
	 *
	 * @return The size of the input buffer for this process.
	 */
	public int getInputBufferLength() {
		return this.inputBufferLength;
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
	 * The size of the temp buffer for this process.
	 *
	 * @return The size of the temp buffer for this process.
	 */
	public int getTempBufferLength() {
		return this.tempBufferLength;
	}

	/**
	 * Retrieves the PID of this PCB.
	 *
	 * @return The PID associated with this PCB.
	 */
	public int getPid() {
		return this.pid;
	}

	/**
	 * Retrieves the start address of this PCB's instructions on the disk.
	 *
	 * @return The start address of this PCB's instructions on the disk.
	 */
	public int getStartDiskInstructionAddress() {
		return this.startDiskInstructionAddress;
	}

	/**
	 * Retrieves the start address of this PCB's input buffer on the disk.
	 *
	 * @return The start address of this PCB's input buffer on the disk.
	 */
	public int getStartDiskInputBufferAddress() {
		return this.startDiskInputBufferAddress;
	}

	/**
	 * Retrieves the start address of this PCB's output buffer on the disk.
	 *
	 * @return The start address of this PCB's output buffer on the disk.
	 */
	public int getStartDiskOutputBufferAddress() {
		return this.startDiskOutputBufferAddress;
	}

	/**
	 * Retrieves the start address of this PCB's temp buffer on the disk.
	 *
	 * @return The start address of this PCB's temp buffer  on the disk.
	 */
	public int getStartDiskTempBufferAddress() {
		return this.startDiskTempBufferAddress;
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
	public Status getStatus() {
		return this.status;
	}

	/**
	 * Sets the new status of the PCB.
	 *
	 * @param status The new status of the PCB.
	 */
	public void setStatus( Status status ) {
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
	public long getElapsedWaitTime() {
		return this.elapsedWaitTime + ( this.status != Status.RUNNING && this.status != Status.TERMINATED ?
				System.currentTimeMillis() - this.clock :
				0 );
	}

	/**
	 * Retrieves the elapsed amount of time that this PCB has been running on the CPU.
	 *
	 * @return the elapsed amount of time this PCB has been running on the CPU.
	 */
	public long getElapsedRunTime() {
		return this.elapsedRunTime + ( this.status == Status.RUNNING ? System.currentTimeMillis() - this.clock : 0 );
	}

	public enum Status {NEW, RUNNING, WAITING, READY, TERMINATED}
}
