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
import java.util.Hashtable;

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
			System.out.println("is simple? enters simple");
			// save T# in the CFGDiffResult
			result.setTNumberBugCFG(bugCount);
			result.setTNumberFixCFG(fixCount);
			toGraphTxt(result.prevColoredNodes, "prev", "b", psBug,
					allNodeTable);
			toGraphTxt(result.newColoredNodes, "new", "f", psFix, allNodeTable);
			
			
		} else {
			// print head
			System.out.println("not simple");
			// save T# in the CFGDiffResult
			result.setTNumberBugCFG(bugCount);
			result.setTNumberFixCFG(fixCount);
			
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
// original codes with random id
	/*	Set<Integer> shownNodes = new HashSet<Integer>();
		
		for (Node n : nodes) {
			if (n.color == 0 && !shownNodes.contains(n.id)) { // if it is
				// diff
				Integer id = getNodeId(allNodeTable, n);
				String node = "v " + n.id + " " + id;
				
				ps.println(node);
				shownNodes.add(n.id);
				
			}
		}
   */
		

		
		
		Set<Integer> shownNodes = new HashSet<Integer>();
		
		// mapping the random node.id to consecutive index starting from 0
		Hashtable<Integer, Integer> idToIndex = new Hashtable<Integer, Integer>();
		//Hashtable<Integer, String>	hashNodes=new Hashtable<Integer, String>();
		
		Integer index=0;
		
		for (Node n : nodes) {
			if (n.color == 0 && !shownNodes.contains(n.id)) { // if it is
				// diff
				Integer id = getNodeId(allNodeTable, n);
				if (!idToIndex.containsKey(n.id)){
					idToIndex.put(n.id,index);
			
				}
				String node = "v " + index + " " + id;
				//hashNodes.put(n.id, node);
				ps.println(node);
				shownNodes.add(n.id);
				index++;
				
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
				Integer id = getNodeId(allNodeTable, n);
				Integer targetId = getNodeId(allNodeTable, target);				
				
			//	String edge1 = "e " + n.id + " " + target.id + " " + edgeValue;
				Integer nIndex=(Integer)idToIndex.get(n.id);
				Integer targetIndex=(Integer)idToIndex.get(target.id);
			
			//  change the edge format to use Hong's tool
				int edgeOrder=1;
				if (id>targetId) {edgeOrder=2;}				
				String edge = "e " + nIndex + " " + targetIndex + " " + edgeOrder;

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

/////////////original
/*
 * package hk.ust.cse.pag.cg.cfg;
 */


/* 

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

/*
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

/*
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
/*********
		if (isSimple) {
			toGraphTxt(result.prevColoredNodes, "prev", "b", psBug,
					allNodeTable);
			toGraphTxt(result.newColoredNodes, "new", "f", psFix, allNodeTable);
	**/

/*
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
		
		///////////////////original////////
		/*
		
				Set<Integer> shownNodes = new HashSet<Integer>();
		for (Node n : nodes) {
			if (n.color == 0 && !shownNodes.contains(n.id)) { // if it is
				// diff
				Integer id = getNodeId(allNodeTable, n);
				System.out.println("before write one line");
				String node = "v " + n.id + " " + id;
				
				ps.println(node);
				shownNodes.add(n.id);
				
			}
		}
		
		*/
		//////////////////////////////////

/*
		Set<Integer> shownNodes = new HashSet<Integer>();
		int newID=0;
		for (Node n : nodes) {
			if (n.color == 0 && !shownNodes.contains(n.id)) { // if it is
				// diff
				Integer id = getNodeId(allNodeTable, n);
				n.setID(newID);
				String node = "v " + n.id + " " + id;
				
				ps.println(node);
				shownNodes.add(n.id);
				newID++;
			}
		}
		///////////////////
/*
		Set<Integer> shownNodes = new HashSet<Integer>();
		int newID=0;
		for (Node n : nodes) {
			
			if (n.color == 0 && !shownNodes.contains(n.id)) 
			//if (n.color == 0 && !shownNodes.contains(n.label)){ // if it is
				// diff			
				Integer id = getNodeId(allNodeTable, n);
				n.setID(newID);
				//String node = "v " + n.id + " " + n.label;
				String node = "v " + newID+ " " + id;
				System.out.println("*****+"+node);
				ps.println(node);
				shownNodes.add(id);
				//shownNodes.add(n.label);
				newID++;
			}
		}
*/

/*
		// FIXME: this is hack. We should find out why we have redundant edges
		Set<String> shownEdges = new HashSet<String>();
		
		///////////
		for (Node n : nodes) {
			if (!shownNodes.contains(n.id)) {
				continue;
			}
			for (Node target : n.outgoing) {
				if (!shownNodes.contains(target.id)) {
					continue;
				}
				Integer sourceIdInTable = getNodeId(allNodeTable, n);
				Integer tagetIdInTable=getNodeId(allNodeTable, target);
	            int edgeLabel=	(sourceIdInTable > tagetIdInTable)? 2:1;	
				//String edge = "e " + n.id + " " + target.id + " " + diffColor;
	            String edge = "e " + n.id + " " + target.id + " " + edgeLabel;
	            ////// to get rid of redundant edge
	            String edgeReverse="e " + target.id + " " + n.id + " " + edgeLabel;
	            
	            int edgeReverseLabel=1;
	            if (edgeLabel==1)
	            {	
	            	edgeReverseLabel=2;
	            	
	            }
	            String edgeReverse1="e " + n.id + " " + target.id + " " + edgeReverseLabel;
	            String edgeReverseOther="e " + target.id + " " + n.id + " " + edgeReverseLabel;
	           // String edgeReverseOther1="e " + target.id + " " + n.id + " " + edgeReverseLabel;
	            
	            
	            //////
				if ((!shownEdges.contains(edge))&& (!shownEdges.contains(edgeReverse))&&(!shownEdges.contains(edgeReverseOther))&&(!shownEdges.contains(edgeReverse1))) {
					shownEdges.add(edge);
					ps.println(edge);
				}
			}
		}
		/////////////
/*
		for (Node n : nodes) {
			if (!shownNodes.contains(getNodeId(allNodeTable, n))) {
			//if (!shownNodes.contains(n.label)){
				continue;
			}
			for (Node target : n.outgoing) {
				if (!shownNodes.contains(getNodeId(allNodeTable, target))) {
					continue;
				}
				/*
				String edge = "e " + n.id + " " + target.id + " " + diffColor;
				*/
				// change edgeLabel to be 1 or 2 to use Hong's tool
				//Integer sourceIdInTable = getNodeId(allNodeTable, n);
				//Integer tagetIdInTable=getNodeId(allNodeTable, target);
	           // int edgeLabel=	(sourceIdInTable > tagetIdInTable)? 2:1;	
/*
	            int edgeLabel=	(n.label > target.label)? 2:1;	
	            //String edge = "e " + n.id + " " + target.id +" "+getNodeId(allNodeTable, n)+"-"+getNodeId(allNodeTable, target)+"; "+edgeLabel;
	           //String edge = "e " + n.id + " " + target.id +" "+edgeLabel;

	           String edge1 = "e " + n.id + " " + target.id +" "+edgeLabel; 

				//String edge = "e " + n.id + " " + target.id + " ;" + sourceIdInTable+"- "+ tagetIdInTable+"; "+edgeLabel;
                ///////////////////////////////////////////
				if (!shownEdges.contains(edge1)) {
					shownEdges.add(edge1);
					//ps.println(edge);
					ps.println(edge1);
				
				}
			}

		}
		
	}
	
	*/

/*
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
			System.out.println("*****"+node);
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
*/