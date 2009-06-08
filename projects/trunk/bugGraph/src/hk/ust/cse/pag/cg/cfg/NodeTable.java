package hk.ust.cse.pag.cg.cfg;

import hk.ust.cse.pag.cg.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Build node table from graph_node.txt
 * 
 * @author hunkim
 * 
 */
public class NodeTable {
	Hashtable<Integer, String> graphNodeTable;

	/**
	 * Build node table 
	 * @param projectDir
	 * @throws IOException
	 */
	public NodeTable(String projectDir) throws IOException {
		graphNodeTable = new Hashtable<Integer, String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(
				projectDir, "graph_node.txt")));
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}

			String splits[] = line.split("\\W+", 2);
			if (splits.length == 2 && Util.isAllDigit(splits[0])) {
				graphNodeTable.put(Integer.parseInt(splits[0]), splits[1]);
			}

		}
		br.close();
	}

	public String get(int label) {
		if (graphNodeTable == null) {
			return null;
		}

		return graphNodeTable.get(label);
	}
}
