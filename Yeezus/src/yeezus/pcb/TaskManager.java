package yeezus.pcb;

import yeezus.driver.Loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>The Task Manager for the processes within the {@link yeezus} Operating System. This implementation is little more
 * than a wrapper to control access to the {@link PCB}s it contains. To ensure that it remains a singleton, it is
 * implemented as an {@link Enum} with a single instance.</p><p>Processes are registered by the {@link Loader} at system
 * startup using the {@link TaskManager#addPCB(int, int, int, int, int, int, int, int, int, int)} process. Once a
 * process has been added, its {@code PCB} can be directly retrieved using the {@link TaskManager#getPCB(int)} method. A
 * Job Queue that manages all un-run jobs on the system and a Ready Queue to keep track of which jobs have been loaded
 * into RAM and are ready to go are both instantiated and maintained within this Task Manager. The Job Queue can be
 * retrieved using the {@link TaskManager#getJobQueue()} method while the Ready Queue can be retrieved with the {@link
 * TaskManager#getReadyQueue()} method.</p><p>While all of the methods contained in this class are thread safe, the
 * {@link List}s that contain the Job Queue and the all of the {@link PCB}s are not, and they are returned as clones;
 * any changes to them will not be reflected in the original, and they should be used for reading only. The Ready Queue,
 * however, is thread safe, and can be edited outside of this class.</p>
 */
public enum TaskManager {

	/**
	 * This ensures that there's only a single instance.
	 */
	INSTANCE; // https://stackoverflow.com/questions/70689/what-is-an-efficient-way-to-implement-a-singleton-pattern-in-java

	private final ArrayList<PCB> PCBs = new ArrayList<>();
	private final ArrayList<PCB> jobQueue = new ArrayList<>();
	private final Queue<PCB> readyQueue = new ConcurrentLinkedQueue<>();

	/**
	 * <p>Retrieves the Ready Queue for the system. The Ready Queue is a {@link Queue} of all {@link PCB}s associated
	 * with processes that are ready to run.</p><p>For a list of all processes waiting to be run, use {@link
	 * TaskManager#getJobQueue()}. For a list of all {@link PCB}s of all processes, including those that have not yet
	 * completed, those that are currently running, and those that have already been completed, use {@link
	 * TaskManager#getPCBs()}.</p>
	 *
	 * @return A {@link Queue} of all {@link PCB}s associated with processes that are ready to be run. They should
	 * already be in order based on the current scheduling policy.
	 */
	public Queue<PCB> getReadyQueue() {
		return this.readyQueue;
	}

	/**
	 * Adds a {@link PCB} with the following attributes.
	 *
	 * @param pid                         The PCB ID of the new process.
	 * @param startDiskInstructionAddress The start address of the Instructions on the disk.
	 * @param instructionsLength          The amount of Instructions on the disk.
	 * @param startInputBufferAddress     The start address of the Input Buffer on the disk.
	 * @param inputBufferLength           The length of the Input Buffer on the disk.
	 * @param startOutputBufferAddress    The start address of the Output Buffer on the disk.
	 * @param outputBufferLength          The length of the Output Buffer on the disk.
	 * @param startTempBufferAddress      The start address of the Temp Buffer on the disk.
	 * @param tempBufferLength            The length of the Temp Buffer on the disk.
	 * @param priority                    The given priority of the process.
	 */
	public synchronized void addPCB( int pid, int startDiskInstructionAddress, int instructionsLength,
			int startInputBufferAddress, int inputBufferLength, int startOutputBufferAddress, int outputBufferLength,
			int startTempBufferAddress, int tempBufferLength, int priority ) throws DuplicatePIDException {
		if ( this.contains( pid ) ) {
			throw new DuplicatePIDException( "The PID " + pid + " already exists in this TaskManager." );
		}
		PCB pcb = new PCB( pid, startDiskInstructionAddress, instructionsLength, startInputBufferAddress,
				inputBufferLength, startOutputBufferAddress, outputBufferLength, startTempBufferAddress,
				tempBufferLength, priority );
		this.PCBs.add( pcb );
		this.jobQueue.add( pcb );
	}

	/**
	 * <p>Checks if this TaskManager contains a {@link PCB} with the given {@code PID}.</p><p><b>NOTE:</b> Since this
	 * instance is being used by multiple threads concurrently, there may be a change that occurs after this method has
	 * been run. It's best to attempt to retrieve the process directly, and handle any exceptions if it isn't
	 * there.</p>
	 *
	 * @param pid The ID of the {@link PCB} to be checked.
	 * @return {@code true} if the TaskManager contains a {@link PCB} with the given {@code PID}, {@code false} if it
	 * doesn't.
	 */
	@Deprecated public synchronized boolean contains( int pid ) {
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
	public synchronized PCB getPCB( int pid ) throws ProcessNotFoundException {
		for ( PCB PCB : this.PCBs ) {
			if ( PCB.getPid() == pid ) {
				return PCB;
			}
		}
		throw new ProcessNotFoundException( "The pid " + pid + " does not exist within the TaskManager." );
	}

	/**
	 * <p>Retrieves all of the {@link PCB}s that have run, are running, or will run on this system.</p><p>For a list of
	 * all processes waiting to be run, use {@link TaskManager#getJobQueue()}. For {@link PCB}s of processes that are
	 * ready to be run, use {@link TaskManager#getReadyQueue()}.</p></p><p><b>NOTE:</b> {@link List} is not
	 * synchronized, so the returned list is a clone of the original, not the original itself. Changes made to the
	 * cloned object will not be reflected in the original.</p>
	 *
	 * @return A {@link List} of {@link PCB}s for all processes on the system.
	 */
	public List<PCB> getPCBs() {
		return new ArrayList<PCB>( this.PCBs );
	}

	/**
	 * <p>Retrieves the Job Queue from the Task Manager.</p><p>The Job Queue is the list of all processes that have not
	 * yet run. For a list of all {@link PCB}s of all processes, including those that have not yet completed, those that
	 * are currently running, and those that have already been completed, use {@link TaskManager#getPCBs()}. For {@link
	 * PCB}s of processes that are ready to be run, use {@link TaskManager#getReadyQueue()}.</p><p><b>NOTE:</b> {@link
	 * List} is not synchronized, so the returned list is a clone of the original, not the original itself. Changes made
	 * to the cloned object will not be reflected in the original.</p>
	 *
	 * @return A cloned {@link List} of all {@link PCB}s whose processes have not yet been completed within the system.
	 */
	public List<PCB> getJobQueue() {
		return new ArrayList<PCB>( this.jobQueue );
	}
}
