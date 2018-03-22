package yeezus.memory;

import com.sun.istack.internal.NotNull;

/**
 * A storage mechanism for {@link Word}s in the {@link yeezus} operating system.
 *
 * @author Mark Zeagler
 * @version 2.0
 */
public class Memory {

	private Word[] storage;

	/**
	 * Constructs a new memory.memory device with the given capacity.
	 *
	 * @param capacity The size of the memory to be created.
	 */
	public Memory( int capacity ) throws InvalidWordException {
		this.storage = new Word[capacity];
		Word zero = new Word( "0x00000000" );
		for ( int i = 0; i < capacity; i++ ) {
			this.storage[i] = zero;
		}
	}

	/**
	 * Retrieves the data from the given physicalAddress as a {@link Word}.
	 *
	 * @param physicalAddress The physical address of the location to be read.
	 * @return The {@link Word} stored at the given physical address.
	 * @throws InvalidAddressException Thrown if the physical address given is outside of the scope of this
	 *                                 memory.memory.
	 */
	public synchronized Word read( int physicalAddress ) throws InvalidAddressException {
		if ( physicalAddress > this.storage.length ) {
			throw new InvalidAddressException(
					"Address: " + physicalAddress + " is too high. The capacity is: " + this.storage.length );
		}
		if ( physicalAddress < 0 ) {
			throw new InvalidAddressException( "Can't have a negative physicalAddress (" + physicalAddress + ")." );
		}
		return this.storage[physicalAddress];
	}

	/**
	 * Writes the given {@link Word} to the given physicalAddress.
	 *
	 * @param physicalAddress The location where the {@link Word} should be written.
	 * @param word            The {@link Word} to be stored at the physical address.
	 * @throws InvalidAddressException Thrown if the physical address given is outside of the scope of this
	 *                                 memory.memory.
	 */
	public synchronized void write( int physicalAddress, @NotNull Word word ) throws InvalidAddressException {
		if ( physicalAddress > this.storage.length ) {
			throw new InvalidAddressException(
					"Address: " + physicalAddress + " is too high. The capacity is: " + this.storage.length );
		}
		if ( physicalAddress < 0 ) {
			throw new InvalidAddressException( "Can't have a negative physicalAddress" );
		}
		this.storage[physicalAddress] = word;
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
