package yeezus.cpu;

import org.junit.Test;
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
		CPU cpu = new CPU( new TaskManager(), new Memory( 16 ) );
		ExecutableInstruction executableInstruction = cpu.decode( new Word( "0xC050005C" ) );
		assertEquals( InstructionSet.RD, executableInstruction.type );
		assertEquals( ExecutableInstruction.IOExecutableInstruction.class, executableInstruction.getClass() );
		// TODO Test a lot more
	}

}
