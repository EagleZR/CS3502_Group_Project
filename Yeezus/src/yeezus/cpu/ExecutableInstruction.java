package yeezus.cpu;

import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;
import yeezus.memory.Word;

import java.util.function.Consumer;

import static yeezus.cpu.InstructionSet.values;

/**
 * A class that represents a single CPU Instruction in the OS. This takes in the data from a stored instruction and
 * translates it into something that can be executed by the CPU via the {@link ExecutableInstruction#execute()} method.
 *
 * @version 0.2
 */
abstract class ExecutableInstruction implements Executable {

	// The data retrieved from the instruction
	InstructionSet type;
	Memory registers;

	// Retrieves the type and sets the registers
	private ExecutableInstruction( Word instruction, Memory registers ) throws InvalidInstructionException {
		this.type = getInstructionSet( instruction );
		this.registers = registers;
	}

	// Retrieves the type from the instruction set
	private InstructionSet getInstructionSet( Word instruction ) throws InvalidInstructionException {
		long mask = 0x3F000000;
		long opcode = ( mask & instruction.getData() ) >> 24;
		for ( InstructionSet instructionSet : values() ) {
			if ( instructionSet.getCode() == opcode ) {
				return instructionSet;
			}
		}
		throw new InvalidInstructionException( "The Opcode " + opcode + " is invalid." );
	}

	/**
	 * A class used for the interpretation and execution of Arithmetic instructions.
	 */
	static class ArithmeticExecutableInstruction extends ExecutableInstruction {

		// The data retrieved from the instruction
		private int s1, s2, d;

		// Interprets the given instruction into a form that can be executed by the system.
		ArithmeticExecutableInstruction( Word instruction, Memory registers ) throws InvalidInstructionException {
			super( instruction, registers );

			// TODO This can probably be done more efficiently, but I'm afraid I'd lose my mind
			// Find s1
			int s1Mask = 0x00F00000;
			this.s1 = (int) ( ( instruction.getData() & s1Mask ) >> 20 );

			// Find s2
			int s2Mask = 0x000F0000;
			this.s2 = (int) ( ( instruction.getData() & s2Mask ) >> 16 );

			// Find d
			int dMask = 0x0000F000;
			this.d = (int) ( ( instruction.getData() & dMask ) >> 12 );
		}

