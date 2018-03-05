package yeezus.cpu;

import yeezus.memory.InvalidAddressException;
import yeezus.memory.InvalidWordException;

public interface Executable {
	void execute() throws ExecutionException, InvalidAddressException, InvalidWordException;
}
