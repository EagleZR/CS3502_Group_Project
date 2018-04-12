package yeezus.memory;

import yeezus.pcb.PCB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The MMU helps organize the RAM {@link Memory} in the {@link yeezus} Operating System. The MMU keeps track of which
 * processes own which RAM addresses or blocks of addresses, and acts as the translator between logical and physical
 * memory.
 *
 * @author Mark Zeagler
 * @version 2.0
 */
public class MMU {

	public static final int FRAME_SIZE = 4;
	private final List<PageFault> pageFaults = Collections.synchronizedList( new LinkedList<>() );
	private ArrayList<ArrayList<Integer>> frameMap;
	private ArrayList<Integer> pool;
	private Memory RAM, disk;
	private int numFaults = 0;

	/**
	 * Constructs a new MMU around the given RAM.
	 *
	 * @param disk The RAM this MMU is meant to manage.
	 * @param RAM  The RAM this MMU is meant to manage.
	 */
	public MMU( Memory disk, Memory RAM ) {
		this.RAM = RAM;
		this.disk = disk;
		this.pool = new ArrayList<>();
		for ( int i = 0; i < RAM.getCapacity(); i += FRAME_SIZE ) {
			this.pool.add( i );
		}
		this.frameMap = new ArrayList<>();
	}

	private synchronized void incNumFaults() {
		this.numFaults++;
	}

	public synchronized int getNumFaults() {
		return this.numFaults;
	}

