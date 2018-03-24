package yeezus;

import org.junit.Test;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.junit.Assert.assertTrue;

public class Test_File {

	@Test public void testFileLoad() throws UnsupportedEncodingException {
		File file = new File( URLDecoder
				.decode( this.getClass().getClassLoader().getResource( "Program-File.txt" ).getFile(), "UTF-8" ) );
		assertTrue( file.getPath() + " does not exist.", file.exists() );
	}
}
