package yeezus.cpu;

/**
 * To be thrown during the execution of an {@link Executable} object.
 */
public class ExecutionException extends Exception {
	public ExecutionException( String s ) {
		super( s );
	}
}
