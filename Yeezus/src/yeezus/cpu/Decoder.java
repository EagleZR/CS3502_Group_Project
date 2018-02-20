package yeezus.cpu;

import yeezus.memory.InvalidWordException;
import yeezus.memory.Word;

public class Decoder {

	public static ExecutableInstruction decode( Word word ) throws InvalidInstructionException, InvalidWordException {
		long signature = word.getData() & new Word( "0xC0000000" ).getData();
		if ( signature == 0x00000000 ) {
			return new ExecutableInstruction.ArithmeticExecutableInstruction( word );
		} else if ( signature == 0x40000000 ) {
			return new ExecutableInstruction.ConditionalExecutableInstruction( word );
		} else if ( signature == 0x80000000 ) {
			return new ExecutableInstruction.UnconditionalJumpExecutableInstruction( word );
		} else {
			return new ExecutableInstruction.IOExecutableInstruction( word );
		}
	}
}
