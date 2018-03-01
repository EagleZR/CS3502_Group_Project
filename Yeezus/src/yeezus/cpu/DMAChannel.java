package yeezus.cpu;

import yeezus.memory.Memory;
import yeezus.pcb.PCB;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DMAChannel implements Runnable {

	Memory RAM;
	Memory registers;

	public DMAChannel( Memory RAM, Memory registers ) {

	}

	@Override public void run() {

	}
}
