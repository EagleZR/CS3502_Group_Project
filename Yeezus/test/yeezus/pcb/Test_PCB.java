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

	private PCB pcb;

	@Before public void setUp() {
		this.pcb = new PCB( 14, 10, 6, 10, 13, 10, 2 );
	}

	@Test public void getNumIO() {
		this.pcb.incNumIO();
		this.pcb.incNumIO();
		this.pcb.incNumIO();
		assertEquals( 3, this.pcb.getNumIO() );

	}

	@Test public void getPid() {
		assertEquals( 14, this.pcb.getPID() );
	}

	@Test public void getStartInstructionAddress() {
		assertEquals( 10, this.pcb.getStartDiskAddress() );
	}

	@Test public void getInstructionLength() {
		assertEquals( 6, this.pcb.getInstructionsLength() );
	}

	@Test public void getStartInputBufferAddress() {
		assertEquals( 16, this.pcb.getInputBufferDiskAddress() );
	}

	@Test public void getInputBufferLength() {
		assertEquals( 10, this.pcb.getInputBufferLength() );
	}

	@Test public void getStartOutputBufferAddress() {
		assertEquals( 26, this.pcb.getOutputBufferDiskAddress() );
	}

	@Test public void getOutputBufferLength() {
		assertEquals( 13, this.pcb.getOutputBufferLength() );
	}

	@Test public void getStartTempBufferAddress() {
		assertEquals( 39, this.pcb.getTempBufferDiskAddress() );
	}

	@Test public void getTempBufferLength() {
		assertEquals( 10, this.pcb.getTempBufferLength() );
	}

	@Test public void getPriority() {
		assertEquals( 2, this.pcb.getPriority() );
	}

	@Test public void setStatus() throws Exception {
		assertEquals( 0, this.pcb.getElapsedRunTime() );
		this.pcb.setStatus( yeezus.pcb.PCB.Status.RUNNING );
		assertEquals( yeezus.pcb.PCB.Status.RUNNING, this.pcb.getStatus() ); // For that 100% coverage. So robust ;P
		Thread.sleep( 1 );
		assertTrue( this.pcb.getElapsedRunTime() > 0 );
		long waitTime = this.pcb.getElapsedWaitTime();
		Thread.sleep( 1 );
		assertEquals( waitTime, this.pcb.getElapsedWaitTime() );

		this.pcb.setStatus( yeezus.pcb.PCB.Status.WAITING );
		Thread.sleep( 1 );
		long runTime = this.pcb.getElapsedRunTime();
		assertNotEquals( waitTime, this.pcb.getElapsedWaitTime() );
		Thread.sleep( 1 );
		assertEquals( runTime, this.pcb.getElapsedRunTime() );
	}

	@Test public void getElapsedWaitTime() throws Exception {
		long timestamp = this.pcb.getElapsedWaitTime();
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.pcb.getElapsedWaitTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.pcb.getElapsedWaitTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.pcb.getElapsedWaitTime() );
	}

	@Test public void getElapsedRunTime() throws Exception {
		this.pcb.setStatus( yeezus.pcb.PCB.Status.RUNNING );
		long timestamp = this.pcb.getElapsedRunTime();
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.pcb.getElapsedRunTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.pcb.getElapsedRunTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.pcb.getElapsedRunTime() );
	}

}
