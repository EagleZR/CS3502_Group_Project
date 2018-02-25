package yeezus.pcb;

/**
 * Thrown if the {@code PID} was not found within the {@link ProcessList}.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class ProcessNotFoundException extends Exception {

	public ProcessNotFoundException( String s ) {
		super( s );
	}
}
