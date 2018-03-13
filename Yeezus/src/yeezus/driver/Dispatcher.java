package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

public class Dispatcher implements Runnable {

	TaskManager taskManager;
	CPU cpu;

	public Dispatcher( TaskManager taskManager, CPU cpu ) {
		this.taskManager = taskManager;
		this.cpu = cpu;
	}

	@Override public void run() {
		if ( cpu.getProcess() == null || PCB.Status.RUNNING != cpu.getProcess().getStatus() && this.taskManager.getReadyQueue().size() > 0 ) {
			PCB next = this.taskManager.getReadyQueue().remove();
			cpu.setProcess( next );
		} else {
			// TODO Sleep until something else is added to the ready queue?
			return;
		}
	}
}
