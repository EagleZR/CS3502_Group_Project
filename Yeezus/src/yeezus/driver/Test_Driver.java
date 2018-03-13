package yeezus.driver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.cpu.CPU;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class Test_Driver {

	private Memory controlDisk;
	private Memory disk;
	private MMU mmu;

	@Before public void setUp() throws Exception {
		this.disk = new Memory( 2048 );
		this.controlDisk = new Memory( 2048 );
		this.mmu = new MMU( new Memory( 1024 ) );
		File file = new File( "src/yeezus/Test-File.txt" );
		assertTrue( file.exists() );
		Driver.loadFile( this.disk, file );
		// Copy contents into a control disk to verify that changes have occurred
		for ( int i = 0; i < disk.getCapacity(); i++ ) {
			this.controlDisk.write( i, disk.read( i ) );
		}
		for ( int i = 0; i < disk.getCapacity(); i++ ) {
			assertEquals( this.controlDisk.read( i ), this.disk.read( i ) );
		}
	}

	@After public void tearDown() throws Exception {
		Driver.reset();
		TaskManager.INSTANCE.reset();
		CPU.reset();
	}

	@Test public void loadFile() throws Exception {
		new Driver( 0, this.disk, this.mmu, new Memory( 16 ), CPUSchedulingPolicy.FCFS );
		new Driver( 1, this.disk, this.mmu, new Memory( 16 ), CPUSchedulingPolicy.FCFS );
		// Should be no exception here
		Driver.reset();
		try {
			new Driver( 0, this.disk, this.mmu, new Memory( 16 ), CPUSchedulingPolicy.FCFS );
			fail();
		} catch ( UninitializedDriverException e ) {
			// Correct exception thrown
		}
	}

	@Test public void runFCFS() throws Exception {
		new Driver( 0, this.disk, this.mmu, new Memory( 16 ), CPUSchedulingPolicy.FCFS ).run();
		// Print the disk contents for manual verification
		File output = new File( "src/yeezus/FCFS_Output_Test_File.txt" );
		output.createNewFile();
		PrintStream out = new PrintStream( new FileOutputStream( output ) );
		for ( int i = 0; i < this.disk.getCapacity(); i++ ) {
			out.println( this.disk.read( i ) );
		}
		// Check if any of them are not equal, indicating that some change has been made
		for ( int i = 0; i < disk.getCapacity(); i++ ) {
			if ( !this.controlDisk.read( i ).equals( this.disk.read( i ) ) ) {
				return;
			}
		}
		fail();
	}

	@Test public void runPriority() throws Exception {
		new Driver( 0, this.disk, this.mmu, new Memory( 16 ), CPUSchedulingPolicy.Priority ).run();
		// Print the disk contents for manual verification
		File output = new File( "src/yeezus/Priority_Output_Test_File.txt" );
		output.createNewFile();
		PrintStream out = new PrintStream( new FileOutputStream( output ) );
		for ( int i = 0; i < this.disk.getCapacity(); i++ ) {
			out.println( this.disk.read( i ) );
		}
		// Check if any of them are not equal, indicating that some change has been made
		for ( int i = 0; i < disk.getCapacity(); i++ ) {
			if ( !this.controlDisk.read( i ).equals( this.disk.read( i ) ) ) {
				return;
			}
		}
		fail();
	}
}
