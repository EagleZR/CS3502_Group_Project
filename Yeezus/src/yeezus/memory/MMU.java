package yeezus.memory;

import yeezus.pcb.PCB;

import java.util.ArrayList;

/**
 * The MMU helps organize the RAM {@link Memory} in the {@link yeezus} Operating System. The MMU keeps track of which
 * processes own which RAM addresses or blocks of addresses, and acts as the translator between logical and physical
 * memory.
 */
public class MMU {

	private ArrayList<ArrayList<Integer>> addressMap; // TODO Make this more efficient later
	private ArrayList<Integer> freeAddresses;
	private Memory RAM;

	/**
	 * Constructs a new MMU around the given RAM.
	 *
	 * @param RAM The RAM this MMU is meant to manage.
	 */
	public MMU( Memory RAM ) {
		this.RAM = RAM;
		this.freeAddresses = new ArrayList<>();
		for ( int i = 0; i < RAM.getCapacity(); i++ ) {
			this.freeAddresses.add( i );
		}
		// this.addressOwnershipRegistry = new int[RAM.getCapacity()];
		this.addressMap = new ArrayList<>();
	}

	/**
	 * Maps the requested amount of memory in RAM to the given Process ID.
	 *
	 * @param pcb The PCB of the process to be mapped.
	 * @return {@code true} if the memory was successfully mapped for the process.
	 */
	public synchronized boolean mapMemory( PCB pcb ) {
		if ( pcb == null ) {
			return false;
		}
		int pid = pcb.getPID();
		int size = pcb.getTotalSize();
		// System.out.println( "Loading process " + pid + " into RAM." );
		try {
			for ( int i = 0; i < size; i++ ) {
				mapAddress( pid, i );
			}
			// System.out.println( "The process was successfully mapped." );
			return true;
		} catch ( InvalidAddressException e ) {
			terminateProcessMemory( pcb );
			// e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks if the given Process ID has any associated memory mappings in RAM.
	 *
	 * @param pcb The PCB of the memory mappings to be checked.
	 * @return {@code true} if the Process ID is associated with any memory mappings in RAM.
	 */
	public synchronized boolean processMapped( PCB pcb ) {
		return this.addressMap.get( pcb.getPID() ) != null;
	}

	/**
	 * {@code @Deprecated} Don't use this anymore. Use {@link MMU#mapMemory(PCB)} instead.
	 *
	 * @param pid            The Process ID of the process whose memory is to be mapped.
	 * @param logicalAddress The logical address of the memory as recognized by the process that owns it.
	 * @throws InvalidAddressException Thrown if the physical memory is out of room.
	 */
	private void mapAddress( int pid, int logicalAddress ) throws InvalidAddressException {
		if ( this.freeAddresses.size() > 0 ) {
			mapAddress( pid, logicalAddress, this.freeAddresses.remove( 0 ) );
		} else {
			throw new InvalidAddressException( "Not really invalid, we've just run out of memory..." );
		}
	}

	private void mapAddress( int pid, int logicalAddress, int physicalAddress ) {
		ArrayList<Integer> processAddresses;

		if ( pid >= this.addressMap.size() || this.addressMap.get( pid ) == null ) {
			while ( ( pid - addressMap.size() ) >= 0 ) { // Only executes if it's not large enough
				addressMap.add( null );
			}
			processAddresses = new ArrayList<>();
			this.addressMap.set( pid, processAddresses );
		} else {
			processAddresses = this.addressMap.get( pid );
		}
		//		try {
		//			processAddresses = this.addressMap.get( pid );
		//		} catch ( IndexOutOfBoundsException e ) {
		//			processAddresses = new ArrayList<>();
		//			while ( ( pid - addressMap.size() ) + 1 > 0 ) {
		//				addressMap.add( null );
		//			}
		//			this.addressMap.set( pid, processAddresses );
		//		}

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
	protected synchronized Word read( int physicalAddress ) throws InvalidAddressException {
		return this.RAM.read( physicalAddress );
	}

	/**
	 * Reads the {@link Word} whose physical address corresponds to the given logical address for the given process.
	 *
	 * @param pcb            The PCB of the process whose memory is to be read.
	 * @param logicalAddress The logical address for the given process.
	 * @return The {@link Word} stored in the physical location associated with the logical address of the given
	 * process.
	 * @throws InvalidAddressException Thrown if the logical address has not been mapped to a physical address.
	 */
	public synchronized Word read( PCB pcb, int logicalAddress ) throws InvalidAddressException {
		try {
			return this.RAM.read( this.addressMap.get( pcb.getPID() ).get( logicalAddress ) );
		} catch ( IndexOutOfBoundsException | NullPointerException e ) {
			throw new InvalidAddressException(
					"The given logical address, " + logicalAddress + ", is not mapped to a physical address." );
		}
	}

	/**
	 * Writes the given {@link Word} to the given physical address.
	 *
	 * @param physicalAddress The physical location that is to be written to.
	 * @param data            The information that is to be stored.
	 * @throws InvalidAddressException Thrown if the requested address is outside of the scope of the RAM.
	 */
	protected synchronized void write( int physicalAddress, Word data ) throws InvalidAddressException {
		this.RAM.write( physicalAddress, data );
	}

	/**
	 * Writes the given word to the given logical address for the given process.
	 *
	 * @param pcb            The PCB of the process whose memory is to be written to.
	 * @param logicalAddress The logical address for the given process.
	 * @param data           The information that is to be stored.
	 * @throws InvalidAddressException Thrown if the logical address has not been mapped to a physical address.
	 */
	public synchronized void write( PCB pcb, int logicalAddress, Word data ) throws InvalidAddressException {
		this.RAM.write( this.addressMap.get( pcb.getPID() ).get( logicalAddress ), data );
	}

	/**
	 * Un-maps the memory for a terminated process, freeing it up so that another can use it.
	 *
	 * @param pcb The PCB of the process whose memory is to be freed.
	 */
	public synchronized void terminateProcessMemory( PCB pcb ) {
		if ( pcb == null ) {
			return;
		}
		int pid = pcb.getPID();
		// System.out.println( "Removing process " + pid + " from RAM." );
		if ( this.addressMap.size() > pid && processMapped( pcb ) ) {
			while ( !this.addressMap.get( pid ).isEmpty() ) {
				this.freeAddresses.add( this.addressMap.get( pid ).remove( 0 ) );
			}
			this.addressMap.set( pid, null );
		}
	}
}
