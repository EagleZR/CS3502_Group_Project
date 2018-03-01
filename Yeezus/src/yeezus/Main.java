package yeezus;

import yeezus.cpu.ExecutionException;
import yeezus.cpu.InvalidInstructionException;
import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
import yeezus.driver.UninitializedDriverException;
import yeezus.memory.*;
import yeezus.pcb.DuplicatePIDException;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main( String[] args )
			throws InvalidWordException, UninitializedDriverException, DuplicatePIDException, IOException,
			InvalidAddressException, InvalidInstructionException,
			ExecutionException { // Since there shouldn't be an exception, we want everything to fail if there is one
		// Initialize memory
		Memory disk = new Memory( 2048 );
		Memory RAM = new Memory( 1024 );
		MMU mmu = new MMU( RAM );
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

		// Start Driver
		Driver driver = new Driver( disk, mmu, registers, CPUSchedulingPolicy.FCFS );
		driver.run();
	}
}
