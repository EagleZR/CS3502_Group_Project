package yeezus.cpu;

/**
 * To be thrown during the execution of an {@link Executable} object.
 */
public class ExecutionException extends RuntimeException {
	public ExecutionException( String s ) {
		super( s );
	}
}
