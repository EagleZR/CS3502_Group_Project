package yeezus.memory;

import java.util.ArrayList;

/**
 * The MMU helps organize the RAM {@link Memory} in the {@link yeezus} Operating System. The MMU keeps track of which
 * processes own which RAM addresses or blocks of addresses, and acts as the translator between logical and physical
 * memory.
 */
public class MMU {

	private ArrayList<ArrayList<Integer>> addressMap; // TODO Make this more efficient later
	private ArrayList<Integer> pids;
	private int[] addressOwnershipRegistry;
	private Memory RAM;

	/**
	 * Constructs a new MMU around the given RAM.
	 *
	 * @param RAM The RAM this MMU is meant to manage.
	 */
	public MMU( Memory RAM ) {
		this.RAM = RAM;
		this.addressOwnershipRegistry = new int[RAM.getCapacity()];
		this.addressMap = new ArrayList<>();
		this.pids = new ArrayList<>();
	}

	/**
	 * Maps the requested amount of memory in RAM to the given Process ID.
	 *
	 * @param pid  The Process ID for the process whose memory is to be mapped.
	 * @param size The amount of memory that is to be reserved for the process.
	 * @return {@code true} if the memory was successfully mapped for the process.
	 */
	public boolean mapMemory( int pid, int size ) {
		try {
			for ( int i = 0; i < size; i++ ) {
				mapAddress( pid, i );
			}
			this.pids.add( pid );
			return true;
		} catch ( InvalidAddressException e ) {
			terminatePID( pid );
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks if the given Process ID has any associated memory mappings in RAM.
	 *
	 * @param pid The Process ID of the memory mappings to be checked.
	 * @return {@code true} if the Process ID is associated with any memory mappings in RAM.
	 */
	public boolean processMapped( int pid ) {
		return this.pids.contains( pid );
	}

	/**
	 * {@code @Deprecated} Don't use this anymore. Use {@link MMU#mapMemory(int, int)} instead.
	 *
	 * @param pid            The Process ID of the process whose memory is to be mapped.
	 * @param logicalAddress The logical address of the memory as recognized by the process that owns it.
	 * @throws InvalidAddressException Thrown if the physical memory is out of room.
	 */
	@Deprecated public void mapAddress( int pid, int logicalAddress ) throws InvalidAddressException {
		for ( int i = 0; i < this.addressOwnershipRegistry.length; i++ ) {
			if ( this.addressOwnershipRegistry[i] == 0 ) {
				mapAddress( pid, logicalAddress, i );
				this.addressOwnershipRegistry[i] = pid;
				return;
			}
		}
		throw new InvalidAddressException( "Not really invalid, we've just run out of memory..." );
	}

	@Deprecated private synchronized void mapAddress( int pid, int logicalAddress, int physicalAddress ) {
		ArrayList<Integer> processAddresses;
		try {
			processAddresses = this.addressMap.get( pid );
		} catch ( IndexOutOfBoundsException e ) {
			processAddresses = new ArrayList<>();
			while ( ( pid - addressMap.size() ) + 1 > 0 ) {
				addressMap.add( null );
			}
			this.addressMap.set( pid, processAddresses );
		}
		this.addressOwnershipRegistry[physicalAddress] = pid;

		while ( logicalAddress - processAddresses.size() + 1 > 0 ) {
			processAddresses.add( null );
		}

		processAddresses.set( logicalAddress, physicalAddress );
	}

	/**
	 * Reads the {@link Word} from the given physical address.
	 *
	 * @param physicalAddress The physical address of the word to be read.
	 * @return The {@link Word} stored at the given physical address.
	 * @throws InvalidAddressException Thrown if the requested address is outside of the scope of the RAM.
	 */
	public synchronized Word read( int physicalAddress ) throws InvalidAddressException {
		return this.RAM.read( physicalAddress );
	}

	/**
	 * Reads the {@link Word} whose physical address corresponds to the given logical address for the given process.
	 *
	 * @param pid            The Process ID of the process whose memory is to be read.
	 * @param logicalAddress The logical address for the given process.
	 * @return The {@link Word} stored in the physical location associated with the logical address of the given
	 * process.
	 * @throws InvalidAddressException Thrown if the logical address has not been mapped to a physical address.
	 */
	public synchronized Word read( int pid, int logicalAddress ) throws InvalidAddressException {
		try {
			return this.RAM.read( this.addressMap.get( pid ).get( logicalAddress ) );
		} catch ( IndexOutOfBoundsException | NullPointerException e ) {
			throw new InvalidAddressException( "The given logical address is not mapped to a physical address." );
		}
	}

	/**
	 * Writes the given {@link Word} to the given physical address.
	 *
	 * @param physicalAddress The physical location that is to be written to.
	 * @param data            The information that is to be stored.
	 * @throws InvalidAddressException Thrown if the requested address is outside of the scope of the RAM.
	 */
	public synchronized void write( int physicalAddress, Word data ) throws InvalidAddressException {
		this.RAM.write( physicalAddress, data );
	}

	/**
	 * Writes the given word to the given logical address for the given process.
	 *
	 * @param pid            The Process ID of the process whose memory is to be written to.
	 * @param logicalAddress The logical address for the given process.
	 * @param data           The information that is to be stored.
	 * @throws InvalidAddressException Thrown if the logical address has not been mapped to a physical address.
	 */
	public synchronized void write( int pid, int logicalAddress, Word data ) throws InvalidAddressException {
		this.RAM.write( this.addressMap.get( pid ).get( logicalAddress ), data );
	}

	/**
	 * Un-maps the memory for a terminated process, freeing it up so that another can use it.
	 *
	 * @param pid The Process ID of the process whose memory is to be freed.
	 */
	public synchronized void terminatePID( int pid ) {
		try {
			this.addressMap.set( pid, null );
			for ( int i = 0; i < this.addressOwnershipRegistry.length; i++ ) {
				if ( this.addressOwnershipRegistry[i] == pid ) {
					this.addressOwnershipRegistry[i] = 0;
				}
			}
			this.pids.remove( pid );
		} catch ( IndexOutOfBoundsException e ) {
			// Do nothing, it's already been removed, so we're good
		}
	}
}
