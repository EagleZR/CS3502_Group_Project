package yeezus.memory;

import java.util.ArrayList;

public class MMU {

	private Memory RAM;
	// int physicalAddresses[][];
	ArrayList<ArrayList<Integer>> physicalAddresses; // TODO Make this more efficient later

	public MMU( Memory RAM ) {
		this.RAM = RAM;
		this.physicalAddresses = new ArrayList<>();
	}

	public void recordAddress( int pid, int logicalAddress, int physicalAddress ) {
		ArrayList<Integer> processAddresses;
		try {
			processAddresses = this.physicalAddresses.get( pid );
		} catch ( IndexOutOfBoundsException e ) {
			processAddresses = new ArrayList<>();
			this.physicalAddresses.add( pid, processAddresses );
		}
		processAddresses.add( logicalAddress, physicalAddress );
	}

	public Word read( int physicalAddress ) throws InvalidAddressException {
		return this.RAM.read( physicalAddress );
	}

	public Word read( int pid, int logicalAddress ) throws InvalidAddressException {
		try {
			return this.RAM.read( this.physicalAddresses.get( pid ).get( logicalAddress ) );
		} catch ( IndexOutOfBoundsException | NullPointerException e ) {
			throw new InvalidAddressException( "The given logical address is not mapped to a physical address." );
		}
	}

	public void write( int physicalAddress, Word data ) throws InvalidAddressException {
		this.RAM.write( physicalAddress, data );
	}

	public void write( int pid, int logicalAddress, Word data ) throws InvalidAddressException {
		this.RAM.write( this.physicalAddresses.get( pid ).get( logicalAddress ), data );
	}
}
