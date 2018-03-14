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
		PCB pcb = taskManager.getPCB( 1 );
		mmu.mapMemory( 1, 54 );
		for ( int i = 0; i < pcb.getInstructionsLength() - 1; i++ ) {
			mmu.write( 1, i, new Word( "0x13000000" ) ); // NOP
		}
		mmu.write( 1, 9, new Word( "0x92000000" ) ); // HLT
		for ( int i = pcb.getInstructionsLength(); i < pcb.getTotalSize(); i++ ) {
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
		// Run several cycles to increment counter
		for ( int i = 0; i < 4; i++ ) { // Move counter to 4
			this.cpu.debugRun();
		}

		// Set up old process and save the control data
		PCB oldProcess = taskManager.getPCB( 1 );
		oldProcess.setStatus( PCB.Status.READY );
		Memory oldRegisters = new Memory( this.cpu.getRegisters().getCapacity() );
		for ( int i = 0; i < this.cpu.getRegisters().getCapacity(); i++ ) {
			oldRegisters.write( i, this.cpu.getRegisters().read( i ) );
		}
		Memory oldCache = new Memory( this.cpu.getCache().getCapacity() );
		for ( int i = 0; i < this.cpu.getCache().getCapacity(); i++ ) {
			oldCache.write( i, this.cpu.getCache().read( i ) );
		}

		// Make ready new process
		taskManager.addPCB( 2, 55, 4, 9, 7, 6, 2 );
		PCB newProcess = taskManager.getPCB( 2 );
		this.mmu.mapMemory( 2, newProcess.getTotalSize() );
		for ( int i = 0; i < newProcess.getTotalSize(); i++ ) {
			mmu.write( 1, i, new Word( i * i ) );
		}
		taskManager.getReadyQueue().add( newProcess );

		// Run dispatcher (for swap)
		dispatcher.run();

		// Check if the processes have been successfully swapped
		assertEquals( 2, cpu.getProcess().getPID() );
		assertEquals( 1, taskManager.getReadyQueue().size() );
		assertEquals( oldProcess, taskManager.getReadyQueue().peek() );

		// Check that the cache has been swapped, and old changes are reflected in the PCB
		for ( int i = 0; i < newProcess.getTotalSize(); i++ ) {
			assertEquals( mmu.read( 2, i ), this.cpu.getCache().read( i ) );
		}
		for ( int i = 0; i < oldRegisters.getCapacity(); i++ ) {
			assertEquals( oldRegisters.read( i ), oldProcess.getRegisters().read( i ) );
		}
		for ( int i = 0; i < oldCache.getCapacity(); i++ ) {
			assertEquals( oldCache.read( i ), oldProcess.getCache().read( i ) );
		}
		assertEquals( 4, oldProcess.getPC() );

		// Swap again
		newProcess.setStatus( PCB.Status.READY );
		dispatcher.run();

		// Ensure that registers and cache are swapped in/restored
		for ( int i = 0; i < oldRegisters.getCapacity(); i++ ) {
			assertEquals( oldRegisters.read( i ), this.cpu.getRegisters().read( i ) );
		}
		for ( int i = 0; i < oldCache.getCapacity(); i++ ) {
			assertEquals( oldCache.read( i ), this.cpu.getCache().read( i ) );
		}

		// Ensure that the pc was successfully loaded
		for ( int i = 0; i < 6; i++ ) {
			this.cpu.debugRun();
		}
		assertEquals( PCB.Status.TERMINATED, oldProcess.getStatus() );
	}
}
