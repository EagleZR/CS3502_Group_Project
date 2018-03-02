package yeezus.cpu;

import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;

public class DMAChannel {

	Memory RAM;
	Memory registers;

	public DMAChannel( MMU mmu, Memory registers ) {

	}

	public void handle( ExecutableInstruction.IOExecutableInstruction instruction, PCB pcb ) {

	}
}