		// Executes the actions specified by this instruction
		@Override public void execute() throws InvalidAddressException, InvalidWordException {
			System.out.println(
					"Executing: " + this.type + ", " + this.s1 + "(" + this.registers.read( s1 ).getData() + "), "
							+ this.s2 + "(" + this.registers.read( s2 ).getData() + "), " + this.d + "("
							+ this.registers.read( s1 ).getData() + ")" );
			switch ( this.type ) { // Not the most efficient, but it will work for now
				case MOV: // Transfers the content of one register into another
					super.registers.write( this.d, super.registers.read( this.s1 ) );
					break;
				case ADD: // Adds content of two S-regs into D-reg
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() + super.registers.read( this.s2 ).getData() ) );
					break;
				case SUB: // Subtracts content of two S-regs into D-reg
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() - super.registers.read( this.s2 ).getData() ) );
					break;
				case MUL: // Multiplies content of two S-regs into D-reg
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() * super.registers.read( this.s2 ).getData() ) );
					break;
				case DIV: // Divides content of two S-regs into D-reg
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() + super.registers.read( this.s2 ).getData() ) );
					break;
				case AND: // Logical AND of two S-regs into D-reg
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() & super.registers.read( this.s2 ).getData() ) );
					break;
				case OR: // Logical OR of two S-regs into D-reg
					super.registers.write( this.d, new Word(
							super.registers.read( this.s1 ).getData() | super.registers.read( this.s2 ).getData() ) );
					break;
				case SLT: // Sets the D-reg to 1 if  first S-reg is less than the B-reg; 0 otherwise
					super.registers.write( this.d, new Word(
							( super.registers.read( this.s1 ).getData() < super.registers.read( this.s2 ).getData() ?
									1 :
									0 ) ) );
					break;
				case NOP: // Does nothing and moves to next instruction
					// Do nothing
					break;
			}
		}
	}

	/**
	 * A class used for the interpretation and execution of conditional instructions.
	 */
	static class ConditionalExecutableInstruction extends ExecutableInstruction {

		// The data retrieved from the instruction
		private int bReg, dReg, data;
		private Memory cache;
		private Consumer<Integer> countChanger;

		// Interprets the given instruction into a form that can be executed by the system.
		ConditionalExecutableInstruction( Word instruction, Memory registers, Memory cache,
				Consumer<Integer> countChanger ) throws InvalidInstructionException {
			super( instruction, registers );

			this.cache = cache;
			this.countChanger = countChanger;

			// Find B-reg
			int bRegMask = 0x00F00000;
			this.bReg = (int) ( ( instruction.getData() & bRegMask ) >> 20 );

			// Find D-reg
			int dRegMask = 0x000F0000;
			this.dReg = (int) ( ( instruction.getData() & dRegMask ) >> 16 );

			// Find data
			int dataMask = 0x0000FFFF;
			this.data = (int) ( ( instruction.getData() & dataMask ) );

		}

		// Executes the actions specified by this instruction
		@Override public void execute() throws InvalidWordException, InvalidAddressException {
			System.out.println(
					"Executing: " + this.type + ", " + this.bReg + "(" + this.registers.read( bReg ).getData() + "), "
							+ this.dReg + "(" + this.registers.read( dReg ).getData() + "), " + this.data );
			switch ( this.type ) {
				case ST: // Stores content of a reg.  into an address
					this.cache.write( ( this.data + (int) this.registers.read( this.dReg ).getData() ) / 4,
							this.registers.read( this.bReg ) );
					break;
				case LW: // Loads the content of an address into a reg.
					this.registers.write( this.dReg,
							this.cache.read( ( this.data + (int) this.registers.read( this.bReg ).getData() ) / 4 ) );
					break;
				case MOVI: // Transfers address/data directly into a register
					this.registers.write( this.dReg, new Word( this.data ) );
					break;
				case ADDI: // Adds a data value directly to the content of a register
					this.registers
							.write( this.dReg, new Word( this.registers.read( this.dReg ).getData() + this.data ) );
					break;
				case MULI: // Multiplies a data value directly with the content of a register
					this.registers
							.write( this.dReg, new Word( this.registers.read( this.dReg ).getData() * this.data ) );
					break;
				case DIVI: // Divides a data directly to the content of a register
					this.registers
							.write( this.dReg, new Word( this.registers.read( this.dReg ).getData() / this.data ) );
					break;
				case LDI: // Loads a data/address directly to the content of a register
					this.registers.write( this.dReg, new Word( this.data ) );
					break;
				case SLTI:// Sets the D-reg to 1 if  first S-reg is less than a data; 0 otherwise
					super.registers.write( this.dReg,
							new Word( ( super.registers.read( this.bReg ).getData() < this.data ? 1 : 0 ) ) );
					break;
				case BEQ: // Branches to an address when content of B-reg = D-reg
					this.countChanger.accept(
							this.registers.read( this.bReg ).getData() == this.registers.read( this.dReg ).getData() ?
									this.data / 4 :
									-1 );
					break;
				case BNE: // Branches to an address when content of B-reg <> D-reg
					this.countChanger.accept(
							this.registers.read( this.bReg ).getData() != this.registers.read( this.dReg ).getData() ?
									this.data / 4 :
									-1 );
					break;
				case BEZ: // Branches to an address when content of B-reg = 0
					this.countChanger.accept( this.registers.read( this.bReg ).getData() == 0 ? this.data / 4 : -1 );
					break;
				case BNZ: // Branches to an address when content of B-reg <> 0
					this.countChanger.accept( this.registers.read( this.bReg ).getData() != 0 ? this.data / 4 : -1 );
					break;
				case BGZ: // Branches to an address when content of B-reg > 0
					this.countChanger.accept( this.registers.read( this.bReg ).getData() > 0 ? this.data / 4 : -1 );
					break;
				case BLZ: // Branches to an address when content of B-reg < 0
					this.countChanger.accept( this.registers.read( this.bReg ).getData() < 0 ? this.data / 4 : -1 );
					break;
				case NOP: // Does nothing and moves to next instruction
					// Do nothing
					break;
			}
		}
	}

	/**
	 * A class used for the interpretation and execution of unconditional jump instructions.
	 */
	static class UnconditionalJumpExecutableInstruction extends ExecutableInstruction {

		Consumer<Integer> countChanger;
		// The data retrieved from the instruction
		private int address;

		// Interprets the given instruction into a form that can be executed by the system.
		UnconditionalJumpExecutableInstruction( Word instruction, Memory registers, Consumer<Integer> countChanger )
				throws InvalidInstructionException {
			super( instruction, registers );
			this.countChanger = countChanger;

			// Find address
			int addressMask = 0x00FFFFFF;
			this.address = (int) instruction.getData() & addressMask;
		}

		// Executes the actions specified by this instruction
		@Override public void execute() {
			System.out.println( "Executing: " + this.type + ", " + this.address );
			switch ( this.type ) {
				case HLT: // Logical end of program
					// Handled elsewhere, don't worry about it
					break;
				case JMP: // Jumps to a specified location
					this.countChanger.accept( address / 4 );
					break;
				case NOP: // Does nothing and moves to next instruction
					// Do nothing
					break;
			}
		}
	}

	/**
	 * A class used for the interpretation and execution of I/O instructions.
	 */
	static class IOExecutableInstruction extends ExecutableInstruction {

		// The data retrieved from the instruction
		int reg1, reg2, address;

		// Interprets the given instruction into a form that can be executed by the system.
		IOExecutableInstruction( Word instruction, Memory registers ) throws InvalidInstructionException {

			super( instruction, registers );

			// Find reg1
			int reg1Mask = 0x00F00000;
			this.reg1 = (int) ( ( instruction.getData() & reg1Mask ) >> 20 );

			// Find reg2
			int reg2Mask = 0x000F0000;
			this.reg2 = (int) ( ( instruction.getData() & reg2Mask ) >> 16 );

			// Find address
			int addressMask = 0x0000FFFF;
			this.address = (int) ( instruction.getData() & addressMask );
		}

		// Executes the actions specified by this instruction
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
