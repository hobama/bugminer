package hk.ust.cse.pag.cg.cfg;

import hk.ust.cse.pag.cg.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.ArrayList;

/**
 * Build node table from graph_node.txt
 * 
 * @author hunkim
 * 
 */
public class NodeTable {
	Hashtable<Integer, String> graphNodeTable;
	ArrayList<Integer> indexedKeyArrayList; //indexed hashKey in this list

	/**
	 * Build node table 
	 * @param projectDir
	 * @throws IOException
	 */
	public NodeTable(String projectDir) throws IOException {
		Hashtable<Integer, String> graphNodeHashTable;
		indexedKeyArrayList=new ArrayList<Integer>();
		int index=0;
		graphNodeHashTable = new Hashtable<Integer, String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(
				projectDir, "graph_node.txt")));
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}

			String splits[] = line.split("\\W+", 2);
			if (splits.length == 2 && Util.isAllDigit(splits[0])) {
				graphNodeHashTable.put(Integer.parseInt(splits[0]), splits[1]);
				// build index-hashKey arrayList
				indexedKeyArrayList.add(index, Integer.parseInt(splits[0]));
				index++;
			}

		}
		br.close();
		
		//build graphNodeTable		
		graphNodeTable = new Hashtable<Integer, String>();
		for (int i=0;i<graphNodeHashTable.size();i++){
			graphNodeTable.put(i, graphNodeHashTable.get(indexedKeyArrayList.get(i)));
		}
	}

	

	public String get(int label) {
		if (graphNodeTable == null) {
			return null;
		}
		return graphNodeTable.get(label);
	}
}
