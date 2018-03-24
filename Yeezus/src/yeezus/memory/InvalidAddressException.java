package yeezus.memory;

/**
 * To be thrown if the {@link Memory} attempts to access an invalid memory location.
 *
 * @author Mark Zeagler
 * @version 1.1
 */
public class InvalidAddressException extends RuntimeException {

	InvalidAddressException( String s ) {
		super( s );
	}
}
