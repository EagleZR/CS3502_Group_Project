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
		for ( PCB pcb : this.inRAM ) {
			if ( pcb.getStatus() == PCB.Status.TERMINATED && this.mmu.processMapped( pcb ) ) {
				PCB.PageTable pageTable = pcb.getPageTable();
				int i = 0;
				for ( Iterator<Integer> iterator = pageTable.iterator(); iterator.hasNext(); i++ ) {
					int address = iterator.next();
					if ( address != -1 ) {
						this.mmu.writePage( pcb, i );
					}
				}
				this.mmu.terminateProcessMemory( pcb );
				this.inRAM.remove( pcb );
			}
		}

		// TODO Handle page faults

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
					// TODO Write 4 pages to RAM (do in MMU.mapMemory?)
					this.inRAM.add( next );
					this.taskManager.getReadyQueue().add( next );
					next.setStatus( PCB.Status.READY );
				}
			}
		}
	}
}
