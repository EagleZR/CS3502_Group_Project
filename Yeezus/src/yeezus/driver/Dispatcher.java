package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

public class Dispatcher implements Runnable {

	private TaskManager taskManager;
	private CPU[] cpus;
	private MMU mmu;

	Dispatcher( TaskManager taskManager, MMU mmu, CPU... cpus ) {
		this.taskManager = taskManager;
		this.cpus = cpus;
		this.mmu = mmu;
	}

	@Override public void run() {
		for ( CPU cpu : this.cpus ) {
			if ( ( cpu.getProcess() == null || PCB.Status.RUNNING != cpu.getProcess().getStatus() )
					&& this.taskManager.getReadyQueue().size() > 0 ) {
				PCB next;
				next = this.taskManager.getReadyQueue().remove();
				cpu.setProcess( next );
				Memory cache = cpu.getCache();
				for ( int i = 0; i < next.getTotalSize() && i < cache.getCapacity(); i++ ) {
					cache.write( i, this.mmu.read( next, i ) );
				}
				synchronized ( cpu ) {
					cpu.notify();
				}
			}
		}
	}
}
