package hk.ust.cse.pag.cg.cfg;

import java.io.File;
import java.io.IOException;

import edu.umd.cs.findbugs.ba.CFGBuilderException;

/**
 * 
 * @author hunkim
 * 
 */
public class JarToCombindedCFG {
	/**
	 * It draws combined CFG from a give jar
	 * 
	 * @param args
	 * @throws IOException
	 * @throws CFGBuilderException
	 */
	public static void main(String[] args) throws CFGBuilderException,
			IOException {

		if (args.length != 1) {
			System.err.println("give me <dir>");
			return;
		}

		File jarFile = new File(
				"/home/hunkim/checkouts/columba-svn/jar_repos/columba_r353.jar");

		File dir = new File(args[0]);
		File xgmlFile = new File(dir, "all_cfg.xgml");

		if (!dir.exists() || !dir.isDirectory()) {
			System.err.println(dir + " does not exist or not a directory!");
			return;
		}

		CFGExtracter extracter = new CFGExtracter();

		File[] subFiles = dir.listFiles();
		if (subFiles == null) {
			return;
		}

		for (int i = 0; i < Math.min(subFiles.length, Integer.MAX_VALUE); i++) {
			if (subFiles[i].isFile() && subFiles[i].getName().endsWith(".jar")) {
				System.out.println("Jar file: " + subFiles[i]);
				// TODO: we can feed more than one jar here
				// Does it make sense to add multiple jars
				extracter.extractInJarFile(subFiles[i]);
			}
		}

		extracter.listNodes();
		extracter.toXGML(xgmlFile);
	}
}
