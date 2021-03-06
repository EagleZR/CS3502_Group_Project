package yeezus.pcb;

import yeezus.DuplicateIDException;
import yeezus.driver.Loader;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * <p>The Task Manager for the processes within the {@link yeezus} Operating System. This implementation is little more
 * than a wrapper to control access to the {@link PCB}s it contains. To ensure that it remains a singleton, it is
 * implemented as an {@link Enum} with a single instance.</p><p>Processes are registered by the {@link Loader} at system
 * startup using the {@link TaskManager#addPCB(int, int, int, int, int, int, int)} process. Once a process has been
 * added, its {@code PCB} can be directly retrieved using the {@link TaskManager#getPCB(int)} method. A Job Queue that
 * manages all un-run jobs on the system and a Ready Queue to keep track of which jobs have been loaded into RAM and are
 * ready to go are both instantiated and maintained within this Task Manager. The Job Queue can be retrieved using the
 * {@link TaskManager#getJobQueue()} method while the Ready Queue can be retrieved with the {@link
 * TaskManager#getReadyQueue()} method.</p><p>While all of the methods contained in this class are thread safe, the
 * {@link List}s that contain the Job Queue and the all of the {@link PCB}s are not, and they are returned as clones;
 * any changes to them will not be reflected in the original, and they should be used for reading only. The Ready Queue,
 * however, is thread safe, and can be edited outside of this class.</p>
 *
 * @author Mark Zeagler
 * @version 2.0
 */
public enum TaskManager implements Iterable<PCB> {

	/**
	 * This ensures that there's only a single instance.
	 */
	INSTANCE; // https://stackoverflow.com/questions/70689/what-is-an-efficient-way-to-implement-a-singleton-pattern-in-java

	private final List<PCB> PCBs = new ArrayList<>();
	private final List<PCB> jobQueue = Collections.synchronizedList( new ArrayList<PCB>() );
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
	 * @param inputBufferLength           The length of the Input Buffer on the disk.
	 * @param outputBufferLength          The length of the Output Buffer on the disk.
	 * @param tempBufferLength            The length of the Temp Buffer on the disk.
	 * @param priority                    The given priority of the process.
	 */
	public synchronized void addPCB( int pid, int startDiskInstructionAddress, int instructionsLength,
			int inputBufferLength, int outputBufferLength, int tempBufferLength, int priority )
			throws DuplicateIDException {
		if ( this.contains( pid ) ) {
			throw new DuplicateIDException( "The PID " + pid + " already exists in this TaskManager." );
		}
		PCB pcb = new PCB( pid, startDiskInstructionAddress, instructionsLength, inputBufferLength, outputBufferLength,
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
	public synchronized boolean contains( int pid ) {
		for ( PCB PCB : this.PCBs ) {
			if ( PCB.getPID() == pid ) {
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
			if ( PCB.getPID() == pid ) {
				return PCB;
			}
		}
		throw new ProcessNotFoundException( "The pid " + pid + " does not exist within the TaskManager." );
	}

	/**
	 * <p>Retrieves all of the {@link PCB}s that have run, are running, or will run on this system.</p><p>For a list of
	 * all processes waiting to be run, use {@link TaskManager#getJobQueue()}. For {@link PCB}s of processes that are
	 * ready to be run, use {@link TaskManager#getReadyQueue()}.</p><p><b>NOTE:</b> {@link List} is not synchronized, so
	 * the returned list is a clone of the original, not the original itself. Changes made to the cloned object will not
	 * be reflected in the original.</p>
	 *
	 * @return A {@link List} of {@link PCB}s for all processes on the system.
	 */
	private List<PCB> getPCBs() {
		return new ArrayList<>( this.PCBs );
	}

	/**
	 * Retrieves the number of {@link PCB}s stored in this task manager.
	 *
	 * @return The number of {@link PCB}s in this task manager.
	 */
	public int size() {
		return this.PCBs.size();
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
		return this.jobQueue;
	}

	/**
	 * Resets this class to its default state, clearing all of the data in the process.
	 */
	public void reset() {
		this.jobQueue.clear();
		this.PCBs.clear();
		this.readyQueue.clear();
	}

	@Override public void forEach( Consumer<? super PCB> action ) {
		iterator().forEachRemaining( action );
	}

	@Override public Iterator<PCB> iterator() {
		return new Iterator<PCB>() { // If this actually works...

			private int i = 0;

			@Override public void forEachRemaining( Consumer<? super PCB> action ) {
				for ( ; this.i < TaskManager.INSTANCE.getPCBs().size(); this.i++ ) {
					action.accept( TaskManager.INSTANCE.getPCB( i ) );
				}
			}

			@Override public boolean hasNext() {
				return this.i < TaskManager.INSTANCE.getPCBs().size();
			}

			@Override public PCB next() {
				if ( hasNext() ) {
					return TaskManager.INSTANCE.getPCB( ++this.i );
				} else {
					throw new NoSuchElementException();
				}
			}

			/**
			 * Not supported. To clear the {@link TaskManager}, use {@link TaskManager#reset()}.
			 */
			@Override public void remove() {
				throw new UnsupportedOperationException(
						"We don't want to remove PCBs from the Task Manager. To clear the Task Manager, use TaskManager.reset()" );
			}
		};
	}
}
