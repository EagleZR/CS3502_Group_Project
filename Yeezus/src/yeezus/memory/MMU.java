package yeezus.memory;

import java.util.ArrayList;

public class MMU {

	private ArrayList<ArrayList<Integer>> addressMap; // TODO Make this more efficient later
	private int[] addresses;
	private Memory RAM;

	public MMU( Memory RAM ) {
		this.RAM = RAM;
		this.addresses = new int[RAM.getCapacity()];
		this.addressMap = new ArrayList<>();
	}

	public void mapAddress( int pid, int logicalAddress ) throws InvalidAddressException {
		for ( int i = 0; i < this.addresses.length; i++ ) {
			if ( this.addresses[i] == 0 ) {
				mapAddress( pid, logicalAddress, i );
			}
		}
		throw new InvalidAddressException( "Not really invalid, we've just run out of memory..." );
	}

	private synchronized void mapAddress( int pid, int logicalAddress, int physicalAddress ) {
		ArrayList<Integer> processAddresses;
		try {
			processAddresses = this.addressMap.get( pid );
		} catch ( IndexOutOfBoundsException e ) {
			processAddresses = new ArrayList<>();
			this.addressMap.add( pid, processAddresses );
		}
		this.addresses[physicalAddress] = logicalAddress;
		processAddresses.add( logicalAddress, physicalAddress ); // TODO Unmap memory on process completion
	}

	public synchronized Word read( int physicalAddress ) throws InvalidAddressException {
		return this.RAM.read( physicalAddress );
	}

	public synchronized Word read( int pid, int logicalAddress ) throws InvalidAddressException {
		try {
			return this.RAM.read( this.addressMap.get( pid ).get( logicalAddress ) );
		} catch ( IndexOutOfBoundsException | NullPointerException e ) {
			throw new InvalidAddressException( "The given logical address is not mapped to a physical address." );
		}
	}

	public synchronized void write( int physicalAddress, Word data ) throws InvalidAddressException {
		this.RAM.write( physicalAddress, data );
	}

	public synchronized void write( int pid, int logicalAddress, Word data ) throws InvalidAddressException {
		this.RAM.write( this.addressMap.get( pid ).get( logicalAddress ), data );
	}

	public synchronized void terminatePID( int pid ) {
		try {
			this.addressMap.remove( pid );
		} catch ( IndexOutOfBoundsException e ) {
			// Do nothing, it's already been removed, so we're good
		}
	}
}
