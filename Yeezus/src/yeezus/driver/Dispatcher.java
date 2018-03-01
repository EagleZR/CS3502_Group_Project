package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

public class Dispatcher implements Runnable {

	TaskManager taskManager;
	CPU cpu;

	Dispatcher( TaskManager taskManager, CPU cpu ) {
		this.cpu = cpu;
	}

	@Override public void run() {
		if ( this.taskManager.getReadyQueue().peek() != null ) {
			PCB next = this.taskManager.getReadyQueue().remove();
		} else {
			// TODO Sleep until something else is added to the ready queue?
		}
	}
}
