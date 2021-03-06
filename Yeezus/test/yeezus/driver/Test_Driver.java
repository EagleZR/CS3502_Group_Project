package yeezus.driver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.cpu.CPU;
import yeezus.memory.Memory;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.util.Objects;

import static org.junit.Assert.*;

public class Test_Driver {

	private Memory controlDisk;
	private Memory disk;

	@Before public void setUp() throws Exception {
		this.disk = new Memory( 150 );
		this.controlDisk = new Memory( 150 );
		File file = new File( ( URLDecoder.decode(
				Objects.requireNonNull( this.getClass().getClassLoader().getResource( "Test-File.txt" ) ).getFile(),
				"UTF-8" ) ) );
		assertTrue( file.exists() );
		Driver.loadFile( this.disk, file );
		// Copy contents into a control disk to verify that changes have occurred
		for ( int i = 0; i < this.disk.getCapacity(); i++ ) {
			this.controlDisk.write( i, this.disk.read( i ) );
		}
		for ( int i = 0; i < this.disk.getCapacity(); i++ ) {
			assertEquals( this.controlDisk.read( i ), this.disk.read( i ) );
		}
	}

	@After public void tearDown() {
		Driver.reset();
		TaskManager.INSTANCE.reset();
		CPU.reset();
	}

	@Test public void loadFile() {
		new Driver( 1, this.disk, 16, 100, 100, CPUSchedulingPolicy.FCFS );
		// Should be no exception here
		Driver.reset();
		try {
			new Driver( 1, this.disk, 16, 100, 100, CPUSchedulingPolicy.FCFS );
		} catch ( UninitializedDriverException e ) {
			// Correct exception thrown
			return;
		}
		fail();
	}

	@Test public void runFCFS() throws Exception {
		new Driver( 1, this.disk, 16, 100, 100, CPUSchedulingPolicy.FCFS ).run();
		// Print the disk contents for manual verification
		File output = new File( "output/FCFS_Output_Test_File.txt" );
		output.getParentFile().mkdirs();
		if ( output.exists() || output.createNewFile() ) {
			PrintStream out = new PrintStream( new FileOutputStream( output ) );
			for ( int i = 0; i < this.disk.getCapacity(); i++ ) {
				out.println( this.disk.read( i ) );
			}
		}
		assertEquals( this.disk.read( 43 ).getData(), 228 );
		// Check if any of them are not equal, indicating that some change has been made
		for ( int i = 0; i < this.disk.getCapacity(); i++ ) {
			if ( !this.controlDisk.read( i ).equals( this.disk.read( i ) ) ) {
				System.out.println(
						"Data change at address " + i + ".\tControl: " + this.controlDisk.read( i ) + ", Disk: "
								+ this.disk.read( i ) );
				return;
			}
		}
		fail();
	}

	@Test public void runPriority() throws Exception {
		new Driver( 1, this.disk, 16, 100, 100, CPUSchedulingPolicy.Priority ).run();
		// Print the disk contents for manual verification
		File output = new File( "output/Priority_Output_Test_File.txt" );
		output.getParentFile().mkdirs();
		if ( output.exists() || output.createNewFile() ) {
			PrintStream out = new PrintStream( new FileOutputStream( output ) );
			for ( int i = 0; i < this.disk.getCapacity(); i++ ) {
				out.println( this.disk.read( i ) );
			}
		}
		assertEquals( this.disk.read( 43 ).getData(), 228 );
		// Check if any of them are not equal, indicating that some change has been made
		for ( int i = 0; i < this.disk.getCapacity(); i++ ) {
			if ( !this.controlDisk.read( i ).equals( this.disk.read( i ) ) ) {
				System.out.println(
						"Data change at address " + i + ".\tControl: " + this.controlDisk.read( i ) + ", Disk: "
								+ this.disk.read( i ) );
				return;
			}
		}
		fail();
	}
}
