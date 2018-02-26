package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.cpu.DMAChannel;
import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.Memory;
import yeezus.pcb.DuplicatePIDException;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class represents the CPU Driver within the {@link yeezus} operating system. Multiple instances of this class
 * indicate the presence of multiple logical CPUs.
 */
public class Driver implements Runnable {

	private static Loader loader; // TODO Create as static variable? Throw exception if the Driver constructor is called while this is null?
	private static TaskManager taskManager;
	private ConcurrentLinkedQueue<PCB> readyQueue;
	private ConcurrentLinkedQueue<PCB> dmaQueue;
	private Scheduler scheduler;
	private Dispatcher dispatcher;
	private CPU cpu;

	public Driver( Memory disk, Memory RAM, Memory registers, CPUSchedulingPolicy schedulingMethod )
			throws UninitializedDriverException {
		if ( loader == null ) {
			// This makes sure that the loader has already been run. This allows us to easily create multiple Drivers for multi-threading
			throw new UninitializedDriverException(
					"Please use the loadFile static method before creating an instance of this class." );
		}

		this.cpu = new CPU( taskManager, registers );

		this.scheduler = new Scheduler( taskManager, this.dmaQueue, schedulingMethod );
		this.dispatcher = new Dispatcher( this.readyQueue, this.cpu );

		this.dmaQueue = new ConcurrentLinkedQueue<>();
		DMAChannel dmaChannel = new DMAChannel( disk, RAM, this.dmaQueue );
	}

	/**
	 * This loads a file onto the disk in preparation for the creation of Dispatcher instances which will load and
	 * execute the programs contained in the file.
	 *
	 * @param disk        The disk onto which the contents of the programFile will be loaded.
	 * @param programFile The file whose contents will be loaded onto the disk.
	 */
	public static void loadFile( Memory disk, File programFile ) throws InvalidAddressException, DuplicatePIDException, InvalidWordException, IOException {
		taskManager = new TaskManager();
		loader = new Loader( taskManager, programFile, disk );
	}

	@Override public void run() {
		while ( true ) { // TODO Exit on interrupt, or when everything is finished
			scheduler.run();
			dispatcher.run();
			cpu.run();
			// TODO Handle interrupts
		}
	}
}
