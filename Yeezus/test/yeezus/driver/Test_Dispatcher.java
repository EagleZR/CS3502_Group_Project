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
	private CPU[] cpus;
	private Dispatcher dispatcher;

	@Before public void setup() {
		this.taskManager = TaskManager.INSTANCE;
		this.mmu = new MMU( new Memory( 1024 ) );
		this.cpus = new CPU[1];
		this.cpus[0] = new CPU( 0, this.mmu, 16, 100 );
		this.dispatcher = new Dispatcher( this.taskManager, this.cpus, this.mmu );
		this.taskManager.addPCB( 1, 0, 10, 14, 20, 10, 1 );
		this.taskManager.getReadyQueue().add( this.taskManager.getPCB( 1 ) );
		PCB pcb = this.taskManager.getPCB( 1 );
		this.mmu.mapMemory( pcb );
		for ( int i = 0; i < pcb.getInstructionsLength() - 1; i++ ) {
			this.mmu.write( pcb, i, new Word( "0x13000000" ) ); // NOP
		}
		this.mmu.write( pcb, 9, new Word( "0x92000000" ) ); // HLT
		for ( int i = pcb.getInstructionsLength(); i < pcb.getTotalSize(); i++ ) {
			this.mmu.write( pcb, i, new Word( i ) );
		}

		// Test that the dispatcher loads the next process into an empty CPU
		assertEquals( null, this.cpus[0].getProcess() );
		this.dispatcher.run();
	}

	@After public void tearDown() {
		CPU.reset();
		TaskManager.INSTANCE.reset();
	}

	@Test public void testDispatch() {
		assertEquals( 1, this.cpus[0].getProcess().getPID() );
		assertEquals( PCB.Status.RUNNING, this.taskManager.getPCB( 1 ).getStatus() );
		assertEquals( 0, this.taskManager.getReadyQueue().size() );
	}

	// Test that cache is loaded
	@Test public void testLoadCache() {
		PCB pcb = this.taskManager.getPCB( 1 );
		Memory cache = this.cpus[0].getCache();
		for ( int i = 0; i < pcb.getTotalSize(); i++ ) {
			assertEquals( this.mmu.read( pcb, i ), cache.read( i ) );
		}
	}

	// Add another process to the ready queue, and check that the two are swapped
	public void testSwap() { // TODO Re-enable when we actually need to swap
		// Run several cycles to increment counter
		for ( int i = 0; i < 4; i++ ) { // Move counter to 4
			this.cpus[0].debugRun();
		}

		// Set up old process and save the control data
		PCB oldProcess = this.taskManager.getPCB( 1 );
		oldProcess.setStatus( PCB.Status.READY );
		Memory oldRegisters = new Memory( this.cpus[0].getRegisters().getCapacity() );
		for ( int i = 0; i < this.cpus[0].getRegisters().getCapacity(); i++ ) {
			oldRegisters.write( i, this.cpus[0].getRegisters().read( i ) );
		}
		Memory oldCache = new Memory( this.cpus[0].getCache().getCapacity() );
		for ( int i = 0; i < this.cpus[0].getCache().getCapacity(); i++ ) {
			oldCache.write( i, this.cpus[0].getCache().read( i ) );
		}

		// Make ready new process
		this.taskManager.addPCB( 2, 55, 4, 9, 7, 6, 2 );
		PCB newProcess = this.taskManager.getPCB( 2 );
		this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 2 ) );
		for ( int i = 0; i < newProcess.getTotalSize(); i++ ) {
			this.mmu.write( TaskManager.INSTANCE.getPCB( 1 ), i, new Word( i * i ) );
		}
		this.taskManager.getReadyQueue().add( newProcess );

		// Run dispatcher (for swap)
		this.dispatcher.run();

		// Check if the processes have been successfully swapped
		assertEquals( 2, this.cpus[0].getProcess().getPID() );
		assertEquals( 1, this.taskManager.getReadyQueue().size() );
		assertEquals( oldProcess, this.taskManager.getReadyQueue().peek() );

		// Check that the cache has been swapped, and old changes are reflected in the PCB
		for ( int i = 0; i < newProcess.getTotalSize(); i++ ) {
			assertEquals( this.mmu.read( TaskManager.INSTANCE.getPCB( 2 ), i ), this.cpus[0].getCache().read( i ) );
		}
		checkMemory( oldRegisters, oldProcess.getRegisters() );
		checkMemory( oldCache, oldProcess.getCache() );
		assertEquals( 4, oldProcess.getPC() );

		// Swap again
		newProcess.setStatus( PCB.Status.READY );
		this.dispatcher.run();

		// Ensure that registers and cache are swapped in/restored
		checkMemory( oldRegisters, this.cpus[0].getRegisters() );
		checkMemory( oldCache, this.cpus[0].getCache() );

		// Ensure that the pc was successfully loaded
		for ( int i = 0; i < 6; i++ ) {
			this.cpus[0].debugRun();
		}
		assertEquals( PCB.Status.TERMINATED, oldProcess.getStatus() );
	}

	private void checkMemory( Memory mem1, Memory mem2 ) {
		assertEquals( mem1.getCapacity(), mem2.getCapacity() );
		for ( int i = 0; i < mem1.getCapacity(); i++ ) {
			assertEquals( mem1.read( i ), mem2.read( i ) );
		}
	}
}
