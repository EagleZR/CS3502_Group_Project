package yeezus.cpu;

import yeezus.memory.*;

public class CPU {

	private MMU mmu;
	private Memory registers;
	private int pid = 0;
	private int pc;

	public CPU( MMU mmu, Memory registers ) {
		this.mmu = mmu;
		this.registers = registers;
	}

	public void setProcess( int pid ) {
		this.pid = pid;
		pc = 0;
	}

	public void run()
			throws InvalidInstructionException, InvalidWordException, ExecutionException, InvalidAddressException {
		if ( pid == 0 ) {
			// TODO Throw an invalid PID exception or something
		}
		while ( true ) {
			// TODO Fetch
			Word instruction = mmu.read( this.pid, this.pc++ );

			// Decode
			ExecutableInstruction executableInstruction = decode( instruction );

			// Execute
			if ( executableInstruction.type == InstructionSet.HLT ) {
				this.pid = 0;
				return;
			}

			if ( executableInstruction.getClass() == ExecutableInstruction.IOExecutableInstruction.class ) {
				// TODO Use DMA-Channel
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
