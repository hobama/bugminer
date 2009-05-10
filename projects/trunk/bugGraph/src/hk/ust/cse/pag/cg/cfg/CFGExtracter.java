package hk.ust.cse.pag.cg.cfg;

import hk.ust.cse.pag.cg.util.Const;
import hk.ust.cse.pag.cg.util.HashCount;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.Edge;

/**
 * Extract CFG
 * 
 * @author hunkim
 * 
 */
public class CFGExtracter {

	HashCount<Node> uniqueNode = new HashCount<Node>();
	HashCount<Connection> uniqueEdge = new HashCount<Connection>();

	void extract(CFG cfg) throws IOException {
		// draw node
		Iterator<BasicBlock> bbIterator = cfg.blockIterator();

		while (bbIterator.hasNext()) {
			BasicBlock bb = bbIterator.next();
			Node node = new Node(bb);

			uniqueNode.increase(node);
		}

		Iterator<Edge> edgeIterator = cfg.edgeIterator();
		while (edgeIterator.hasNext()) {
			Edge edge = edgeIterator.next();
			Node src = new Node(edge.getSource());
			Node target = new Node(edge.getTarget());
			Connection conn = new Connection(src, target);
			uniqueEdge.increase(conn);
		}
	}

	void listNodes() {
		for (Node node : uniqueNode.getAscendingKeyList()) {
			System.out.println(node + " count: " + uniqueNode.getCount(node));
		}

		for (Connection conn : uniqueEdge.getAscendingKeyList()) {
			System.out.println(conn + " count: " + uniqueEdge.getCount(conn));
		}
	}

	void extract(InputStream classFis, String className) throws IOException,
			CFGBuilderException {
		List<CFG> list = CFGUtil.getCFGList(classFis, className);
		for (CFG cfg : list) {
			extract(cfg);
		}
	}

	/**
	 * Get Class in a jar file
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 * @throws CFGBuilderException
	 */
	public void extractInJarFile(File in) throws IOException,
			CFGBuilderException {
		// jar file
		JarFile inJar = new JarFile(in);

		// get all jar entries
		Enumeration<JarEntry> entries = inJar.entries();
		while (entries.hasMoreElements()) {
			// get a jar entry and input stream
			JarEntry entry = entries.nextElement();
			InputStream inJarStream = inJar.getInputStream(entry);

			if (entry.isDirectory()) {
				// do nothing for directory
			} else if (entry.getName().endsWith(".class")) {
				// System.out.println("Workong on " + entry.getName());
				extract(inJarStream, entry.getName());
				// a jar file inside jar?
			} else if (entry.getName().endsWith(".jar")) {
				// getMethodsInJarFile(in);
			}

			// close the inJar stream
			inJarStream.close();
		}

		inJar.close();
	}

	/**
	 * Show the combined CFG see the trends
	 * 
	 * @param xgmlFile
	 * @throws IOException
	 */
	public void toXGML(File xgmlFile) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(xgmlFile));
		CFG2GraphXML.toGraphXML(uniqueNode, uniqueEdge, bw);
		bw.close();
	}
	
	/**
     * return all Nodes
     */
	public HashCount<Node> getNodes()
	{
	    return uniqueNode;
	}
	
	/**
     * return all Connections
     */
	public HashCount<Connection> getConnections()
	{
	    return uniqueEdge;
	}

	/**
	 * It draws combined CFG from a give jar
	 * 
	 * @param args
	 * @throws IOException
	 * @throws CFGBuilderException
	 */
	public static void main(String[] args) throws CFGBuilderException,
			IOException {

		File jarFile = new File(Const.CHECKOUT_DIR
				+ "/columba-svn/jar_repos/columba_r353.jar");

		File xgmlFile = new File(jarFile.getParentFile(), jarFile.getName()
				+ ".xgml");

		CFGExtracter extracter = new CFGExtracter();
		extracter.extractInJarFile(jarFile);
		extracter.listNodes();
		extracter.toXGML(xgmlFile);
	}
}
