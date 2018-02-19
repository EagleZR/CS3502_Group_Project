package yeezus.memory;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class Test_Memory {

	private static final int REGISTER_CAPACITY = 16;
	private static final int RAM_CAPACITY = 1024;
	private static final int DISK_CAPACITY = 2048;

	private Memory registers = new Memory( REGISTER_CAPACITY );
	private Memory RAM = new Memory( RAM_CAPACITY );
	private Memory disk = new Memory( DISK_CAPACITY );

	@Test public void testCapacities() throws InvalidAddressException {
		// Registers
		for ( int i = 0; i < REGISTER_CAPACITY; i++ ) {
			this.registers.write( i, new Word( Integer.decode( "0x00000000" ) ) );
		}

		// RAM
		for ( int i = 0; i < RAM_CAPACITY; i++ ) {
			this.RAM.write( i, new Word( Integer.decode( "0x00000000" ) ) );
		}

		// Storage
		for ( int i = 0; i < DISK_CAPACITY; i++ ) {
			this.disk.write( i, new Word( Integer.decode( "0x00000000" ) ) );
		}
	}

	@Test( expected = InvalidAddressException.class ) public void testInvalidRegisterAddress()
			throws InvalidAddressException {
		this.registers.read( REGISTER_CAPACITY + 1 );
	}

	@Test( expected = InvalidAddressException.class ) public void testInvalidRAMAddress()
			throws InvalidAddressException {
		this.registers.read( RAM_CAPACITY + 1 );
	}

	@Test( expected = InvalidAddressException.class ) public void testInvalidDiskAddress()
			throws InvalidAddressException {
		this.registers.read( DISK_CAPACITY + 1 );
	}

	@Test( expected = InvalidAddressException.class ) public void testNegativeRegisterAddress()
			throws InvalidAddressException {
		this.registers.read( -1 );
	}

	@Test( expected = InvalidAddressException.class ) public void testNegativeRAMAddress()
			throws InvalidAddressException {
		this.registers.read( -1 );
	}

	@Test( expected = InvalidAddressException.class ) public void tesNegativeDiskAddress()
			throws InvalidAddressException {
		this.registers.read( -1 );
	}

	@Test public void testRead() throws InvalidAddressException {
		this.registers.write( 0, new Word( Integer.decode( "0x00003123" ) ) );
		assertEquals( 0x00003123, this.registers.read( 0 ).getData() );

		ArrayList<Word> array = new ArrayList<>();

		for ( int i = 0; i < REGISTER_CAPACITY; i++ ) {
			Word word = new Word( i );
			this.registers.write( i, word );
			array.add( word );
		}

		for ( int i = 0; i < REGISTER_CAPACITY; i++ ) {
			assertEquals( array.get( i ), this.registers.read( i ) );
		}
	}
}
