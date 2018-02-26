package yeezus.cpu;

import yeezus.memory.Memory;
import yeezus.pcb.TaskManager;

public class CPU implements Runnable {

	private Memory registers;

	public CPU( TaskManager taskManager, Memory registers ) {

	}

	@Override public void run() {

	}

	public Memory getRegisters() {
		return this.registers;
	}
}
