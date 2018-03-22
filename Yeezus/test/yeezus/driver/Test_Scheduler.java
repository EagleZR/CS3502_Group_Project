package yeezus.driver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
		this.taskManager = TaskManager.INSTANCE;
		this.disk = new Memory( 2048 );
		this.RAM = new Memory( 1024 );
		this.mmu = new MMU( this.RAM );
		new Loader( this.taskManager, new File( "src/yeezus/Program-File.txt" ), this.disk );
	}

	@Test public void testFCFS() { // Job 1
		Scheduler scheduler = new Scheduler( this.mmu, this.disk, this.taskManager, CPUSchedulingPolicy.FCFS );
		scheduler.run();
		assertEquals( "0xC050005C", this.mmu.read( TaskManager.INSTANCE.getPCB( 1 ), 0 ).toString() );
		PCB pcb = this.taskManager.getPCB( 1 );
		assertEquals( pcb, this.taskManager.getReadyQueue().peek() );
		assertEquals( PCB.Status.READY, pcb.getStatus() );
	}

	@Test public void testPriority() {
		Scheduler scheduler = new Scheduler( this.mmu, this.disk, this.taskManager, CPUSchedulingPolicy.Priority );
		scheduler.run();
		assertEquals( "0xC050004C", this.mmu.read( TaskManager.INSTANCE.getPCB( 8 ), 0 ).toString() );
		PCB pcb = this.taskManager.getPCB( 8 );
		assertEquals( pcb, this.taskManager.getReadyQueue().peek() );
		assertEquals( PCB.Status.READY, pcb.getStatus() );
	}

	@After public void tearDown() {
		this.taskManager.reset();
	}

}
