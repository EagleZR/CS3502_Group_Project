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
	List<PCB> list = taskManager.getJobQueue();


	Scheduler(TaskManager taskManager, CPUSchedulingPolicy schedulingMethod) {

	}

	/**
	 * Loads one process into RAM on each iteration. Iterations are called externally.
	 */
	@Override
	public void run() {

		//Load next into RAM

	}
	//Find highest priority process
	public void highestPriority(List<PCB> list) throws InvalidAddressException {
		PCB next = list.get(0);
		int counter = 1;
		for (PCB pcb : list) {
			if (next.getPriority() < list.get(counter).getPriority()) {
				next = list.get(counter);
				counter++;
			}
		}

		int totalSize = next.getTotalSize();
		for (int i = 0; i < totalSize; i++) {
			mmu.mapMemory(next.getPID(),i);
			mmu.write(next.getPID(), i,
					disk.read(next.getStartDiskAddress() + i));

		}
	}

	public void fcfs(List<PCB> list) throws InvalidAddressException {
		int counter = 0;
		PCB next = list.get(counter);
		for (PCB pcb : list) {
			next = list.get(counter);
			counter++;
		}

		int totalSize = next.getTotalSize();
		for (int i = 0; i < totalSize; i++) {
			mmu.mapMemory(next.getPID(),i);
			mmu.write(next.getPID(), i,
					disk.read(next.getStartDiskAddress() + i));

		}
	}

}
