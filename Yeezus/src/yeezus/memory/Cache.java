package yeezus.memory;

import yeezus.pcb.PCB;

/**
 * A cache controller for the {@link yeezus} operating system.
 *
 * @author Mark Zeagler
 * @version 1.0
 */
public class Cache extends Memory {

	private final int TEMP_BUFF_SIZE = 12; // The last 12 words of memory are the Temp Buffer
	private final MMU mmu;
	private int[] containedPages;
	private int oldestPage;

	/**
	 * Constructs a new memory.memory device with the given capacity.
	 *
	 * @param capacity The size of the memory to be created.
	 */
	public Cache( int capacity, MMU mmu ) throws InvalidWordException {
		super( capacity );
		this.mmu = mmu;
		this.containedPages = new int[(int) Math.ceil( ( (double) capacity - this.TEMP_BUFF_SIZE ) / MMU.FRAME_SIZE )];
		this.oldestPage = 0;
	}

	@Override public synchronized void write( int physicalAddress, Word word ) {
		System.out.println( "Please don't use Cache.write(int, Word) unless you're testing..." );
		super.write( physicalAddress, word );
	}

	@Override public synchronized Word read( int physicalAddress ) {
		System.out.println( "Please don't use Cache.read(int) unless you're testing..." );
		return super.read( physicalAddress );
	}

	public synchronized void write( PCB pcb, int logicalAddress, Word word ) throws InvalidAddressException {
		// TODO Test! Test! Test! Cache.write()
		int tempBufferLogicalAddress =
				pcb.getInstructionsLength() + pcb.getInputBufferLength() + pcb.getOutputBufferLength();
		if ( logicalAddress >= pcb.getInstructionsLength() ) {
			throw new InvalidAddressException( "The instructions cannot be overwritten in the cache." );
		}
		if ( logicalAddress < tempBufferLogicalAddress || logicalAddress >= pcb.getTotalSize() ) {
			// Make sure the address is in the temp buffer
			throw new InvalidAddressException( "The address " + logicalAddress
					+ " is not in the temp buffer. The cache cannot be written to at this location." );
		}
		super.write( logicalAddress - tempBufferLogicalAddress + getTempBufferStartAddress(), word );
	}

	public synchronized Word read( PCB pcb, int logicalAddress ) throws InvalidAddressException, MMU.PageFault {
		// TODO Test! Test! Test! Cache.read()
		int tempBufferLogicalAddress =
				pcb.getInstructionsLength() + pcb.getInputBufferLength() + pcb.getOutputBufferLength();
		if ( logicalAddress < tempBufferLogicalAddress || logicalAddress >= pcb.getTotalSize() ) {
			// Make sure the address is in the temp buffer
			throw new InvalidAddressException( "The address " + logicalAddress
					+ " is not in the instructions or temp buffer. The cache cannot be read at this location." );
		}
		if ( logicalAddress < pcb.getInstructionsLength() ) { // If address is an instruction
			// Get page number
			int page = getPageNumber( logicalAddress, MMU.FRAME_SIZE );
			int startAddress = getStartAddress( page );
			if ( startAddress == -1 ) { // Load page if not present
				loadPage( pcb, page );
				startAddress = getStartAddress( page );
			}
			// Get offset
			int offset = logicalAddress % MMU.FRAME_SIZE;
			// Relay data
			return super.read( startAddress % offset );
		} else { // If address is in temp buffer
			return super.read( logicalAddress - tempBufferLogicalAddress + getTempBufferStartAddress() );
		}
	}

	private int getStartAddress( int pageNumber ) {
		for ( int i = 0; i < this.containedPages.length; i++ ) {
			if ( this.containedPages[i] == pageNumber ) {
				return i * 4;
			}
		}
		return -1;
	}

	private int getTempBufferStartAddress() {
		return this.getCapacity() - this.TEMP_BUFF_SIZE;
	}

	public synchronized void loadPage( PCB pcb, int pageNumber ) throws MMU.PageFault {
		Word[] page = this.mmu.getPage( pcb, pageNumber );
		int startAddress = ++this.oldestPage % this.containedPages.length;
		this.containedPages[startAddress] = pageNumber;
		for ( int i = 0; i < page.length; i++ ) {
			super.write( startAddress + i, page[i] );
		}
	}

	public int getWritablePagesCount() {
		return this.containedPages.length;
	}
}
