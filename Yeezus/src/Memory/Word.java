package Memory;

import Support.IntToHex;

/**
 * A stand-in version of a Memory.Word data type that will be the basis of the memory system in this OS.
 *
 * @version 0.2
 */
public class Word {

	/**
	 * Each Memory.Word is 4 {@link Byte}s long.
	 */
	private final int data;

	/**
	 * Constructs a new Memory.Word with value set to '0x00000000'.
	 */
	public Word() {
		this.data = 0x00000000;
	}

	/**
	 * Constructs a new Memory.Word with the specified value.
	 *
	 * @param data The value to initialize the new Memory.Word with. Can be written like {@code new Data(0x020231A9)} for easy hex conversion.
	 */
	public Word( int data ) {
		this.data = data;
	}

	/**
	 * Retrieves the stored value of this Memory.Word as an {@code int}. For the hexadecimal representation, use {@link Word#toString()}.
	 *
	 * @return The stored value of this Memory.Word represented as an {@code int}.
	 */
	public int getData() {
		return this.data;
	}

	/**
	 * Returns a {@link String} containing the hexadecimal value of this Memory.Word.<p>NOTE: Just use this for printing/displaying the data, don't store it this way.</p>
	 *
	 * @return A {@link String} representation of the hexadecimal value of this Memory.Word.
	 */
	@Override public String toString() {
		return IntToHex.convert( data );
	}
}
