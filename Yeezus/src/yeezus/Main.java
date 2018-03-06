package yeezus;

import yeezus.cpu.ExecutionException;
import yeezus.cpu.InvalidInstructionException;
import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
import yeezus.driver.UninitializedDriverException;
import yeezus.memory.*;

import java.io.File;
import java.io.IOException;

public class Main {

	private static int numCPUs = 1;

	public static void main( String[] args )
			throws InvalidWordException, UninitializedDriverException, DuplicateIDException, IOException,
			InvalidAddressException, InvalidInstructionException, ExecutionException, InterruptedException { // Since there shouldn't be an exception, we want everything to fail if there is one
		// Initialize memory
		Memory disk = new Memory( 2048 );
		MMU mmu = new MMU( new Memory( 1024 ) );
		Memory registers = new Memory( 16 ) {
			// Here there be dragons
			@Override public void write( int physicalAddress, Word word ) throws InvalidAddressException {
				if ( physicalAddress == 1 ) {
					// Need to keep this read-only so its value is always 0
					throw new InvalidAddressException( "Cannot write over register 1" );
				}
				super.write( physicalAddress, word );
			}
		};

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
	}
}
