package yeezus.cpu;

import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.TaskManager;

public class CPU {

	private Memory registers;
	private int pid;
	private int pc;

	public CPU( TaskManager taskManager, Memory registers ) {

	}

	public void run() throws InvalidInstructionException, InvalidWordException, ExecutionException {
		while ( true ) { // Check when the process is complete
			// TODO Fetch
			Word instruction = null;

			// Decode
			ExecutableInstruction executableInstruction = decode( instruction );

			// Execute
			if ( executableInstruction.getClass() == ExecutableInstruction.IOExecutableInstruction.class ) {
				// TODO Use DMA-Channel
			} else {
				executableInstruction.execute();
			}

			this.pc++;
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
