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

	private static int numCPUs = 1;

	public static void main( String[] args )
			throws Exception { // Since there shouldn't be an exception, we want everything to fail if there is one
		// Initialize memory
		Memory disk = new Memory( 2048 );
		MMU mmu = new MMU( new Memory( 1024 ) );
		Memory registers = new Memory( 16 );

		// Initialize Driver
		Driver.loadFile( disk, new File( "src/yeezus/Program-File.txt" ) );

		Thread[] threads = new Thread[numCPUs];

		// Start Drivers
		for ( int i = 0; i < numCPUs; i++ ) {
			Driver driver = new Driver( 0, disk, mmu, registers, CPUSchedulingPolicy.FCFS );
			threads[i] = new Thread( driver );
			threads[i].run();
		}

		for ( Thread thread : threads ) {
			thread.join( 500 );
		}

		File output = new File( "src/yeezus/Output_File.txt" );
		output.createNewFile();
		PrintStream out = new PrintStream( new FileOutputStream( output ) );
		for ( int i = 0; i < disk.getCapacity(); i++ ) {
			out.println( disk.read( i ) );
		}

		for ( PCB pcb : TaskManager.INSTANCE.getPCBs() ) {
			System.out.println(
					"Process: " + pcb.getPID() + "\nWait Time: " + pcb.getElapsedWaitTime() + "\nRun Time: " + pcb
							.getElapsedRunTime() + "\nExecution Count: " + pcb.getExecutionCount() + "\n" +
							"IO Count: " +  pcb.getNumIO());
		}
	}
}
