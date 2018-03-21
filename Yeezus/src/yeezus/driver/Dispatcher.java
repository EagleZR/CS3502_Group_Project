package yeezus.driver;

import yeezus.cpu.CPU;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.pcb.PCB;
import yeezus.pcb.TaskManager;

import java.util.NoSuchElementException;

public class Dispatcher implements Runnable {

	TaskManager taskManager;
	CPU cpu;
	MMU mmu;

	public Dispatcher( TaskManager taskManager, CPU cpu, MMU mmu ) {
		this.taskManager = taskManager;
		this.cpu = cpu;
		this.mmu = mmu;
	}

	@Override public void run() {
		if ( cpu.getProcess() == null
				|| PCB.Status.RUNNING != cpu.getProcess().getStatus() && this.taskManager.getReadyQueue().size() > 0 ) {
			PCB next;
			try {
				next = this.taskManager.getReadyQueue().remove();
			} catch ( NoSuchElementException e ) {
				return;
			}
			// System.out.println( "Loading process " + next.getPID() + " to CPU " + cpu.getCPUID() + "." );
			cpu.setProcess( next );
			Memory cache = cpu.getCache();
			for ( int i = 0; i < next.getTotalSize() && i < cache.getCapacity(); i++ ) {
				cache.write( i, mmu.read( next, i ) );
			}
		} else {
			// TODO Sleep until something else is added to the ready queue?
			return;
		}
	}
}
