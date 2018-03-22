package yeezus.driver;

import com.sun.istack.internal.NotNull;
import yeezus.DuplicateIDException;
import yeezus.cpu.CPU;
import yeezus.memory.InvalidWordException;
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
	private final int registerSize, cacheSize, ramSize;
	private Scheduler scheduler;
	private Dispatcher dispatcher;
	private CPU[] cpus;
	private Thread[] threads;
	private Memory disk;
	private long[] idleTimes, executeTimes;

	/**
	 * Constructs a new Driver instance from the given parameters.
	 *
	 * @param numCPUs          The number of CPUs to be used in this system.
	 * @param disk             The disk that stores all of the programs to be run by the system.
	 * @param registerSize     The the amount of registers that are associated with this driver's CPU.
	 * @param cacheSize        The size of the cache to be used by the associated CPU.
	 * @param schedulingPolicy The process scheduling policy that this system will adhere to.
	 * @throws UninitializedDriverException Thrown if a driver instance is created before the loader has been run. This
	 *                                      can be fixed by running {@link Driver#loadFile(Memory, File)} prior to
	 *                                      creating a Driver instance.
	 * @throws DuplicateIDException         Thrown if the given CPU ID already exists with another CPU.
	 */
	public Driver( int numCPUs, @NotNull Memory disk, int registerSize, int cacheSize, int ramSize,
			@NotNull CPUSchedulingPolicy schedulingPolicy )
			throws UninitializedDriverException, DuplicateIDException, InvalidWordException {
		if ( loader == null ) {
			// This makes sure that the loader has already been run. This allows us to easily create multiple Drivers for multi-threading
			throw new UninitializedDriverException(
					"Please use the loadFile static method before creating an instance of this class." );
		}

		if ( numCPUs <= 0 || registerSize <= 0 || cacheSize <= 0 || ramSize <= 0 ) {
			throw new IllegalArgumentException( "Cannot have a zero or negative parameter in the Driver constructor." );
		}

		this.disk = disk;
		this.ramSize = ramSize;
		this.registerSize = registerSize;
		this.cacheSize = cacheSize;

		MMU mmu = new MMU( new Memory( ramSize ) );

		this.cpus = new CPU[numCPUs];

		this.scheduler = new Scheduler( mmu, disk, taskManager, schedulingPolicy );
		this.dispatcher = new Dispatcher( taskManager, this.cpus, mmu );

		// Create threads
		this.threads = new Thread[this.cpus.length];

		// Create CPUs
		for ( int i = 0; i < this.cpus.length; i++ ) {
			CPU cpu = new CPU( i, mmu, registerSize, cacheSize );
			this.cpus[i] = cpu;
			this.threads[i] = new Thread( this.cpus[i] );
			this.threads[i].setUncaughtExceptionHandler( ( t, e ) -> {
				e.printStackTrace();
				cpu.printDump();
			} );
		}
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
	public void run() throws InterruptedException {
		// Start threads
		for ( int i = 0; i < this.cpus.length; i++ ) {
			this.threads[i].start();
		}

		// Wait for all jobs to be completed. Could probably make this more efficient
		boolean jobsDone = false;
		while ( !jobsDone ) { // Loop through Scheduler/Dispatcher
			this.scheduler.run();
			this.dispatcher.run();
			// TODO Handle interrupts

			jobsDone = true;
			for ( PCB pcb : TaskManager.INSTANCE.getPCBs() ) {
				if ( pcb.getStatus() != PCB.Status.TERMINATED ) {
					jobsDone = false;
				}
			}

			boolean allDead = true;
			for ( Thread thread : this.threads ) {
				if ( thread.isAlive() ) {
					allDead = false;
					break;
				}
			}
			if ( allDead ) {
				System.exit( 1 );
			}
		}

		for ( CPU cpu : this.cpus ) {
			cpu.signalShutdown();
			synchronized ( cpu ) {
				cpu.notify();
			}
		}

		// Wait for the threads
		boolean[] joined = new boolean[this.threads.length];
		for ( int i = 0; i < joined.length; i++ ) {
			joined[i] = false;
		}

		boolean allJoined;

		do {
			for ( int i = 0; i < this.cpus.length; i++ ) {
				synchronized ( this.cpus[i] ) {
					this.cpus[i].notify();
				}
				this.threads[i].join( 5 );
				if ( !this.threads[i].isAlive() ) {
					joined[i] = true;
				}
			}

			allJoined = true;

			for ( boolean aJoined : joined ) {
				if ( !aJoined ) {
					allJoined = false;
					break;
				}
			}
		} while ( !allJoined );

		// Ensure that memory is written back to the source
		this.executeTimes = new long[this.cpus.length];
		this.idleTimes = new long[this.cpus.length];
		for ( int i = 0; i < TaskManager.INSTANCE.getPCBs().size(); i++ ) {
			// Wasted iterations, but ensures everything is written back
			this.scheduler.run();
		}

		// Determine idle/execute times
		for ( int i = 0; i < this.cpus.length; i++ ) {
			idleTimes[i] = this.cpus[i].getIdleTime();
			executeTimes[i] = this.cpus[i].getExecuteTime();
		}
	}

	public String getProcPerCPU(){
		String s = "";
		for(int i=0; i<cpus.length; i++){
			s += "\nCPU: " + i +" received " + cpus[i].getNumProcesses();
		}
		return s;
	}
	/**
	 * Prints a dump of the data that contains the current state of the system. This will print to the {@link
	 * System#out} {@link java.io.PrintStream}.
	 */
	public void dumpData() {
		System.out.println(
				"**System Info**\nNumber of CPUs: " + this.cpus.length + "Size of Disk: " + this.disk.getCapacity()
						+ "\nSize of RAM: " + this.ramSize + "\nSize of Cache: " + this.cacheSize
						+ "\nSize of Registers: " + this.registerSize + "\n\n**CPU Info**" );
		for ( CPU cpu : this.cpus ) {
			System.out.println( "CPU " + cpu.getCPUID() );
			cpu.printDump();
			System.out.println( "-----------------------------------\n" );
		}

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
