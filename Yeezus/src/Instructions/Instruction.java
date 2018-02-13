package Instructions;

import Memory.Word;

/**
 * A class meant to represent a single Instructions.Instruction in the OS.
 *
 * @version 0.1
 */
public abstract class Instruction implements Runnable {

	InstructionSet instructionType;

//	Instruction( Word word ) {
//		super( word.getData() );
//	}
//
//	Instruction( int data ) {
//		super( data );
//	}

	public InstructionSet getInstructionType() {
		return this.instructionType;
	}

	// TODO Move to Decoder
	// public static Instruction generate( Word word ) {
	// 		int sign = word & 0xC0000000
	// 		if (sign == 0xC0000000) {
	// 			input/output instruction
	//		} else if (sign == 0x80000000) {
	//			Unconditional Jump
	// 		} else if (sign == 0x40000000) {
	//			Conditional brance instruction
	// 		} else {
	// 			Arithmetic instruction
	// 		}
// }
}
