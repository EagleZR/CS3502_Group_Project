package yeezus;

import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
import yeezus.driver.UninitializedDriverException;
import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.DuplicatePIDException;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main( String[] args )
			throws InvalidWordException, UninitializedDriverException, DuplicatePIDException, IOException,
			InvalidAddressException { // Since there shouldn't be an exception, we want everything to fail if there is one
		// Initialize memory
		Memory disk = new Memory( 2048 );
		Memory RAM = new Memory( 1024 );
		Memory registers = new Memory( 16 ) {
			// Here there be dragons
			@Override public void write( int address, Word word ) throws InvalidAddressException {
				if ( address == 1 ) {
					// Need to keep this read-only so its value is always 0
					throw new InvalidAddressException( "Cannot write over register 1" );
				}
				super.write( address, word );
			}
		};

		// Initialize Driver
		Driver.loadFile( disk, new File( "src/yeezus/Program-File.txt" ) );

		// Start Driver
		Driver driver = new Driver( disk, RAM, registers, CPUSchedulingPolicy.FCFS );
		driver.run();
	}
}
