package yeezus.cpu;

import yeezus.DuplicateIDException;
import yeezus.memory.*;
import yeezus.pcb.PCB;

import java.util.ArrayList;

public class CPU {

	private static final ArrayList<Integer> cpuids = new ArrayList<>();
	private final int cpuid;
	private MMU mmu;
	private Memory registers;
	private DMAChannel dmaChannel;
	private PCB pcb;
	private int pc;

	public CPU( int cpuid, MMU mmu, Memory registers ) throws DuplicateIDException {
		if ( cpuids.contains( cpuid ) ) {
			throw new DuplicateIDException( "The CPU ID " + cpuid + " already exists in this system." );
		}
		this.cpuid = cpuid;
		cpuids.add( cpuid );

		this.mmu = mmu;
		this.registers = registers;
		this.dmaChannel = new DMAChannel( mmu, registers );
	}

	public static void reset() {
		cpuids.clear();
	}

	public int getCPUID() {
		return cpuid;
	}

	public void setProcess( PCB pcb ) {
		this.pcb = pcb;
		this.pc = 0;
	}

	public void run()
			throws InvalidInstructionException, InvalidWordException, ExecutionException, InvalidAddressException {
		if ( this.pcb == null ) {
			// TODO Throw an invalid PID exception or something
		}
		while ( true ) {
			// TODO Fetch
			Word instruction = this.mmu.read( this.pcb.getPid(), this.pc++ );

			// Decode
			ExecutableInstruction executableInstruction = decode( instruction );

			// Execute
			if ( executableInstruction.type == InstructionSet.HLT ) {
				this.pcb = null;
				return;
			}

			if ( executableInstruction.getClass() == ExecutableInstruction.IOExecutableInstruction.class ) {
				this.dmaChannel
						.handle( (ExecutableInstruction.IOExecutableInstruction) executableInstruction, this.pcb );
			} else {
				executableInstruction.execute();
			}
		}
	}

	ExecutableInstruction decode( Word word ) throws InvalidInstructionException {
		long signature = word.getData() & 0xC0000000;
		if ( signature == 0x00000000 ) {
			return new ExecutableInstruction.ArithmeticExecutableInstruction( word, this.registers );
		} else if ( signature == 0x40000000 ) {
			return new ExecutableInstruction.ConditionalExecutableInstruction( word, this.registers );
		} else if ( signature == 0x80000000 ) {
			return new ExecutableInstruction.UnconditionalJumpExecutableInstruction( word, this.registers );
		} else {
			return new ExecutableInstruction.IOExecutableInstruction( word, this.registers );
		}
	}
}
