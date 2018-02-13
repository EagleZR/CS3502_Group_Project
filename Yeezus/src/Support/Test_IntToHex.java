package Support;

import org.junit.Test;

import static org.junit.Assert.*;

public class Test_IntToHex {

	@Test public void apply() {
		int a = 0x000ABC12;
		assertEquals( IntToHex.convert( a ), "0x000ABC12" );
	}
}
