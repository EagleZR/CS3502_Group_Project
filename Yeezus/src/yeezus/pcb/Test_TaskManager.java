package yeezus.pcb;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_TaskManager {

	private TaskManager taskManager;

	@Before public void setUp() throws Exception {
		this.taskManager = TaskManager.INSTANCE;
		this.taskManager.addPCB( 14, 10, 15, 16, 25, 26, 38, 39, 50, 2 );
	}

	@Test( expected = DuplicatePIDException.class ) public void addProcess() throws Exception {
		this.taskManager.addPCB( 14, 10, 15, 16, 25, 26, 38, 39, 50, 2 );
	}

	@Test public void contains() throws Exception {
		assertTrue( this.taskManager.contains( 14 ) );
		assertFalse( this.taskManager.contains( 3 ) );
		assertFalse( this.taskManager.contains( -1 ) );
	}

	@Test public void getProcess() throws Exception {
		PCB PCB = this.taskManager.getPCB( 14 );
		assertEquals( 10, PCB.getStartDiskInstructionAddress() );
		assertEquals( 15, PCB.getInstructionsLength() );
		assertEquals( 16, PCB.getStartDiskInputBufferAddress() );
		assertEquals( 25, PCB.getInputBufferLength() );
		assertEquals( 26, PCB.getStartDiskOutputBufferAddress() );
		assertEquals( 38, PCB.getOutputBufferLength() );
		assertEquals( 39, PCB.getStartDiskTempBufferAddress() );
		assertEquals( 50, PCB.getTempBufferLength() );
		assertEquals( 2, PCB.getPriority() );
	}

	@Test( expected = ProcessNotFoundException.class ) public void invalidPID() throws Exception {
		this.taskManager.getPCB( 3 );
	}

}
