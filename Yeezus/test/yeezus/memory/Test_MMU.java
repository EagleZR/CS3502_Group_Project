package yeezus.memory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import static org.junit.Assert.*;

public class Test_MMU {

	private MMU mmu;

	@Before public void setup() {
		this.mmu = new MMU( new Memory( 2048 ), new Memory( 32 ) );
	}

	@Test public void testMapMemory() throws Throwable {
		TaskManager.INSTANCE.addPCB( 1, 21, 32, 32, 32, 32, 1 );
		// Map 100 addresses to process 0
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 1 ) ) );

		// Check read/write (for 4 pages)
		for ( int i = 0; i < 4 * MMU.FRAME_SIZE; i++ ) {
			this.mmu.write( TaskManager.INSTANCE.getPCB( 1 ), i, new Word( i ) );
			assertEquals( "The data is not being stored correctly", i,
					this.mmu.read( TaskManager.INSTANCE.getPCB( 1 ), i ).getData() );
		}
		for ( int i = 0; i < 4 * MMU.FRAME_SIZE; i++ ) {
			assertEquals( "The data is being overwritten or corrupted by subsequent writes", i,
					this.mmu.read( TaskManager.INSTANCE.getPCB( 1 ), i ).getData() );
		}

		// map 100 addresses to process 1
		TaskManager.INSTANCE.addPCB( 2, 20, 40, 20, 20, 20, 4 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 2 ) ) );

		// Check read/write
		for ( int i = 0; i < 4 * MMU.FRAME_SIZE; i++ ) {
			this.mmu.write( TaskManager.INSTANCE.getPCB( 2 ), i, new Word( i * i ) );
		}
		for ( int i = 0; i < 4 * MMU.FRAME_SIZE; i++ ) {
			assertEquals( i * i, this.mmu.read( TaskManager.INSTANCE.getPCB( 2 ), i ).getData() );
		}

		// Check that they don't equal each other
		for ( int i = 2; i < 4 * MMU.FRAME_SIZE; i++ ) {
			assertNotEquals( this.mmu.read( TaskManager.INSTANCE.getPCB( 1 ), i ),
					this.mmu.read( TaskManager.INSTANCE.getPCB( 2 ), i ) );

		}
	}

	@Test public void testCapacity() {
		TaskManager.INSTANCE.addPCB( 1, 0, 1024, 0, 0, 0, 1 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 1 ) ) );

		TaskManager.INSTANCE.addPCB( 2, 0, 1024, 0, 0, 0, 1 );
		assertTrue( this.mmu.mapMemory(
				TaskManager.INSTANCE.getPCB( 2 ) ) ); // Changed to true due to the space savings from paging
	}

	@Test public void testTerminate() throws Throwable {
		TaskManager.INSTANCE.addPCB( 1, 0, 1024, 0, 0, 0, 1 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 1 ) ) );
		assertTrue( this.mmu.loadPage( TaskManager.INSTANCE.getPCB( 1 ), 4 ) );
		this.mmu.terminateProcessMemory( TaskManager.INSTANCE.getPCB( 1 ) );

		TaskManager.INSTANCE.addPCB( 2, 0, 200, 0, 0, 0, 1 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 2 ) ) );

		TaskManager.INSTANCE.addPCB( 3, 0, 200, 0, 0, 0, 1 );
		assertTrue( this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 3 ) ) );

		this.mmu.write( TaskManager.INSTANCE.getPCB( 3 ), 0, new Word( 210 ) );
		this.mmu.terminateProcessMemory( TaskManager.INSTANCE.getPCB( 2 ) );
	}

	// Test that pages are correctly loaded
	@Test public void testPageLoad() {
		TaskManager.INSTANCE.addPCB( 1, 0, 12, 12, 12, 12, 1 );
		PCB pcb = TaskManager.INSTANCE.getPCB( 1 );
		assertTrue( this.mmu.mapMemory( pcb ) );
		// Ensure that no page faults are thrown within the first 4 pages
		if ( pcb.getTotalSize() < 4 * MMU.FRAME_SIZE ) {
			fail( "Please make the tested PCB larger than " + 4 * MMU.FRAME_SIZE + "(currently at " + pcb.getTotalSize()
					+ ") so that page loading can be appropriately tested." );
		}

		// Ensure that the address is invalid
		try {
			this.mmu.read( pcb, 4 * MMU.FRAME_SIZE );
			fail( "A page fault should be thrown at address " + 4 * MMU.FRAME_SIZE );
		} catch ( MMU.PageFault pageFault ) {
			// Page fault successfully thrown
		}

		// Load the page
		assertTrue( this.mmu.loadPage( pcb, 5 ) );

		// Ensure that the address is now valid
		try {
			this.mmu.read( pcb, 5 * MMU.FRAME_SIZE );
		} catch ( MMU.PageFault pageFault ) {
			fail( "The page was not successfully loaded" );
		}

		// Load a later page that isn't in line with the already-loaded pages
		try {
			this.mmu.read( pcb, 7 * MMU.FRAME_SIZE );
			fail( "A page fault should be thrown at address " + 7 * MMU.FRAME_SIZE );
		} catch ( MMU.PageFault pageFault ) {
			// Page fault successfully thrown
		}

		// Load the page
		this.mmu.loadPage( pcb, 7 );

		// Ensure that the address is now valid
		try {
			this.mmu.read( pcb, 7 * MMU.FRAME_SIZE );
		} catch ( MMU.PageFault pageFault ) {
			fail( "The page was not successfully loaded" );
		}

	}

	// Test that page faults are correctly generated
	@Test public void testPageFault() {
		TaskManager.INSTANCE.addPCB( 1, 0, 12, 12, 12, 12, 1 );
		PCB pcb = TaskManager.INSTANCE.getPCB( 1 );
		assertTrue( this.mmu.mapMemory( pcb ) );
		// Ensure that no page faults are thrown within the first 4 pages
		if ( pcb.getTotalSize() < 4 * MMU.FRAME_SIZE ) {
			fail( "Please make the tested PCB larger than " + 4 * MMU.FRAME_SIZE + "(currently at " + pcb.getTotalSize()
					+ ") so that page faulting can be appropriately tested." );
		}
		for ( int i = 0; i < 4 * MMU.FRAME_SIZE; i++ ) {
			try {
				this.mmu.read( pcb, i );
			} catch ( MMU.PageFault pageFault ) {
				fail( "There should be no page faults thrown within the first 4 pages of a newly-mapped process in the MMU. There was a page fault thrown at address "
						+ i );
			}
		}
		// Ensure that page faults are thrown for every page after the first 4
		for ( int i = 4 * MMU.FRAME_SIZE; i < pcb.getTotalSize(); i++ ) {
			try {
				this.mmu.read( pcb, i );
				fail( "A page fault should be thrown at address " + i );
			} catch ( MMU.PageFault pageFault ) {
				// Page fault successfully thrown
			}
		}
	}

	@After public void tearDown() {
		TaskManager.INSTANCE.reset();
	}
}
