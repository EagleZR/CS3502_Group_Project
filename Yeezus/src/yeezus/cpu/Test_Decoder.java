package yeezus.cpu;

import org.junit.Test;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.TaskManager;

import static org.junit.Assert.assertEquals;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_Decoder {

	@Test public void decode() throws Exception {
		CPU cpu = new CPU( new MMU( new Memory( 1024 ) ), new Memory( 16 ) );
		ExecutableInstruction executableInstruction = cpu.decode( new Word( "0xC050005C" ) );
		assertEquals( InstructionSet.RD, executableInstruction.type );
		assertEquals( ExecutableInstruction.IOExecutableInstruction.class, executableInstruction.getClass() );
		// TODO Test a lot more
	}

	@Test public void add() throws Exception {
		Memory registers = new Memory( 16 );
		CPU cpu = new CPU( new MMU( new Memory( 1024 ) ), registers );
		registers.write( 7, new Word( "0x2341" ) );
		cpu.decode( new Word( "0x05070000" ) ).execute();
		assertEquals( "0x00002341", registers.read( 0 ).toString() );

	}

}
