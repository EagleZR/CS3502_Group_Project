package yeezus.cpu;

/**
 * To be thrown during the execution of an {@link ExecutableInstruction}.
 *
 * @author Mark Zeagler
 * @version 1.1
 */
class ExecutionException extends RuntimeException {
	ExecutionException( String s ) {
		super( s );
	}
}
