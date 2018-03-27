package yeezus.driver;

/**
 * An enumeration of the different types of scheduling policies that the {@link yeezus} Operating System must schedule
 * its processes by.
 *
 * @author Mark Zeagler
 * @version 2.0
 */
public enum CPUSchedulingPolicy {
	/**
	 * <p>First-Come, First-Served</p><p>The first processes loaded into the Job/Ready Queue will be the first to be
	 * executed.</p>
	 */
	FCFS, /**
	 * The processes with the highest priority will be executed first.
	 */
	Priority,
	//	/**
	//	* <p>Shortest Job First</p><p>The processes with the shortest burt times will be executed first.</p>
	//	*/
	SJF
}
