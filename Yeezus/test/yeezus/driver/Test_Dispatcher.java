package yeezus.driver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.cpu.CPU;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import static org.junit.Assert.assertEquals;

public class Test_Dispatcher {

	private static TaskManager taskManager;
	private static MMU mmu;
	private static CPU cpu;
	private static Dispatcher dispatcher;

	@Before public void setup() throws Exception {
		taskManager = TaskManager.INSTANCE;
		mmu = new MMU( new Memory( 1024 ) );
		cpu = new CPU( 0, mmu, new Memory( 16 ) );
		dispatcher = new Dispatcher( taskManager, cpu );
		taskManager.addPCB( 0, 0, 10, 14, 20, 10, 1 );
		taskManager.getReadyQueue().add( taskManager.getPCB( 0 ) );
		mmu.mapMemory( 0, 54 );
		for ( int i = 0; i < 54; i++ ) {
			mmu.write( 0, i, new Word( i ) );
		}

		// Test that the dispatcher loads the next process into an empty CPU
		assertEquals( null, cpu.getProcess() );
		dispatcher.run();
	}

	@After public void tearDown() {
		CPU.reset();
		TaskManager.INSTANCE.reset();
	}

	@Test public void testDispatch() throws Exception {
		assertEquals( 0, cpu.getProcess().getPID() );
		assertEquals( PCB.Status.RUNNING, taskManager.getPCB( 0 ).getStatus() );
		assertEquals( 0, taskManager.getReadyQueue().size() );
		// TODO Test that cache is loaded
	}

	// Add another process to the ready queue, and check that the two are swapped
//	public void testSwap() throws Exception {
//		PCB oldProcess = taskManager.getPCB( 0 );
//		oldProcess.setStatus( PCB.Status.READY );
//		// TODO Change cache
//		taskManager.addPCB( 1, 55, 4, 9, 7, 6, 2 );
//		taskManager.getReadyQueue().add( taskManager.getPCB( 1 ) );
//		dispatcher.run();
//
//		assertEquals( 1, cpu.getProcess().getPID() );
//		assertEquals( 1, taskManager.getReadyQueue().size() );
//		assertEquals( oldProcess, taskManager.getReadyQueue().peek() );
//
//		// TODO Check that cache has been swapped, and old changes are reflected in RAM
//	}
}
