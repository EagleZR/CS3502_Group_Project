package yeezus.cpu;

import org.jetbrains.annotations.NotNull;
import yeezus.DuplicateIDException;
import yeezus.memory.*;
import yeezus.pcb.PCB;

import java.util.ArrayList;

/**
 * This class emulates some of the CPU's actions in the {@link yeezus} Operating System. Once it has been assigned a
 * {@link PCB}, it runs until the associated process has been terminated. The CPU fetches instructions from the {@link
 * MMU}, decodes them, and executes them. Changes to the process data are reflected in the RAM.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class CPU implements Runnable {

	private static final ArrayList<Integer> cpuids = new ArrayList<>();
	private final int cpuid;
	private final Memory registers, cache;
	private DMAChannel dmaChannel;
	private PCB pcb;
	private int pc;
	private ExecutableInstruction previousInstruction;
	private ArrayList<String> log;
	private boolean shutdown = false;

	/**
	 * Constructs a new CPU from the given parameters.
	 *
	 * @param cpuid        The ID of the new CPU. <b>NOTE: This must be a unique value.</b>
	 * @param mmu          The MMU that manages this system's RAM.
	 * @param registerSize The amount of registers to be used by this CPU.
	 * @param cacheSize    The size of the cache to be used by this CPU.
	 * @throws DuplicateIDException Thrown if the given CPU ID is not unique.
	 */
	public CPU( int cpuid, @NotNull MMU mmu, int registerSize, int cacheSize )
			throws DuplicateIDException, InvalidWordException {
		if ( cpuids.contains( cpuid ) ) {
			throw new DuplicateIDException( "The CPU ID " + cpuid + " already exists in this system." );
		}
		this.cpuid = cpuid;
		cpuids.add( cpuid );

		this.registers = new Memory( registerSize );
		this.cache = new Memory( cacheSize );
		this.dmaChannel = new DMAChannel( mmu, this.registers );
		this.log = new ArrayList<>();
	}

	/**
	 * Used to reset the state of the CPU in testing.
	 */
	public static void reset() {
		cpuids.clear();
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
	 * Retrieves the Program Counter for the Process being executed by this CPU.
	 *
	 * @return The Program Counter of the Process in this CPU.
	 */
	protected synchronized int getPC() {
		return this.pc;
	}

	/**
	 * Sets the new Program Counter for the Process being executed by this CPU.
	 *
	 * @param pc <p>The new Program Counter for the Process in this CPU.</p><p>A negative value, or any value larger
	 *           than the number of instructions for this process will be ignored.</p>
	 */
	protected synchronized void setPC( int pc ) {
		if ( pc >= 0 && pc < this.pcb.getInstructionsLength() ) {
			this.pc = pc;
		}
	}

	/**
	 * Retrieves the CPU ID for this CPU instance.
	 *
	 * @return The CPU ID associated with this CPU.
	 */
	public int getCPUID() {
		return this.cpuid;
	}

	/**
	 * Retrieves the {@link PCB} of the process currently associated with this CPU.
	 *
	 * @return The {@link PCB} of the process associated with this CPU.
	 */
	public synchronized PCB getProcess() {
		return this.pcb;
	}

	/**
	 * <p>Sets a new process for this CPU.</p><p><b>NOTE:</b> It is imperative that {@link Object#notify()} be called on
	 * this CPU instance after using this method, or the thread it runs on will continue to sleep.</p>
	 *
	 * @param pcb The {@link PCB} of the new process to be run by this CPU.
	 */
	public synchronized void setProcess( @NotNull PCB pcb ) {
		if ( this.pcb != null ) {
			this.pcb.setCPUID( -1 );
		}
		this.pcb = pcb;
		this.pcb.setCPUID( this.cpuid );
		this.pcb.setStatus( PCB.Status.RUNNING );
		setPC( 0 );
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
			while ( getProcess() != null && getProcess().getStatus() != PCB.Status.TERMINATED ) {
				// Check if this process has had a pc error
				if ( getPC() >= getProcess().getInstructionsLength() ) {
					System.err.println( generateSimpleDump() );
					printDump();
					getProcess().setStatus( PCB.Status.TERMINATED );
				} else {
					// Fetch
					Word instruction = this.cache.read( getPC() );
					setPC( getPC() + 1 );

					// Decode
					ExecutableInstruction executableInstruction = decode( instruction );

					// Execute
					getProcess().incExecutionCount();

					if ( executableInstruction.type == InstructionSet.HLT ) {
						getProcess().setStatus(
								PCB.Status.TERMINATED ); // Make sure this is the last call to getProcess() this loop
						this.previousInstruction = null;
						this.log.clear();
					} else {
						if ( executableInstruction.getClass() == ExecutableInstruction.IOExecutableInstruction.class ) {
							this.dmaChannel
									.handle( (ExecutableInstruction.IOExecutableInstruction) executableInstruction,
											getProcess() );
						} else {
							executableInstruction.run();
						}
						this.previousInstruction = executableInstruction;
						this.log.add( generateSimpleDump() );
					}
				}
			}
			synchronized ( this ) {
				try {
					this.wait();
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * For testing use only. For regular execution, use {@link CPU#run()}.
	 */
	public void debugRun() {
		if ( getProcess() == null ) {
			// Do nothing
			return;
		}
		// Fetch
		Word instruction = this.cache.read( getPC() );
		setPC( getPC() + 1 );

		// Decode
		ExecutableInstruction executableInstruction = decode( instruction );

		// Execute
		if ( executableInstruction.type == InstructionSet.HLT ) {
			getProcess().setStatus( PCB.Status.TERMINATED );
			return;
		}

		if ( executableInstruction.getClass() == ExecutableInstruction.IOExecutableInstruction.class ) {
			this.dmaChannel.handle( (ExecutableInstruction.IOExecutableInstruction) executableInstruction, getProcess() );
		} else {
			executableInstruction.run();
		}
	}

	/**
	 * Retrieves the registers used by this CPU.
	 *
	 * @return The registers used by this CPU.
	 */
	public Memory getRegisters() {
		return this.registers;
	}

	/**
	 * Retrieves the cache used by this CPU.
	 *
	 * @return The cache used by this CPU.
	 */
	public Memory getCache() {
		return this.cache;
	}

	/**
	 * Decodes the given {@link Word} into an {@link ExecutableInstruction} that the CPU can then execute.
	 *
	 * @param word The data to be decoded.
	 * @return An {@link ExecutableInstruction} containing the details of the decoded data in a form that's ready to be
	 * executed by the CPU.
	 * @throws InvalidInstructionException Thrown if the given {@link Word} cannot be successfully decoded into a known
	 *                                     instruction type.
	 */
	ExecutableInstruction decode( @NotNull Word word ) throws InvalidInstructionException {
		long signature = word.getData() & 0xC0000000;
		if ( signature == 0x00000000 ) {
			return new ExecutableInstruction.ArithmeticExecutableInstruction( word, this.registers );
		} else if ( signature == 0x40000000 ) {
			return new ExecutableInstruction.ConditionalExecutableInstruction( word, this.registers, this.cache, this );
		} else if ( signature == 0x80000000 ) {
			return new ExecutableInstruction.UnconditionalJumpExecutableInstruction( word, this.registers, this );
		} else {
			return new ExecutableInstruction.IOExecutableInstruction( word, this.registers );
		}
	}

	/**
	 * Generates a simple dump {@link String} containing the current process's information.
	 *
	 * @return A {@link String} that displays the current state of this process's execution.
	 */
	public String generateSimpleDump() {
		StringBuilder dumpReport = new StringBuilder(
				"CPU: " + this.cpuid + "\nPC: " + getPC() + "\nPID: " + getProcess().getPID() + "\nInstruction Count: "
						+ getProcess().getExecutionCount() + "\nPrevious Instruction: " + this.previousInstruction
						+ "\nRegisters(" + this.registers.getCapacity() + "): " );
		for ( int i = 0; i < this.registers.getCapacity(); i++ ) {
			dumpReport.append( "\n\t" ).append( this.registers.read( i ) );
		}
		return dumpReport.toString();
	}

	/**
	 * Prints the dump log generated by this CPU for this process. This will print to the {@link System#out} {@link
	 * java.io.PrintStream}.
	 */
	public void printDump() {
		while ( !this.log.isEmpty() ) {
			System.out.println( this.log.remove( this.log.size() - 1 ) );
		}
	}
}
