package yeezus;

import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Main {

	private final static int NUM_CPUS = 4;
	private final static int DISK_SIZE = 2048;
	private final static int RAM_SIZE = 1024;
	private final static int CACHE_SIZE = 100;
	private final static int REGISTER_SIZE = 16;

	public static void main( String[] args )
			throws Exception { // Since there shouldn't be an exception, we want everything to fail if there is one
		// Initialize memory
		Memory disk = new Memory( DISK_SIZE );
		MMU mmu = new MMU( new Memory( RAM_SIZE ) );

		// Initialize Driver
		Driver.loadFile( disk, new File( "src/yeezus/Program-File.txt" ) );

		Thread[] threads = new Thread[NUM_CPUS];

		// Create Drivers
		for ( int i = 0; i < NUM_CPUS; i++ ) {
			Driver driver = new Driver( i, disk, mmu, REGISTER_SIZE, CACHE_SIZE, CPUSchedulingPolicy.FCFS );
			threads[i] = new Thread( driver );
		}

		// Log start time
		long startTime = System.currentTimeMillis();

		// Start threads
		for ( int i = 0; i < NUM_CPUS; i++ ) {
			threads[i].start();
		}

		// (Busy) Wait for the threads
		for ( Thread thread : threads ) {
			thread.join( 5 );
		}

		// Log end time
		long endTime = System.currentTimeMillis();

		System.out.println( "The system completed in " + ( endTime - startTime ) + " milliseconds." );

		// Print out the disk
		File output = new File( "src/yeezus/Output_File.txt" );
		output.createNewFile();
		PrintStream out = new PrintStream( new FileOutputStream( output ) );
		for ( int i = 0; i < disk.getCapacity(); i++ ) {
			out.println( disk.read( i ) );
		}

		// Print out runtime information
		for ( PCB pcb : TaskManager.INSTANCE.getPCBs() ) {
			System.out.println(
					"Process: " + pcb.getPID() + "\nWait Time: " + pcb.getElapsedWaitTime() + "\nRun Time: " + pcb
							.getElapsedRunTime() + "\nExecution Count: " + pcb.getExecutionCount() + "\n" );
		}
	}
}
