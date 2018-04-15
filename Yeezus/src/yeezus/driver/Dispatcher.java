package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.memory.Cache;
import yeezus.memory.MMU;
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
			if ( ( cpu.getProcess() == null || PCB.Status.RUNNING != cpu.getProcess().getStatus() )
					&& this.taskManager.getReadyQueue().size() > 0 ) {
				if ( cpu.getProcess() != null && PCB.Status.TERMINATED != cpu.getProcess().getStatus() ) {
					// Save old process data
					PCB oldProcess = cpu.getProcess();
					oldProcess.setPC( cpu.getPC() );
					oldProcess.setRegisters( cpu.getRegisters() );
					oldProcess.setCache( cpu.getCache() );
				}

				PCB next = this.taskManager.getReadyQueue().remove(); // They're already in order
				cpu.setProcess( next );

				cpu.setPC( next.getPC() ); // If it's uninitialized, it should be 0 anyways, so no need to check

				// Write the starting instructions to the cache. TODO Start with the saved PC in the PCB.
				Cache cache = cpu.getCache();
				for ( int i = 0; i < cache.getWritablePagesCount(); i++ ) {
					try {
						cache.loadPage( next, i );
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
						cpu.getCache().write( next, next.getTempBufferDiskAddress() - next.getStartDiskAddress() + i,
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
