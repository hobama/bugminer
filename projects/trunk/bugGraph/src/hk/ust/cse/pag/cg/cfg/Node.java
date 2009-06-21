package hk.ust.cse.pag.cg.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.bcel.generic.InstructionHandle;

import edu.umd.cs.findbugs.ba.BasicBlock;

/**
 * Represent node for simplified basic block
 * 
 * @author hunkim
 * 
 */
public class Node implements Comparable<Node> {
	int id; // from basic block
	int label; // from basic block

	String name;
	int color;
	List<Node> incoming = new ArrayList<Node>();
	List<Node> outgoing = new ArrayList<Node>();

	void addIncoming(Node node) {
		incoming.add(node);
	}

	void addOutgoing(Node node) {
		outgoing.add(node);
	}

	/**
	 * Change bb to node
	 * 
	 * @param bb
	 * @return
	 */
	public Node(BasicBlock bb) {
		if (bb == null) {
			return;
		}
		id = bb.getId();
		label = bb.getLabel();

		String instStr = "";
		Iterator<InstructionHandle> instIterator = bb.instructionIterator();
		while (instIterator.hasNext()) {
			InstructionHandle iHandle = instIterator.next();
			// instStr += iHandle.getInstruction().toString() + "\n";
			// more abstract
			instStr += iHandle.getInstruction().getName() + ":";
		}

		name = instStr;
	}

	String toString(List<Node> list) {
		Collections.sort(list);
		String ret = "";
		for (Node node : list) {
			ret += node.name + "-";
		}

		return ret;
	}

	@Override
	public String toString() {
		return name + "\n  in(" + toString(incoming) + ")\n  out("
				+ toString(outgoing) + ")";
	}

	boolean edgeSet(Set<Node> s1, Set<Node> s2) {
		if (s1.size() != s2.size()) {
			return false;
		}

		for (Node node : s1) {
			if (!s2.contains(node)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			Node node = (Node) obj;

			if (node == null) {
				return false;
			}

			return toString().equals(node.toString());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * We order node by name
	 */
	public int compareTo(Node o) {
		return name.compareTo(o.name);
	}
	public void setID(int id){
		this.id=id;		
	}
}
