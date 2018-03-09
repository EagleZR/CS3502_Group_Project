package yeezus;

/**
 * Thrown if the submitted {@code ID} conflicts with an existing {@code ID}.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class DuplicateIDException extends Exception {
	public DuplicateIDException( String s ) {
		super( s );
	}
}
