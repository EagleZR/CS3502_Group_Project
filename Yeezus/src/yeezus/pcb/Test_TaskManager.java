package yeezus.pcb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_TaskManager {

	private static TaskManager taskManager;

	@BeforeClass public static void setUp() throws Exception {
		taskManager = TaskManager.INSTANCE;
		taskManager.addPCB( 14, 10, 6, 10, 25, 26, 2 );
	}

	@Test( expected = DuplicatePIDException.class ) public void addProcess() throws Exception {
		taskManager.addPCB( 14, 10, 15, 10, 25, 26, 2 );
	}

	@Test public void contains() {
		assertTrue( taskManager.contains( 14 ) );
		assertFalse( taskManager.contains( 3 ) );
		assertFalse( taskManager.contains( -1 ) );
	}

	@Test public void getProcess() throws Exception {
		PCB PCB = taskManager.getPCB( 14 );
		assertEquals( 10, PCB.getStartDiskAddress() );
		assertEquals( 6, PCB.getInstructionsLength() );
		assertEquals( 16, PCB.getInputBufferDiskAddress() );
		assertEquals( 10, PCB.getInputBufferLength() );
		assertEquals( 26, PCB.getOutputBufferDiskAddress() );
		assertEquals( 25, PCB.getOutputBufferLength() );
		assertEquals( 51, PCB.getTempBufferDiskAddress() );
		assertEquals( 26, PCB.getTempBufferLength() );
		assertEquals( 2, PCB.getPriority() );
	}

	@Test( expected = ProcessNotFoundException.class ) public void invalidPID() throws Exception {
		taskManager.getPCB( 3 );
	}

	@AfterClass public static void tearDown() {
		taskManager.reset();
	}

}
