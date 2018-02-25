package yeezus;

import yeezus.cpu.DMAChannel;
import yeezus.driver.Driver;
import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;

import java.io.File;

public class Main {

	public static void main( String[] args ) throws InvalidWordException {
		// Initialize memory
		Memory disk = new Memory( 2048 );
		Memory RAM = new Memory( 1024 );
		Memory registers = new Memory( 16 );

		// Start DMA Channel thread
		DMAChannel dmaChannel = new DMAChannel( RAM );
		Thread thread = new Thread( dmaChannel );
		thread.start();

		// Start Driver
		Driver driver = new Driver( disk, RAM, registers, new File( "src/yeezus/Program-File.txt" ) );
		driver.run();
	}
}
