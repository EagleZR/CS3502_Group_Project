package yeezus.driver;

import yeezus.memory.MMU;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Scheduler implements Runnable {

	private final int NUM_CPUS; // Used to limit the amount of processes added to RAM to help limit deadlock
	private MMU mmu;
	private TaskManager taskManager;
	private CPUSchedulingPolicy schedulingMethod;
	private ArrayList<PCB> inRAM;

	Scheduler( MMU mmu, TaskManager taskManager, CPUSchedulingPolicy schedulingMethod, int numCPUs ) {
		this.mmu = mmu;
		this.taskManager = taskManager;
		this.schedulingMethod = schedulingMethod;
		this.inRAM = new ArrayList<>();
		this.NUM_CPUS = numCPUs;
	}

	/**
	 * Loads one process into RAM on each iteration. Iterations are called externally.
	 */
	@Override public void run() {
		// Remove terminated processes from the RAM
		for ( Iterator<PCB> pcbIterator = this.inRAM.iterator(); pcbIterator.hasNext(); ) {
			PCB pcb = pcbIterator.next();
			if ( pcb.getStatus() == PCB.Status.TERMINATED && this.mmu.processMapped( pcb ) ) {
				PCB.PageTable pageTable = pcb.getPageTable();
				int i = 0;
				for ( Iterator<Integer> integerIterator = pageTable.iterator(); integerIterator.hasNext(); i++ ) {
					int address = integerIterator.next();
					if ( address != -1 ) {
						this.mmu.writePage( pcb, i );
					}
				}
				this.mmu.terminateProcessMemory( pcb );
				pcbIterator.remove();
			}
		}

		// Handle page faults
		synchronized ( this.mmu ) {
			for ( Iterator<MMU.PageFault> iterator = this.mmu.getPageFaults().iterator(); iterator.hasNext(); ) {
				MMU.PageFault pageFault = iterator.next();
				PCB pcb = pageFault.getPCB();
				this.mmu.loadPage( pcb, pageFault.getPageNumber() );
				iterator.remove();
				pcb.setStatus( PCB.Status.READY );
				this.taskManager.getReadyQueue().add( pcb );
			}
		}

		// Limit the number of loaded processes to reduce chances of deadlock
		if ( this.inRAM.size() < this.NUM_CPUS * 2 ) {
			// Add new process to MMU/Ready Queue
			List<PCB> jobQueue = this.taskManager.getJobQueue();

			// Find next process
			if ( jobQueue.size() > 0 ) {
				PCB next = jobQueue.get( 0 );
				Comparator<PCB> comparator = this.schedulingMethod.getComparator();
				for ( PCB pcb : jobQueue ) {
					if ( comparator.compare( pcb, next ) < 0 ) {
						next = pcb;
					}
				}

				// Verify that the process's memory can be mapped
				if ( this.mmu.mapMemory( next ) ) {
					jobQueue.remove( next );
					this.taskManager.getReadyQueue().add( next );
					this.inRAM.add( next );
					next.setStatus( PCB.Status.READY );
				}
			}
		}
	}
}
