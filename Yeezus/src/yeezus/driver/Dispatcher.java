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
				PCB next = this.taskManager.getReadyQueue().remove(); // They're already in order
				cpu.setProcess( next );
				// Write the starting instructions to the cache
				Cache cache = cpu.getCache();
				for ( int i = 0; i < cache.getWritablePagesCount(); i++ ) {
					try {
						cache.loadPage( next, i );
					} catch ( MMU.PageFault pageFault ) {
						// If they're not loaded, it's fine. The Cache can do demand paging if it needs... I hope :/
						// TODO Test the above statement
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
