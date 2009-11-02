package hk.ust.cse.pag.cg.jar;

import hk.ust.cse.pag.cg.cfg.CFG2GraphXML;
import hk.ust.cse.pag.cg.cfg.CFGDiff;
import hk.ust.cse.pag.cg.cfg.CFGDiffResult;
import hk.ust.cse.pag.cg.util.Const;
import hk.ust.cse.pag.cg.util.HashCount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import edu.umd.cs.findbugs.ba.CFGBuilderException;

/**
 * This program reads all jars in the directory and generate graph for Hong's
 * tool.
 * 
 * @author hunkim
 * 
 */
public class JarDiffMain {
	JarDiffResult getDiff(File prevJar, File newJar) throws IOException,
			CFGBuilderException {
		JarDiffResult result = new JarDiffResult();

		Hashtable<String, ClassRep> prevClassTable = getClassList(prevJar);
		Hashtable<String, ClassRep> newClassTable = getClassList(newJar);

		for (ClassRep rep : prevClassTable.values()) {
			ClassRep newRep = newClassTable.get(rep.name);

			// removed one
			if (newRep == null) {
				// TODO: need to deal with removed ones
				continue;
			}

			// they are changed
			if (!rep.equals(newRep)) {
				System.out.println("Diff class: " + rep.getName());
				List<CFGDiffResult> resultList = CFGDiff.diff(rep, newRep);
				result.changedCFGList.addAll(resultList);
			}

			// remove the same one from new table
			newClassTable.remove(rep.getName());
		}

		// added ones
		for (ClassRep rep : newClassTable.values()) {
			// added one
			// TODO: deal with rep
		}

		return result;
	}

	Hashtable<String, ClassRep> getClassList(File jar) throws IOException {

		Hashtable<String, ClassRep> repList = new Hashtable<String, ClassRep>();

		// jar file
		JarFile inJar = new JarFile(jar);

		// get all jar entries
		Enumeration<JarEntry> entries = inJar.entries();
		while (entries.hasMoreElements()) {
			// get a jar entry and input stream
			JarEntry entry = entries.nextElement();

			InputStream inJarStream = inJar.getInputStream(entry);

			if (entry.isDirectory()) {
				// do nothing for directory
			} else if (entry.getName().endsWith(".class")) {
				if (entry.getSize() > Integer.MAX_VALUE) {
					// File is too large
					continue;
				}

				ClassRep rep = new ClassRep();
				rep.name = entry.getName();
				rep.body = getByte(entry, inJarStream);
				repList.put(rep.name, rep);

				// a jar file inside jar?
			} else if (entry.getName().endsWith(".jar")) {

			}

			// close the inJar stream
			inJarStream.close();
		}

		inJar.close();
		return repList;
	}

	private byte[] getByte(JarEntry entry, InputStream is) throws IOException {
		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) entry.getSize()];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ entry.getName());
		}

		return bytes;
	}

	private static void getJarDiffGraph(
			Hashtable<String, Integer> allNodeTable, File checkOutDir,
			String jarName) throws FileNotFoundException, IOException,
			CFGBuilderException {
		File fixRevFile = new File(checkOutDir, "fix_rev.txt");
		String jarPrefix = checkOutDir.getAbsolutePath() + File.separator
				+ "jar_repos" + File.separator + jarName + "_r";

		System.out.println(jarPrefix);

		File fixGFile = new File(checkOutDir, "fix_graph.txt");
		File bugGFile = new File(checkOutDir, "bug_graph.txt");
		File nodeGFile = new File(checkOutDir, "graph_node.txt");

		PrintStream psBug = new PrintStream(new FileOutputStream(bugGFile));
		PrintStream psFix = new PrintStream(new FileOutputStream(fixGFile));
		PrintStream psNode = new PrintStream(new FileOutputStream(nodeGFile));

		// bug and fix count
		HashCount<String> bugCount = new HashCount<String>();
		HashCount<String> fixCount = new HashCount<String>();

		BufferedReader br = new BufferedReader(new FileReader(fixRevFile));
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}

			// starts with empty space
			if (!line.startsWith(" ")) {
				continue;
			}

			String splits[] = line.trim().split(":");
			if (splits.length != 2) {
				continue;
			}

			File prevJar = new File(jarPrefix + splits[0] + ".jar");
			File newJar = new File(jarPrefix + splits[1] + ".jar");

			if (!prevJar.exists() || !newJar.exists()) {
				System.out.println("the file does not exist");
				continue;
			}

			System.out.println("Working on " + prevJar + " and " + newJar);
			JarDiffMain diff = new JarDiffMain();
			JarDiffResult result = diff.getDiff(prevJar, newJar);
			
			for (CFGDiffResult cfgDiffResult : result.changedCFGList) {
				CFG2GraphXML.toGraphXML(cfgDiffResult, psBug, psFix, true,
						allNodeTable);

				// update bug and fix counts
				cfgDiffResult.updateCount(bugCount, fixCount);
			}
			
		}
		
		for (String name : allNodeTable.keySet()) {
			psNode.println(allNodeTable.get(name) + " " + name);
		}
		
		psBug.close();
		psFix.close();
		psNode.close();

		br.close();
	}

	/**
	 * This is the main. Get bug and fix graphs from given two revisions
	 * 
	 * @param args
	 * @throws IOException
	 * @throws CFGBuilderException
	 */
	public static void main(String[] args) throws IOException,
			CFGBuilderException {

		Hashtable<String, Integer> allNodeTable = new Hashtable<String, Integer>();

		// We have to run all of them together
		// so that we capture the graph type correctly.
		//getJarDiffGraph(allNodeTable, new File(Const.CHECKOUT_DIR
		//		+ "/columba-svn"), "columba");
		//getJarDiffGraph(allNodeTable, new File(Const.CHECKOUT_DIR + "/jedit"),
		//		"jedit");

		//getJarDiffGraph(allNodeTable, new File(Const.CHECKOUT_DIR + "/scarab"),
		//		"scarab");

		getJarDiffGraph(allNodeTable,
				new File(Const.CHECKOUT_DIR + "/argouml"), "argouml");
	}
}