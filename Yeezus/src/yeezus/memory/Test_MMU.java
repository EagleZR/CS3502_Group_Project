package yeezus.memory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class Test_MMU {

	MMU mmu;

	@Before public void setup() throws Exception {
		mmu = new MMU( new Memory( 1024 ) );
	}

	@Test public void testMapMemory() throws Exception {
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
			if ( i != 0 && i != 1 ) {
				assertNotEquals( mmu.read( 1, i ), mmu.read( 2, i ) );
			}
		}
	}

	@Test public void testCapacity() {
		assertTrue( mmu.mapMemory( 1, 1024 ) );
		assertFalse( mmu.mapMemory( 2, 1024 ) );
	}

	@Test public void testTerminate() throws Exception {
		assertTrue( mmu.mapMemory( 1, 1024 ) );
		mmu.terminatePID( 1 );
		assertTrue( mmu.mapMemory( 2, 200 ) );
		assertTrue( mmu.mapMemory( 3, 200 ) );
		mmu.write( 3, 0, new Word( 210 ) );
		mmu.terminatePID( 2 );
		assertEquals( 210, mmu.read( 3, 0 ).getData() );
	}

}
