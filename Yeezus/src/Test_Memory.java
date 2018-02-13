import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class Test_Memory {

	private static final int REGISTER_CAPACITY = 16;
	private static final int RAM_CAPACITY = 1024;
	private static final int DISK_CAPACITY = 2048;

	@Test public void testCapacities() throws InvalidAddressException {
		// Registers
		for ( int i = 0; i < REGISTER_CAPACITY; i++ ) {
			Memory.registers.write( i, new Word( 0x00000000 ) );
		}

		// RAM
		for ( int i = 0; i < RAM_CAPACITY; i++ ) {
			Memory.RAM.write( i, new Word( 0x00000000 ) );
		}

		// Storage
		for ( int i = 0; i < DISK_CAPACITY; i++ ) {
			Memory.disk.write( i, new Word( 0x00000000 ) );
		}
	}

	@Test( expected = InvalidAddressException.class ) public void testInvalidRegisterAddress()
			throws InvalidAddressException {
		Memory.registers.read( REGISTER_CAPACITY + 1 );
	}

	@Test( expected = InvalidAddressException.class ) public void testInvalidRAMAddress()
			throws InvalidAddressException {
		Memory.registers.read( RAM_CAPACITY + 1 );
	}

	@Test( expected = InvalidAddressException.class ) public void testInvalidDiskAddress()
			throws InvalidAddressException {
		Memory.registers.read( DISK_CAPACITY + 1 );
	}

	@Test( expected = InvalidAddressException.class ) public void testNegativeRegisterAddress()
			throws InvalidAddressException {
		Memory.registers.read( -1 );
	}

	@Test( expected = InvalidAddressException.class ) public void testNegativeRAMAddress()
			throws InvalidAddressException {
		Memory.registers.read( -1 );
	}

	@Test( expected = InvalidAddressException.class ) public void tesNegativeDiskAddress()
			throws InvalidAddressException {
		Memory.registers.read( -1 );
	}

	@Test public void testRead() throws InvalidAddressException {
		Memory.registers.write( 0, new Word( 0x00003123 ) );
		assertEquals( 0x00003123, Memory.registers.read( 0 ).getData() );

		ArrayList<Word> array = new ArrayList<>();

		for ( int i = 0; i < REGISTER_CAPACITY; i++ ) {
			Word word = new Word( i );
			Memory.registers.write( i, word );
			array.add( word );
		}

		for ( int i = 0; i < REGISTER_CAPACITY; i++ ) {
			assertEquals( array.get( i ), Memory.registers.read( i ) );
		}
	}
}
