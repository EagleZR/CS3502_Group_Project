package yeezus.cpu;

import yeezus.memory.Memory;
import yeezus.pcb.ProcessList;

public class CPU implements Runnable {

	private Memory registers;

	public CPU( ProcessList processList, Memory registers ) {

	}

	@Override public void run() {

	}

	public Memory getRegisters() {
		return this.registers;
	}
}
