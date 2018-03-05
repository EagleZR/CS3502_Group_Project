package yeezus.cpu;

import yeezus.memory.InvalidAddressException;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;

public class DMAChannel {

	Memory RAM;
	Memory registers;

	MMU mmu;
	int pid;

	public DMAChannel( MMU mmu, Memory registers ) {
		this.mmu = mmu;
		this.registers = registers;
	}

	public void handle( ExecutableInstruction.IOExecutableInstruction instruction, PCB pcb ) throws InvalidAddressException {
		pid = pcb.getPid();

		//read
		if(instruction.type == InstructionSet.RD)
		{
			registers.write(instruction.reg1, mmu.read(pid, instruction.address) );
		}

		//write
		if(instruction.type == InstructionSet.WR)
		{
			mmu.write(pid, instruction.address, registers.read(instruction.reg1) );
		}
	}
}
