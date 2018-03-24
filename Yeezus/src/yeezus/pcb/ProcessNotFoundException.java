package yeezus.pcb;

/**
 * Thrown if the {@code PID} was not found within the {@link TaskManager}.
 *
 * @author Mark Zeagler
 * @version 1.1
 */
class ProcessNotFoundException extends RuntimeException {

	ProcessNotFoundException( String s ) {
		super( s );
	}
}
