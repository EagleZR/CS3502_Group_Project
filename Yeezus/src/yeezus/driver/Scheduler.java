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
    CPUSchedulingPolicy schedulingMethod;


	Scheduler(TaskManager taskManager, CPUSchedulingPolicy schedulingMethod) {

	}

	/**
	 * Loads one process into RAM on each iteration. Iterations are called externally.
	 */
	@Override
	public void run(){
	    int counter = 0;
        PCB next = list.get(counter);
        if (schedulingMethod == CPUSchedulingPolicy.Priority) {
            //Find highest priority process
            counter++;
            for (PCB pcb : list) {
                if (next.getPriority() < list.get(counter).getPriority()) {
                    next = list.get(counter);
                    counter++;
                }
            }

            int totalSize = next.getTotalSize();
            for (int i = 0; i < totalSize; i++) {
                mmu.mapMemory(next.getPID(), i);
                try {
                    mmu.write(next.getPID(), i,
                            disk.read(next.getStartDiskAddress() + i));
                } catch (InvalidAddressException e) {
                    e.printStackTrace();
                }
            }
        }else if (schedulingMethod == CPUSchedulingPolicy.FCFS){
            for (PCB pcb : list) {
                next = list.get(counter);
                counter++;
            }

            int totalSize = next.getTotalSize();
            for (int i = 0; i < totalSize; i++) {
                mmu.mapMemory(next.getPID(),i);
                try {
                    mmu.write(next.getPID(), i,
                            disk.read(next.getStartDiskAddress() + i));
                } catch (InvalidAddressException e) {
                    e.printStackTrace();
                }

            }
        }
	}
}
