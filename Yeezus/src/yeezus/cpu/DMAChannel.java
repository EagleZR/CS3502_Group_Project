package yeezus.cpu;

import yeezus.memory.Memory;
import yeezus.pcb.PCB;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DMAChannel implements Runnable {

	Memory RAM;

	public DMAChannel( Memory Disk, Memory RAM, ConcurrentLinkedQueue<PCB> dmaQueue ) {

	}

	@Override public void run() {

	}
}
