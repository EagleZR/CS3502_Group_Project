package yeezus.cpu;

import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;
import yeezus.memory.Word;

import static yeezus.cpu.InstructionSet.values;

/**
 * A class meant to represent a single CPU ExecutableInstruction in the OS.
 *
 * @version 0.2
 */
abstract class ExecutableInstruction implements Executable {

	InstructionSet type;
	Memory registers;

	private ExecutableInstruction( Word word, Memory registers ) throws InvalidInstructionException {
		this.type = getInstructionSet( word );
		this.registers = registers;
	}

	private InstructionSet getInstructionSet( Word word ) throws InvalidInstructionException {
		long mask = 0x3F000000;
		long opcode = ( mask & word.getData() ) >> 24;
		for ( InstructionSet instructionSet : values() ) {
			if ( instructionSet.getCode() == opcode ) {
				return instructionSet;
			}
		}
		throw new InvalidInstructionException( "The Opcode " + opcode + " is invalid." );
	}

	static class ArithmeticExecutableInstruction extends ExecutableInstruction {

		private int s1, s2, d;

		ArithmeticExecutableInstruction( Word word, Memory registers ) throws InvalidInstructionException {
			super( word, registers );

			// TODO This can probably be done more efficiently, but I'm afraid I'd lose my mind
			// Find s1
			int s1Mask = 0x00F00000;
			this.s1 = (int) ( ( word.getData() & s1Mask ) >> 20 );

			// Find s2
			int s2Mask = 0x000F0000;
			this.s2 = (int) ( ( word.getData() & s2Mask ) >> 16 );

			// Find d
			int dMask = 0x0000F000;
			this.d = (int) ( ( word.getData() & dMask ) >> 12 );
		}

		@Override public void execute() throws InvalidAddressException, InvalidWordException {
			switch ( this.type ) { // Not the most efficient, but it will work for now
				// TODO Add behaviors into enum?
				case MOV:
					// TODO Move s1 into d?
					super.registers.write( this.d, super.registers.read( this.s1 ) );
					break;
				case ADD:
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() + super.registers.read( this.s2 ).getData() ) );
					break;
				case SUB:
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() - super.registers.read( this.s2 ).getData() ) );
					break;
				case MUL:
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() * super.registers.read( this.s2 ).getData() ) );
					break;
				case DIV:
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() + super.registers.read( this.s2 ).getData() ) );
					break;
				case AND:
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() & super.registers.read( this.s2 ).getData() ) );
					break;
				case OR:
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() | super.registers.read( this.s2 ).getData() ) );
					break;
				case SLT:
					// TODO Verify correct
					super.registers.write( this.d, new Word(
							( super.registers.read( this.s1 ).getData() > super.registers.read( this.s2 ).getData() ?
									"0xFFFFFFFF" :
									"0x00000000" ) ) );
					break;
				case NOP: // Does nothing and moves to next instruction
					// Do nothing
					break;
			}
		}
	}

	static class ConditionalExecutableInstruction extends ExecutableInstruction {

		private int bReg, dReg, data;

		ConditionalExecutableInstruction( Word word, Memory registers ) throws InvalidInstructionException {
			super( word, registers );

			// Find B-reg
			int bRegMask = 0x00F00000;
			this.bReg = (int) ( ( word.getData() & bRegMask ) >> 20 );

			// Find D-reg
			int dRegMask = 0x000F0000;
			this.dReg = (int) ( ( word.getData() & dRegMask ) >> 16 );

			// Find data
			int dataMask = 0x0000FFFF;
			this.data = (int) ( ( word.getData() & dataMask ) );

		}

		@Override public void execute() {
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

		UnconditionalJumpExecutableInstruction( Word word, Memory registers ) throws InvalidInstructionException {
			super( word, registers );

			// Find address
			int addressMask = 0x00FFFFFF;
			// TODO Find address
		}

		@Override public void execute() {
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

		IOExecutableInstruction( Word word, Memory registers ) throws InvalidInstructionException {
			super( word, registers );

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

		@Override public void execute() throws ExecutionException {
			throw new ExecutionException( "Send this to the DMA-Channel for processing, don't execute." );
			//			switch ( this.type ) {
			//				case RD: // Reads content of I/P buffer into a accumulator
			//					// TODO RD
			//					break;
			//				case WR: // Writes the content of accumulator into O/P buffer
			//					// TODO WR
			//					break;
			//				case NOP: // Does nothing and moves to next instruction
			//					// Do nothing
			//					break;
			//			}
		}
	}

}
