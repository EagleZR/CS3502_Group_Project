package yeezus.driver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.net.URLDecoder;
import java.util.Objects;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * @author Mark Zeagler
 * @version 2.0
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
		this.mmu = new MMU( this.disk, this.RAM );
		new Loader( this.taskManager, new File( ( URLDecoder.decode(
				Objects.requireNonNull( this.getClass().getClassLoader().getResource( "Program-File.txt" ) ).getFile(),
				"UTF-8" ) ) ), this.disk );
	}

	@Test public void testFCFS() throws Throwable { // Job 1
		CPUSchedulingPolicy schedulingPolicy = CPUSchedulingPolicy.FCFS;
		Scheduler scheduler = new Scheduler( this.mmu, this.taskManager, schedulingPolicy, 1 );
		this.taskManager.createReadyQueue( schedulingPolicy.getComparator() );
		scheduler.run();
		assertEquals( "0xC050005C", this.mmu.read( TaskManager.INSTANCE.getPCB( 1 ), 0 ).toString() );
		PCB pcb = this.taskManager.getPCB( 1 );
		assertEquals( pcb, this.taskManager.getReadyQueue().peek() );
		assertEquals( PCB.Status.READY, pcb.getStatus() );
	}

	@Test public void testPriority() throws Throwable {
		CPUSchedulingPolicy schedulingPolicy = CPUSchedulingPolicy.Priority;
		Scheduler scheduler = new Scheduler( this.mmu, this.taskManager, schedulingPolicy, 1 );
		this.taskManager.createReadyQueue( schedulingPolicy.getComparator() );
		scheduler.run();
		assertEquals( "0xC050004C", this.mmu.read( TaskManager.INSTANCE.getPCB( 8 ), 0 ).toString() );
		PCB pcb = this.taskManager.getPCB( 8 );
		assertEquals( pcb, this.taskManager.getReadyQueue().peek() );
		assertEquals( PCB.Status.READY, pcb.getStatus() );
	}

	@Test public void testSJF() {
		CPUSchedulingPolicy schedulingPolicy = CPUSchedulingPolicy.SJF;
		Scheduler scheduler = new Scheduler( this.mmu, this.taskManager, schedulingPolicy, 1 );
		this.taskManager.createReadyQueue( schedulingPolicy.getComparator() );
		scheduler.run();
		int[] possibleJobs = { 4, 7, 8, 14, 16, 21, 26, 30 }; // Each job has the same length
		PCB mmuPCB = null;
		PCB readyQPCB = this.taskManager.getReadyQueue().remove();
		for ( int job : possibleJobs ) {
			PCB pcb = this.taskManager.getPCB( job );
			if ( this.mmu.processMapped( pcb ) ) {
				mmuPCB = pcb;
			}
		}
		assertNotEquals( "The PCB was not loaded into the MMU.", null, mmuPCB );
		assertEquals( "The same PCB was not loaded into both the Ready Queue and the MMU", mmuPCB, readyQPCB );
		assertEquals( PCB.Status.READY, readyQPCB.getStatus() );
	}

	// Test that page faults are dealt with correctly
	@Test public void testHandlePageFaults() {
		this.taskManager.createReadyQueue( CPUSchedulingPolicy.FCFS.getComparator() );
		Scheduler scheduler = new Scheduler( this.mmu, this.taskManager, CPUSchedulingPolicy.FCFS, 1 );
		scheduler.run();
		PCB pcb = this.taskManager.getReadyQueue().remove();
		assertNotEquals( null, pcb );

		try {
			this.mmu.read( pcb, 4 * MMU.FRAME_SIZE );
			fail( "Need a different address, this one is already loaded" );
		} catch ( MMU.PageFault pageFault ) {
			// Successfully-thrown page fault
		}

		scheduler.run();
		assertEquals( "The PCB was not loaded back into the ready queue.", pcb,
				this.taskManager.getReadyQueue().peek() );

		try {
			this.mmu.read( pcb, 4 * MMU.FRAME_SIZE );
		} catch ( MMU.PageFault pageFault ) {
			fail( "The page fault was not handled correctly." );
		}
	}

	// Ensure that terminated processes have their data written back to the disk and are cleared from the MMU
	@Test public void testHandleTerminatedProcesses() throws MMU.PageFault {
		PCB pcb = this.taskManager.getPCB( 1 );
		Memory oldDisk = new Memory( this.disk.getCapacity() );
		for ( int i = 0; i < pcb.getTotalSize(); i++ ) {
			oldDisk.write( i, this.disk.read( i ) );
		}
		Scheduler scheduler = new Scheduler( this.mmu, this.taskManager, CPUSchedulingPolicy.FCFS, 1 );
		TaskManager.INSTANCE.createReadyQueue( CPUSchedulingPolicy.FCFS.getComparator() );
		scheduler.run();
		assertTrue( this.mmu.processMapped( pcb ) );
		this.mmu.write( pcb, 5, new Word( 550 ) );
		pcb.setStatus( PCB.Status.TERMINATED );
		scheduler.run();
		for ( int i = 0; i < pcb.getTotalSize(); i++ ) {
			if ( i != 5 ) {
				assertEquals( oldDisk.read( i ), this.disk.read( i ) );
			} else {
				assertEquals( 550, this.disk.read( 5 ).getData() );
			}
		}
		assertFalse( this.mmu.processMapped( pcb ) );
	}

	@After public void tearDown() {
		this.taskManager.reset();
	}
}
