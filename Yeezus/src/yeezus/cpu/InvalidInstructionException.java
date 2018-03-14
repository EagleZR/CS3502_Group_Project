package yeezus.cpu;

import yeezus.memory.Word;

/**
 * To be thrown when there are issues matching a given {@link Word}'s data any of the opcodes in the {@link
 * InstructionSet}.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class InvalidInstructionException extends RuntimeException {
	public InvalidInstructionException( String s ) {
		super( s );
	}
}
