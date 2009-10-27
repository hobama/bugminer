package hk.ust.cse.pag.cg.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide UDparser interface. It will have propUDCMetrics per each method
 * 
 * @author hunkim
 * 
 */
public class ScriptRunner {
	String command;

	List<String> arguments = new ArrayList<String>();

	boolean verbose;

	PrintStream stdErr;

	private PrintStream stdOut;

	/**
	 * Set command for the script runner
	 * 
	 * @param command
	 */
	public ScriptRunner(String command) {
		this.command = command;
	}

	/**
	 * Add arguments
	 * 
	 * @param arg
	 */
	public void addArgument(String arg) {
		arguments.add(arg);
	}

	public int run() throws Exception {

		for (int i = 0; i < arguments.size(); i++) {
			command += " " + arguments.get(i).toString();
		}

		Process process = Runtime.getRuntime().exec(command);
		if (verbose) {
			System.out.println("Running " + command + " ...");
		}

		/*
		 * Taken from example on web:
		 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
		 */
		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(
				process.getErrorStream(), "Error: ", stdErr);

		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(process
				.getInputStream(), "Output: ", stdOut);

		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		int status = process.waitFor();
		// waitFor() should do the job, but it is not.
		// Without this, we sometimes have only partial logs
		while (errorGobbler.isAlive() || outputGobbler.isAlive())
			;

		process.destroy();

		if (verbose) {
			System.out.println("Running " + command + " done (" + status + ")");
		}

		return status;
	}

	/**
	 * @return Returns the arguments.
	 */
	public List<String> getArguments() {
		return arguments;
	}

	/**
	 * @param arguments
	 *            The arguments to set.
	 */
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	/**
	 * @return Returns the command.
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @return Returns the stdErr.
	 */
	public PrintStream getStdErr() {
		return stdErr;
	}

	/**
	 * @param stdErr
	 *            The stdErr to set.
	 */
	public void setStdErr(PrintStream stdErr) {
		this.stdErr = stdErr;
	}

	/**
	 * @return Returns the stdOut.
	 */
	public PrintStream getStdOut() {
		return stdOut;
	}

	/**
	 * @param stdOut
	 *            The stdOut to set.
	 */
	public void setStdOut(PrintStream stdOut) {
		this.stdOut = stdOut;
	}

	/**
	 * @return Returns the verbose.
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param verbose
	 *            The verbose to set.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}
