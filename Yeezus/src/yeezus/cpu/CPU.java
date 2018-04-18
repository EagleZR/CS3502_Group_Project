package yeezus.cpu;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
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
	private final Memory registers;
	private final Cache cache;
	private DMAChannel dmaChannel;
	private PCB pcb;
	private int pc;
	private ExecutableInstruction previousInstruction;
	private boolean shutdown = false;
	private long idleTime = 0;
	private long executeTime = 0;
	private int numProcesses = 0;
	private volatile boolean isAsleep = false;

	/**
	 * Constructs a new CPU from the given parameters.
	 *
	 * @param cpuid        The ID of the new CPU. <b>NOTE: This must be a unique value.</b>
	 * @param dmaChannel   The DMA Channel through which this CPU will input/output.
	 * @param registerSize The amount of registers to be used by this CPU.
	 * @param cacheSize    The size of the cache to be used by this CPU.
	 * @throws DuplicateIDException Thrown if the given CPU ID is not unique.
	 */
	public CPU( int cpuid, @NotNull DMAChannel dmaChannel, @NotNull MMU mmu, int registerSize, int cacheSize )
			throws DuplicateIDException, InvalidWordException {
		if ( cpuids.contains( cpuid ) ) {
			throw new DuplicateIDException( "The CPU ID " + cpuid + " already exists in this system." );
		}
		this.cpuid = cpuid;
		cpuids.add( cpuid );

		this.dmaChannel = dmaChannel;
		this.registers = new Memory( registerSize );
		this.cache = new Cache( cacheSize, mmu );
	}

	/**
	 * Used to reset the state of the CPU in testing.
	 */
	public static void reset() {
		cpuids.clear();
	}

	public boolean isAsleep() {
		return this.isAsleep;
	}

	public void setAsleep( boolean asleep ) {
		this.isAsleep = asleep;
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
	public synchronized int getPC() {
		return this.pc;
	}

	/**
	 * Sets the new Program Counter for the Process being executed by this CPU.
	 *
	 * @param pc <p>The new Program Counter for the Process in this CPU.</p><p>A negative value, or any value larger
	 *           than the number of instructions for this process will be ignored.</p>
	 */
	public synchronized void setPC( int pc ) {
		if ( pc >= 0 && pc < this.pcb.getInstructionsLength() ) {
			this.pc = pc;
		}
	}

	public int getNumProcesses() {
		return this.numProcesses;
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
	 * <p>Sets a new process for this CPU.</p><p><b>NOTE:</b> It is imperative that {@link Object#notify()} be called
	 * on this CPU instance after using this method, or the thread it runs on will continue to sleep.</p>
	 *
	 * @param pcb The {@link PCB} of the new process to be run by this CPU.
	 */
	public synchronized void setProcess( @Nullable PCB pcb ) {
		this.pcb = pcb;
		if ( pcb != null ) {
			this.pcb.setCPUID( this.cpuid );
			this.pcb.setStatus( PCB.Status.RUNNING );
			setPC( 0 );
			this.numProcesses++;
		}
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
		long startExecuteTime = System.nanoTime();
		while ( !isShutdown() ) {
			PCB process = getProcess();
			if ( process != null && process.getStatus() == PCB.Status.RUNNING ) {
				// Check if this process has had a pc error
				if ( getPC() >= process.getInstructionsLength() ) {
					System.err.println( "The PC (" + getPC() + ") is beyond the instruction count (" + process
							.getInstructionsLength() + ")." );
					System.err.println( generateSimpleDump() );
					printDump();
					process.setStatus( PCB.Status.TERMINATED );
				} else {
					synchronized ( this ) {
						synchronized ( this.cache ) {
							synchronized ( this.registers ) {
								try {
									// Fetch
									Word instruction = this.cache.read( process, getPC() );
									setPC( getPC() + 1 );

									// Decode
									ExecutableInstruction executableInstruction = decode( instruction );
									// Want this here in case there's an error in the execution of the process
									this.previousInstruction = executableInstruction;
									this.pcb.getLog().add( generateSimpleDump() );

									// Execute
									if ( executableInstruction.type == InstructionSet.HLT ) {
										process.incExecutionCount();
										process.setStatus(
												PCB.Status.TERMINATED ); // Make sure this is the last call to getProcess() this loop
										this.previousInstruction = null;
										process.getLog().add( "***Program Complete***" );
									} else {
										if ( executableInstruction.getClass()
												== ExecutableInstruction.IOExecutableInstruction.class ) {
											// First try to retrieve input from the DMA Channel if there is any, and if not, schedule the input to be retrieved
											if ( !this.dmaChannel.retrieveInput(
													(ExecutableInstruction.IOExecutableInstruction) executableInstruction,
													process, this.registers ) ) {
												// Schedule the I/O with the DMA Channel and set the PCB status to WAITING
												synchronized ( this.dmaChannel ) {
													process.setStatus(
															PCB.Status.WAITING ); // Set to waiting so the Dispatcher will swap it
													setPC( getPC() - 1 );
													process.getLog().add( "***I/O Block***" );
													this.dmaChannel.handle(
															(ExecutableInstruction.IOExecutableInstruction) executableInstruction,
															process, this.registers );
												}
											} else {
												process.incExecutionCount();
											}
										} else {
											try {
												executableInstruction.run();
											} catch ( Exception e ) {
												synchronized ( System.out ) {
													System.err.println(
															"There was an exception while running an instruction." );
													e.printStackTrace();
													printDump();
												}
											}
											process.incExecutionCount();
										}
									}
								} catch ( MMU.PageFault pageFault ) {
									process.getLog().add( "***Page Fault***" );
									setPC( getPC() - 1 );
								}
							}
						}
					}
				}
			} else {
				long startSleepTime = System.nanoTime();
				this.executeTime += startSleepTime - startExecuteTime;
				synchronized ( this ) {
					try {
						setAsleep( true );
						this.wait();
						setAsleep( false );
					} catch ( InterruptedException e ) {
						e.printStackTrace();
					}
				}
				startExecuteTime = System.nanoTime();
				this.idleTime += startExecuteTime - startSleepTime;
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
		try {
			// Fetch
			Word instruction = this.cache.read( getProcess(), getPC() );
			setPC( getPC() + 1 );

			// Decode
			ExecutableInstruction executableInstruction = decode( instruction );

			// Execute
			if ( executableInstruction.type == InstructionSet.HLT ) {
				getProcess().setStatus( PCB.Status.TERMINATED );
				return;
			}

			if ( executableInstruction.getClass() == ExecutableInstruction.IOExecutableInstruction.class ) {
				if ( !this.dmaChannel
						.retrieveInput( (ExecutableInstruction.IOExecutableInstruction) executableInstruction,
								getProcess(), this.registers ) ) {
					this.dmaChannel.handle( (ExecutableInstruction.IOExecutableInstruction) executableInstruction,
							getProcess(), this.registers );
					getProcess().setStatus( PCB.Status.WAITING );
				}
			} else {
				executableInstruction.run();
			}
		} catch ( MMU.PageFault pageFault ) {
			getProcess().setStatus( PCB.Status.WAITING );
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
	public Cache getCache() {
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
			return new ExecutableInstruction.ConditionalExecutableInstruction( word, this.registers, this.cache, this,
					getProcess() );
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
	private String generateSimpleDump() {
		StringBuilder dumpReport = new StringBuilder(
				"CPU: " + this.cpuid + "\nPC: " + getPC() + "\nPID: " + getProcess().getPID() + "\nInstruction Count: "
						+ getProcess().getExecutionCount() + "\nInstruction: " + this.previousInstruction
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
		synchronized ( System.out ) {
			while ( !this.pcb.getLog().isEmpty() ) {
				System.out.println( this.pcb.getLog().remove( this.pcb.getLog().size() - 1 ) );
			}
		}
	}
}
