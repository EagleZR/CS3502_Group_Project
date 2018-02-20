package yeezus.memory;

/**
 * A stand-in version of a memory.Word data type that will be the basis of the memory system in this OS.
 *
 * @version 0.2
 */
public class Word {

	/**
	 * Each memory.Word is 4 {@link Byte}s long.
	 */
	private final long data;

	/**
	 * Constructs a new memory.Word with value set to '0x00000000'.
	 */
	public Word() {
		this.data = 0x00000000;
	}

	/**
	 * Constructs a new memory.Word with the specified value.
	 *
	 * @param data The value to initialize the new memory.Word with. Can be written like {@code new Data(0x020231A9)}
	 *             for easy hex conversion.
	 * @throws InvalidWordException Thrown if the given string is longer than 10 characters, indicating that it exceeds
	 *                              the '0xFFFFFFFF' limit.
	 */
	public Word( String data ) throws InvalidWordException {
		if ( data.length() > 10 ) {
			throw new InvalidWordException(
					"Argument" + data + " is too long. Please limit hex arguments to under 0xFFFFFFFF." );
		}
		this.data = Long.parseLong( data.substring( 2 ), 16 );
	}

	/**
	 * Retrieves the stored value of this memory.Word as an {@code int}. For the hexadecimal representation, use {@link
	 * Word#toString()}.
	 *
	 * @return The stored value of this memory.Word represented as an {@code int}.
	 */
	public long getData() {
		return this.data;
	}

	/**
	 * Returns a {@link String} containing the hexadecimal value of this memory.Word.<p>NOTE: Just use this for
	 * printing/displaying the data, don't store it this way.</p>
	 *
	 * @return A {@link String} representation of the hexadecimal value of this memory.Word.
	 */
	@Override public String toString() {
		String string = Long.toHexString( this.data );
		StringBuilder stringBuilder = new StringBuilder( "0x" );
		for ( int i = 0; i < 8 - string.length(); i++ ) {
			stringBuilder.append( 0 );
		}
		stringBuilder.append( string );
		return stringBuilder.toString();
	}
}
