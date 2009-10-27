package hk.ust.cse.pag.cg.cfg;

import hk.ust.cse.pag.cg.util.Const;
import hk.ust.cse.pag.cg.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * Read results from Hong and draw graphs to see the results
 * 
 * @author hunkim
 * 
 */
public class SubgraphToXML {
	NodeTable nTable;
	File checkOutDir;

	/**
	 * Constructor. Need checkOutDir which includes graph_node.txt and
	 * suggraph.txt
	 * 
	 * @param checkOutDir
	 * @throws IOException
	 */
	public SubgraphToXML(String checkOutDirName) throws IOException {
		this.checkOutDir = new File(checkOutDirName);

		// build graphNodeTable from file
		nTable = new NodeTable(checkOutDirName);
	}

	

	public void toXGML(String fileName) throws IOException {
		File file = new File(checkOutDir, fileName);
		PrintStream ps = new PrintStream(new FileOutputStream(new File(file
				.getAbsolutePath()
				+ ".xgml")));
		ps.println(CFG2GraphXML.head());

		BufferedReader br = new BufferedReader(new FileReader(file));
		String graphId = "";

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
				int nodeId = Integer.parseInt(splits[2]);
				ps.println(getNodeSection(graphId + "-" + id, nodeId));
			}

			if (line.startsWith("e")) {
				ps.println(getEdgeSection(graphId + "-" + splits[1], graphId
						+ "-" + splits[2]));
			}

			if (line.startsWith("t")) {
				graphId = splits[1];
			}
		}
		br.close();

		ps.println(CFG2GraphXML.tail());

		ps.close();
	}

	private String getNodeSection(String id, int label) {
		String nodeName = nTable.get(label);

		String node = "<section name=\"node\">\n";
		node += "  <attribute key=\"id\" type=\"String\">" + id
				+ "</attribute>\n";
		node += "  <attribute key=\"label\" type=\"String\">" + "(" + label
				+ ")" + nodeName.replaceAll(":", "\n") + "</attribute>\n";
		node += "  <section name=\"graphics\">\n";
		node += "    <attribute key=\"w\" type=\"double\">" + 100
				+ "</attribute>\n";
		node += "    <attribute key=\"h\" type=\"double\">" + 50
				+ "</attribute>\n";
		node += "    <attribute key=\"type\" type=\"String\">rectangle3d</attribute>\n";
		node += "    <attribute key=\"fill\" type=\"String\">#FFCC00</attribute>\n";
		node += "    <attribute key=\"outline\" type=\"String\">#000000</attribute>\n";
		node += "  </section>\n";
		node += "</section>\n";

		return node;
	}

	private String getEdgeSection(String source, String target) {
		String edge = "<section name=\"edge\">\n";
		edge += "<attribute key=\"source\" type=\"String\">" + source
				+ "</attribute>\n";
		edge += "<attribute key=\"target\" type=\"String\">" + target
				+ "</attribute>\n";
		edge += "<section name=\"graphics\">\n";
		edge += "<attribute key=\"fill\" type=\"String\">#000000</attribute>\n";
		edge += "<attribute key=\"targetArrow\" type=\"String\">standard</attribute>\n";
		edge += "  </section>\n";
		edge += "</section>\n";

		return edge;
	}

	/**
	 * Test
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		SubgraphToXML subraph = new SubgraphToXML(Const.CHECKOUT_DIR + "/columba-svn");
		subraph.toXGML("subgraph.txt");

		subraph = new SubgraphToXML(Const.CHECKOUT_DIR + "/columba-svn");
		subraph.toXGML("subgraph.txt");
	}
}
