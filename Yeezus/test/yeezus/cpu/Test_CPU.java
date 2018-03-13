package yeezus.cpu;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import yeezus.driver.CPUSchedulingPolicy;
import yeezus.driver.Dispatcher;
import yeezus.driver.Driver;
import yeezus.driver.Scheduler;
import yeezus.memory.MMU;
import yeezus.memory.Memory;
import yeezus.memory.Word;
import yeezus.pcb.TaskManager;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Test_CPU {

	private Memory registers;
	private CPU cpu;

	@Before public void setUp() throws Exception {
		File file = new File( "src/yeezus/Test-File.txt" );
		assertTrue( file.exists() );
		Memory disk = new Memory( 2048 );
		Driver.loadFile( disk, file );
		MMU mmu = new MMU( new Memory( 1024 ) );
		new Scheduler( mmu, disk, TaskManager.INSTANCE, CPUSchedulingPolicy.FCFS ).run();
		this.registers = new Memory( 16 );
		this.cpu = new CPU( 0, mmu, this.registers );
		new Dispatcher( TaskManager.INSTANCE, this.cpu ).run();
	}

	@After public void tearDown() throws Exception {
		Driver.reset();
		CPU.reset();
		TaskManager.INSTANCE.reset();
	}

	@Test public void testCPU() throws Exception {
		// Instruction 0
		this.cpu.debugRun();
		assertEquals( new Word( "0x0000000A" ), this.registers.read( 5 ) );

		// Instruction 1
		this.cpu.debugRun();
		assertEquals( new Word( "0x00000000" ), this.registers.read( 6 ) );

		// Instruction 2
		this.cpu.debugRun();
		assertEquals( new Word( "0x00000000" ), this.registers.read( 1 ) );

		// Instruction 3
		this.cpu.debugRun();
		assertEquals( new Word( "0x00000000" ), this.registers.read( 0 ) );

		// Instruction 4
		this.cpu.debugRun();
		assertEquals( new Word( "0x00000005C" ), this.registers.read( 10 ) );

		// Instruction 5
		this.cpu.debugRun();
		assertEquals( new Word( "0x0000000DC" ), this.registers.read( 13 ) );

		// Instruction 6
		this.cpu.debugRun();
		assertEquals( new Word( "0x000000060" ), this.registers.read( 10 ) );

		// Instruction 7
		this.cpu.debugRun();
		assertEquals( new Word( "0x000000006" ), this.registers.read( 11 ) );  // TODO Notify Jessica of this change
	}

}