	/**
	 * Maps the requested amount of memory in RAM to the given Process ID.
	 *
	 * @param pcb The PCB of the process to be mapped.
	 * @return {@code true} if the memory was successfully mapped for the process.
	 */
	public synchronized boolean mapMemory( PCB pcb ) {
		// TODO Map the process disk addresses to pages
		if ( this.pool.size() >= 4 ) {
			PCB.PageTable pageTable = pcb.getPageTable();
			if ( pcb.getPageTable() == null ) {
				pcb.generatePageTable( FRAME_SIZE );
				pageTable = pcb.getPageTable();
			}

		}
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
	 * @throws PageFault               If the page which contains the given logical address is not yet mapped to
	 *                                 memory.
	 */
	public synchronized Word read( PCB pcb, int logicalAddress ) throws InvalidAddressException, PageFault {
		int physicalAddress = pcb.getPageTable().getAddress( Memory.getPageNumber( logicalAddress, FRAME_SIZE ) );
		if ( physicalAddress == -1 ) {
			try {
				return this.RAM.read( physicalAddress );
			} catch ( IndexOutOfBoundsException | NullPointerException e ) {
				throw new InvalidAddressException(
						"The given logical address, " + logicalAddress + ", is not mapped to a physical address." );
			}
		} else {
			PageFault pageFault = new PageFault( pcb, Memory.getPageNumber( logicalAddress, FRAME_SIZE ) );
			this.pageFaults.add( pageFault );
			incNumFaults();
			throw pageFault;
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
	 * @throws PageFault               If the page which contains the given logical address is not yet mapped to
	 *                                 memory.
	 */
	public synchronized void write( PCB pcb, int logicalAddress, Word data ) throws InvalidAddressException, PageFault {
		int physicalAddress = pcb.getPageTable().getAddress( Memory.getPageNumber( logicalAddress, FRAME_SIZE ) );
		if ( physicalAddress == -1 ) {
			try {
				this.RAM.write( physicalAddress, data );
			} catch ( IndexOutOfBoundsException | NullPointerException e ) {
				throw new InvalidAddressException(
						"The given logical address, " + logicalAddress + ", is not mapped to a physical address." );
			}
		} else {
			PageFault pageFault = new PageFault( pcb, Memory.getPageNumber( logicalAddress, FRAME_SIZE ) );
			this.pageFaults.add( pageFault );
			incNumFaults();
			throw pageFault;
		}
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
				this.pool.add( this.frameMap.get( pid ).remove( 0 ) );
			}
			this.frameMap.set( pid, null );
		}
	}

	/**
	 * Loads a given page, identified by page number and PCB, into RAM.
	 *
	 * @param pcb        The PCB of the data to be loaded into RAM.
	 * @param pageNumber The page number of the data to be loaded into RAM.
	 */
	public synchronized boolean loadPage( PCB pcb, int pageNumber ) {
		if ( this.pool.size() > 0 ) {
			PCB.PageTable pageTable = pcb.getPageTable();
			int physicalDiskAddress = pcb.getStartDiskAddress() + pageNumber * FRAME_SIZE;
			int physicalRAMAddress = this.pool.remove( 0 );
			for ( int i = 0; i < FRAME_SIZE; i++ ) {
				this.RAM.write( physicalRAMAddress + i, this.disk.read( physicalDiskAddress + i ) );
			}
			pageTable.setAddress( pageNumber, physicalRAMAddress );
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Saves a given page, identified by page number and PCB, from RAM back into the disk. <b>This removes the page from
	 * RAM.</b>
	 *
	 * @param pcb            The PCB of the data to be saved to the disk.
	 * @param logicalAddress The logical address of the data to be saved to the disk.
	 */
	public synchronized void writePage( PCB pcb, int logicalAddress ) {
		int pageNumber = (int) Math.floor( (double) logicalAddress / FRAME_SIZE );
		PCB.PageTable pageTable = pcb.getPageTable();
		int physicalDiskAddress = pcb.getStartDiskAddress() + pageNumber * FRAME_SIZE;
		int physicalRAMAddress = pageTable.getAddress( pageNumber );
		for ( int i = 0; i < FRAME_SIZE; i++ ) {
			this.disk.write( physicalDiskAddress + i, this.RAM.read( physicalRAMAddress + i ) );
		}
		this.pool.add( physicalRAMAddress );
		pageTable.clearAddress( pageNumber );
	}

	public synchronized Word[] getPage( PCB pcb, int pageNumber ) throws PageFault {
		Word[] page = new Word[FRAME_SIZE];
		int startAddress = pcb.getPageTable().getAddress( pageNumber );
		if ( startAddress == -1 ) {
			PageFault pageFault = new PageFault( pcb, pageNumber );
			this.pageFaults.add( pageFault );
			incNumFaults();
			throw pageFault;
		}
		for ( int i = 0; i < FRAME_SIZE; i++ ) {
			page[i] = this.RAM.read( startAddress + i );
		}
		return page;
	}

	public List<PageFault> getPageFaults() {
		return this.pageFaults;
	}

	//	public void handlePageFaults() { // TODO Handle in Scheduler
	//		while ( !this.pageFaults.isEmpty() && !this.pool.isEmpty() ) {
	//			PageFault pageFault = this.pageFaults.get( 0 );
	//			loadPage( pageFault.getPCB(), pageFault.getPageNumber() );
	//			// TODO Remove from waiting queue, add to ready queue?
	//		}
	//	}

	/**
	 * A {@link Throwable} that indicates that a page which was attempted to be accessed is not currently available in
	 * RAM.
	 */
	public class PageFault extends Throwable {
		private final PCB pcb;
		private final int pageNumber;

		/**
		 * Thrown to the CPU to indicate that a page does not exist in RAM.
		 *
		 * @param pcb        The PCB of the page that was attempted to be accessed.
		 * @param pageNumber The page number of the page that was attempted to be accessed.
		 */
		private PageFault( PCB pcb, int pageNumber ) {
			super( "Page #" + pageNumber + " for process " + pcb.getPID() + " is not in RAM." );
			this.pcb = pcb;
			this.pageNumber = pageNumber;
		}

		public PCB getPCB() {
			return this.pcb;
		}

		public int getPageNumber() {
			return this.pageNumber;
		}
	}
}
