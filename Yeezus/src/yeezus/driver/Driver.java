package yeezus.driver;

import yeezus.DuplicateIDException;
import yeezus.cpu.CPU;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;

/**
 * This class represents the CPU Driver within the {@link yeezus} operating system. Multiple instances of this class
 * indicate the presence of multiple logical CPUs. To initialize the class, the {@link Loader} must be called using the
 * static {@link Driver#loadFile(Memory, File)} method to load the contents of the Program-File.txt to the virtual
 * disk.
 *
 * @author Mark Zeagler
 * @version 2.0
 */
public class Driver {

	private static Loader loader;
	private static TaskManager taskManager;
	private Scheduler scheduler;
	private Dispatcher dispatcher;
	private CPU cpu;
	private Thread[] threads;
	private Memory disk;
	private long[] idleTimes, executeTimes;

	/**
	 * Constructs a new Driver instance from the given parameters.
	 *
	 * @param cpuid            The ID of the CPU that is to be controlled by this driver instance.
	 * @param disk             The disk that stores all of the programs to be run by the system.
	 * @param mmu              The Memory Management Unit that controls access to the RAM.
	 * @param registers        The registers that are associated with this driver's CPU.
	 * @param schedulingPolicy The process scheduling policy that this system will adhere to.
	 * @throws UninitializedDriverException Thrown if a driver instance is created before the loader has been run. This
	 *                                      can be fixed by running {@link Driver#loadFile(Memory, File)} prior to
	 *                                      creating a Driver instance.
	 * @throws DuplicateIDException         Thrown if the given CPU ID already exists with another CPU.
	 */
	public Driver( int cpuid, Memory disk, MMU mmu, Memory registers, CPUSchedulingPolicy schedulingPolicy )
			throws UninitializedDriverException, DuplicateIDException {
		if ( loader == null ) {
			// This makes sure that the loader has already been run. This allows us to easily create multiple Drivers for multi-threading
			throw new UninitializedDriverException(
					"Please use the loadFile static method before creating an instance of this class." );
		}

		this.cpu = new CPU( cpuid, mmu, registers );

		this.scheduler = new Scheduler( mmu, disk, taskManager, schedulingPolicy );
		this.dispatcher = new Dispatcher( taskManager, this.cpu );
	}

	/**
	 * This loads a file onto the disk in preparation for the creation of Dispatcher instances which will load and
	 * execute the programs contained in the file.
	 *
	 * @param disk        The disk onto which the contents of the programFile will be loaded.
	 * @param programFile The file whose contents will be loaded onto the disk.
	 * @throws Exception Thrown if there is an issue while loading the file.
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

	public long[] getIdleTimes() {
		return idleTimes;
	}

	public long[] getExecuteTimes() {
		return executeTimes;
	}

	/**
	 * Executes the main loop of the driver. This loop will run until all processes have been completed, and the process
	 * data has been written back to the disk.
	 */
	public void run() {
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

	public String getProcPerCPU() {
		return "CPU: " + this.cpu.getCPUID() + " received " + this.cpu.getNumProcesses();
	}

	/**
	 * Prints a dump of the data that contains the current state of the system. This will print to the {@link
	 * System#out} {@link java.io.PrintStream}.
	 */
	public void dumpData() {

		System.out.println( "**Process Info**" );
		for ( PCB pcb : TaskManager.INSTANCE.getPCBs() ) {
			StringBuilder pcbDump = new StringBuilder();
			pcbDump.append( "Process: " ).append( pcb.getPID() ).append( "\nStatus: " ).append( pcb.getStatus() )
					.append( "\nOutput Buffer: " );
			for ( int i = 0; i < pcb.getOutputBufferLength(); i++ ) {
				pcbDump.append( "\n" ).append( this.disk.read( pcb.getOutputBufferDiskAddress() + i ) );
			}
			pcbDump.append( "\nTempBuffer: " );
			for ( int i = 0; i < pcb.getTempBufferLength(); i++ ) {
				pcbDump.append( "\n" ).append( this.disk.read( pcb.getTempBufferDiskAddress() + i ) );
			}
			pcbDump.append( "\n" );
			System.out.println( pcbDump );
		}
	}
}
