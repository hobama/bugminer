package hk.ust.cse.pag.cg.building;

import hk.ust.cse.pag.cg.util.FileUtil;
import hk.ust.cse.pag.cg.util.ScriptRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;

/**
 * Get the working directory location and parse the log Based on the log, check
 * out each revision and build and store the jar files
 * 
 * @author hunkim
 * 
 */
public class Builder {
	final String LOGFILE = "log.xml";
	final String WORKSPACE = "workspace";
	final String JARREPOS = "jar_repos";

	private File workingDir;
	List<SVNLog> logList;
	String svnRepos;
	private boolean useCheckout = true;

	public Builder(String svnRepos, File workingDir) throws IOException,
			SAXException {
		this.workingDir = workingDir;
		SVNLogParser parser = new SVNLogParser();
		logList = parser.parse(new File(workingDir, LOGFILE));
		this.svnRepos = svnRepos;
	}

	public void builder() throws Exception {
		for (int i = 0; i < logList.size(); i++) {
			SVNLog log = logList.get(i);
			System.out.println("working in " + log.revision);
			if (i == 0 || useCheckout) {
				checkOut(log.getRevision());
			} else {
				update(log.getRevision());
			}

			builder(log);
		}
	}

	public void checkOut(int revision) throws Exception {
		FileUtil workspaceFile = new FileUtil(workingDir, WORKSPACE);
		String workspace = workspaceFile.getAbsolutePath();

		System.out.println("Deleting " + workspaceFile + " ...");
		workspaceFile.deleteDir();

		ScriptRunner runner = new ScriptRunner("svn");
		runner.addArgument("co");
		runner.addArgument("-r " + revision);
		runner.addArgument(svnRepos);
		runner.addArgument(workspace);
		runner.run();
	}

	/**
	 * Build a jar
	 * 
	 * @param log
	 * @throws Exception
	 */
	private void builder(SVNLog log) throws Exception {
		ScriptRunner runner;
		String workspace = new File(workingDir, WORKSPACE).getAbsolutePath();
		// run script
		String scriptName = new File(workingDir, "builder").getAbsolutePath();

		runner = new ScriptRunner(scriptName);
		runner.addArgument(workspace);
		runner.addArgument("" + log.getRevision());
		runner.setStdOut(System.out);
		runner.setStdErr(System.err);
		runner.run();
	}

	private String update(int revision) throws Exception {
		String workspace = new File(workingDir, WORKSPACE).getAbsolutePath();

		// run SVN
		ScriptRunner runner = new ScriptRunner("svn");
		runner.addArgument("up");
		runner.addArgument("-r " + revision);
		runner.addArgument(workspace);
		runner.setStdOut(System.out);
		runner.setStdErr(System.err);
		runner.run();
		return workspace;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("usage: builder svnRepos dirname");
			return;
		}

		Builder builder = new Builder(args[0], new File(args[1]));
		builder.builder();
	}

}
