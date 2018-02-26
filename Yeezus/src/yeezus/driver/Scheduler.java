package yeezus.driver;

import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Scheduler implements Runnable {

	Scheduler( TaskManager taskManager, ConcurrentLinkedQueue<PCB> dmaQueue, CPUSchedulingPolicy schedulingMethod ) {

	}

	@Override public void run() {
		// TODO Read through the PCBs to find the highest priority job
		// TODO Add the PCB with the highest priority to the dmaQueue
	}
}
