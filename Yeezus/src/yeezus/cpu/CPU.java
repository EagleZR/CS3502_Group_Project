package yeezus.cpu;

import yeezus.memory.*;
import yeezus.pcb.PCB;

public class CPU {

	private MMU mmu;
	private Memory registers;
	private DMAChannel dmaChannel;
	private PCB pcb;
	private int pc;

	public CPU( MMU mmu, Memory registers ) {
		this.mmu = mmu;
		this.registers = registers;
		this.dmaChannel = new DMAChannel( mmu, registers );
	}

	public void setProcess( PCB pcb ) {
		this.pcb = pcb;
		this.pc = 0;
	}

	public void run()
			throws InvalidInstructionException, InvalidWordException, ExecutionException, InvalidAddressException {
		if ( this.pcb == null ) {
			// TODO Throw an invalid PID exception or something
		}
		while ( true ) {
			// TODO Fetch
			Word instruction = this.mmu.read( this.pcb.getPid(), this.pc++ );

			// Decode
			ExecutableInstruction executableInstruction = decode( instruction );

			// Execute
			if ( executableInstruction.type == InstructionSet.HLT ) {
				this.pcb = null;
				return;
			}

			if ( executableInstruction.getClass() == ExecutableInstruction.IOExecutableInstruction.class ) {
				this.dmaChannel
						.handle( (ExecutableInstruction.IOExecutableInstruction) executableInstruction, this.pcb );
			} else {
				executableInstruction.execute();
			}
		}
	}

	ExecutableInstruction decode( Word word ) throws InvalidInstructionException {
		long signature = word.getData() & 0xC0000000;
		if ( signature == 0x00000000 ) {
			return new ExecutableInstruction.ArithmeticExecutableInstruction( word, this.registers );
		} else if ( signature == 0x40000000 ) {
			return new ExecutableInstruction.ConditionalExecutableInstruction( word, this.registers );
		} else if ( signature == 0x80000000 ) {
			return new ExecutableInstruction.UnconditionalJumpExecutableInstruction( word, this.registers );
		} else {
			return new ExecutableInstruction.IOExecutableInstruction( word, this.registers );
		}
	}
}
