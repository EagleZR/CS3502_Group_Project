package yeezus.cpu;

/**
 * The given opcodes for the set of instructions to be performed by the {@link yeezus} Operating System.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public enum InstructionSet {

	// https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html
	/**
	 * Reads content of I/P buffer into a accumulator.
	 */
	RD( 0x00 ), /**
	 * Writes the content of accumulator into O/P buffer.
	 */
	WR( 0x01 ), /**
	 * Stores content of a reg. into an address.
	 */
	ST( 0x02 ), /**
	 * Loads the content of an address into a reg.
	 */
	LW( 0x03 ), /**
	 * Transfers the content of one register into another.
	 */
	MOV( 0x04 ), /**
	 * Adds content of two S-regs into D-reg.
	 */
	ADD( 0x05 ), /**
	 * Subtracts content of two S-regs into D-reg.
	 */
	SUB( 0x06 ), /**
	 * Multiplies content of two S-regs into D-reg.
	 */
	MUL( 0x07 ), /**
	 * Divides content of two S-regs into D-reg.
	 */
	DIV( 0x08 ), /**
	 * Logical AND of two S-regs into D-reg.
	 */
	AND( 0x09 ), /**
	 * Logical OR of two S-regs into D-reg.
	 */
	OR( 0x0A ), /**
	 * Transfers address/data directly into a register.
	 */
	MOVI( 0x0B ), /**
	 * Adds a data value directly to the content of a register.
	 */
	ADDI( 0x0C ), /**
	 * Multiplies a data value directly with the content of a register.
	 */
	MULI( 0x0D ), /**
	 * Divides a data directly to the content of a register.
	 */
	DIVI( 0x0E ), /**
	 * Loads a data/address directly to the content of a register.
	 */
	LDI( 0x0F ), /**
	 * Sets the D-reg to 1 if first S-reg is less than the B-reg; 0 otherwise.
	 */
	SLT( 0x10 ), /**
	 * Sets the D-reg to 1 if first S-reg is less than a data; 0 otherwise.
	 */
	SLTI( 0x11 ), /**
	 * Logical end of program.
	 */
	HLT( 0x12 ), /**
	 * Does nothing and moves to next instruction.
	 */
	NOP( 0x13 ), /**
	 * Jumps to a specified location.
	 */
	JMP( 0x14 ), /**
	 * Branches to an address when content of B-reg = D-reg.
	 */
	BEQ( 0x15 ), /**
	 * Branches to an address when content of B-reg &lt;&gt; D-reg.
	 */
	BNE( 0x16 ), /**
	 * Branches to an address when content of B-reg = 0.
	 */
	BEZ( 0x17 ), /**
	 * Branches to an address when content of B-reg &lt;&gt; 0.
	 */
	BNZ( 0x18 ), /**
	 * Branches to an address when content of B-reg &gt; 0.
	 */
	BGZ( 0x19 ), /**
	 * Branches to an address when content of B-reg &lt; 0.
	 */
	BLZ( 0x1A );

	private int code;

	InstructionSet( int code ) {
		this.code = code;
	}

	/**
	 * Retrieves the opcode associated with this instruction type.
	 *
	 * @return The opcode associated with this instruction type.
	 */
	public int getCode() {
		return this.code;
	}
}
