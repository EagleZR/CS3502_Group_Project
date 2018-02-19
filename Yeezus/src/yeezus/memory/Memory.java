package yeezus.memory;

/**
 * The collective memory system of the OS, including the registers, RAM, and disk storage space.
 *
 * @version 0.1
 */
public class Memory {

//	/**
//	 * Pre-made storage for the registers to be used by the OS.
//	 */
//	public static memory registers = new memory( 16 );
//	/**
//	 * Pre-made storage for the RAM to be used by the OS.
//	 */
//	public static memory RAM = new memory( 1024 );
//	/**
//	 * Pre-made storage for the disk to be used by the OS.
//	 */
//	public static memory disk = new memory( 2048 );

	private Word[] storage;

	/**
	 * Constructs a new memory.memory device with the given capacity.
	 */
	public Memory( int capacity ) {
		this.storage = new Word[capacity];
	}

	/**
	 * Retrieves the data from the given address as a {@link Word}.
	 *
	 * @param address The address of the location to be read.
	 * @return The {@link Word} stored at the given address.
	 * @throws InvalidAddressException Thrown if the address given is outside of the scope of this memory.memory.
	 */
	public Word read( int address ) throws InvalidAddressException {
		if ( address > storage.length ) {
			throw new InvalidAddressException(
					"Address: " + address + " is too high. The capacity is: " + storage.length );
		}
		if ( address < 0 ) {
			throw new InvalidAddressException( "Can't have a negative address" );
		}
		return storage[address];
	}

	/**
	 * Writes the given {@link Word} to the given address.
	 *
	 * @param address The location where the {@link Word} should be written.
	 * @param word    The {@link Word} to be stored at the address.
	 * @throws InvalidAddressException Thrown if the address given is outside of the scope of this memory.memory.
	 */
	public void write( int address, Word word ) throws InvalidAddressException {
		if ( address > storage.length ) {
			throw new InvalidAddressException(
					"Address: " + address + " is too high. The capacity is: " + storage.length );
		}
		if ( address < 0 ) {
			throw new InvalidAddressException( "Can't have a negative address" );
		}
		storage[address] = word;
	}

	/**
	 * Provides the memory as a list of words written in hex. Each hex code is on its own line.
	 *
	 * @return The memory as a list of Words written in hex.
	 */
	@Override public String toString() {
		StringBuilder string = new StringBuilder();
		for ( Word word : this.storage ) {
			string.append( word.toString() + "\n" );
		}
		return string.toString();
	}
}
