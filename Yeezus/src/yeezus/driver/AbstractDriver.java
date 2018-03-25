package yeezus.driver;

import com.sun.istack.internal.NotNull;
import yeezus.cpu.CPU;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.TaskManager;

import java.io.File;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public abstract class AbstractDriver {

	static TaskManager taskManager;
	private static Loader loader;
	final int registerSize, cacheSize, ramSize;
	final Memory disk, RAM;
	Scheduler scheduler;
	MMU mmu;

	AbstractDriver( @NotNull Memory disk, int registerSize, int cacheSize, int ramSize,
			@NotNull CPUSchedulingPolicy schedulingPolicy ) {
		if ( registerSize <= 0 || cacheSize <= 0 || ramSize <= 0 ) {
			throw new IllegalArgumentException( "Cannot have a zero or negative parameter in the Driver constructor." );
		}
		if ( loader == null ) {
			// This makes sure that the loader has already been run. This allows us to easily create multiple Drivers for multi-threading
			throw new UninitializedDriverException(
					"Please use the loadFile static method before creating an instance of this class." );
		}

		this.disk = disk;
		this.ramSize = ramSize;
		this.registerSize = registerSize;
		this.cacheSize = cacheSize;

		this.RAM = new Memory( ramSize );

		this.mmu = new MMU( this.RAM );

		this.scheduler = new Scheduler( this.mmu, disk, taskManager, schedulingPolicy );
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

	public abstract void run();

	/**
	 * Prints a dump of the data that contains the current state of the system. This will print to the {@link
	 * System#out} {@link java.io.PrintStream}.
	 */
	public abstract void dumpData();

	public Memory getRAM() {
		return this.RAM;
	}

	public abstract CPU[] getCPUs();

}
