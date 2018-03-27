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

	Scheduler( MMU mmu, Memory disk, TaskManager taskManager, CPUSchedulingPolicy schedulingMethod ) {
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
		for ( PCB pcb : this.taskManager ) {
			if ( pcb.getStatus() == PCB.Status.TERMINATED && this.mmu.processMapped( pcb ) ) {
				try {
					// Write process back to disk
					for ( int i = 0; i < pcb.getTotalSize(); i++ ) {
						this.disk.write( pcb.getStartDiskAddress() + i, this.mmu.read( pcb, i ) );
					}
					// Terminate the process's memory
					this.mmu.terminateProcessMemory( pcb );
				} catch ( InvalidAddressException e ) {
					// Do nothing, process has already been removed
				}
			}
		}

		// Add new process to MMU/Ready Queue
		List<PCB> list = this.taskManager.getJobQueue();

		// Find next process
		if ( list.size() > 0 ) {
			PCB next = list.get( 0 );
			if ( this.schedulingMethod == CPUSchedulingPolicy.Priority ) {
				//Find highest priority process
				for ( PCB pcb : list ) {
					if ( next.getPriority() < pcb.getPriority() ) {
						next = pcb;
					}
				}
			} else if ( this.schedulingMethod == CPUSchedulingPolicy.FCFS ) {
				// Find the next loaded process
				next = list.get( 0 );
			} else if (this.schedulingMethod == CPUSchedulingPolicy.SJF){
				for( PCB pcb : list){
					if ( next.getInstructionsLength() > pcb.getInstructionsLength() )
						next  = pcb;
				}
			}

			// System.out.println( "Scheduling Process " + next.getPID() );

			// Verify that the process's memory can be mapped
			if ( this.mmu.mapMemory( next ) ) {
				list.remove( next );
				int totalSize = next.getTotalSize();
				for ( int i = 0; i < totalSize; i++ ) {
					try {
						this.mmu.write( next, i, this.disk.read( next.getStartDiskAddress() + i ) );
					} catch ( InvalidAddressException e ) {
						e.printStackTrace();
						System.err.println(
								"Fatal error. The addresses have already been mapped, so there should be no issues writing. Check the PCB.getTotalSize() method's calculation." );
						System.exit( 1 );
					}
				}
				this.taskManager.getReadyQueue().add( next );
				next.setStatus( PCB.Status.READY );
			}
		}
	}
}
