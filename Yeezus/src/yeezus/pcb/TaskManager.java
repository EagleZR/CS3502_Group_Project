package yeezus.pcb;

import java.util.ArrayList;

/**
 * The PCB Control Block for the Yeezus Operating System. This implementation is little more than a Collection
 * wrapper to control access to the {@link PCB}s it contains.
 */
public class TaskManager {

	private ArrayList<PCB> PCBs;

	public TaskManager() {
		this.PCBs = new ArrayList<>();
	}

	/**
	 * Adds a {@link PCB} with the following attributes.
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
	public void addPCB( int pid, int startInstructionAddress, int endInstructionAddress, int startInputBufferAddress, int endInputBufferAddress,
			int startOutputBufferAddress, int endOutputBufferAddress, int startTempBufferAddress,
			int endTempBufferAddress, int priority ) throws DuplicatePIDException {
		if ( this.contains( pid ) ) {
			throw new DuplicatePIDException( "The PID " + pid + " already exists in this TaskManager." );
		}
		this.PCBs.add( new PCB( pid, startInstructionAddress, endInstructionAddress, startInputBufferAddress,
				endInputBufferAddress, startOutputBufferAddress, endOutputBufferAddress, startTempBufferAddress,
				endTempBufferAddress, priority ) );
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
	 * @throws ProcessNotFoundException
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
