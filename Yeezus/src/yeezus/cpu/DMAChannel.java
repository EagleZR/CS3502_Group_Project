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

	public void handle( ExecutableInstruction.IOExecutableInstruction instruction, PCB pcb )
			throws InvalidAddressException {
		pid = pcb.getPID();
		int reg1 = instruction.reg1;
		int reg2 = instruction.reg2;

		System.out.println( "Executing: " + instruction.type + ", " + reg1 + ", " + reg2 + ", " + instruction.address );

		//RW operation
		if ( instruction.type == InstructionSet.RD ) {
			//reading address into reg1
			if ( instruction.reg1 == 0 && instruction.address != 0 ) {
				registers.write( instruction.reg1, mmu.read( pid, instruction.address / 4 ) );
				//System.out.println( "Reading address: " + instruction.address / 4 + " into reg1: " + instruction.reg1 );
			}
			//reading reg2 into reg1
			else {
				registers.write( instruction.reg1, mmu.read( pid, (int) ( registers.read( reg2 ).getData() / 4 ) ) );
				//System.out.println( "Reading reg2: " + instruction.reg2 + " into reg1: " + instruction.reg1 );
			}
		}

		//WR operation
		else if ( instruction.type == InstructionSet.WR ) {
			//writing register 1 to address
			if ( instruction.reg1 == 0 && instruction.address != 0 ) {
				mmu.write( pid, instruction.address / 4, registers.read( instruction.reg1 ) );
				//System.out.println( "Writing reg1: " + instruction.reg1 + " into address: " + instruction.address / 4 );
			}
			//writing register 1 to register 2
			else {
				mmu.write( pid, (int) registers.read( instruction.reg2 ).getData() / 4,
						registers.read( instruction.reg1 ) );
				//System.out.println( "Writing reg1: " + instruction.reg1 + " into reg2: " + instruction.reg2 );
			}
		}
	}
}
