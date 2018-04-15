package yeezus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import yeezus.cpu.Test_DMAChannel;
import yeezus.cpu.Test_Decoder;
import yeezus.cpu.Test_InstructionSet;
import yeezus.driver.Test_Dispatcher;
import yeezus.driver.Test_Driver;
import yeezus.driver.Test_Loader;
import yeezus.driver.Test_Scheduler;
import yeezus.memory.Test_Cache;
import yeezus.memory.Test_MMU;
import yeezus.memory.Test_Memory;
import yeezus.memory.Test_Word;
import yeezus.pcb.Test_PCB;
import yeezus.pcb.Test_TaskManager;

@RunWith( Suite.class ) @Suite.SuiteClasses( { Test_File.class, Test_InstructionSet.class, Test_Memory.class,
		Test_TaskManager.class, Test_PCB.class, Test_Decoder.class, Test_Word.class, Test_Loader.class,
		Test_Scheduler.class, Test_Dispatcher.class, Test_MMU.class, Test_Cache.class, Test_DMAChannel.class,
		Test_Driver.class } ) public class Test_All {
}
