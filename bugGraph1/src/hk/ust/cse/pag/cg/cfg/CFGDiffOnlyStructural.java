package hk.ust.cse.pag.cg.cfg;

import hk.ust.cse.pag.cg.jar.ClassRep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.Edge;

/**
 * Compute CFG diff
 * 
 * @author hunkim
 * 
 */
public class CFGDiffOnlyStructural {
	static HashSet<Node> getNodeSet(CFG cfg) {
		Hashtable<Integer, Node> table = new Hashtable<Integer, Node>();

		Iterator<BasicBlock> bbIterator = cfg.blockIterator();

		while (bbIterator.hasNext()) {
			BasicBlock bb = bbIterator.next();
			Node node = new Node(bb);
			table.put(bb.getLabel(), node);
		}

		Iterator<Edge> edgeIterator = cfg.edgeIterator();
		while (edgeIterator.hasNext()) {
			Edge edge = edgeIterator.next();
			Node srcNode = table.get(edge.getSource().getLabel());
			Node targetNode = table.get(edge.getTarget().getLabel());

			srcNode.addOutgoing(targetNode);
			targetNode.addIncoming(srcNode);
		}

		// Done, let's set a Set
		HashSet<Node> set = new HashSet<Node>(table.values());
		return set;
	}

	/**
	 * Find changed node and mark them and return the result
	 * 
	 * @param prev
	 * @param cfg
	 * @return
	 */
	public static CFGDiffResult diff(CFG prev, CFG cfg) {
		CFGDiffResult result = new CFGDiffResult();

		// bild pre node set
		HashSet<Node> prevNodes = getNodeSet(prev);
		HashSet<Node> newNodes = getNodeSet(cfg);

		result.changedNodeCount = 0;

		// iteration to changes in the previous
		for (Node node : prevNodes) {
			if (newNodes.contains(node)) {
				node.color = 1; // means the same
			} else {
				result.changedNodeCount++;
			}
		}

		// iteration to changes in the new
		// TODO: perhaps we can optimise this, but just make sure it works
		for (Node node : newNodes) {
			if (prevNodes.contains(node)) {
				node.color = 1; // means the same
			} else {
				result.changedNodeCount++;
			}
		}

		result.prevColoredNodes = prevNodes;
		result.newColoredNodes = newNodes;

		return result;
	}

	/**
	 * List are immutable. CFG.getMethodName() MUST be set!
	 * this method transmit class info for tracing back and explanation for the patterns
	 * 
	 * @param prevList
	 * @param newList
	 * @return
	 */
	public static List<CFGDiffResult> diff(List<CFG> prevList, List<CFG> newList, ClassRep preRep, ClassRep newRep) {
		List<CFGDiffResult> list = new ArrayList<CFGDiffResult>();

		// build hash table for fast search
		Hashtable<String, CFG> newTable = new Hashtable<String, CFG>();
		for (CFG cfg : newList) {
			newTable.put(cfg.getMethodName(), cfg);
		}

		// walk each prev
		for (CFG cfg : prevList) {
			CFGDiffResult result = new CFGDiffResult();

			CFG matching = newTable.get(cfg.getMethodName()); //find the matching CFG in the new 
			//table by given relative name in the prevList

			// prev one is removed
			if (matching == null) {
				//do nothing to removed cfg, because this method can only compare cfg change within
				//mapping cfgs
				//result = new CFGDiffResult();

				//result.prevColoredNodes = getNodeSet(cfg);
				//result.changedNodeCount = result.prevColoredNodes.size();
				//add info for tracing back which class the method belongs to
				//result.setPreClass(preRep);
			} else {
				result = diff(cfg, matching);
				//add infor for tracing back which class the method belongs to
				result.setPreClass(preRep);
				result.setNewClass(newRep);
				// remove this from table, so that we know what's the new one, which means additive from 
				//prev to newList
				newTable.remove(cfg.getMethodName());
			}

			result.setMethodName(cfg.getMethodName());
			list.add(result);
		}

		// let's check if any CFG is left in the table
		/*
		for (CFG cfg : newTable.values()) {
			// do nothing also for adding CFGs
			//CFGDiffResult result = new CFGDiffResult();
			//result.newColoredNodes = getNodeSet(cfg);
			//result.changedNodeCount = result.newColoredNodes.size();
			//result.setMethodName(cfg.getMethodName());
			//result.setNewClass(newRep);
			//list.add(result);
		}
		*/
		return list;
	}

	//original method, not called in this case:
	public static List<CFGDiffResult> diff(List<CFG> prevList, List<CFG> newList) {
		List<CFGDiffResult> list = new ArrayList<CFGDiffResult>();

		// build hash table for fast search
		Hashtable<String, CFG> newTable = new Hashtable<String, CFG>();
		for (CFG cfg : newList) {
			newTable.put(cfg.getMethodName(), cfg);
		}

		// walk each prev
		for (CFG cfg : prevList) {
			CFGDiffResult result = new CFGDiffResult();

			CFG matching = newTable.get(cfg.getMethodName()); //find the matching CFG in the new 
			//table by given relative name in the prevList

			// prev one is removed
			if (matching == null) {
				result = new CFGDiffResult();

				result.prevColoredNodes = getNodeSet(cfg);
				result.changedNodeCount = result.prevColoredNodes.size();
			} else {
				result = diff(cfg, matching);

				// remove this from table, so that we know what's the new one, which means additive from 
				//prev to newList
				newTable.remove(cfg.getMethodName());
			}

			result.setMethodName(cfg.getMethodName());
			list.add(result);
		}

		// let's check if any CFG is left in the table
		for (CFG cfg : newTable.values()) {
			CFGDiffResult result = new CFGDiffResult();
			result.newColoredNodes = getNodeSet(cfg);
			result.changedNodeCount = result.newColoredNodes.size();
			result.setMethodName(cfg.getMethodName());
			list.add(result);
		}

		return list;
	}

	public static List<CFGDiffResult> diff(ClassRep preRep, ClassRep newRep)
			throws CFGBuilderException, IOException {
		List<CFG> prevList = CFGUtil.getCFGList(preRep);
		List<CFG> newList = CFGUtil.getCFGList(newRep);

		return diff(prevList, newList, preRep, newRep);
	}

}

