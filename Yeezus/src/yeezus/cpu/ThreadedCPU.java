package yeezus.cpu;

import yeezus.DuplicateIDException;
import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.MMU;
import yeezus.pcb.PCB;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class ThreadedCPU extends ContinuousCPU {

	private boolean shutdown = false;
	private long idleTime = 0;
	private long executeTime = 0;

	/**
	 * Constructs a new CPU from the given parameters.
	 *
	 * @param cpuid        The ID of the new CPU. <b>NOTE: This must be a unique value.</b>
	 * @param mmu          The MMU that manages this system's RAM.
	 * @param registerSize The amount of registers to be used by this CPU.
	 * @param cacheSize    The size of the cache to be used by this CPU.
	 * @throws DuplicateIDException Thrown if the given CPU ID is not unique.
	 */
	public ThreadedCPU( int cpuid, MMU mmu, int registerSize, int cacheSize )
			throws DuplicateIDException, InvalidWordException {
		super( cpuid, mmu, registerSize, cacheSize );
	}

	/**
	 * Executes any process that is loaded into this CPU. <p><b>NOTE:</b> If there is no currently-set process, this
	 * method will cause its parent thread to sleep. To wake it up, use {@link CPU#setProcess(PCB)} to set a new
	 * process, followed by {@link Object#notify()} to begin its execution again.</p>
	 *
	 * @throws InvalidInstructionException Thrown if the fetched Instruction could not be successfully decoded.
	 * @throws InvalidWordException        Thrown if there was an issue with storing new data in the execution of the
	 *                                     instruction.
	 * @throws ExecutionException          Thrown if the CPU attempts to execute an I/O instruction (the DMA-Channel
	 *                                     should handle that).
	 * @throws InvalidAddressException     Thrown if an instruction tries to access an invalid address in memory.
	 */
	@Override public void run() {
		while ( !isShutdown() ) {
			long startExecuteTime = System.nanoTime();

			super.run();

			long startSleepTime = System.nanoTime();
			this.executeTime += startSleepTime - startExecuteTime;
			synchronized ( this ) {
				try {
					this.wait();
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}
			startExecuteTime = System.nanoTime();
			this.idleTime += startExecuteTime - startSleepTime;
		}
	}

	/**
	 * Checks if the shutdown signal has been sent to this CPU.
	 *
	 * @return {@code true} if this CPU has been signaled to shut down.
	 */
	private synchronized boolean isShutdown() {
		return this.shutdown;
	}

	/**
	 * Signals this CPU to shut down.
	 */
	public synchronized void signalShutdown() {
		this.shutdown = true;
	}

	/**
	 * The amount of time this CPU has been executing processes.
	 *
	 * @return The amount of time in nanoseconds that this CPU has been busy.
	 */
	public long getExecuteTime() {
		return this.executeTime;
	}

	/**
	 * Retrieves the elapsed idle time for this CPU.
	 *
	 * @return The amount of time in nanoseconds that this CPU has been idling, without a process.
	 */
	public long getIdleTime() {
		return this.idleTime;
	}
}
