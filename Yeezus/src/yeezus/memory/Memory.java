package yeezus.memory;

/**
 * The collective memory system of the OS, including the registers, RAM, and disk storage space.
 *
 * @version 1.0
 */
public class Memory {

	private Word[] storage;

	/**
	 * Constructs a new memory.memory device with the given capacity.
	 */
	public Memory( int capacity ) throws InvalidWordException {
		this.storage = new Word[capacity];
		for ( int i = 0; i < capacity; i++ ) {
			String word = "0x00000000";
			this.storage[i] = new Word( word );
		}
	}

	/**
	 * Retrieves the data from the given physicalAddress as a {@link Word}.
	 *
	 * @param physicalAddress The physical address of the location to be read.
	 * @return The {@link Word} stored at the given physical address.
	 * @throws InvalidAddressException Thrown if the physical address given is outside of the scope of this memory.memory.
	 */
	public Word read( int physicalAddress ) throws InvalidAddressException {
		if ( physicalAddress > storage.length ) {
			throw new InvalidAddressException(
					"Address: " + physicalAddress + " is too high. The capacity is: " + storage.length );
		}
		if ( physicalAddress < 0 ) {
			throw new InvalidAddressException( "Can't have a negative physicalAddress." );
		}
		return storage[physicalAddress];
	}

	/**
	 * Writes the given {@link Word} to the given physicalAddress.
	 *
	 * @param physicalAddress The location where the {@link Word} should be written.
	 * @param word            The {@link Word} to be stored at the physical address.
	 * @throws InvalidAddressException Thrown if the physical address given is outside of the scope of this memory.memory.
	 */
	public void write( int physicalAddress, Word word ) throws InvalidAddressException {
		if ( physicalAddress > storage.length ) {
			throw new InvalidAddressException(
					"Address: " + physicalAddress + " is too high. The capacity is: " + storage.length );
		}
		if ( physicalAddress < 0 ) {
			throw new InvalidAddressException( "Can't have a negative physicalAddress" );
		}
		storage[physicalAddress] = word;
	}

	/**
	 * Returns the storage capacity of this memory instance.
	 *
	 * @return The amount of {@link Word}s this memory instance can store.
	 */
	public int getCapacity() {
		return this.storage.length;
	}

	/**
	 * Provides the memory as a list of words written in hex. Each hex code is on its own line.
	 *
	 * @return The memory as a list of Words written in hex.
	 */
	@Override public String toString() {
		StringBuilder string = new StringBuilder();
		for ( Word word : this.storage ) {
			string.append( word.toString() ).append( "\n" );
		}
		return string.toString();
	}
}
