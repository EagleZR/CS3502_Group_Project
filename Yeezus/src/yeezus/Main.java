package yeezus;

import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Objects;

public class Main {

	// System Variables
	private final static int NUM_CPUS = 4;
	private final static CPUSchedulingPolicy POLICY = CPUSchedulingPolicy.FCFS;

	// Memory Data
	private final static int DISK_SIZE = 2048;
	private final static int RAM_SIZE = 1024;
	private final static int CACHE_SIZE = 100;
	private final static int REGISTER_SIZE = 16;

	public static void main( String[] args ) {
		Memory disk;
		Driver driver;

		try {
			// Initialize memory
			disk = new Memory( DISK_SIZE );

			// Initialize and create Driver
			Driver.loadFile( disk, new File(
					Objects.requireNonNull( Main.class.getClassLoader().getResource( "Program-File.txt" ) )
							.getFile() ) );
			driver = new Driver( NUM_CPUS, disk, REGISTER_SIZE, CACHE_SIZE, RAM_SIZE, POLICY );
		} catch ( Exception e ) {
			System.err.println( "An exception occurred in system initialization." );
			e.printStackTrace();
			return;
		}

		// Log start time
		long startTime = System.nanoTime();

		try {
			driver.run();
		} catch ( Exception e ) {
			System.err.println( "An exception occurred in system Execution." );
			e.printStackTrace();
			driver.dumpData();
			return;
		}
		// Log end time
		long endTime = System.nanoTime();

		System.out.println( "The system completed in " + ( endTime - startTime ) + " nanoseconds." );

		// Print out the disk
		try {
			File output = new File( "output/Output_File.txt" );
			if ( !output.exists() && !output.createNewFile() ) {
				throw new Exception( "The output file could not be created." ); // idk how else to exit a try block
			}
			PrintStream out = new PrintStream( new FileOutputStream( output ) );
			for ( PCB pcb : TaskManager.INSTANCE.getPCBs() ) {
				out.println( "****Job " + pcb.getPID() + "****" );
				// Instructions
				out.println( "Job " + pcb.getPID() + " Instructions:" );
				for ( int i = 0; i < pcb.getInstructionsLength(); i++ ) {
					out.println( disk.read( pcb.getInstructionDiskAddress() + i ) );
				}
				out.println();

				// Input Buffer
				out.println( "Job " + pcb.getPID() + " Input Buffer:" );
				for ( int i = 0; i < pcb.getInputBufferLength(); i++ ) {
					out.println( disk.read( pcb.getInputBufferDiskAddress() + i ) );
				}
				out.println();

				// Output Buffer
				out.println( "Job " + pcb.getPID() + " Output Buffer:" );
				for ( int i = 0; i < pcb.getOutputBufferLength(); i++ ) {
					out.println( disk.read( pcb.getOutputBufferDiskAddress() + i ) );
				}
				out.println();

				// Temp Buffer
				out.println( "Job " + pcb.getPID() + " Temp Buffer:" );
				for ( int i = 0; i < pcb.getTempBufferLength(); i++ ) {
					out.println( disk.read( pcb.getTempBufferDiskAddress() + i ) );
				}
				out.println( "______________________\n" );
			}
		} catch ( Exception e ) {
			System.err.println( "An exception occurred while writing to the output file." );
			e.printStackTrace();
		}

		// Print out runtime information
		try {
			for ( PCB pcb : TaskManager.INSTANCE.getPCBs() ) {
				System.out.println( "Process: " + pcb.getPID() + "\nWait Time (ns): " + pcb.getElapsedWaitTime()
						+ "\nRun Time (ns): " + pcb.getElapsedRunTime() + "\nExecution Count: " + pcb
						.getExecutionCount() + "\n" + "IO Count: " + pcb.getNumIO() + "\n" );
			}
		} catch ( Exception e ) {
			System.err.println( "An exception occurred while printing the process data." );
			e.printStackTrace();
		}
	}
}
