package yeezus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import yeezus.cpu.Test_InstructionSet;
import yeezus.driver.Test_File;
import yeezus.memory.Test_IntToHex;
import yeezus.memory.Test_Memory;
import yeezus.pcb.Test_PCB;
import yeezus.pcb.Test_Process;

@RunWith( Suite.class ) @Suite.SuiteClasses( { Test_File.class, Test_InstructionSet.class, Test_IntToHex.class,
		Test_Memory.class, Test_PCB.class, Test_Process.class } ) public class Test_All {
}
