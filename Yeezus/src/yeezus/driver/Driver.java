package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;

import java.io.File;

public class Driver implements Runnable {

	private /*static*/ PCB pcb;
	private /*static*/ Loader loader; // TODO Create as static variable? Throw exception if the Driver constructor is called while this is null?
	private Scheduler scheduler;
	private Dispatcher dispatcher;
	private CPU cpu;

	public Driver( Memory disk, Memory RAM, Memory registers, File programFile ) {
		this.pcb = new PCB();
		this.loader = new Loader( pcb, programFile, disk );
		// this.loader.run(); // TODO Trigger this somewhere else? Have it execute automatically when created?
		this.scheduler = new Scheduler( pcb, disk, RAM );
		this.dispatcher = new Dispatcher( pcb, RAM, registers );
		this.cpu = new CPU( pcb, registers );
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
		pcb = new PCB();
		loader = new Loader( pcb, programFile, disk );
		loader.run();
	}*/
}
