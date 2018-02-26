package yeezus.pcb;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_TaskManager {

	TaskManager taskManager;

	@Before public void setUp() throws Exception {
		this.taskManager = new TaskManager();
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
		PCB PCB = taskManager.getPCB( 14 );
		assertEquals( 10, PCB.getStartDiskInstructionAddress() );
		assertEquals( 15, PCB.getEndDiskInstructionAddress() );
		assertEquals( 16, PCB.getStartDiskInputBufferAddress() );
		assertEquals( 25, PCB.getEndInputBufferAddress() );
		assertEquals( 26, PCB.getStartDiskOutputBufferAddress() );
		assertEquals( 38, PCB.getEndOutputBufferAddress() );
		assertEquals( 39, PCB.getStartDiskTempBufferAddress() );
		assertEquals( 50, PCB.getEndTempBufferAddress() );
		assertEquals( 2, PCB.getPriority() );
	}

	@Test( expected = ProcessNotFoundException.class ) public void invalidPID() throws Exception {
		this.taskManager.getPCB( 3 );
	}

}
