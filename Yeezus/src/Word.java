/**
 * A stand-in version of a Word data type that will be the basis of the memory system in this OS.
 *
 * @version 0.2
 */
public class Word {

	/**
	 * Each Word is 4 {@link Byte}s long.
	 */
	private int data;

	/**
	 * Constructs a new Word with value set to '0x00000000'.
	 */
	public Word() {
		this.data = 0x00000000;
	}

	/**
	 * Constructs a new Word with the specified value.
	 *
	 * @param data The value to initialize the new Word with. Can be written like {@code new Data(0x020231A9)} for easy hex conversion.
	 */
	public Word( int data ) {
		this.data = data;
	}

	/**
	 * Assigns a new value to this Word.
	 *
	 * @param data The new value to be assigned to this Word. Can be written like {@code new Data(0x020231A9)} for easy hex conversion.
	 */
	public void setData( int data ) {
		this.data = data;
	}

	/**
	 * Retrieves the stored value of this Word as an {@code int}. For the hexadecimal representation, use {@link Word#toString()}.
	 *
	 * @return The stored value of this Word represented as an {@code int}.
	 */
	public int getData() {
		return this.data;
	}

	/**
	 * Returns a {@link String} containing the hexadecimal value of this Word.
	 *
	 * @return A {@link String} representaion of the hexadecimal value of this Word.
	 */
	@Override public String toString() {
		return IntToHex.convert( data );
	}
}
