/**
 * The collective memory system of the OS, including the registers, RAM, and disk storage space.
 *
 * @version 0.1
 */
public class Memory {

	public static Memory registers = new Memory( 16 );
	public static Memory RAM = new Memory( 1024 );
	public static Memory disk = new Memory( 2048 );

	Word[] storage;

	private Memory( int capacity ) {
		this.storage = new Instruction[capacity];
	}

	public Word read( int address ) {
//		if( address > storage.length) {
//			throw  new InvalidAddressException("Can't go that high!");
//		}
		return storage[address];
	}

	public void write( int address, Word word ) {
		storage[address] = word;
	}

}
