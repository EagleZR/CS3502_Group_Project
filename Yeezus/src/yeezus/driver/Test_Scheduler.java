package yeezus.driver;

import org.junit.*;
import yeezus.memory.MMU;
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

	private Memory disk;
	private Memory RAM;
	private MMU mmu;
	private TaskManager taskManager;

	@Before public void setUp() throws Exception {
		taskManager = TaskManager.INSTANCE;
		disk = new Memory( 2048 );
		RAM = new Memory( 1024 );
		mmu = new MMU(RAM);
		new Loader( taskManager, new File( "src/yeezus/Program-File.txt" ), disk );
	}

	@Test public void testFCFS() throws Exception { // Job 1
		Scheduler scheduler = new Scheduler( mmu, disk, taskManager, CPUSchedulingPolicy.FCFS );
		scheduler.run();
		assertEquals( "0xC050005C", RAM.read( 0 ).toString() );
		PCB pcb = taskManager.getPCB( 1 );
		/* TODO Check by logical addresses using the MMU
		assertEquals( 0, pcb.getStartRAMInstructionAddress() );
		assertEquals( 23, pcb.getStartRAMInputBufferAddress() );
		assertEquals( 43, pcb.getStartRAMOutputBufferAddress() );
		assertEquals( 55, pcb.getStartRAMTempBufferAddress() );
		*/
		assertEquals( pcb, taskManager.getReadyQueue().peek() );
		assertEquals( PCB.Status.READY, pcb.getStatus() );
	}

	@Test public void testPriority() throws Exception { // Job 13
		Scheduler scheduler = new Scheduler( mmu, disk, taskManager, CPUSchedulingPolicy.Priority );
		scheduler.run();
		assertEquals( "0xC0500070", RAM.read( 0 ).toString() );
		PCB pcb = taskManager.getPCB( 1 );
		/*
		assertEquals( 0, pcb.getStartRAMInstructionAddress() );
		assertEquals( 19, pcb.getStartRAMInputBufferAddress() );
		assertEquals( 38, pcb.getStartRAMOutputBufferAddress() );
		assertEquals( 49, pcb.getStartRAMTempBufferAddress() ); // TODO need to check these addresses again
		*/
		assertEquals( pcb, taskManager.getReadyQueue().peek() );
		assertEquals( PCB.Status.READY, pcb.getStatus() );
	}

	@After public void tearDown() {
		taskManager.reset();
	}

}
