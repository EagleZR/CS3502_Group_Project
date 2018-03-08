package yeezus.memory;

import org.junit.Test;

import static org.junit.Assert.*;

public class Test_MMU {

	@Test public void testMapMemory() throws Exception {
		MMU mmu = new MMU( new Memory( 1024 ) );

		// Map 100 addresses to process 0
		assertTrue( mmu.mapMemory( 1, 100 ) );

		// Check read/write
		for ( int i = 0; i < 100; i++ ) {
			mmu.write( 1, i, new Word( i ) );
		}
		for ( int i = 0; i < 100; i++ ) {
			assertEquals( i, mmu.read( 1, i ).getData() );
		}

		// map 100 addresses to process 1
		assertTrue( mmu.mapMemory( 2, 100 ) );

		// Check read/write
		for ( int i = 0; i < 100; i++ ) {
			mmu.write( 2, i, new Word( i * i ) );
		}
		for ( int i = 0; i < 100; i++ ) {
			assertEquals( i * i, mmu.read( 2, i ).getData() );
		}

		// Check that they don't equal each other
		for ( int i = 0; i < 100; i++ ) {
			assertNotEquals( mmu.read( 1, i ), mmu.read( 2, i ) );
		}
	}

}
