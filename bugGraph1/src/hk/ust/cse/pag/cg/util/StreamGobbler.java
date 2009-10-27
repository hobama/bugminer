package hk.ust.cse.pag.cg.util;

/**
 * Taken, with minor formatting and visibility changes, from
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html, written by
 * Michael C. Daconta.
 * 
 * Actually, I also added in the optional printing...in case you don't actually
 * want to have the extra output.
 */
import java.io.*;

public class StreamGobbler extends Thread {
	InputStream in;
	String type;
	PrintStream out;
	
	public StreamGobbler(InputStream in, PrintStream out) {
		this(in, "", out);
	}

	public StreamGobbler(InputStream in, String type, PrintStream out) {
		this.in = in;
		this.type = type;
		this.out = out;
	}

	public void run() {
		try {
			InputStreamReader in_r = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(in_r);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (out != null) {
					out.println(type + line);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}

