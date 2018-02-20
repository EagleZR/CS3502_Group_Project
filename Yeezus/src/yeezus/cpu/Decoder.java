package yeezus.cpu;

import yeezus.memory.InvalidWordException;
import yeezus.memory.Word;

public class Decoder {

	public static Instruction decode( Word word ) throws InvalidInstructionException, InvalidWordException {
		long signature = word.getData() & new Word( "0xC0000000" ).getData();
		if ( signature == 0x00000000 ) {
			return new Instruction.ArithmeticInstruction( word );
		} else if ( signature == 0x40000000 ) {
			return new Instruction.ConditionalInstruction( word );
		} else if ( signature == 0x80000000 ) {
			return new Instruction.UnconditionalJumpInstruction( word );
		} else {
			return new Instruction.IOInstruction( word );
		}
	}
}
