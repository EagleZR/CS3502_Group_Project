package yeezus.pcb;

/**
 * A class to hold various information relating to the processes run by the Yeezus Operating System. The data held by
 * these instances are to be used by the {@link yeezus.driver.Driver} and related classes for their interactions with
 * the processes stored in {@link yeezus.memory.Memory}.
 */
public class PCB {

	private final int pid, startInstructionAddress, endInstructionAddress, startInputBufferAddress, endInputBufferAddress, startOutputBufferAddress, endOutputBufferAddress, startTempBufferAddress, endTempBufferAddress, priority;
	private long clock;
	private long elapsedWaitTime;
	private long elapsedRunTime;
	private Status status;

	/**
	 * Constructs a PCB with the given characteristics.
	 *
	 * @param pid                      The PCB ID of the new PCB.
	 * @param startInstructionAddress  The start address of the Instructions on the disk.
	 * @param endInstructionAddress    The end address of the Instructions on the disk.
	 * @param startInputBufferAddress  The start address of the Input Buffer on the disk.
	 * @param endInputBufferAddress    The end address of the Input Buffer on the disk.
	 * @param startOutputBufferAddress The start address of the Output Buffer on the disk.
	 * @param endOutputBufferAddress   The end address of the Output Buffer on the disk.
	 * @param startTempBufferAddress   The start address of the Temp Buffer on the disk.
	 * @param endTempBufferAddress     The end address of the Temp Buffer on the disk.
	 * @param priority                 The given priority of the PCB.
	 */
	PCB( int pid, int startInstructionAddress, int endInstructionAddress, int startInputBufferAddress,
			int endInputBufferAddress, int startOutputBufferAddress, int endOutputBufferAddress,
			int startTempBufferAddress, int endTempBufferAddress, int priority ) {
		this.clock = System.currentTimeMillis();
		this.elapsedWaitTime = 0;
		this.elapsedRunTime = 0;
		this.status = Status.WAITING;
		this.pid = pid;
		this.startInstructionAddress = startInstructionAddress;
		this.endInstructionAddress = endInstructionAddress;
		this.startInputBufferAddress = startInputBufferAddress;
		this.endInputBufferAddress = endInputBufferAddress;
		this.startOutputBufferAddress = startOutputBufferAddress;
		this.endOutputBufferAddress = endOutputBufferAddress;
		this.startTempBufferAddress = startTempBufferAddress;
		this.endTempBufferAddress = endTempBufferAddress;
		this.priority = priority;
	}

	/**
	 * Retrieves the PID of this PCB.
	 *
	 * @return The PID associated with this PCB.
	 */
	public int getPid() {
		return pid;
	}

	/**
	 * Retrieves the start address of this PCB's instructions on the disk.
	 *
	 * @return The start address of this PCB's instructions on the disk.
	 */
	public int getStartInstructionAddress() {
		return startInstructionAddress;
	}

	/**
	 * Retrieves the end address of this PCB's instructions on the disk.
	 *
	 * @return The end address of this PCB's instructions on the disk.
	 */
	public int getEndInstructionAddress() {
		return endInstructionAddress;
	}

	/**
	 * Retrieves the start address of this PCB's input buffer on the disk.
	 *
	 * @return The start address of this PCB's input buffer on the disk.
	 */
	public int getStartInputBufferAddress() {
		return startInputBufferAddress;
	}

	/**
	 * Retrieves the end address of this PCB's input buffer on the disk.
	 *
	 * @return The end address of this PCB's input buffer on the disk.
	 */
	public int getEndInputBufferAddress() {
		return endInputBufferAddress;
	}

	/**
	 * Retrieves the start address of this PCB's output buffer on the disk.
	 *
	 * @return The start address of this PCB's output buffer on the disk.
	 */
	public int getStartOutputBufferAddress() {
		return startOutputBufferAddress;
	}

	/**
	 * Retrieves the end address of this PCB's output buffer on the disk.
	 *
	 * @return The end address of this PCB's output buffer  on the disk.
	 */
	public int getEndOutputBufferAddress() {
		return endOutputBufferAddress;
	}

	/**
	 * Retrieves the start address of this PCB's temp buffer on the disk.
	 *
	 * @return The start address of this PCB's temp buffer  on the disk.
	 */
	public int getStartTempBufferAddress() {
		return startTempBufferAddress;
	}

	/**
	 * Retrieves the end address of this PCB's temp buffer on the disk.
	 *
	 * @return The end address of this PCB's temp buffer  on the disk.
	 */
	public int getEndTempBufferAddress() {
		return endTempBufferAddress;
	}

	/**
	 * Retrieves the priority of this PCB.
	 *
	 * @return The priority of this PCB.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Retrieves the current status of the PCB.
	 *
	 * @return The current status of the PCB.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the new status of the PCB.
	 *
	 * @param status The new status of the PCB.
	 */
	public void setStatus( Status status ) {
		long timestamp = System.currentTimeMillis();
		long elapsedTime = timestamp - clock;
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
		return elapsedWaitTime + ( this.status != Status.RUNNING && this.status != Status.TERMINATED ?
				System.currentTimeMillis() - this.clock :
				0 );
	}

	/**
	 * Retrieves the elapsed amount of time that this PCB has been running on the CPU.
	 *
	 * @return the elapsed amount of time this PCB has been running on the CPU.
	 */
	public long getElapsedRunTime() {
		return elapsedRunTime + ( this.status == Status.RUNNING ? System.currentTimeMillis() - this.clock : 0 );
	}

	public enum Status {NEW, RUNNING, WAITING, READY, TERMINATED} // TODO Make sure these are updated appropriately later
}
