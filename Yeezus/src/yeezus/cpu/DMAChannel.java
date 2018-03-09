package yeezus.cpu;
//author: jessica brummel
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
		pid = pcb.getPID();

		//RW operation
		if(instruction.type == InstructionSet.RD)
		{
			//reading address into reg1
		    if(instruction.reg1==0 && instruction.address!=0)
			    registers.write(instruction.reg1, mmu.read(pid, instruction.address) );
			//reading reg2 into reg1
		    else
				registers.write(instruction.reg1, mmu.read(pid, instruction.reg2));
		}

		//WR operation
		if(instruction.type == InstructionSet.WR)
		{
			//writing register 1 to address
			if(instruction.reg1==0 && instruction.address!=0)
				mmu.write(pid, instruction.address, registers.read(instruction.reg1) );
			//writing register 1 to register 2
			else
				mmu.write(pid, instruction.reg2, registers.read(instruction.reg1) );

		}
	}
}
