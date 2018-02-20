package yeezus.cpu;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class InvalidInstructionException extends Exception {
	public InvalidInstructionException( String s ) {
		super( s );
	}
}
