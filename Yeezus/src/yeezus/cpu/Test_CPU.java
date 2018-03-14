package yeezus.cpu;

import org.junit.Before;
import org.junit.Test;
import yeezus.memory.MMU;
import yeezus.memory.Memory;

public class Test_CPU {

	CPU cpu;

	@Before public void setup() throws Exception {
		this.cpu = new CPU( 0, new MMU( new Memory( 1024 ) ), new Memory( 16 ) );
	}

	@Test public void setProcess() {

	}

	@Test public void run() {

	}
}
