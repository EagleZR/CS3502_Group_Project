package yeezus;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class Test_File {

	@Test public void testFileLoad() {
		File file = new File( this.getClass().getClassLoader().getResource( "Program-File.txt" ).getFile() );
		assertTrue( file.exists() );
	}
}
