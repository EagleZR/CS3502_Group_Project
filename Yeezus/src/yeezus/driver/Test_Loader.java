package yeezus.driver;

import org.junit.BeforeClass;
import org.junit.Test;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.Process;

import java.io.File;

import static org.junit.Assert.*;

public class Test_Loader {

	private PCB pcb;
	private Memory disk;

	@BeforeClass public void setup() {
		this.pcb = new PCB();
		this.disk = new Memory( 2048 );
		new Loader( this.pcb, new File( "src/yeezus/Program-File.txt" ), this.disk ).run();
	}

	// Test that the Disk is filled correctly by the Loader
	@Test public void testMemory() throws Exception {
		// Test first address
		assertEquals( "0xC050005C", this.disk.read( 0 ).toString() );
		// Test last address of first job
		assertEquals( "0x92000000", this.disk.read( 22 ).toString() );
		// Test first address of first job's data
		assertEquals( "0x0000000A", this.disk.read( 23 ).toString() );
		// Test last address of first job's data
		assertEquals( "0x00000000", this.disk.read( 66 ).toString() );
		// Test first first address of second job
		assertEquals( "0xC0500070", this.disk.read( 67 ).toString() );
	}

	// Test that the PCB is filled correctly by the Loader
	@Test public void testPCB() throws Exception {
		// Ensure that the first job's PID exists in the PCB
		assertTrue( pcb.contains( 1 ) );
		Process process = pcb.getProcess( 1 );
		// Test the first job's start instruction address
		assertEquals( 0, process.getStartInstructionAddress() );
		// Test the first job's end instruction address
		assertEquals( 22, process.getEndInstructionAddress() );
		// Test the first job's start input buffer address
		assertEquals( 23, process.getStartInputBufferAddress() );
		// Test the first job's end input buffer address
		assertEquals( 42, process.getEndInputBufferAddress() );
		// Test the first job's start output buffer address
		assertEquals( 43, process.getStartOutputBufferAddress() );
		// Test the first job's end output buffer address
		assertEquals( 54, process.getEndOutputBufferAddress() );
		// Test the first job's start temp buffer address
		assertEquals( 55, process.getStartTempBufferAddress() );
		// Test the first job's end temp buffer address
		assertEquals( 66, process.getEndTempBufferAddress() );
		// Test the first job's priority
		assertEquals( 2, process.getPriority() );

		// Test that all jobs have been loaded
		for ( int i = 1; i <= 30; i++ ) {
			assertTrue( pcb.contains( i ) );
		}
	}

}
