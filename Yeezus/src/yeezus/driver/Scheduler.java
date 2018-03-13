package yeezus.driver;

import yeezus.memory.InvalidAddressException;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.util.List;

public class Scheduler implements Runnable {

	private MMU mmu;
	private Memory disk;
	private TaskManager taskManager;
	private CPUSchedulingPolicy schedulingMethod;

	public Scheduler( MMU mmu, Memory disk, TaskManager taskManager, CPUSchedulingPolicy schedulingMethod ) {
		this.mmu = mmu;
		this.taskManager = taskManager;
		this.disk = disk;
		this.schedulingMethod = schedulingMethod;
	}

	/**
	 * Loads one process into RAM on each iteration. Iterations are called externally.
	 */
	@Override public void run() {
		// Remove terminated processes from the RAM
		for ( PCB pcb : taskManager.getPCBs() ) {
			int pid = pcb.getPID();
			if ( pcb.getStatus() == PCB.Status.TERMINATED && this.mmu.processMapped( pid ) ) {
				try {
					for ( int i = 0; i < pcb.getTotalSize(); i++ ) {
						this.disk.write( pcb.getStartDiskAddress() + i, this.mmu.read( pid, i ) );
					}
					this.mmu.terminatePID( pid );
				} catch ( InvalidAddressException e ) {
					// Do nothing, process has already been removed
				}
			}
		}

		// Add new process to MMU/Ready Queue
		List<PCB> list = taskManager.getJobQueue();

		// Find next process
		if ( list.size() > 0 ) {
			PCB next = list.get( 0 );
			if ( schedulingMethod == CPUSchedulingPolicy.Priority ) {
				//Find highest priority process
				for ( PCB pcb : list ) {
					if ( next.getPriority() < pcb.getPriority() ) {
						next = pcb;
					}
				}
				list.remove( next );
			} else if ( schedulingMethod == CPUSchedulingPolicy.FCFS ) {
				// Find the next loaded process
				next = list.remove( 0 );
			}

			// Verify that the process's memory can be mapped
			int totalSize = next.getTotalSize();
			if ( mmu.mapMemory( next.getPID(), totalSize ) ) {
				for ( int i = 0; i < totalSize; i++ ) {
					try {
						mmu.write( next.getPID(), i, disk.read( next.getStartDiskAddress() + i ) );
					} catch ( InvalidAddressException e ) {
						e.printStackTrace();
						System.err.println(
								"Fatal error. The addresses have already been mapped, so there should be no issues writing. Check the PCB.getTotalSize() method's calculation." );
						System.exit( 1 );
					}
				}
				taskManager.getReadyQueue().add( next );
				next.setStatus( PCB.Status.READY );
			}
		}
	}
}
