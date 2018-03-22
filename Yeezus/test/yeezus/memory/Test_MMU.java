package yeezus.memory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.pcb.TaskManager;

import static org.junit.Assert.*;

public class Test_MMU {

	private MMU mmu;

	@Before public void setup() {
		this.mmu = new MMU( new Memory( 1024 ) );
	}

	@Test public void testMapMemory() {
		TaskManager.INSTANCE.addPCB( 1, 21, 32, 32, 32, 32, 1 );
		// Map 100 addresses to process 0
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 1 ) ) );

		// Check read/write
		for ( int i = 0; i < 100; i++ ) {
			this.mmu.write( TaskManager.INSTANCE.getPCB( 1 ), i, new Word( i ) );
		}
		for ( int i = 0; i < 100; i++ ) {
			assertEquals( i, this.mmu.read( TaskManager.INSTANCE.getPCB( 1 ), i ).getData() );
		}

		// map 100 addresses to process 1
		TaskManager.INSTANCE.addPCB( 2, 20, 40, 20, 20, 20, 4 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 2 ) ) );

		// Check read/write
		for ( int i = 0; i < 100; i++ ) {
			this.mmu.write( TaskManager.INSTANCE.getPCB( 2 ), i, new Word( i * i ) );
		}
		for ( int i = 0; i < 100; i++ ) {
			assertEquals( i * i, this.mmu.read( TaskManager.INSTANCE.getPCB( 2 ), i ).getData() );
		}

		// Check that they don't equal each other
		for ( int i = 0; i < 100; i++ ) {
			if ( i != 0 && i != 1 ) {
				assertNotEquals( this.mmu.read( TaskManager.INSTANCE.getPCB( 1 ), i ),
						this.mmu.read( TaskManager.INSTANCE.getPCB( 2 ), i ) );
			}
		}
	}

	@Test public void testCapacity() {
		TaskManager.INSTANCE.addPCB( 1, 0, 1024, 0, 0, 0, 1 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 1 ) ) );

		TaskManager.INSTANCE.addPCB( 2, 0, 1024, 0, 0, 0, 1 );
		assertFalse( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 2 ) ) );
	}

	@Test public void testTerminate() {
		TaskManager.INSTANCE.addPCB( 1, 0, 1024, 0, 0, 0, 1 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 1 ) ) );
		this.mmu.terminateProcessMemory( TaskManager.INSTANCE.getPCB( 1 ) );

		TaskManager.INSTANCE.addPCB( 2, 0, 200, 0, 0, 0, 1 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 2 ) ) );

		TaskManager.INSTANCE.addPCB( 3, 0, 200, 0, 0, 0, 1 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 3 ) ) );

		this.mmu.write( TaskManager.INSTANCE.getPCB( 3 ), 0, new Word( 210 ) );
		this.mmu.terminateProcessMemory( TaskManager.INSTANCE.getPCB( 2 ) );
	}

	@After public void tearDown() {
		TaskManager.INSTANCE.reset();
	}
}
