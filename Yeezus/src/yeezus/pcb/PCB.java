package yeezus.pcb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class PCB implements Map<Integer, Process> { // TODO Do it like this?

	public void addProcess( int pid, int startInstructionAddress, int endInstructionAddress,
			int startInputBufferAddress, int endInputBufferAddress, int startOutputBufferAddress,
			int endOutputBufferAddress, int startTempBufferAddress, int endTempBufferAddress ) {

	}

	@Override public int size() {
		return 0;
	}

	@Override public boolean isEmpty() {
		return false;
	}

	@Override public boolean containsKey( Object key ) {
		return false;
	}

	@Override public boolean containsValue( Object value ) {
		return false;
	}

	@Override public Process get( Object key ) {
		return null;
	}

	@Override public Process put( Integer key, Process value ) {
		return null;
	}

	@Override public Process remove( Object key ) {
		return null;
	}

	@Override public void putAll( Map<? extends Integer, ? extends Process> m ) {

	}

	@Override public void clear() {

	}

	@Override public Set<Integer> keySet() {
		return null;
	}

	@Override public Collection<Process> values() {
		return null;
	}

	@Override public Set<Entry<Integer, Process>> entrySet() {
		return null;
	}
}
