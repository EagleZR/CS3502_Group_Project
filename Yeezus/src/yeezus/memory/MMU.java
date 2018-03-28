package yeezus.memory;

import yeezus.pcb.PCB;

import java.util.ArrayList;

/**
 * The MMU helps organize the RAM {@link Memory} in the {@link yeezus} Operating System. The MMU keeps track of which
 * processes own which RAM addresses or blocks of addresses, and acts as the translator between logical and physical
 * memory.
 *
 * @author Mark Zeagler
 * @version 2.0
 */
public class MMU {

	private static final int FRAME_SIZE = 4;
	private ArrayList<ArrayList<Integer>> frameMap;
	private ArrayList<Integer> freeFrames;
	private Memory RAM, disk;

	/**
	 * Constructs a new MMU around the given RAM.
	 *
	 * @param disk The RAM this MMU is meant to manage.
	 * @param RAM  The RAM this MMU is meant to manage.
	 */
	public MMU( Memory disk, Memory RAM ) {
		this.RAM = RAM;
		this.disk = disk;
		this.freeFrames = new ArrayList<>();
		for ( int i = 0; i < RAM.getCapacity(); i += FRAME_SIZE ) {
			this.freeFrames.add( i );
		}
		this.frameMap = new ArrayList<>();
	}

	/**
	 * Maps the requested amount of memory in RAM to the given Process ID.
	 *
	 * @param pcb The PCB of the process to be mapped.
	 * @return {@code true} if the memory was successfully mapped for the process.
	 */
	public synchronized boolean mapMemory( PCB pcb ) {
		// TODO Map the process disk addresses to pages
		return false;
	}

	/**
	 * Checks if the given Process ID has any associated memory mappings in RAM.
	 *
	 * @param pcb The PCB of the memory mappings to be checked.
	 * @return {@code true} if the Process ID is associated with any memory mappings in RAM.
	 */
	public synchronized boolean processMapped( PCB pcb ) {
		try {
			return this.frameMap.get( pcb.getPID() ) != null;
		} catch ( Exception e ) {
			return false;
		}
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
			return this.RAM.read( this.frameMap.get( pcb.getPID() ).get( logicalAddress ) );
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
		this.RAM.write( this.frameMap.get( pcb.getPID() ).get( logicalAddress ), data );
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
		if ( this.frameMap.size() > pid && processMapped( pcb ) && this.frameMap.get( pid ) != null ) {
			while ( !this.frameMap.get( pid ).isEmpty() ) {
				this.freeFrames.add( this.frameMap.get( pid ).remove( 0 ) );
			}
			this.frameMap.set( pid, null );
		}
	}
}
