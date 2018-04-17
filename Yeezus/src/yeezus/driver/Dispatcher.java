package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.memory.Cache;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

public class Dispatcher implements Runnable {

	private TaskManager taskManager;
	private CPU[] cpus;

	Dispatcher( TaskManager taskManager, CPU[] cpus ) {
		this.taskManager = taskManager;
		this.cpus = cpus;
	}

	@Override public void run() {
		for ( CPU cpu : this.cpus ) {
			PCB oldProcess = cpu.getProcess();
			if ( oldProcess == null || PCB.Status.RUNNING != oldProcess.getStatus() ) {
				// Save old process data
				if ( oldProcess != null && PCB.Status.TERMINATED != oldProcess.getStatus() ) {
					oldProcess.setPC( cpu.getPC() );
					oldProcess.setRegisters( cpu.getRegisters() );
					oldProcess.setCache( cpu.getCache() );
				}

				if ( !this.taskManager.getReadyQueue().isEmpty() ) {
					PCB next = this.taskManager.getReadyQueue().remove(); // They're already in order
					cpu.setProcess( next );

					cpu.setPC( next.getPC() ); // If it's uninitialized, it should be 0 anyways, so no need to check

					// Write the starting instructions to the cache.
					Cache cache = cpu.getCache();
					for ( int i = 0; i < cache.getWritablePagesCount(); i++ ) {
						try {
							// Write the pages starting with the page that contains the instruction associated with the PC to the cache (loop to page 0 if it runs out of instruction pages)
							cache.loadPage( next,
									( i + Memory.getPageNumber( next.getPC(), MMU.FRAME_SIZE ) ) % ( Memory
											.getPageNumber( next.getInstructionsLength(), MMU.FRAME_SIZE ) ) );
						} catch ( MMU.PageFault pageFault ) {
							// If they're not loaded, it's fine. The Cache can do demand paging if it needs... I hope :/
							// TODO Test the above statement
						}
					}

					// Write the saved registers back to the CPU registers
					if ( next.getRegisters() != null ) {
						for ( int i = 0; i < cpu.getRegisters().getCapacity(); i++ ) {
							cpu.getRegisters().write( i, next.getRegisters().read( i ) );
						}
					}

					// Write the temp cache data from the PCB to the CPU cache
					if ( next.getCache() != null ) {
						for ( int i = 0; i < next.getTempBufferLength(); i++ ) {
							cpu.getCache()
									.write( next, next.getTempBufferDiskAddress() - next.getStartDiskAddress() + i,
											next.getCache().read( i ) );
						}
					}

					// Wake up the CPU
					synchronized ( cpu ) {
						cpu.notify();
					}
				}
			}
		}
	}
}
