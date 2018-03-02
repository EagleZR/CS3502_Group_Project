package yeezus.driver;

import org.junit.Before;
import org.junit.Test;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_Scheduler {

	private Memory RAM;
	private TaskManager taskManager;

	@Before public void setUp() throws Exception {
		this.taskManager = TaskManager.INSTANCE;
		Memory disk = new Memory( 2048 );
		this.RAM = new Memory( 1024 );
		new Loader( this.taskManager, new File( "src/yeezus/Program-File.txt" ), disk );
	}

	@Test public void testFCFS() throws Exception { // Job 1
		Scheduler scheduler = new Scheduler( this.taskManager, CPUSchedulingPolicy.FCFS );
		scheduler.run();
		assertEquals( "0xC050005C", this.RAM.read( 0 ).toString() );
		PCB pcb = this.taskManager.getPCB( 1 );
		assertEquals( 0, pcb.getStartRAMInstructionAddress() );
		assertEquals( 23, pcb.getStartRAMInputBufferAddress() );
		assertEquals( 43, pcb.getStartRAMOutputBufferAddress() );
		assertEquals( 55, pcb.getStartRAMTempBufferAddress() );
		assertEquals( pcb, this.taskManager.getReadyQueue().peek() );
		assertEquals( PCB.Status.READY, pcb.getStatus() );
	}

	@Test public void testPriority() throws Exception { // Job 13
		Scheduler scheduler = new Scheduler( this.taskManager, CPUSchedulingPolicy.Priority );
		scheduler.run();
		assertEquals( "0xC0500070", this.RAM.read( 0 ).toString() );
		PCB pcb = this.taskManager.getPCB( 1 );
		assertEquals( 0, pcb.getStartRAMInstructionAddress() );
		assertEquals( 19, pcb.getStartRAMInputBufferAddress() );
		assertEquals( 38, pcb.getStartRAMOutputBufferAddress() );
		assertEquals( 49, pcb.getStartRAMTempBufferAddress() ); // TODO need to check these addresses again
		assertEquals( pcb, this.taskManager.getReadyQueue().peek() );
		assertEquals( PCB.Status.READY, pcb.getStatus() );
	}

}
