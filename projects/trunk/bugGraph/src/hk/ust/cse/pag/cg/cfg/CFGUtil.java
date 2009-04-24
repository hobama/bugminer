package hk.ust.cse.pag.cg.cfg;

import hk.ust.cse.pag.cg.jar.ClassRep;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.BetterCFGBuilder2;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilder;
import edu.umd.cs.findbugs.ba.CFGBuilderException;
import edu.umd.cs.findbugs.ba.Edge;

/**
 * CFG related util
 * 
 * @author hunkim
 * 
 */
public class CFGUtil {
	/**
	 * Get CFG from a given file
	 * 
	 * @param classFile
	 * @return
	 * @throws IOException
	 * @throws CFGBuilderException
	 */
	public static List<CFG> getCFGList(File classFile) throws IOException,
			CFGBuilderException {
		InputStream is = new FileInputStream(classFile);
		List<CFG> list = getCFGList(is, classFile.getName());
		is.close();
		return list;
	}

	/**
	 * Get CFGs from ClassRep
	 * 
	 * @param rep
	 * @return
	 * @throws IOException
	 * @throws CFGBuilderException
	 */
	public static List<CFG> getCFGList(ClassRep rep) throws IOException,
			CFGBuilderException {
		InputStream is = new ByteArrayInputStream(rep.getBody());
		List<CFG> list = getCFGList(is, rep.getName());
		is.close();
		return list;
	}

	/**
	 * Get CFG list from stream
	 * 
	 * @param classFis
	 * @return
	 * @throws IOException
	 * @throws CFGBuilderException
	 */
	public static List<CFG> getCFGList(InputStream classFis, String className)
			throws IOException, CFGBuilderException {
		List<CFG> list = new ArrayList<CFG>();

		ClassParser cp = new ClassParser(classFis, className);

		if (cp == null) {
			System.err.println("Class does not exist");
			return null;
		}
		JavaClass aClass = cp.parse();

		for (Method m : aClass.getMethods()) {
			try {
				list.add(getCFG(aClass, m));
			} catch (Exception e) {
				System.err.println(e);
			}
		}

		return list;
	}

	static String removeReturnType(String signature) {
		int index = signature.indexOf(')');
		if (index == -1) { // no return type
			return signature;
		}

		return signature.substring(0, index + 1);
	}

	/**
	 * Cloning
	 * 
	 * @param bb
	 * @return
	 */

	private static BasicBlock _cloneBB(BasicBlock bb) {
		BasicBlock newBB = new BasicBlock();

		// We need more things to copy
		Iterator<InstructionHandle> instIterator = bb.instructionIterator();
		while (instIterator.hasNext()) {
			InstructionHandle handle = instIterator.next();

			newBB.addInstruction(handle);
		}

		newBB.setLabel(bb.getLabel());

		return newBB;
	}

	/**
	 * Create BB only once
	 * 
	 * @param clonedBBTable
	 * @param source
	 * @return
	 */
	private static BasicBlock cloneBB(
			Hashtable<Integer, BasicBlock> clonedBBTable, BasicBlock bb) {
		BasicBlock newBB = clonedBBTable.get(bb.getLabel());
		if (newBB != null) {
			return newBB;
		}
		newBB = _cloneBB(bb);
		clonedBBTable.put(bb.getLabel(), newBB);

		return newBB;
	}

	/**
	 * Remove empty BBs
	 * 
	 * FindBugs newCFG.createEdge changed the label of original BBs. It was
	 * really difficult to find this problem.
	 * 
	 * Solution: we need to clone this BBs
	 * 
	 * @param cfg
	 * @return
	 */
	public static CFG removeEmptyBlocks(CFG cfg) {
		boolean hasEmtyBB = false;

		// create a new BB
		CFG newCFG = new CFG();
		newCFG.setMethodName(cfg.getMethodName());

		Iterator<Edge> edgeIterator = cfg.edgeIterator();

		Hashtable<Integer, BasicBlock> clonedBBTable = new Hashtable<Integer, BasicBlock>();

		// walk through edge
		while (edgeIterator.hasNext()) {
			Edge edge = edgeIterator.next();

			BasicBlock source = cloneBB(clonedBBTable, edge.getSource());
			BasicBlock target = cloneBB(clonedBBTable, edge.getTarget());

			// add normal edge
			if (!source.isEmpty() && !target.isEmpty()) {
				newCFG.createEdge(source, target);
				addVertex(newCFG, source);
				addVertex(newCFG, target);
			}

			// if the edge's target is empty?
			if (target.isEmpty()) {
				hasEmtyBB = true;

				// get all BBs that points to empty
				// need to use original BB to find correct connections
				List<BasicBlock> bbList = geTargetBBList(cfg, edge.getTarget());
				if (bbList.size() == 0) {
					continue;
				}

				addVertex(newCFG, source);

				for (BasicBlock newTarget : bbList) {
					if (false) { // debugging message
						System.out.println("edge: "
								+ edge.getSource().getLabel() + " : "
								+ edge.getTarget().getLabel() + " new target: "
								+ newTarget.getLabel());
					}
					BasicBlock clonedBB = cloneBB(clonedBBTable, newTarget);

					newCFG.createEdge(source, clonedBB);
					addVertex(newCFG, clonedBB);
				}
			}
		}

		// recursively remove empty BBs
		if (hasEmtyBB) {
			return removeEmptyBlocks(newCFG);
		}

		return newCFG;
	}

	/**
	 * return if it includes any empty edge
	 * 
	 * @param cfg
	 * @param bb
	 * @return
	 */
	private static void addVertex(CFG cfg, BasicBlock bb) {
		if (!cfg.containsVertex(bb)) {
			cfg.addVertex(bb);
		}
	}

	private static List<BasicBlock> geTargetBBList(CFG cfg, BasicBlock bb) {
		List<BasicBlock> bbList = new ArrayList<BasicBlock>();

		Iterator<Edge> edgeIterator = cfg.edgeIterator();
		while (edgeIterator.hasNext()) {
			Edge edge = edgeIterator.next();
			if (bb.getLabel() == edge.getSource().getLabel()) {
				bbList.add(edge.getTarget());
			}
		}

		return bbList;
	}

	/**
	 * Get CFG from a given method
	 * 
	 * @param aClass
	 * @param m
	 * @return
	 * @throws CFGBuilderException
	 */
	public static CFG getCFG(JavaClass aClass, Method m)
			throws CFGBuilderException {
		MethodGen g = new MethodGen(m, m.getName(), new ConstantPoolGen(aClass
				.getConstantPool()));
		CFGBuilder cfgbuilder = new BetterCFGBuilder2(g);

		cfgbuilder.build();
		CFG cfg = cfgbuilder.getCFG();
		cfg.setMethodName(m.getName() + removeReturnType(m.getSignature()));

		// return cfg;

		// remove empty blocks
		return removeEmptyBlocks(cfg);
	}
}
