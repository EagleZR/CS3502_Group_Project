package yeezus.driver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.cpu.CPU;
import yeezus.cpu.DMAChannel;
import yeezus.memory.Cache;
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

	@Before public void setup() throws Throwable {
		this.taskManager = TaskManager.INSTANCE;
		this.mmu = new MMU( new Memory( 2048 ), new Memory( 1024 ) );
		this.cpus = new CPU[1];
		DMAChannel dmaChannel = new DMAChannel( this.mmu, 8, true );
		this.cpus[0] = new CPU( 0, dmaChannel, this.mmu, 16, 20 );
		this.dispatcher = new Dispatcher( this.taskManager, this.cpus );
		this.taskManager.addPCB( 1, 0, 10, 14, 20, 10, 1 );
		PCB pcb = this.taskManager.getPCB( 1 );
		this.taskManager.createReadyQueue( CPUSchedulingPolicy.FCFS.getComparator() );
		this.taskManager.getReadyQueue().add( pcb );
		this.mmu.mapMemory( pcb );
		for ( int i = 0; i < pcb.getInstructionsLength() - 1; i++ ) {
			this.mmu.write( pcb, i, new Word( "0x13000000" ) ); // NOP
		}
		this.mmu.write( pcb, 9, new Word( "0x92000000" ) ); // HLT
		for ( int i = pcb.getInstructionsLength(); i < pcb.getTotalSize() && i < 4 * MMU.FRAME_SIZE; i++ ) {
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
	@Test public void testLoadCache() throws Throwable {
		PCB pcb = this.taskManager.getPCB( 1 );
		Cache cache = this.cpus[0].getCache();
		for ( int i = 0; i < cache.getWritablePagesCount() * MMU.FRAME_SIZE; i++ ) {
			assertEquals( "Cache data mismatch at logical address " + i, this.mmu.read( pcb, i ),
					cache.read( pcb, i ) );
		}
	}

	// Add another process to the ready queue, and check that the two are swapped
	@Test public void testSwap() throws Throwable {
		// Run several cycles to increment counter
		for ( int i = 0; i < 4; i++ ) { // Move counter to 4
			this.cpus[0].debugRun();
		}
		assertEquals( 4, this.cpus[0].getPC() );

		// Set up old process and save the control data
		PCB oldProcess = this.taskManager.getPCB( 1 );
		oldProcess.setStatus( PCB.Status.WAITING );
		Memory oldRegisters = new Memory( this.cpus[0].getRegisters().getCapacity() );
		for ( int i = 0; i < this.cpus[0].getRegisters().getCapacity(); i++ ) {
			oldRegisters.write( i, this.cpus[0].getRegisters().read( i ) );
		}
		Memory oldCache = new Memory( this.cpus[0].getCache().getCapacity() );
		for ( int i = oldProcess.getTempBufferLogicalAddress(), u =
			  ( this.cpus[0].getCache().getWritablePagesCount() ) * MMU.FRAME_SIZE;
			  i < oldProcess.getTotalSize(); i++, u++ ) {
			oldCache.write( u, this.cpus[0].getCache().read( oldProcess, i ) );
		}

		// Make ready new process
		this.taskManager.addPCB( 2, 55, 4, 9, 7, 6, 2 );
		PCB newProcess = this.taskManager.getPCB( 2 );
		this.mmu.mapMemory( TaskManager.INSTANCE.getPCB( 2 ) );
		for ( int i = 0; i < newProcess.getInstructionsLength() - 1; i++ ) {
			this.mmu.write( newProcess, i, new Word( "0x05000000" ) ); // NOP
		}
		this.mmu.write( newProcess, 9, new Word( "0x87000000" ) ); // HLT
		for ( int i = newProcess.getInstructionsLength();
			  i < newProcess.getTotalSize() && i < 4 * MMU.FRAME_SIZE; i++ ) {
			this.mmu.write( newProcess, i, new Word( i ) );
		}
		this.taskManager.getReadyQueue().add( newProcess );

		// Run dispatcher (for swap)
		this.dispatcher.run();

		// Check if the processes have been successfully swapped
		assertEquals( 2, this.cpus[0].getProcess().getPID() );
		assertEquals( 0, this.cpus[0].getPC() );
		assertEquals( 0, this.taskManager.getReadyQueue().size() );

		// Check that old data has been saved
		checkMemory( oldRegisters, oldProcess.getRegisters() );
		// checkMemory( oldCache, oldProcess.getCache() );
		assertEquals( 4, oldProcess.getPC() );

		// Swap again
		newProcess.setStatus( PCB.Status.WAITING );
		oldProcess.setStatus( PCB.Status.READY );
		TaskManager.INSTANCE.getReadyQueue().add( oldProcess );
		this.dispatcher.run();

		// Ensure that registers and cache are swapped in/restored
		Memory newCache = new Memory( this.cpus[0].getCache().getCapacity() );
		for ( int i = oldProcess.getTempBufferLogicalAddress(), u =
			  ( this.cpus[0].getCache().getWritablePagesCount() ) * MMU.FRAME_SIZE;
			  i < oldProcess.getTotalSize(); i++, u++ ) {
			newCache.write( u, this.cpus[0].getCache().read( oldProcess, i ) );
		}

		checkMemory( oldRegisters, this.cpus[0].getRegisters() );
		checkMemory( oldCache, newCache );

		// Ensure that the pc was successfully loaded
		assertEquals( 4, this.cpus[0].getPC() );
	}

	// NOT A TEST
	private void checkMemory( Memory mem1, Memory mem2 ) {
		assertEquals( mem1.getCapacity(), mem2.getCapacity() );
		for ( int i = 0; i < mem1.getCapacity(); i++ ) {
			assertEquals( "The two given memories are not equal at address " + i, mem1.read( i ), mem2.read( i ) );
		}
	}
}
