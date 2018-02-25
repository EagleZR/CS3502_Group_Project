package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.ProcessList;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Driver implements Runnable {

	private ConcurrentLinkedQueue<PCB> readyQueue;
	private ConcurrentLinkedQueue<PCB> dmaQueue;

	private /*static*/ ProcessList processList;
	private /*static*/ Loader loader; // TODO Create as static variable? Throw exception if the Driver constructor is called while this is null?
	private Scheduler scheduler;
	private Dispatcher dispatcher;
	private CPU cpu;

	public Driver( Memory disk, Memory RAM, Memory registers, File programFile ) {
		this.processList = new ProcessList();
		this.loader = new Loader( processList, programFile, disk );
		// this.loader.run(); // TODO Trigger this somewhere else? Have it execute automatically when created?
		this.scheduler = new Scheduler( processList, disk, RAM );
		this.dispatcher = new Dispatcher( processList, RAM, registers );
		this.cpu = new CPU( processList, registers );
	}

	@Override public void run() {
		while ( true ) { // TODO Exit on interrupt, or when everything is finished
			scheduler.run();
			dispatcher.run();
			cpu.run();
			// TODO Handle interrupts
		}
	}

	/*
	public static void loadFile( Memory disk, File programFile ) {
		processList = new ProcessList();
		loader = new Loader( processList, programFile, disk );
		loader.run();
	}*/
}
