package yeezus.driver;

import yeezus.pcb.PCB;

import java.util.Comparator;

/**
 * An enumeration of the different types of scheduling policies that the {@link yeezus} Operating System must schedule
 * its processes by.
 *
 * @author Mark Zeagler
 * @version 2.1
 */
public enum CPUSchedulingPolicy {
	/**
	 * <p>First-Come, First-Served</p><p>The first processes loaded into the Job/Ready Queue will be the first to be
	 * executed.</p>
	 */
	FCFS( Comparator.comparingInt( PCB::getPID ) ), /**
	 * The processes with the highest priority will be executed first.
	 */
	Priority( Comparator.comparingInt( PCB::getPriority ) ), /**
	 * <p>Shortest Job First</p><p>The processes with the shortest burt times will be executed first.</p>
	 */
	SJF( Comparator.comparingInt( o -> ( o.getInstructionsLength() - o.getPC() ) ) );

	private Comparator<PCB> comparator;

	CPUSchedulingPolicy( Comparator<PCB> comparator ) {
		this.comparator = comparator;
	}

	public Comparator<PCB> getComparator() {
		return this.comparator;
	}
}
