package yeezus.cpu;

import yeezus.DuplicateIDException;
import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;
import yeezus.memory.MMU;
import yeezus.pcb.PCB;

/**
 * This creates a process-completing CPU for the {@link yeezus} Operating System. This will run until the process is
 * completed, unlike the {@link CPU} it inherits from, which only completes a single instruction each time its {@link
 * CPU#run()} is called.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class ContinuousCPU extends CPU {

	/**
	 * Constructs a new CPU from the given parameters.
	 *
	 * @param cpuid        The ID of the new CPU. <b>NOTE: This must be a unique value.</b>
	 * @param mmu          The MMU that manages this system's RAM.
	 * @param registerSize The amount of registers to be used by this CPU.
	 * @param cacheSize    The size of the cache to be used by this CPU.
	 * @throws DuplicateIDException Thrown if the given CPU ID is not unique.
	 */
	public ContinuousCPU( int cpuid, MMU mmu, int registerSize, int cacheSize )
			throws DuplicateIDException, InvalidWordException {
		super( cpuid, mmu, registerSize, cacheSize );
	}

	/**
	 * Executes any process that is loaded into this CPU. This method will run until the process has completed, at which
	 * point it will return to the calling method.
	 *
	 * @throws InvalidInstructionException Thrown if the fetched Instruction could not be successfully decoded.
	 * @throws InvalidWordException        Thrown if there was an issue with storing new data in the execution of the
	 *                                     instruction.
	 * @throws ExecutionException          Thrown if the CPU attempts to execute an I/O instruction (the DMA-Channel
	 *                                     should handle that).
	 * @throws InvalidAddressException     Thrown if an instruction tries to access an invalid address in memory.
	 */
	@Override public void run() {
		while ( getProcess() != null && getProcess().getStatus() == PCB.Status.RUNNING ) {
			super.run();
		}
	}
}
