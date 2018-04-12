package yeezus.cpu;

import org.junit.After;
import org.junit.Test;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.memory.Word;

import static org.junit.Assert.assertEquals;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_Decoder {

	@Test public void decode() {
		MMU mmu = new MMU( new Memory( 2048 ), new Memory( 1024 ) );
		CPU cpu = new CPU( 0, new DMAChannel( mmu, 6, false ), mmu, 16, 20 );
		ExecutableInstruction executableInstruction = cpu.decode( new Word( "0xC050005C" ) );
		assertEquals( InstructionSet.RD, executableInstruction.type );
		assertEquals( ExecutableInstruction.IOExecutableInstruction.class, executableInstruction.getClass() );
		// TODO Test a lot more
	}

	@Test public void add() {
		MMU mmu = new MMU( new Memory( 2048 ), new Memory( 1024 ) );
		CPU cpu = new CPU( 0, new DMAChannel( mmu, 6, false ), mmu, 16, 20 );
		Memory registers = cpu.getRegisters();
		registers.write( 7, new Word( "0x2341" ) );
		cpu.decode( new Word( "0x05070000" ) ).run();
		assertEquals( "0x00002341", registers.read( 0 ).toString() );

	}

	@After public void tearDown() {
		CPU.reset();
	}

}
