package yeezus;

import yeezus.driver.Test_File;
import yeezus.cpu.Test_InstructionSet;
import yeezus.memory.Test_IntToHex;
import yeezus.memory.Test_Memory;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith( Suite.class ) @Suite.SuiteClasses( { Test_File.class, Test_InstructionSet.class, Test_IntToHex.class,
		Test_Memory.class } ) public class Test_All {
}
