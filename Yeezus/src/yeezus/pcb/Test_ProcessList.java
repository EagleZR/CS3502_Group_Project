package yeezus.pcb;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_ProcessList {

	ProcessList processList;

	@Before public void setUp() throws Exception {
		this.processList = new ProcessList();
		this.processList.addProcess( 14, 10, 15, 16, 25, 26, 38, 39, 50, 2 );
	}

	@Test( expected = DuplicatePIDException.class ) public void addProcess() throws Exception {
		this.processList.addProcess( 14, 10, 15, 16, 25, 26, 38, 39, 50, 2 );
	}

	@Test public void contains() throws Exception {
		assertTrue( this.processList.contains( 14 ) );
		assertFalse( this.processList.contains( 3 ) );
		assertFalse( this.processList.contains( -1 ) );
	}

	@Test public void getProcess() throws Exception {
		PCB PCB = processList.getProcess( 14 );
		assertEquals( 10, PCB.getStartInstructionAddress() );
		assertEquals( 15, PCB.getEndInstructionAddress() );
		assertEquals( 16, PCB.getStartInputBufferAddress() );
		assertEquals( 25, PCB.getEndInputBufferAddress() );
		assertEquals( 26, PCB.getStartOutputBufferAddress() );
		assertEquals( 38, PCB.getEndOutputBufferAddress() );
		assertEquals( 39, PCB.getStartTempBufferAddress() );
		assertEquals( 50, PCB.getEndTempBufferAddress() );
		assertEquals( 2, PCB.getPriority() );
	}

	@Test( expected = ProcessNotFoundException.class ) public void invalidPID() throws Exception {
		this.processList.getProcess( 3 );
	}

}
