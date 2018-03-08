package yeezus.driver;

import yeezus.memory.InvalidAddressException;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.util.List;

public class Scheduler implements Runnable {
	PCB PCB;
	MMU mmu;
	Memory disk;
	TaskManager taskManager;
	CPUSchedulingPolicy schedulingMethod;

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
		int counter1 = 0;
		int counter2 = 1;
		List<PCB> list = taskManager.getJobQueue();
		PCB next = list.get( 0 );
		if ( schedulingMethod == CPUSchedulingPolicy.Priority ) {
			System.out.println( "=========Priority=========" );
			//Find highest priority process
			for ( PCB pcb : list ) {
				if ( next.getPriority() < pcb.getPriority() ) {
					next = pcb;
				}
			}

			int totalSize = next.getTotalSize();
			mmu.mapMemory( next.getPID(), totalSize );
			for ( int i = 0; i < totalSize; i++ ) {
				try {
					mmu.write( next.getPID(), i, disk.read( next.getStartDiskAddress() + i ) );
				} catch ( InvalidAddressException e ) {
					e.printStackTrace();
				}
			}
		} else if ( schedulingMethod == CPUSchedulingPolicy.FCFS ) {
			System.out.println( "=========FCFS=========" );
			for ( PCB pcb : list ) {
				next = list.get( counter1 );
				counter1++;
			}

			int totalSize = next.getTotalSize();
			mmu.mapMemory( next.getPID(), totalSize );
			for ( int i = 0; i < totalSize; i++ ) {
				try {
					mmu.write( next.getPID(), i, disk.read( next.getStartDiskAddress() + i ) );
				} catch ( InvalidAddressException e ) {
					e.printStackTrace();
				}
			}
		}
	}
}
