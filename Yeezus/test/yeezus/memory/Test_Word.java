package yeezus.memory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_Word {

	private Word word;

	@Before public void setUp() throws InvalidWordException {
		this.word = new Word( "0x01234567" );
	}

	@Test public void getData() {
		assertEquals( Long.parseLong( "01234567", 16 ), this.word.getData() );
		assertEquals( 0x01234567, this.word.getData() );
	}

	@Test public void test_toString() {
		assertEquals( "0x01234567", this.word.toString() );
		assertEquals( "0x00000001", new Word( "0x00000001" ).toString() );
	}

}
