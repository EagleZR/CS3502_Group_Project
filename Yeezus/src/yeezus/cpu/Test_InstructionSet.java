package yeezus.cpu;

import org.junit.Test;

import static org.junit.Assert.*;

public class Test_InstructionSet {

	/**
	 * Tests that each enum value has a unique code.
	 */
	@Test public void testValues() {
		for ( InstructionSet val1 : InstructionSet.values() ) {
			for ( InstructionSet val2 : InstructionSet.values() ) {
				if ( val1 != val2 ) {
					assertNotEquals( val1.getCode(), val2.getCode() );
				}
			}
		}
	}

}
