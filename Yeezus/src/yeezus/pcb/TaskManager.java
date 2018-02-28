package yeezus.pcb;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The PCB Control Block for the Yeezus Operating System. This implementation is little more than a Collection
 * wrapper to control access to the {@link PCB}s it contains.
 */
public class TaskManager {

	private ArrayList<PCB> PCBs;
	private ConcurrentLinkedQueue<PCB> readyQueue;

	/**
	 *
	 */
	public TaskManager() {
		this.PCBs = new ArrayList<>();
		this.readyQueue = new ConcurrentLinkedQueue<>();
	}

	/**
	 * @return
	 */
	public ConcurrentLinkedQueue<PCB> getReadyQueue() {
		return readyQueue;
	}

	/**
	 * Adds a {@link PCB} with the following attributes.
	 *
	 * @param pid                      The PCB ID of the new PCB.
	 * @param startInstructionAddress  The start address of the Instructions on the disk.
	 * @param instructionsLength       The end address of the Instructions on the disk.
	 * @param startInputBufferAddress  The start address of the Input Buffer on the disk.
	 * @param inputBufferLength        The end address of the Input Buffer on the disk.
	 * @param startOutputBufferAddress The start address of the Output Buffer on the disk.
	 * @param outputBufferLength       The end address of the Output Buffer on the disk.
	 * @param startTempBufferAddress   The start address of the Temp Buffer on the disk.
	 * @param tempBufferLength         The end address of the Temp Buffer on the disk.
	 * @param priority                 The given priority of the PCB.
	 */
	public void addPCB( int pid, int startInstructionAddress, int instructionsLength, int startInputBufferAddress,
			int inputBufferLength, int startOutputBufferAddress, int outputBufferLength, int startTempBufferAddress,
			int tempBufferLength, int priority ) throws DuplicatePIDException {
		if ( this.contains( pid ) ) {
			throw new DuplicatePIDException( "The PID " + pid + " already exists in this TaskManager." );
		}
		this.PCBs.add( new PCB( pid, startInstructionAddress, instructionsLength, startInputBufferAddress,
				inputBufferLength, startOutputBufferAddress, outputBufferLength, startTempBufferAddress,
				tempBufferLength, priority ) );
	}

	/**
	 * Checks if this TaskManager contains a {@link PCB} with the given {@code PID}.
	 *
	 * @param pid The ID of the {@link PCB} to be checked.
	 * @return {@code true} if the TaskManager contains a {@link PCB} with the given {@code PID}, {@code false} if it
	 * doesn't.
	 */
	public boolean contains( int pid ) {
		for ( PCB PCB : this.PCBs ) {
			if ( PCB.getPid() == pid ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves the {@link PCB} with the given {@code PID}.
	 *
	 * @param pid The ID of the {@link PCB} being searched for.
	 * @return The {@link PCB} with the given {@code pid}.
	 * @throws ProcessNotFoundException Thrown if the pid is not contained within the task manager
	 */
	public PCB getPCB( int pid ) throws ProcessNotFoundException {
		for ( PCB PCB : this.PCBs ) {
			if ( PCB.getPid() == pid ) {
				return PCB;
			}
		}
		throw new ProcessNotFoundException( "The pid " + pid + " does not exist within the TaskManager." );
	}

}
