package yeezus.pcb;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_PCB {
	// TODO Please feel free to make this more robust if you like. I don't even check to see if the addresses overlap or anything sensible like that

	PCB PCB;

	@Before public void setUp() {
		this.PCB = new PCB( 14, 10, 15, 16, 25, 26, 38, 39, 50, 2 );
	}

	@Test public void getPid() throws Exception {
		assertEquals( 14, this.PCB.getPid() );
	}

	@Test public void getStartInstructionAddress() throws Exception {
		assertEquals( 10, this.PCB.getStartDiskInstructionAddress() );
	}

	@Test public void getEndInstructionAddress() throws Exception {
		assertEquals( 15, this.PCB.getInstructionsLength() );
	}

	@Test public void getStartInputBufferAddress() throws Exception {
		assertEquals( 16, this.PCB.getStartDiskInputBufferAddress() );
	}

	@Test public void getEndInputBufferAddress() throws Exception {
		assertEquals( 25, this.PCB.getInputBufferLength() );
	}

	@Test public void getStartOutputBufferAddress() throws Exception {
		assertEquals( 26, this.PCB.getStartDiskOutputBufferAddress() );
	}

	@Test public void getEndOutputBufferAddress() throws Exception {
		assertEquals( 38, this.PCB.getOutputBufferLength() );
	}

	@Test public void getStartTempBufferAddress() throws Exception {
		assertEquals( 39, this.PCB.getStartDiskTempBufferAddress() );
	}

	@Test public void getEndTempBufferAddress() throws Exception {
		assertEquals( 50, this.PCB.getTempBufferLength() );
	}

	@Test public void getPriority() throws Exception {
		assertEquals( 2, this.PCB.getPriority() );
	}

	@Test public void setStatus() throws Exception {
		assertEquals( 0, this.PCB.getElapsedRunTime() );
		this.PCB.setStatus( yeezus.pcb.PCB.Status.RUNNING );
		assertEquals( yeezus.pcb.PCB.Status.RUNNING, this.PCB.getStatus() ); // For that 100% coverage. So robust ;P
		Thread.sleep( 1 );
		assertTrue( this.PCB.getElapsedRunTime() > 0 );
		long waitTime = this.PCB.getElapsedWaitTime();
		Thread.sleep( 1 );
		assertEquals( waitTime, this.PCB.getElapsedWaitTime() );

		this.PCB.setStatus( yeezus.pcb.PCB.Status.WAITING );
		Thread.sleep( 1 );
		long runTime = this.PCB.getElapsedRunTime();
		assertNotEquals( waitTime, this.PCB.getElapsedWaitTime() );
		Thread.sleep( 1 );
		assertEquals( runTime, this.PCB.getElapsedRunTime() );
	}

	@Test public void getElapsedWaitTime() throws Exception {
		long timestamp = this.PCB.getElapsedWaitTime();
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.PCB.getElapsedWaitTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.PCB.getElapsedWaitTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.PCB.getElapsedWaitTime() );
	}

	@Test public void getElapsedRunTime() throws Exception {
		this.PCB.setStatus( yeezus.pcb.PCB.Status.RUNNING );
		long timestamp = this.PCB.getElapsedRunTime();
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.PCB.getElapsedRunTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.PCB.getElapsedRunTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.PCB.getElapsedRunTime() );
	}

}
