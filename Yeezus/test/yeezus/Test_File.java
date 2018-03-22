package yeezus;

import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

public class Test_File {

	@Test public void testFileLoad() {
		File file = new File( Objects
				.requireNonNull( this.getClass().getClassLoader().getResource( "Program-File.txt" ) ).getFile() );
		assertTrue( file.exists() );
	}
}
