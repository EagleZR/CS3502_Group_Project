package yeezus.driver;

import yeezus.memory.Memory;

import java.io.File;

/**
 * To be thrown if a {@link Driver} instance is created before the {@link Loader} has been run via the {@link
 * Driver#loadFile(Memory, File)} method.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class UninitializedDriverException extends RuntimeException {

	public UninitializedDriverException( String s ) {
		super( s );
	}
}
