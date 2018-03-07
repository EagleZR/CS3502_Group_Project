package yeezus.driver;

import yeezus.DuplicateIDException;
import yeezus.cpu.CPU;
import yeezus.cpu.ExecutionException;
import yeezus.cpu.InvalidInstructionException;
import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.io.IOException;

/**
 * This class represents the CPU Driver within the {@link yeezus} operating system. Multiple instances of this class
 * indicate the presence of multiple logical CPUs.
 */
public class Driver implements Runnable {

	private static Loader loader; // TODO Create as static variable? Throw exception if the Driver constructor is called while this is null?
	private static TaskManager taskManager;
	private Scheduler scheduler;
	private Dispatcher dispatcher;
	private CPU cpu;

	/**
	 * Constructs a new Driver instance from the given parameters.
	 *
	 * @param cpuid
	 * @param disk
	 * @param mmu
	 * @param registers
	 * @param schedulingMethod
	 * @throws UninitializedDriverException
	 * @throws DuplicateIDException
	 */
	public Driver( int cpuid, Memory disk, MMU mmu, Memory registers, CPUSchedulingPolicy schedulingMethod )
			throws UninitializedDriverException, DuplicateIDException {
		if ( loader == null ) {
			// This makes sure that the loader has already been run. This allows us to easily create multiple Drivers for multi-threading
			throw new UninitializedDriverException(
					"Please use the loadFile static method before creating an instance of this class." );
		}

		this.cpu = new CPU( cpuid, mmu, registers );

		this.scheduler = new Scheduler( taskManager, schedulingMethod );
		this.dispatcher = new Dispatcher( taskManager, this.cpu );
	}

	/**
	 * This loads a file onto the disk in preparation for the creation of Dispatcher instances which will load and
	 * execute the programs contained in the file.
	 *
	 * @param disk        The disk onto which the contents of the programFile will be loaded.
	 * @param programFile The file whose contents will be loaded onto the disk.
	 */
	public static void loadFile( Memory disk, File programFile )
			throws InvalidAddressException, DuplicateIDException, InvalidWordException, IOException {
		taskManager = TaskManager.INSTANCE;
		loader = new Loader( taskManager, programFile, disk );
	}

	/**
	 * Executes the main loop of the driver.
	 */
	@Override public void run() {
		while ( !taskManager.getJobQueue().isEmpty() ) {
			this.scheduler.run();
			this.dispatcher.run();
			try {
				this.cpu.run();
			} catch ( InvalidInstructionException | InvalidWordException | ExecutionException | InvalidAddressException e ) {
				e.printStackTrace();
				System.exit( 1 ); // Want to fail to indicate any error
			}
			// TODO Handle interrupts
		}

		// Ensure that memory is written back to the source
		this.dispatcher.run();
		this.scheduler.run();
	}

}
