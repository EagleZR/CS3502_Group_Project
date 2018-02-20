package yeezus.pcb;

/**
 * Thrown if the submitted {@code PID} conflicts with an existing {@code PID}.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class DuplicatePIDException extends Exception {
	public DuplicatePIDException( String s ) {
		super( s );
	}
}
