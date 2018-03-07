package yeezus.memory;

/**
 * To be thrown if invalid values are passed to the constructor of {@link Word}.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class InvalidWordException extends Exception {
	public InvalidWordException( String s ) {
		super( s );
	}
}
