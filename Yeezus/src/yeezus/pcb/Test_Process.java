package yeezus.pcb;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_Process {
	// TODO Please feel free to make this more robust if you like. I don't even check to see if the addresses overlap or anything sensible like that

	Process process;

	@Before public void setUp() {
		this.process = new Process( 14, 10, 15, 16, 25, 26, 38, 39, 50, 2 );
	}

	@Test public void getPid() throws Exception {
		assertEquals( 14, this.process.getPid() );
	}

	@Test public void getStartInstructionAddress() throws Exception {
		assertEquals( 10, this.process.getStartInstructionAddress() );
	}

	@Test public void getEndInstructionAddress() throws Exception {
		assertEquals( 15, this.process.getEndInstructionAddress() );
	}

	@Test public void getStartInputBufferAddress() throws Exception {
		assertEquals( 16, this.process.getStartInputBufferAddress() );
	}

	@Test public void getEndInputBufferAddress() throws Exception {
		assertEquals( 25, this.process.getEndInputBufferAddress() );
	}

	@Test public void getStartOutputBufferAddress() throws Exception {
		assertEquals( 26, this.process.getStartOutputBufferAddress() );
	}

	@Test public void getEndOutputBufferAddress() throws Exception {
		assertEquals( 38, this.process.getEndOutputBufferAddress() );
	}

	@Test public void getStartTempBufferAddress() throws Exception {
		assertEquals( 39, this.process.getStartTempBufferAddress() );
	}

	@Test public void getEndTempBufferAddress() throws Exception {
		assertEquals( 50, this.process.getEndTempBufferAddress() );
	}

	@Test public void getPriority() throws Exception {
		assertEquals( 2, this.process.getPriority() );
	}

	@Test public void setStatus() throws Exception {
		assertEquals( 0, this.process.getElapsedRunTime() );
		this.process.setStatus( Process.Status.RUNNING );
		assertEquals( Process.Status.RUNNING, this.process.getStatus() ); // For that 100% coverage. So robust ;P
		Thread.sleep( 1 );
		assertTrue( this.process.getElapsedRunTime() > 0 );
		long waitTime = this.process.getElapsedWaitTime();
		Thread.sleep( 1 );
		assertEquals( waitTime, this.process.getElapsedWaitTime() );

		this.process.setStatus( Process.Status.WAITING );
		Thread.sleep( 1 );
		long runTime = this.process.getElapsedRunTime();
		assertNotEquals( waitTime, this.process.getElapsedWaitTime() );
		Thread.sleep( 1 );
		assertEquals( runTime, this.process.getElapsedRunTime() );
	}

	@Test public void getElapsedWaitTime() throws Exception {
		long timestamp = this.process.getElapsedWaitTime();
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.process.getElapsedWaitTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.process.getElapsedWaitTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.process.getElapsedWaitTime() );
	}

	@Test public void getElapsedRunTime() throws Exception {
		this.process.setStatus( Process.Status.RUNNING );
		long timestamp = this.process.getElapsedRunTime();
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.process.getElapsedRunTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.process.getElapsedRunTime() );
		Thread.sleep( 1 );
		assertNotEquals( timestamp, this.process.getElapsedRunTime() );
	}

}
