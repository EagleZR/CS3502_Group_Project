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

	private TaskManager taskManager;
	private MMU mmu;
	private CPU cpu;
	private Dispatcher dispatcher;

	@Before public void setup() throws Exception {
		taskManager = TaskManager.INSTANCE;
		mmu = new MMU( new Memory( 1024 ) );
		cpu = new CPU( 0, mmu, 16, 100 );
		dispatcher = new Dispatcher( taskManager, cpu );
		taskManager.addPCB( 1, 0, 10, 14, 20, 10, 1 );
		taskManager.getReadyQueue().add( taskManager.getPCB( 1 ) );
		mmu.mapMemory( 1, 54 );
		for ( int i = 0; i < 54; i++ ) {
			mmu.write( 1, i, new Word( i ) );
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
		assertEquals( 1, cpu.getProcess().getPID() );
		assertEquals( PCB.Status.RUNNING, taskManager.getPCB( 1 ).getStatus() );
		assertEquals( 0, taskManager.getReadyQueue().size() );
	}

	// Test that cache is loaded
	@Test public void testLoadCache() throws Exception {
		PCB pcb = taskManager.getPCB( 1 );
		Memory cache = cpu.getCache();
		int pid = pcb.getPID();
		for ( int i = 0; i < pcb.getTotalSize(); i++ ) {
			assertEquals( mmu.read( pid, i ), cache.read( i ) );
		}
	}

	// Add another process to the ready queue, and check that the two are swapped
	@Test public void testSwap() throws Exception {
		PCB oldProcess = taskManager.getPCB( 1 );
		oldProcess.setStatus( PCB.Status.READY );

		// Change cache
		Word changeWord = new Word( "0xA0112310" );
		this.cpu.getCache().write( 21, changeWord );

		// Make ready new process
		taskManager.addPCB( 2, 55, 4, 9, 7, 6, 2 );
		taskManager.getReadyQueue().add( taskManager.getPCB( 2 ) );

		// Run dispatcher (for swap)
		dispatcher.run();

		// Check if the processes have been successfully swapped
		assertEquals( 2, cpu.getProcess().getPID() );
		assertEquals( 1, taskManager.getReadyQueue().size() );
		assertEquals( oldProcess, taskManager.getReadyQueue().peek() );

		// Check that cache has been swapped, and old changes are reflected in RAM
		for ( int i = 0; i < taskManager.getPCB( 2 ).getTotalSize(); i++ ) {
			assertEquals( mmu.read( 2, i ), this.cpu.getCache().read( i ) );
		}
		assertEquals( changeWord, mmu.read( 1, 21 ) );
	}
}
