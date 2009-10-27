package hk.ust.cse.pag.cg.cfg;

import hk.ust.cse.pag.cg.util.Const;
import hk.ust.cse.pag.cg.util.HashCount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Count bugs for each node using buggy nodes from bug_graph.txt (labels) and
 * graph_node.txt
 * 
 * @author hunkim
 * 
 */
public class CountBuggyNodes {
	final static String NODE_BUG_COUNT = "node_bug_count.txt";
	final static String BUG_GRAPH = "bug_graph.txt";

	HashCount<Integer> nodeBugCount;

	private void countNodeLabels(String projectDir) throws IOException {
		nodeBugCount = new HashCount<Integer>();

		BufferedReader br = new BufferedReader(new FileReader(new File(
				projectDir, BUG_GRAPH)));

		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}

			String splits[] = line.split("\\W+");
			if (splits.length < 2) {
				continue;
			}

			if (line.startsWith("v")) {
				String id = splits[1];
				int nodeLabel = Integer.parseInt(splits[2]);
				nodeBugCount.increase(nodeLabel);
			}
		}
		br.close();
	}

	public CountBuggyNodes(String projectDir) throws IOException {
		// countNodeLabels
		countNodeLabels(projectDir);

		NodeTable nTable = new NodeTable(projectDir);

		// prepare output
		PrintStream ps = new PrintStream(new FileOutputStream(new File(
				projectDir, NODE_BUG_COUNT)));

		ps.println("LabelId\tnodeName\tBugCount");

		List<Integer> nodeList = nodeBugCount.getDescendingKeyList();
		for (int nodeLabel : nodeList) {
			ps.println(nodeLabel + "\t" + nTable.get(nodeLabel) + "\t" + nodeBugCount.getCount(nodeLabel));
		}

		ps.close();
	}
	
	public static void main(String[] args) throws IOException {
		new CountBuggyNodes(Const.CHECKOUT_DIR + "/argouml");
		new CountBuggyNodes(Const.CHECKOUT_DIR + "/columba-svn");
		new CountBuggyNodes(Const.CHECKOUT_DIR + "/jedit");
		new CountBuggyNodes(Const.CHECKOUT_DIR + "/scarab");
	}

}
