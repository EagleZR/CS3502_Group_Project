package yeezus.driver;

import yeezus.DuplicateIDException;
import yeezus.cpu.CPU;
import yeezus.cpu.ThreadedCPU;
import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class ThreadedDriver extends AbstractDriver {

	private Thread[] threads;
	private ThreadedCPU[] cpus;
	private Memory[] registers;
	private long[] idleTimes, executeTimes;
	private Dispatcher dispatcher;

	/**
	 * Constructs a new Driver instance from the given parameters.
	 *
	 * @param numCPUs          The number of CPUs to be used in this system.
	 * @param disk             The disk that stores all of the programs to be run by the system.
	 * @param registerSize     The the amount of registers that are associated with this driver's CPU.
	 * @param cacheSize        The size of the cache to be used by the associated CPU.
	 * @param ramSize          The size of the cache to be used by the OS.
	 * @param schedulingPolicy The process scheduling policy that this system will adhere to.
	 * @throws UninitializedDriverException Thrown if a driver instance is created before the loader has been run. This
	 *                                      can be fixed by running {@link ThreadedDriver#loadFile(Memory, File)} prior
	 *                                      to creating a Driver instance.
	 * @throws DuplicateIDException         Thrown if the given CPU ID already exists with another CPU.
	 */
	public ThreadedDriver( int numCPUs, Memory disk, int registerSize, int cacheSize, int ramSize,
			CPUSchedulingPolicy schedulingPolicy )
			throws UninitializedDriverException, DuplicateIDException, InvalidWordException {
		super( disk, registerSize, cacheSize, ramSize, schedulingPolicy );

		if ( numCPUs < 0 ) {
			throw new IllegalArgumentException( "Cannot have a zero or negative parameter in the Driver constructor." );
		}

		this.cpus = new ThreadedCPU[numCPUs];
		this.registers = new Memory[numCPUs];
		this.threads = new Thread[this.cpus.length];

		// Create CPUs
		for ( int i = 0; i < this.cpus.length; i++ ) {
			ThreadedCPU cpu = new ThreadedCPU( i, this.mmu, registerSize, cacheSize );
			this.cpus[i] = cpu;
			this.registers[i] = cpu.getRegisters();
			this.threads[i] = new Thread( this.cpus[i] );
			this.threads[i].setUncaughtExceptionHandler( ( t, e ) -> {
				e.printStackTrace();
				cpu.printDump();
			} );
		}

		this.dispatcher = new Dispatcher( taskManager, this.mmu, this.cpus );
	}

	public long[] getIdleTimes() {
		return this.idleTimes;
	}

	public long[] getExecuteTimes() {
		return this.executeTimes;
	}

	public String getProcPerCPU() {
		String s = "";
		for ( int i = 0; i < this.cpus.length; i++ ) {
			s += "\nCPU: " + i + " received " + this.cpus[i].getNumProcesses();
		}
		return s;
	}

	public Memory[] getRegisters() {
		return this.registers;
	}

	@Override public void run() {
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
			for ( PCB pcb : TaskManager.INSTANCE ) {
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

		for ( ThreadedCPU cpu : this.cpus ) {
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
				try {
					this.threads[i].join( 5 );
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
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
		for ( int i = 0; i < TaskManager.INSTANCE.size(); i++ ) {
			// Wasted iterations, but ensures everything is written back
			this.scheduler.run();
		}

		// Determine idle/execute times
		this.executeTimes = new long[this.cpus.length];
		this.idleTimes = new long[this.cpus.length];
		for ( int i = 0; i < this.cpus.length; i++ ) {
			this.idleTimes[i] = this.cpus[i].getIdleTime();
			this.executeTimes[i] = this.cpus[i].getExecuteTime();
		}
	}

	@Override public void dumpData() {
		System.out.println(
				"**System Info**\nNumber of CPUs: " + this.cpus.length + "Size of Disk: " + this.disk.getCapacity()
						+ "\nSize of RAM: " + this.ramSize + "\nSize of Cache: " + this.cacheSize
						+ "\nSize of Registers: " + this.registerSize + "\n\n**CPU Info**" );
		for ( CPU cpu : this.cpus ) {
			System.out.println( "CPU " + cpu.getCPUID() + ":" );
			cpu.printDump();
			System.out.println( "-----------------------------------\n" );
		}

		System.out.println( "**Process Info**" );
		for ( PCB pcb : TaskManager.INSTANCE ) {
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
