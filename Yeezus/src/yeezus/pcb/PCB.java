package yeezus.pcb;

import java.util.ArrayList;

/**
 * The Process Control Block for the Yeezus Operating System. This implementation is little more than a Collection
 * wrapper to control access to the {@link Process}es it contains.
 */
public class PCB {

	private ArrayList<Process> processes;

	public PCB() {
		this.processes = new ArrayList<>();
	}

	/**
	 * Adds a {@link Process} with the following attributes.
	 *
	 * @param pid                      The Process ID of the new process.
	 * @param startInstructionAddress  The start address of the Instructions on the disk.
	 * @param endInstructionAddress    The end address of the Instructions on the disk.
	 * @param startInputBufferAddress  The start address of the Input Buffer on the disk.
	 * @param endInputBufferAddress    The end address of the Input Buffer on the disk.
	 * @param startOutputBufferAddress The start address of the Output Buffer on the disk.
	 * @param endOutputBufferAddress   The end address of the Output Buffer on the disk.
	 * @param startTempBufferAddress   The start address of the Temp Buffer on the disk.
	 * @param endTempBufferAddress     The end address of the Temp Buffer on the disk.
	 * @param priority                 The given priority of the process.
	 */
	public void addProcess( int pid, int startInstructionAddress, int endInstructionAddress, int startInputBufferAddress, int endInputBufferAddress,
			int startOutputBufferAddress, int endOutputBufferAddress, int startTempBufferAddress,
			int endTempBufferAddress, int priority ) throws DuplicatePIDException {
		if ( this.contains( pid ) ) {
			throw new DuplicatePIDException( "The PID " + pid + " already exists in this PCB." );
		}
		this.processes.add( new Process( pid, startInstructionAddress, endInstructionAddress, startInputBufferAddress,
				endInputBufferAddress, startOutputBufferAddress, endOutputBufferAddress, startTempBufferAddress,
				endTempBufferAddress, priority ) );
	}

	/**
	 * Checks if this PCB contains a {@link Process} with the given {@code PID}.
	 *
	 * @param pid The ID of the {@link Process} to be checked.
	 * @return {@code true} if the PCB contains a {@link Process} with the given {@code PID}, {@code false} if it
	 * doesn't.
	 */
	public boolean contains( int pid ) {
		for ( Process process : this.processes ) {
			if ( process.getPid() == pid ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves the {@link Process} with the given {@code PID}.
	 *
	 * @param pid The ID of the {@link Process} being searched for.
	 * @return The {@link Process} with the given {@code pid}.
	 * @throws ProcessNotFoundException
	 */
	public Process getProcess( int pid ) throws ProcessNotFoundException {
		for ( Process process : this.processes ) {
			if ( process.getPid() == pid ) {
				return process;
			}
		}
		throw new ProcessNotFoundException( "The pid " + pid + " does not exist within the PCB." );
	}

}
