package yeezus.driver;

import org.junit.BeforeClass;
import org.junit.Test;
import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;
import yeezus.pcb.DuplicatePIDException;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Test_Loader {

	private static TaskManager taskManager;
	private static Memory disk;

	@BeforeClass public static void setup() throws InvalidWordException, DuplicatePIDException, IOException, InvalidAddressException {
		taskManager = new TaskManager();
		disk = new Memory( 2048 );
		new Loader( taskManager, new File( "src/yeezus/Program-File.txt" ), disk );
	}

	// Test that the Disk is filled correctly by the Loader
	@Test public void testMemory() throws Exception {
		// Test first address
		assertEquals( "0xc050005c", disk.read( 0 ).toString() );
		// Test last address of first job
		assertEquals( "0x92000000", disk.read( 22 ).toString() );
		// Test first address of first job's data
		assertEquals( "0x0000000a", disk.read( 23 ).toString() );
		// Test last address of first job's data
		assertEquals( "0x00000000", disk.read( 66 ).toString() );
		// Test first first address of second job
		assertEquals( "0xc0500070", disk.read( 67 ).toString() );
	}

	// Test that the TaskManager is filled correctly by the Loader
	@Test public void testPCB() throws Exception {
		// Ensure that the first job's PID exists in the TaskManager
		assertTrue( taskManager.contains( 1 ) );
		PCB pcb = taskManager.getPCB( 1 );
		// Test the first job's start instruction address
		assertEquals( 0, pcb.getStartDiskInstructionAddress() );
		// Test the first job's end instruction address
		assertEquals( 23, pcb.getInstructionsLength() );
		// Test the first job's start input buffer address
		assertEquals( 23, pcb.getStartDiskInputBufferAddress() );
		// Test the first job's end input buffer address
		assertEquals( 20, pcb.getInputBufferLength() );
		// Test the first job's start output buffer address
		assertEquals( 43, pcb.getStartDiskOutputBufferAddress() );
		// Test the first job's end output buffer address
		assertEquals( 12, pcb.getOutputBufferLength() );
		// Test the first job's start temp buffer address
		assertEquals( 55, pcb.getStartDiskTempBufferAddress() );
		// Test the first job's end temp buffer address
		assertEquals( 12, pcb.getOutputBufferLength() );
		// Test the first job's priority
		assertEquals( 2, pcb.getPriority() );

		// Test that all jobs have been loaded
		for ( int i = 1; i <= 30; i++ ) {
			assertTrue( taskManager.contains( i ) );
		}
	}

}
