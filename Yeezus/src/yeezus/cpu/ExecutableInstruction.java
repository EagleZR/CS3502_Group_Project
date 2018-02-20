package yeezus.cpu;

import yeezus.memory.Word;

import static yeezus.cpu.InstructionSet.values;

/**
 * A class meant to represent a single CPU ExecutableInstruction in the OS.
 *
 * @version 0.2
 */
abstract class ExecutableInstruction implements Runnable {

	InstructionSet type;

	private ExecutableInstruction( Word word ) throws InvalidInstructionException {
		this.type = getInstructionSet( word );
	}

	InstructionSet getInstructionSet( Word word ) throws InvalidInstructionException {
		int mask = 0x3F000000;
		long opcode = mask & word.getData();
		for ( InstructionSet instructionSet : values() ) {
			if ( instructionSet.getCode() == opcode ) {
				return instructionSet;
			}
		}
		throw new InvalidInstructionException( "The Opcode " + opcode + " is invalid." );
	}

	static class ArithmeticExecutableInstruction extends ExecutableInstruction {

		private int s1, s2, d;

		ArithmeticExecutableInstruction( Word word ) throws InvalidInstructionException {
			super( word );

			// Find s1
			int s1Mask = 0x00F00000;
			// TODO Find s

			// Find s2
			int s2Mask = 0x000F0000;
			// TODO Find s2

			// Find d
			int dMask = 0x0000F000;
			// TODO Find d

		}

		@Override public void run() {
			switch ( this.type ) {
				case MOV:
					// TODO MOV
					break;
				case ADD:
					// TODO ADD
					break;
				case SUB:
					// TODO SUB
					break;
				case MUL:
					// TODO MUL
					break;
				case DIV:
					// TODO DIV
					break;
				case AND:
					// TODO AND
					break;
				case OR:
					// TODO OR
					break;
				case SLT:
					// TODO SLT
					break;
				case NOP: // Does nothing and moves to next instruction
					// Do nothing
					break;
			}
		}
	}

	static class ConditionalExecutableInstruction extends ExecutableInstruction {

		private int bReg, dReg, data;

		ConditionalExecutableInstruction( Word word ) throws InvalidInstructionException {
			super( word );

			// Find B-reg
			int bRegMask = 0x00F00000;
			// TODO Find bReg

			// Find D-reg
			int dRegMask = 0x000F0000;
			// TODO Find dReg

			// Find data
			int dataMask = 0x0000FFFF;
			// TODO Find data

		}

		@Override public void run() {
			switch ( this.type ) {
				case ST:
					// TODO ST
					break;
				case LW:
					// TODO LW
					break;
				case MOVI:
					// TODO MOVI
					break;
				case ADDI:
					// TODO ADDI
					break;
				case MULI:
					// TODO MULI
					break;
				case DIVI:
					// TODO DIVI
					break;
				case LDI:
					// TODO LDI
					break;
				case BEQ:
					// TODO BEQ
					break;
				case BNE:
					// TODO BNE
					break;
				case BEZ:
					// TODO BEZ
					break;
				case BNZ:
					// TODO BNZ
					break;
				case BGZ:
					// TODO BGZ
					break;
				case BLZ:
					// TODO BLZ
					break;
				case NOP: // Does nothing and moves to next instruction
					// Do nothing
					break;
			}

		}
	}

	static class UnconditionalJumpExecutableInstruction extends ExecutableInstruction {

		int address;

		UnconditionalJumpExecutableInstruction( Word word ) throws InvalidInstructionException {
			super( word );

			// Find address
			int addressMask = 0x00FFFFFF;
			// TODO Find address
		}

		@Override public void run() {
			switch ( this.type ) {
				case HLT: // Logical end of program
					// TODO HLT
					break;
				case JMP: // Jumps to a specified location
					// TODO JMP
					break;
				case NOP: // Does nothing and moves to next instruction
					// Do nothing
					break;
			}

		}
	}

	static class IOExecutableInstruction extends ExecutableInstruction {

		int reg1, reg2, address;

		IOExecutableInstruction( Word word ) throws InvalidInstructionException {
			super( word );

			// Find reg1
			int reg1Mask = 0x00F00000;
			// TODO Find reg1

			// Find reg2
			int reg2Mask = 0x000F0000;
			// TODO Find reg2

			// Find address
			int addressMask = 0x0000FFFF;
			// TODO Find address

		}

		@Override public void run() {
			switch ( this.type ) {
				case RD: // Reads content of I/P buffer into a accumulator
					// TODO RD
					break;
				case WR: // Writes the content of accumulator into O/P buffer
					// TODO WR
					break;
				case NOP: // Does nothing and moves to next instruction
					// Do nothing
					break;
			}
		}
	}

}
