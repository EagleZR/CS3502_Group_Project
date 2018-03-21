package yeezus.cpu;
//author: jessica brummel

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

	public void handle( ExecutableInstruction.IOExecutableInstruction instruction, PCB pcb ) {
		int reg1 = instruction.reg1;
		int reg2 = instruction.reg2;

		//	System.out.println( "Executing: " + instruction.type + ", " + reg1 + ", " + reg2 + "(" + (int) registers.read( reg2 ).getData() / 4 )+"), " + instruction.address );

		//RW operation
		if ( instruction.type == InstructionSet.RD ) {
			//reading address into reg1
			if ( instruction.reg2 == 0 && instruction.address != 0 ) {
				registers.write( instruction.reg1, mmu.read( pcb, instruction.address / 4 ) );
				// System.out.println( "Reading address: " + instruction.address / 4 + " into reg1: " + instruction.reg1 );
			}
			//reading reg2 into reg1
			else {
				registers.write( instruction.reg1, mmu.read( pcb, (int) ( registers.read( reg2 ).getData() / 4 ) ) );
				// System.out.println("Reading address: " + registers.read( instruction.reg2 ).getData() / 4 + " into reg1: "+ instruction.reg1 );
			}
		}

		//WR operation
		else if ( instruction.type == InstructionSet.WR ) {
			//writing register 1 to address
			if ( instruction.reg2 == 0 && instruction.address != 0 ) {
				mmu.write( pcb, instruction.address / 4, registers.read( instruction.reg1 ) );
				// System.out.println( "Writing " + registers.read( instruction.reg1 ).getData() + " into address: "+ instruction.address / 4 );
			}
			//writing register 1 to register 2
			else {
				mmu.write( pcb, (int) registers.read( instruction.reg2 ).getData() / 4,
						registers.read( instruction.reg1 ) );
				// System.out.println( "Writing " + registers.read( instruction.reg1 ).getData() + " into address: "+ registers.read( instruction.reg2 ).getData() / 4 );
			}
		}
	}
}
