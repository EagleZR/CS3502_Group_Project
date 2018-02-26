package yeezus.cpu;

import org.junit.Test;
import yeezus.memory.Word;

import static org.junit.Assert.assertEquals;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_Decoder {
	@Test public void decode() throws Exception {
		ExecutableInstruction executableInstruction = Decoder.decode( new Word( "0xC050005C" ) );
		assertEquals( InstructionSet.RD, executableInstruction.type );
		assertEquals( ExecutableInstruction.IOExecutableInstruction.class, executableInstruction.getClass() );
	}

}