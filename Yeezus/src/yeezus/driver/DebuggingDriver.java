package yeezus.driver;

import yeezus.DuplicateIDException;
import yeezus.cpu.CPU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class DebuggingDriver extends AbstractDriver {

	private CPU cpu;
	private Memory registers;
	private Dispatcher dispatcher;
	private boolean jobsDone = false;

	/**
	 * Constructs a new Driver instance from the given parameters.
	 *
	 * @param disk             The disk that stores all of the programs to be run by the system.
	 * @param registerSize     The the amount of registers that are associated with this driver's CPU.
	 * @param cacheSize        The size of the cache to be used by the associated CPU.
	 * @param ramSize          The size of the cache to be used by the OS.
	 * @param schedulingPolicy The process scheduling policy that this system will adhere to.
	 * @throws UninitializedDriverException Thrown if a driver instance is created before the loader has been run. This
	 *                                      can be fixed by running {@link DebuggingDriver#loadFile(Memory, File)} prior
	 *                                      to creating a Driver instance.
	 * @throws DuplicateIDException         Thrown if the given CPU ID already exists with another CPU.
	 */
	public DebuggingDriver( Memory disk, int registerSize, int cacheSize, int ramSize,
			CPUSchedulingPolicy schedulingPolicy ) {
		super( disk, registerSize, cacheSize, ramSize, schedulingPolicy );

		this.cpu = new CPU( 0, this.mmu, registerSize, cacheSize );
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
		// Check if all jobs have been completed. Could probably make this more efficient...
		for ( PCB pcb : TaskManager.INSTANCE ) {
			if ( pcb.getStatus() != PCB.Status.TERMINATED ) {
				this.jobsDone = false;
			}
		}

		if ( !this.jobsDone ) { // Loop through Scheduler/Dispatcher
			this.scheduler.run();
			this.dispatcher.run();
			this.cpu.run();
			// TODO Handle interrupts
		} else {
			// Ensure that memory is written back to the source
			for ( int i = 0; i < TaskManager.INSTANCE.size(); i++ ) {
				// Wasted iterations, but ensures everything is written back
				this.scheduler.run();
			}
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

	public String getProcPerCPU() {
		return "\nCPU: 0 received " + this.cpu.getNumProcesses();
	}

	public Memory getRegisters() {
		return this.registers;
	}

	public boolean isFinished() {
		return this.jobsDone;
	}
}
