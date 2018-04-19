package yeezus.cpu;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.driver.CPUSchedulingPolicy;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import static junit.framework.TestCase.*;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_DMAChannel {

	private Thread dmaThread;
	private DMAChannel dmaChannel;
	private MMU mmu;
	private PCB pcb;

	@Before public void setup() {
		this.mmu = new MMU( new Memory( 500 ), new Memory( 500 ) );
		this.dmaChannel = new DMAChannel( this.mmu, 8, true );
		this.dmaThread = new Thread( this.dmaChannel );
		this.dmaThread.start();
		TaskManager.INSTANCE.addPCB( 1, 0, 23, 20, 12, 12, 1 );
		TaskManager.INSTANCE.createReadyQueue( CPUSchedulingPolicy.FCFS.getComparator() );
		this.pcb = TaskManager.INSTANCE.getPCB( 1 );
		assertTrue( this.mmu.mapMemory( this.pcb ) );
	}

	// Check that the correct value is read into the correct register without errors
	@Test public void testHandleIORead() {
		assertTrue( this.mmu.loadPage( this.pcb, 5 ) ); // To eliminate page fault
		try {
			this.mmu.write( this.pcb, 23, new Word( "0x0000000A" ) );
		} catch ( MMU.PageFault pageFault ) {
			fail( "The correct page was not loaded." );
		}
		CPU cpu = new CPU( 1, this.dmaChannel, this.mmu, 16, 20, 12 );
		ExecutableInstruction.IOExecutableInstruction instruction = (ExecutableInstruction.IOExecutableInstruction) cpu
				.decode( new Word( "0xC050005C" ) );
		this.dmaChannel.handle( instruction, this.pcb, cpu.getRegisters() );
		long startTime = System.currentTimeMillis();
		while ( this.pcb.getStatus() != PCB.Status.READY ) {
			if ( System.currentTimeMillis() - startTime >= 500 ) {
				fail( "The I/O took too long." );
			}
		}
		this.dmaChannel.retrieveInput( instruction, this.pcb, cpu.getRegisters() );
		assertEquals( "The correct value was not read.", new Word( "0x0000000A" ), cpu.getRegisters().read( 5 ) );
	}

	// Check that the correct value is written to the correct RAM address without errors
	@Test public void testHandleIOWrite() {
		assertTrue( this.mmu.loadPage( this.pcb, 10 ) ); // To eliminate page fault
		CPU cpu = new CPU( 1, this.dmaChannel, this.mmu, 16, 20, 12 );
		cpu.getRegisters().write( 0, new Word( "0x000000E4" ) );
		ExecutableInstruction.IOExecutableInstruction instruction = (ExecutableInstruction.IOExecutableInstruction) cpu
				.decode( new Word( "0xC10000AC" ) );
		this.dmaChannel.handle( instruction, this.pcb, cpu.getRegisters() );
		long startTime = System.currentTimeMillis();
		while ( this.pcb.getStatus() != PCB.Status.READY ) {
			if ( System.currentTimeMillis() - startTime >= 5000 ) {
				fail( "The I/O took too long." );
			}
		}
		assertEquals( "The DMA Channel needs to set the status to READY", PCB.Status.READY, this.pcb.getStatus() );
		try {
			assertEquals( "The correct value was not read.", new Word( "0x000000E4" ), this.mmu.read( this.pcb, 43 ) );
		} catch ( MMU.PageFault pageFault ) {
			fail( "There shouldn't be a fault here. The wrong page was loaded earlier." );
		}
	}

	@Test public void testPageFaultRead() {
		assertEquals( 0, this.mmu.getPageFaults().size() );
		try {
			this.mmu.write( this.pcb, 23, new Word( "0x0000000A" ) );
			fail( "No fault was thrown." );
		} catch ( MMU.PageFault pageFault ) {
			// Do nothing
		}
		CPU cpu = new CPU( 1, this.dmaChannel, this.mmu, 16, 20, 12 );
		ExecutableInstruction.IOExecutableInstruction instruction = (ExecutableInstruction.IOExecutableInstruction) cpu
				.decode( new Word( "0xC050005C" ) );
		this.dmaChannel.handle( instruction, this.pcb, cpu.getRegisters() );
		long startTime = System.currentTimeMillis();
		while ( this.mmu.getPageFaults().size()
				== 0 ) { // The PCB starts as NEW, is never set to WAITING for the I/O, and is set to WAITING by the MMU
			if ( System.currentTimeMillis() - startTime >= 500 ) {
				fail( "The I/O took too long." );
			}
		}
		assertEquals( 1, this.mmu.getPageFaults().size() );
		assertEquals( this.pcb, this.mmu.getPageFaults().get( 0 ).getPCB() );
		// assertEquals( PCB.Status.WAITING, this.pcb.getStatus() ); // Handled by the CPU
	}

	@Test public void testPageFaultWrite() {
		assertEquals( 0, this.mmu.getPageFaults().size() );
		// assertTrue( this.mmu.loadPage( this.pcb, 10 ) ); // To eliminate page fault
		CPU cpu = new CPU( 1, this.dmaChannel, this.mmu, 16, 20, 12 );
		cpu.getRegisters().write( 0, new Word( "0x000000E4" ) );
		ExecutableInstruction.IOExecutableInstruction instruction = (ExecutableInstruction.IOExecutableInstruction) cpu
				.decode( new Word( "0xC10000AC" ) );
		this.dmaChannel.handle( instruction, this.pcb, cpu.getRegisters() );
		long startTime = System.currentTimeMillis();
		while ( this.mmu.getPageFaults().size() == 0 ) {
			if ( System.currentTimeMillis() - startTime >= 5000 ) {
				fail( "The I/O took too long." );
			}
		}
		assertEquals( 1, this.mmu.getPageFaults().size() );
		assertEquals( this.pcb, this.mmu.getPageFaults().get( 0 ).getPCB() );
		// assertEquals( PCB.Status.WAITING, this.pcb.getStatus() ); // Handled by the CPU
	}

	@Test public void testMultipleRetrieveData() {
		assertTrue( this.mmu.loadPage( this.pcb, 5 ) ); // To eliminate page fault
		try {
			this.mmu.write( this.pcb, 23, new Word( "0x0000000A" ) );
		} catch ( MMU.PageFault pageFault ) {
			fail( "The correct page was not loaded." );
		}
		TaskManager.INSTANCE.addPCB( 2, 80, 23, 20, 12, 12, 2 );
		PCB pcb2 = TaskManager.INSTANCE.getPCB( 2 );
		assertTrue( this.mmu.mapMemory( pcb2 ) );
		assertTrue( this.mmu.loadPage( pcb2, 5 ) ); // To eliminate page fault
		try {
			this.mmu.write( pcb2, 23, new Word( "0x0000000B" ) );
		} catch ( MMU.PageFault pageFault ) {
			fail( "The correct page was not loaded." );
		}
		TaskManager.INSTANCE.addPCB( 3, 160, 23, 20, 12, 12, 3 );
		PCB pcb3 = TaskManager.INSTANCE.getPCB( 3 );
		assertTrue( this.mmu.mapMemory( pcb3 ) );
		assertTrue( this.mmu.loadPage( pcb3, 5 ) ); // To eliminate page fault
		try {
			this.mmu.write( pcb3, 23, new Word( "0x0000000C" ) );
		} catch ( MMU.PageFault pageFault ) {
			fail( "The correct page was not loaded." );
		}
		TaskManager.INSTANCE.addPCB( 4, 240, 23, 20, 12, 12, 4 );
		PCB pcb4 = TaskManager.INSTANCE.getPCB( 4 );
		assertTrue( this.mmu.mapMemory( pcb4 ) );
		assertTrue( this.mmu.loadPage( pcb4, 5 ) ); // To eliminate page fault
		try {
			this.mmu.write( pcb4, 23, new Word( "0x0000000D" ) );
		} catch ( MMU.PageFault pageFault ) {
			fail( "The correct page was not loaded." );
		}

		CPU cpu1 = new CPU( 1, this.dmaChannel, this.mmu, 16, 20, 12 );
		CPU cpu2 = new CPU( 2, this.dmaChannel, this.mmu, 16, 20, 12 );
		CPU cpu3 = new CPU( 3, this.dmaChannel, this.mmu, 16, 20, 12 );
		CPU cpu4 = new CPU( 4, this.dmaChannel, this.mmu, 16, 20, 12 );

		ExecutableInstruction.IOExecutableInstruction instruction1 = (ExecutableInstruction.IOExecutableInstruction) cpu1
				.decode( new Word( "0xC050005C" ) );
		ExecutableInstruction.IOExecutableInstruction instruction2 = (ExecutableInstruction.IOExecutableInstruction) cpu2
				.decode( new Word( "0xC050005C" ) );
		ExecutableInstruction.IOExecutableInstruction instruction3 = (ExecutableInstruction.IOExecutableInstruction) cpu3
				.decode( new Word( "0xC050005C" ) );
		ExecutableInstruction.IOExecutableInstruction instruction4 = (ExecutableInstruction.IOExecutableInstruction) cpu4
				.decode( new Word( "0xC050005C" ) );

		this.dmaChannel.handle( instruction1, this.pcb, cpu1.getRegisters() );
		this.dmaChannel.handle( instruction2, pcb2, cpu2.getRegisters() );
		this.dmaChannel.handle( instruction3, pcb3, cpu3.getRegisters() );
		this.dmaChannel.handle( instruction4, pcb4, cpu4.getRegisters() );

		long startTime = System.currentTimeMillis();
		while ( this.pcb.getStatus() != PCB.Status.READY ) {
			if ( System.currentTimeMillis() - startTime >= 5000 ) {
				fail( "The I/O took too long." );
			}
		}
		startTime = System.currentTimeMillis();
		while ( pcb2.getStatus() != PCB.Status.READY ) {
			if ( System.currentTimeMillis() - startTime >= 5000 ) {
				fail( "The I/O took too long." );
			}
		}
		startTime = System.currentTimeMillis();
		while ( pcb3.getStatus() != PCB.Status.READY ) {
			if ( System.currentTimeMillis() - startTime >= 5000 ) {
				fail( "The I/O took too long." );
			}
		}
		startTime = System.currentTimeMillis();
		while ( pcb4.getStatus() != PCB.Status.READY ) {
			if ( System.currentTimeMillis() - startTime >= 5000 ) {
				fail( "The I/O took too long." );
			}
		}

		assertTrue( this.dmaChannel.retrieveInput( instruction4, pcb4, cpu4.getRegisters() ) );
		assertTrue( this.dmaChannel.retrieveInput( instruction3, pcb3, cpu3.getRegisters() ) );
		assertTrue( this.dmaChannel.retrieveInput( instruction2, pcb2, cpu2.getRegisters() ) );
		assertTrue( this.dmaChannel.retrieveInput( instruction1, this.pcb, cpu1.getRegisters() ) );

		assertEquals( "The correct value was not read.", new Word( "0x0000000A" ), cpu1.getRegisters().read( 5 ) );
		assertEquals( "The correct value was not read.", new Word( "0x0000000B" ), cpu2.getRegisters().read( 5 ) );
		assertEquals( "The correct value was not read.", new Word( "0x0000000C" ), cpu3.getRegisters().read( 5 ) );
		assertEquals( "The correct value was not read.", new Word( "0x0000000D" ), cpu4.getRegisters().read( 5 ) );
	}

	@After public void tearDown() throws InterruptedException {
		this.dmaChannel.scheduleShutdown();
		this.dmaThread.join();
		TaskManager.INSTANCE.reset();
		CPU.reset();
	}
}
