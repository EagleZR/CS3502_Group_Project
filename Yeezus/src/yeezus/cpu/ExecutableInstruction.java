package yeezus.cpu;

import com.sun.istack.internal.NotNull;
import yeezus.memory.*;
import yeezus.pcb.PCB;

import static yeezus.cpu.InstructionSet.values;

/**
 * A class that represents a single CPU Instruction in the OS. This takes in the data from a stored instruction and
 * translates it into something that can be executed by the CPU via the {@link Runnable#run()} method.
 *
 * @author Mark Zeagler
 * @version 1.1
 */
abstract class ExecutableInstruction implements Runnable {

	// The data retrieved from the instruction
	InstructionSet type;
	Memory registers;

	// Retrieves the type and sets the registers
	private ExecutableInstruction( @NotNull Word instruction, @NotNull Memory registers )
			throws InvalidInstructionException {
		this.type = getInstructionSet( instruction );
		this.registers = registers;
	}

	// Retrieves the type from the instruction set
	private InstructionSet getInstructionSet( @NotNull Word instruction ) throws InvalidInstructionException {
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
		ArithmeticExecutableInstruction( @NotNull Word instruction, @NotNull Memory registers )
				throws InvalidInstructionException {
			super( instruction, registers );

			// This can probably be done more efficiently, but I'm afraid I'd lose my mind
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
		@Override public void run() throws InvalidAddressException, InvalidWordException {
			try {
				switch ( this.type ) { // Not the most efficient, but it will work for now
					case MOV: // Transfers the content of one register into another
						super.registers.write( this.d, super.registers.read( this.s1 ) );
						break;
					case ADD: // Adds content of two S-regs into D-reg
						super.registers.write( this.d, new Word(
								super.registers.read( this.s1 ).getData() + super.registers.read( this.s2 )
										.getData() ) );
						break;
					case SUB: // Subtracts content of two S-regs into D-reg
						super.registers.write( this.d, new Word(
								super.registers.read( this.s1 ).getData() - super.registers.read( this.s2 )
										.getData() ) );
						break;
					case MUL: // Multiplies content of two S-regs into D-reg
						super.registers.write( this.d, new Word(
								super.registers.read( this.s1 ).getData() * super.registers.read( this.s2 )
										.getData() ) );
						break;
					case DIV: // Divides content of two S-regs into D-reg
						super.registers.write( this.d, new Word(
								super.registers.read( this.s1 ).getData() + super.registers.read( this.s2 )
										.getData() ) );
						break;
					case AND: // Logical AND of two S-regs into D-reg
						super.registers.write( this.d, new Word(
								super.registers.read( this.s1 ).getData() & super.registers.read( this.s2 )
										.getData() ) );
						break;
					case OR: // Logical OR of two S-regs into D-reg
						super.registers.write( this.d, new Word(
								super.registers.read( this.s1 ).getData() | super.registers.read( this.s2 )
										.getData() ) );
						break;
					case SLT: // Sets the D-reg to 1 if  first S-reg is less than the B-reg; 0 otherwise
						super.registers.write( this.d, new Word(
								( super.registers.read( this.s1 ).getData() < super.registers.read( this.s2 )
										.getData() ? 1 : 0 ) ) );
						break;
					case NOP: // Does nothing and moves to next instruction
						// Do nothing
						break;
				}
			} catch ( InvalidAddressException | InvalidWordException e ) {
				System.err.println( "Instruction in error: " + toString() );
				throw e;
			}
		}

		@Override public String toString() {
			return this.type + ", " + this.s1 + "(" + this.registers.read( this.s1 ).getData() + "), " + this.s2 + "("
					+ this.registers.read( this.s2 ).getData() + "), " + this.d + "(" + this.registers.read( this.s1 )
					.getData() + ")";
		}
	}

	/**
	 * A class used for the interpretation and execution of conditional instructions.
	 */
	static class ConditionalExecutableInstruction extends ExecutableInstruction {

		CPU cpu;
		// The data retrieved from the instruction
		private int bReg, dReg, data;
		private Cache cache;
		private PCB pcb;

		// Interprets the given instruction into a form that can be executed by the system.
		ConditionalExecutableInstruction( @NotNull Word instruction, @NotNull Memory registers, @NotNull Cache cache,
				@NotNull CPU cpu, @NotNull PCB pcb ) throws InvalidInstructionException {
			super( instruction, registers );

			this.cache = cache;
			this.cpu = cpu;
			this.pcb = pcb;

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
		@Override public void run() throws InvalidWordException, InvalidAddressException {
			try {
				switch ( this.type ) {
					case ST: // Stores content of a reg.  into an address
						this.cache
								.write( this.pcb, ( this.data + (int) this.registers.read( this.dReg ).getData() ) / 4,
										this.registers.read( this.bReg ) );
						break;
					case LW: // Loads the content of an address into a reg.
						this.registers.write( this.dReg, this.cache.read( this.pcb,
								( this.data + (int) this.registers.read( this.bReg ).getData() ) / 4 ) );
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
						this.cpu.setPC( this.registers.read( this.bReg ).getData() == this.registers.read( this.dReg )
								.getData() ? this.data / 4 : this.cpu.getPC() );
						break;
					case BNE: // Branches to an address when content of B-reg <> D-reg
						this.cpu.setPC( this.registers.read( this.bReg ).getData() != this.registers.read( this.dReg )
								.getData() ? this.data / 4 : this.cpu.getPC() );
						break;
					case BEZ: // Branches to an address when content of B-reg = 0
						this.cpu.setPC(
								this.registers.read( this.bReg ).getData() == 0 ? this.data / 4 : this.cpu.getPC() );
						break;
					case BNZ: // Branches to an address when content of B-reg <> 0
						this.cpu.setPC(
								this.registers.read( this.bReg ).getData() != 0 ? this.data / 4 : this.cpu.getPC() );
						break;
					case BGZ: // Branches to an address when content of B-reg > 0
						this.cpu.setPC(
								this.registers.read( this.bReg ).getData() > 0 ? this.data / 4 : this.cpu.getPC() );
						break;
					case BLZ: // Branches to an address when content of B-reg < 0
						this.cpu.setPC(
								this.registers.read( this.bReg ).getData() < 0 ? this.data / 4 : this.cpu.getPC() );
						break;
					case NOP: // Does nothing and moves to next instruction
						// Do nothing
						break;
				}
			} catch ( MMU.PageFault pageFault ) {
				System.err.println( "There really shouldn't be a page fault here, it's all going to the buffer..." );
				pageFault.printStackTrace();
			}
		}

		@Override public String toString() {
			return this.type + ", " + this.bReg + "(" + this.registers.read( this.bReg ).getData() + "), " + this.dReg
					+ "(" + this.registers.read( this.dReg ).getData() + "), " + this.data;
		}
	}

	/**
	 * A class used for the interpretation and execution of unconditional jump instructions.
	 */
	static class UnconditionalJumpExecutableInstruction extends ExecutableInstruction {

		private CPU cpu;
		// The data retrieved from the instruction
		private int address;

		// Interprets the given instruction into a form that can be executed by the system.
		UnconditionalJumpExecutableInstruction( @NotNull Word instruction, @NotNull Memory registers, @NotNull CPU cpu )
				throws InvalidInstructionException {
			super( instruction, registers );
			this.cpu = cpu;

			// Find address
			int addressMask = 0x00FFFFFF;
			this.address = (int) instruction.getData() & addressMask;
		}

		// Executes the actions specified by this instruction
		@Override public void run() {
			// System.out.println( "Executing: " + this.type + ", " + this.address );
			switch ( this.type ) {
				case HLT: // Logical end of program
					// Handled elsewhere, don't worry about it
					break;
				case JMP: // Jumps to a specified location
					this.cpu.setPC( ( this.address / 4 ) - 1 );
					break;
				case NOP: // Does nothing and moves to next instruction
					// Do nothing
					break;
			}
		}

		@Override public String toString() {
			return this.type + ", " + this.address;
		}
	}

	/**
	 * A class used for the interpretation and execution of I/O instructions.
	 */
	static class IOExecutableInstruction extends ExecutableInstruction {

		// The data retrieved from the instruction
		int reg1, reg2, address;

		// Interprets the given instruction into a form that can be executed by the system.
		IOExecutableInstruction( @NotNull Word instruction, @NotNull Memory registers )
				throws InvalidInstructionException {

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
		@Override public void run() throws ExecutionException {
			throw new ExecutionException( "Send this to the DMA-Channel for processing, don't execute." );
			//			switch ( this.type ) {
			//				case RD: // Reads content of I/P buffer into a accumulator
			//					break;
			//				case WR: // Writes the content of accumulator into O/P buffer
			//					break;
			//				case NOP: // Does nothing and moves to next instruction
			//					// Do nothing
			//					break;
			//			}
		}

		@Override public String toString() {
			return this.type + ", " + this.reg1 + "(" + this.registers.read( this.reg1 ).getData() + "), " + this.reg2
					+ "(" + this.registers.read( this.reg2 ).getData() + "), " + this.address;
		}
	}
}
