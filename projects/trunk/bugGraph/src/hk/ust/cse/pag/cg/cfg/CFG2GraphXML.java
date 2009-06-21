package hk.ust.cse.pag.cg.cfg;

import hk.ust.cse.pag.cg.util.HashCount;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.bcel.generic.InstructionHandle;

import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.Edge;

/**
 * Print control flow graph as graph XML
 * 
 * @author hunkim
 * 
 */
public class CFG2GraphXML {
	private static final int MIN_OCCURS = 10;
	private static int count;
	private static int bugCount;
	private static int fixCount;

	public static String head() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<section name=\"xgml\">\n"
				+ "<attribute key=\"Creator\" type=\"String\">yFiles</attribute>\n"
				+ "<attribute key=\"Version\" type=\"String\">2.6</attribute>\n"
				+ "<section name=\"graph\">\n"
				+ "<attribute key=\"hierarchic\" type=\"int\">1</attribute>\n"
				+ "<attribute key=\"label\" type=\"String\"></attribute>\n"
				+ "<attribute key=\"directed\" type=\"int\">1</attribute>\n";

	}

	public static String getNodeSection(BasicBlock bb) {
		double width = 150;
		double height = 30;
		String instStr = "";
		Iterator<InstructionHandle> instIterator = bb.instructionIterator();
		while (instIterator.hasNext()) {
			InstructionHandle iHandle = instIterator.next();
			instStr += iHandle.getInstruction().toString() + "\n";
			height += 30;
		}

		String node = "<section name=\"node\">\n";
		node += "  <attribute key=\"id\" type=\"String\">" + bb.getLabel()
				+ "</attribute>\n";
		node += "  <attribute key=\"label\" type=\"String\">" + bb.isEmpty()
				+ bb.getLabel() + " " + instStr + "</attribute>\n";
		node += "  <section name=\"graphics\">\n";
		node += "    <attribute key=\"w\" type=\"double\">" + width
				+ "</attribute>\n";
		node += "    <attribute key=\"h\" type=\"double\">" + height
				+ "</attribute>\n";
		node += "    <attribute key=\"type\" type=\"String\">rectangle3d</attribute>\n";
		node += "    <attribute key=\"fill\" type=\"String\">#FFCC00</attribute>\n";
		node += "    <attribute key=\"outline\" type=\"String\">#000000</attribute>\n";
		node += "  </section>\n";
		node += "</section>\n";

		return node;
	}

	private static CharSequence getEdgeSection(Connection e, int count) {
		String edge = "<section name=\"edge\">\n";
		edge += "<attribute key=\"source\" type=\"String\">" + e.source
				+ "</attribute>\n";
		edge += "<attribute key=\"target\" type=\"String\">" + e.target
				+ "</attribute>\n";
		edge += "  <attribute key=\"label\" type=\"String\">" + count
				+ "</attribute>\n";
		edge += "<section name=\"graphics\">\n";
		// edge += "<attribute key=\"width\" type=\"double\">" + width +
		// "</attribute>\n";
		edge += "<attribute key=\"fill\" type=\"String\">#000000</attribute>\n";
		edge += "<attribute key=\"targetArrow\" type=\"String\">standard</attribute>\n";
		edge += "  </section>\n";
		edge += "</section>\n";

		return edge;
	}

	private static CharSequence getNodeSection(Node n, int count) {
		if (count < MIN_OCCURS) {
			return "";
		}

		int width = 100;
		int height = 50;

		String node = "<section name=\"node\">\n";
		node += "  <attribute key=\"id\" type=\"String\">" + n.name
				+ "</attribute>\n";
		node += "  <attribute key=\"label\" type=\"String\">" + n + "(" + count
				+ ")" + "</attribute>\n";
		node += "  <section name=\"graphics\">\n";
		node += "    <attribute key=\"w\" type=\"double\">" + width
				+ "</attribute>\n";
		node += "    <attribute key=\"h\" type=\"double\">" + height
				+ "</attribute>\n";
		node += "    <attribute key=\"type\" type=\"String\">rectangle3d</attribute>\n";
		node += "    <attribute key=\"fill\" type=\"String\">#FFCC00</attribute>\n";
		node += "    <attribute key=\"outline\" type=\"String\">#000000</attribute>\n";
		node += "  </section>\n";
		node += "</section>\n";

		return node;
	}

	private static String getEdgeSection(Edge e) {
		String edge = "<section name=\"edge\">\n";
		edge += "<attribute key=\"source\" type=\"String\">"
				+ e.getSource().getLabel() + "</attribute>\n";
		edge += "<attribute key=\"target\" type=\"String\">"
				+ e.getTarget().getLabel() + "</attribute>\n";
		edge += "  <attribute key=\"label\" type=\"String\">" + e.getLabel()
				+ "</attribute>\n";
		edge += "<section name=\"graphics\">\n";
		// edge += "<attribute key=\"width\" type=\"double\">" + width +
		// "</attribute>\n";
		edge += "<attribute key=\"fill\" type=\"String\">#000000</attribute>\n";
		edge += "<attribute key=\"targetArrow\" type=\"String\">standard</attribute>\n";
		edge += "  </section>\n";
		edge += "</section>\n";

		return edge;
	}

	public static String tail() {
		return "  </section>\n</section>\n";
	}

	public static void toGraphXML(CFG cfg, BufferedWriter ps)
			throws IOException {
		// print head
		ps.append(head());
		ps.newLine();

		// draw node
		Iterator<BasicBlock> bbIterator = cfg.blockIterator();

		while (bbIterator.hasNext()) {
			BasicBlock bb = bbIterator.next();
			ps.append(getNodeSection(bb));
			ps.newLine();
		}

		Iterator<Edge> edgeIterator = cfg.edgeIterator();
		while (edgeIterator.hasNext()) {
			Edge edge = edgeIterator.next();
			ps.append(getEdgeSection(edge));
			ps.newLine();
		}

		// print tail
		ps.append(tail());
		ps.newLine();
	}

	public static void toGraphXML(HashCount<Node> nodes,
			HashCount<Connection> edges, BufferedWriter ps) throws IOException {
		// print head
		ps.append(head());
		ps.newLine();

		for (Node node : nodes.getKeyList()) {
			ps.append(getNodeSection(node, nodes.getCount(node)));
			ps.newLine();
		}

		for (Connection edge : edges.getKeyList()) {
			ps.append(getEdgeSection(edge, edges.getCount(edge)));
			ps.newLine();
		}

		// print tail
		ps.append(tail());
		ps.newLine();
	}

	/**
	 * Show diffs
	 * 
	 * @param result
	 * @param edges
	 * @param ps
	 * @param allNodeTable
	 * @throws IOException
	 */
	public static void toGraphXML(CFGDiffResult result, PrintStream psBug,
			PrintStream psFix, boolean isSimple,
			Hashtable<String, Integer> allNodeTable) throws IOException {

		if (psFix == null) {
			psFix = psBug;
		}

		System.out.println(result.methodName + " prev:"
				+ result.prevColoredNodes.size() + " new: "
				+ result.newColoredNodes.size() + " changed: "
				+ result.changedNodeCount);

		// no change to show
		if (result.changedNodeCount == 0) {
			return;
		}

		if (isSimple) {
			toGraphTxt(result.prevColoredNodes, "prev", "b", psBug,
					allNodeTable);
			toGraphTxt(result.newColoredNodes, "new", "f", psFix, allNodeTable);
		} else {
			// print head
			psBug.println(head());
			toGraphXML(result.prevColoredNodes, "prev", "#FF0000", psBug);
			toGraphXML(result.newColoredNodes, "new", "#00FF00", psBug);
			// print tail
			psBug.println(tail());
		}

	}

	private static void toGraphTxt(Set<Node> nodes, String prefix,
			String diffColor, PrintStream ps,
			Hashtable<String, Integer> allNodeTable) throws IOException {

		int coloredNodeCount = 0;
		for (Node n : nodes) {
			if (n.color == 0) {
				coloredNodeCount++;
			}
		}

		if (coloredNodeCount == 0) {
			return;
		}

		if (diffColor.equals("b")) {
			ps.println("t # " + bugCount++);
		} else {
			ps.println("t # " + fixCount++);
		}

		Set<Integer> shownNodes = new HashSet<Integer>();
		int i=0;
		for (Node n : nodes) {
			if (n.color == 0 && !shownNodes.contains(n.id)) { // if it is
				// diff				
				Integer id = getNodeId(allNodeTable, n);
				n.setID(i);
				String node = "v " + n.id + " " + id;
				ps.println(node);
				shownNodes.add(n.id);
				i++;
			}
		}

		// FIXME: this is hack. We should find out why we have redundant edges
		Set<String> shownEdges = new HashSet<String>();

		for (Node n : nodes) {
			if (!shownNodes.contains(n.id)) {
				continue;
			}
			for (Node target : n.outgoing) {
				if (!shownNodes.contains(target.id)) {
					continue;
				}

				String edge = "e " + n.id + " " + target.id + " " + diffColor;
				if (!shownEdges.contains(edge)) {
					shownEdges.add(edge);
					ps.println(edge);
				}
			}
		}
	}

	private static Integer getNodeId(Hashtable<String, Integer> allNodeTable,
			Node n) {
		Integer id = allNodeTable.get(n.name);
		if (id == null) {
			id = allNodeTable.size();
			allNodeTable.put(n.name, id);
		}
		return id;
	}

	private static void toGraphXML(Set<Node> nodes, String prefix,
			String diffColor, PrintStream ps) throws IOException {

		for (Node n : nodes) {
			int width = 100;
			int height = 50;

			String node = "<section name=\"node\">\n";
			node += "  <attribute key=\"id\" type=\"String\">" + n + prefix
					+ "</attribute>\n";
			node += "  <attribute key=\"label\" type=\"String\">" + n
					+ "</attribute>\n";
			node += "  <section name=\"graphics\">\n";
			node += "    <attribute key=\"w\" type=\"double\">" + width
					+ "</attribute>\n";
			node += "    <attribute key=\"h\" type=\"double\">" + height
					+ "</attribute>\n";
			node += "    <attribute key=\"type\" type=\"String\">rectangle3d</attribute>\n";
			node += "    <attribute key=\"fill\" type=\"String\">";

			if (n.color == 0) { // if it is diff
				node += diffColor;
			} else {
				node += "#FFCC00";
			}

			node += "</attribute>\n";
			node += "    <attribute key=\"outline\" type=\"String\">#000000</attribute>\n";
			node += "  </section>\n";
			node += "</section>\n";

			ps.println(node);

		}

		for (Node n : nodes) {
			for (Node target : n.outgoing) {
				String edge = "<section name=\"edge\">\n";
				edge += "<attribute key=\"source\" type=\"String\">" + n
						+ prefix + "</attribute>\n";
				edge += "<attribute key=\"target\" type=\"String\">" + target
						+ prefix + "</attribute>\n";
				edge += "<section name=\"graphics\">\n";
				// edge += "<attribute key=\"width\" type=\"double\">" + width +
				// "</attribute>\n";
				edge += "<attribute key=\"fill\" type=\"String\">#000000</attribute>\n";
				edge += "<attribute key=\"targetArrow\" type=\"String\">standard</attribute>\n";
				edge += "  </section>\n";
				edge += "</section>\n";
				ps.append(edge);
			}
		}
	}
}
