package yeezus.pcb;

/**
 * Thrown if the {@code PID} was not found within the {@link TaskManager}.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class ProcessNotFoundException extends RuntimeException {

	public ProcessNotFoundException( String s ) {
		super( s );
	}
}
