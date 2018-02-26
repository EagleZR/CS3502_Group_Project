package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.pcb.PCB;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Dispatcher implements Runnable {

	ConcurrentLinkedQueue<PCB> readyQueue;
	CPU cpu;

	Dispatcher( ConcurrentLinkedQueue<PCB> readyQueue, CPU cpu ) {
		this.readyQueue = readyQueue;
		this.cpu = cpu;
	}

	@Override public void run() {
		if ( this.readyQueue.peek() != null ) {
			PCB next = this.readyQueue.remove();
		} else {
			// TODO Sleep until something else is added to the ready queue?
		}
	}
}
