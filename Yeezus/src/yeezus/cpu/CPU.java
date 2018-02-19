package yeezus.cpu;

import yeezus.memory.Memory;
import yeezus.pcb.PCB;

public class CPU implements Runnable {

	private Memory registers;

	public CPU( PCB pcb, Memory registers ) {

	}

	@Override public void run() {

	}

	public Memory getRegisters() {
		return this.registers;
	}
}
