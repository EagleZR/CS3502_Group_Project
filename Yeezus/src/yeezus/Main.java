package yeezus;

import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Driver;
import yeezus.driver.UninitializedDriverException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;

import java.io.File;

public class Main {

	public static void main( String[] args ) throws InvalidWordException, UninitializedDriverException {
		// Initialize memory
		Memory disk = new Memory( 2048 );
		Memory RAM = new Memory( 1024 );
		Memory registers = new Memory( 16 );

		// Initialize Driver
		Driver.loadFile( disk, new File( "src/yeezus/Program-File.txt" ) );

		// Start Driver
		Driver driver = new Driver( disk, RAM, registers, CPUSchedulingPolicy.FCFS );
		driver.run();
	}
}
