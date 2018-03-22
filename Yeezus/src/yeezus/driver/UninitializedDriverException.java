package yeezus.driver;

import yeezus.memory.Memory;

import java.io.File;

/**
 * To be thrown if a {@link Driver} instance is created before the {@link Loader} has been run via the {@link
 * Driver#loadFile(Memory, File)} method.
 *
 * @author Mark Zeagler
 * @version 1.1
 */
class UninitializedDriverException extends RuntimeException {

	UninitializedDriverException( String s ) {
		super( s );
	}
}
