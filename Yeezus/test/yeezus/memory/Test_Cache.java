package yeezus.memory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import static junit.framework.TestCase.*;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_Cache {

	private Cache cache;
	private PCB pcb;
	private MMU mmu;

	@Before public void setup() {
		Memory disk = new Memory( 56 );
		TaskManager.INSTANCE.addPCB( 1, 0, 20, 12, 12, 12, 1 );
		this.pcb = TaskManager.INSTANCE.getPCB( 1 );
		for ( int i = 0; i < this.pcb.getTotalSize(); i++ ) {
			disk.write( i, new Word( i ) );
		}
		this.mmu = new MMU( disk, new Memory( 56 ) );
		assertTrue( this.mmu.mapMemory( this.pcb ) );
		this.cache = new Cache( 20, this.mmu );
		for ( int i = 0; i < this.cache.getWritablePagesCount(); i++ ) {
			try {
				this.cache.loadPage( this.pcb, i );
			} catch ( MMU.PageFault pageFault ) {
				fail( "There shouldn't be a page fault on the 3rd page. 4 pages should be loaded. Fix the Test_MMU." );
			}
		}
	}

	@After public void tearDown() {
		TaskManager.INSTANCE.reset();
	}

	// Test that only valid addresses can be read
	@Test public void testRead() {
		// Check that there are no page faults within the first 8 instructions
		try {
			for ( int i = 0; i < MMU.FRAME_SIZE * 4 && i < this.pcb.getInstructionsLength(); i++ ) {
				this.cache.read( this.pcb, i );
			}
		} catch ( MMU.PageFault pageFault ) {
			fail( "There shouldn't be a page fault within the first 16 instructions." );
		}

		if ( this.pcb.getInstructionsLength() < MMU.FRAME_SIZE * 4 ) {
			fail( "Need to increase the instructions length so we can test the page fault." );
		}

		try {
			this.cache.read( this.pcb, MMU.FRAME_SIZE * 4 );
			fail( "A fault should be thrown here, or the test should be updated for more pages added to the MMU" );
		} catch ( MMU.PageFault pageFault ) {
			// Gooooooood, goooooooooooood, everything is proceeding as I have foreseen
		}

		// Check that the temp buffer can be fully read
		try {
			for ( int i = 0; i < this.pcb.getTempBufferLength(); i++ ) {
				this.cache.read( this.pcb, this.pcb.getTempBufferLogicalAddress() + i );
			}
		} catch ( MMU.PageFault pageFault ) {
			fail( "There should be no page faults when reading the temp buffer." );
		}
	}

	// Test that only valid address can be written to
	@Test public void testWrite() {
		// Check that there is a page fault when attempting to write to the instructions
		for ( int i = 0;
			  i < this.cache.getWritablePagesCount() * MMU.FRAME_SIZE && i < this.pcb.getInstructionsLength(); i++ ) {
			try {
				this.cache.write( this.pcb, i, new Word( 0 ) );
				fail( "An InvalidAddressException should be thrown for trying to overwrite the instructions." );
			} catch ( InvalidAddressException e ) {
				// Exception successfully thrown
			}
		}

		// Check that the temp buffer can be fully read
		try {
			for ( int i = 0; i < this.pcb.getTempBufferLength(); i++ ) {
				this.cache.write( this.pcb, this.pcb.getTempBufferLogicalAddress() + i, new Word( 0 ) );
			}
		} catch ( InvalidAddressException e ) {
			fail( "There should be no invalid address exceptions when writing to the temp buffer." );
		}
	}

	// Test that the correct pages are loaded
	@Test public void testLoadPage() throws MMU.PageFault {
		// Check that the first 2 pages are correctly loaded
		for ( int i = 0;
			  i < this.cache.getWritablePagesCount() * MMU.FRAME_SIZE && i < this.pcb.getInstructionsLength(); i++ ) {
			assertEquals( "The first 2 pages are not correctly loaded. The issue is at address " + i,
					this.mmu.read( this.pcb, i ), this.cache.read( this.pcb, i ) );
		}

		// Load the 3rd page
		try {
			this.cache.loadPage( this.pcb, this.cache.getWritablePagesCount() );
		} catch ( MMU.PageFault pageFault ) {
			fail( "There shouldn't be a page fault on the 3rd page. 4 pages should be loaded. Fix the Test_MMU." );
		}

		// Ensure that the 3rd page data is correctly in there
		for ( int i = this.cache.getWritablePagesCount() * MMU.FRAME_SIZE;
			  i < ( this.cache.getWritablePagesCount() + 1 ) * MMU.FRAME_SIZE && i < this.pcb
					  .getInstructionsLength(); i++ ) {
			assertEquals( "The 3rd page was not successfully loaded. The issue is at address " + i,
					this.mmu.read( this.pcb, i ), this.cache.read( this.pcb, i ) );
		}
	}
}
