package yeezus.driver;

import com.sun.istack.internal.NotNull;
import yeezus.DuplicateIDException;
import yeezus.cpu.CPU;
import yeezus.cpu.ContinuousCPU;
import yeezus.memory.InvalidWordException;
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
public class Driver extends AbstractDriver {

	private CPU cpu;
	private Memory registers;
	private Dispatcher dispatcher;

	/**
	 * Constructs a new Driver instance from the given parameters.
	 *
	 * @param disk             The disk that stores all of the programs to be run by the system.
	 * @param registerSize     The the amount of registers that are associated with this driver's CPU.
	 * @param cacheSize        The size of the cache to be used by the associated CPU.
	 * @param ramSize          The size of the cache to be used by the OS.
	 * @param schedulingPolicy The process scheduling policy that this system will adhere to.
	 * @throws UninitializedDriverException Thrown if a driver instance is created before the loader has been run. This
	 *                                      can be fixed by running {@link Driver#loadFile(Memory, File)} prior to
	 *                                      creating a Driver instance.
	 * @throws DuplicateIDException         Thrown if the given CPU ID already exists with another CPU.
	 */
	public Driver( @NotNull Memory disk, int registerSize, int cacheSize, int ramSize,
			@NotNull CPUSchedulingPolicy schedulingPolicy )
			throws UninitializedDriverException, DuplicateIDException, InvalidWordException {
		super( disk, registerSize, cacheSize, ramSize, schedulingPolicy );

		this.cpu = new ContinuousCPU( 0, this.mmu, registerSize, cacheSize );
		this.registers = this.cpu.getRegisters();

		this.dispatcher = new Dispatcher( TaskManager.INSTANCE, this.mmu, this.cpu );
	}

	/**
	 * Executes the main loop of the driver. This loop will run until all processes have been completed, and the process
	 * data has been written back to the disk.
	 *
	 * @throws InterruptedException See {@link InterruptedException}.
	 */
	@Override public void run() {
		// Wait for all jobs to be completed. Could probably make this more efficient
		boolean jobsDone = false;
		while ( !jobsDone ) { // Loop through Scheduler/Dispatcher
			this.scheduler.run();
			this.dispatcher.run();
			this.cpu.run();
			// TODO Handle interrupts

			jobsDone = true;
			for ( PCB pcb : TaskManager.INSTANCE ) {
				if ( pcb.getStatus() != PCB.Status.TERMINATED ) {
					jobsDone = false;
				}
			}
		}

		// Ensure that memory is written back to the source
		for ( int i = 0; i < TaskManager.INSTANCE.size(); i++ ) {
			// Wasted iterations, but ensures everything is written back
			this.scheduler.run();
		}
	}

	@Override public void dumpData() {
		System.out.println(
				"**System Info**\nNumber of CPUs: 1\nSize of Disk: " + this.disk.getCapacity() + "\nSize of RAM: "
						+ this.ramSize + "\nSize of Cache: " + this.cacheSize + "\nSize of Registers: "
						+ this.registerSize + "\n\n**CPU Info**" );
		System.out.println( "CPU " + this.cpu.getCPUID() + ":" );
		this.cpu.printDump();
		System.out.println( "-----------------------------------\n" );

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

	@Override public CPU[] getCPUs() {
		CPU[] cpu = new CPU[1];
		cpu[0] = this.cpu;
		return cpu;
	}

	public String getProcPerCPU() {
		return "\nCPU: 0 received " + this.cpu.getNumProcesses();
	}

	public Memory getRegisters() {
		return this.registers;
	}
}
