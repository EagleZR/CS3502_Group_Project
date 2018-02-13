import java.util.function.Function;

/**
 * Converts an {@link Integer} to its hex value.
 *
 * @version 1.0
 */
public class IntToHex implements Function<Integer, String> {

	/**
	 * Accepts an integer and returns the value displayed in hexadecimal.
	 *
	 * @param integer The integer whose value is to be displayed in hexadecimal.
	 * @return The hexadecimal value of the given integer.
	 */
	@Override public String apply( Integer integer ) {
		// https://stackoverflow.com/questions/13851743/how-to-format-numbers-to-a-hex-strings
		return String.format( "0x%08X", integer );
	}

}
