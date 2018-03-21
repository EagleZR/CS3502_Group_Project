package yeezus.driver;

import yeezus.DuplicateIDException;
import yeezus.cpu.CPU;
import yeezus.memory.InvalidWordException;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.TaskManager;

import java.io.File;

/**
 * This class represents the CPU Driver within the {@link yeezus} operating system. Multiple instances of this class
 * indicate the presence of multiple logical CPUs. To initialize the class, the {@link Loader} must be called using the
 * static {@link Driver#loadFile(Memory, File)} method to load the contents of the Program-File.txt to the virtual
 * disk.
 */
public class Driver implements Runnable {

	private static Loader loader;
	private static TaskManager taskManager;
	private Scheduler scheduler;
	private Dispatcher dispatcher;
	private CPU cpu;

	/**
	 * Constructs a new Driver instance from the given parameters.
	 *
	 * @param cpuid            The ID of the CPU that is to be controlled by this driver instance.
	 * @param disk             The disk that stores all of the programs to be run by the system.
	 * @param mmu              The Memory Management Unit that controls access to the RAM.
	 * @param registerSize     The the amount of registers that are associated with this driver's CPU.
	 * @param cacheSize        The size of the cache to be used by the associated CPU.
	 * @param schedulingPolicy The process scheduling policy that this system will adhere to.
	 * @throws UninitializedDriverException Thrown if a driver instance is created before the loader has been run. This
	 *                                      can be fixed by running {@link Driver#loadFile(Memory, File)} prior to
	 *                                      creating a Driver instance.
	 * @throws DuplicateIDException         Thrown if the given CPU ID already exists with another CPU.
	 */
	public Driver( int cpuid, Memory disk, MMU mmu, int registerSize, int cacheSize,
			CPUSchedulingPolicy schedulingPolicy )
			throws UninitializedDriverException, DuplicateIDException, InvalidWordException {
		if ( loader == null ) {
			// This makes sure that the loader has already been run. This allows us to easily create multiple Drivers for multi-threading
			throw new UninitializedDriverException(
					"Please use the loadFile static method before creating an instance of this class." );
		}

		this.cpu = new CPU( cpuid, mmu, registerSize, cacheSize );

		this.scheduler = new Scheduler( mmu, disk, taskManager, schedulingPolicy );
		this.dispatcher = new Dispatcher( taskManager, this.cpu, mmu );
	}

	/**
	 * This loads a file onto the disk in preparation for the creation of Dispatcher instances which will load and
	 * execute the programs contained in the file.
	 *
	 * @param disk        The disk onto which the contents of the programFile will be loaded.
	 * @param programFile The file whose contents will be loaded onto the disk.
	 */
	public static void loadFile( Memory disk, File programFile ) throws Exception {
		taskManager = TaskManager.INSTANCE;
		loader = new Loader( taskManager, programFile, disk );
	}

	/**
	 * Resets the Driver after testing.
	 */
	public static void reset() {
		loader = null;
	}

	/**
	 * Executes the main loop of the driver. This loop will run until all processes have been completed, and the process
	 * data has been written back to the disk.
	 */
	@Override public void run() {
		while ( !taskManager.getJobQueue().isEmpty() ) {
			this.scheduler.run();
			this.dispatcher.run();
			this.cpu.run();
			// TODO Handle interrupts
		}

		// Ensure that memory is written back to the source
		this.dispatcher.run();
		this.scheduler.run();
	}

}
