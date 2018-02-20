package yeezus.pcb;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_PCB {

	PCB pcb;

	@Before public void setUp() throws Exception {
		this.pcb = new PCB();
		this.pcb.addProcess( 14, 10, 15, 16, 25, 26, 38, 39, 50, 2 );
	}

	@Test( expected = DuplicatePIDException.class ) public void addProcess() throws Exception {
		this.pcb.addProcess( 14, 10, 15, 16, 25, 26, 38, 39, 50, 2 );
	}

	@Test public void contains() throws Exception {
		assertTrue( this.pcb.contains( 14 ) );
		assertFalse( this.pcb.contains( 3 ) );
		assertFalse( this.pcb.contains( -1 ) );
	}

	@Test public void getProcess() throws Exception {
		Process process = pcb.getProcess( 14 );
		assertEquals( 10, process.getStartInstructionAddress() );
		assertEquals( 15, process.getEndInstructionAddress() );
		assertEquals( 16, process.getStartInputBufferAddress() );
		assertEquals( 25, process.getEndInputBufferAddress() );
		assertEquals( 26, process.getStartOutputBufferAddress() );
		assertEquals( 38, process.getEndOutputBufferAddress() );
		assertEquals( 39, process.getStartTempBufferAddress() );
		assertEquals( 50, process.getEndTempBufferAddress() );
		assertEquals( 2, process.getPriority() );
	}

	@Test( expected = ProcessNotFoundException.class ) public void invalidPID() throws Exception {
		this.pcb.getProcess( 3 );
	}

}
