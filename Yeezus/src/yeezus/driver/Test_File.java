package yeezus.driver;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;

public class Test_File {

	@Test public void testFileLoad() {
		File file = new File( "src/yeezus/Program-File.txt" );
		assertTrue( file.exists() );
	}
}
