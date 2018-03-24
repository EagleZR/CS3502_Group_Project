package yeezus;

import yeezus.driver.CPUSchedulingPolicy;

/**
 * @author Mark Zeagler
 * @version 1.0
 */
public class Main {

	public static void main( String[] args ) throws Exception {
		new Yeezus( 0, CPUSchedulingPolicy.FCFS, 2048, 1024, 100, 16 );
	}
}
