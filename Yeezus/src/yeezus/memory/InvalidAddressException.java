package yeezus.memory;

/**
 * To be thrown if the {@link Memory} attempts to access an invalid memory location.
 */
public class InvalidAddressException extends Exception {

	public InvalidAddressException( String s ) {
		super( s );
	}
}
