package yeezus.cpu;

/**
 * A {@link Runnable}-like interface that allows for exceptions.
 */
public interface Executable {
	/**
	 * Executes the actions associated with an instance of this class.
	 *
	 * @throws Exception An exception resulting from the execution of this instance.
	 */
	void execute() throws Exception;
}
