package hk.ust.cse.pag.cg.tests;

import hk.ust.cse.pag.cg.cfg.CFG2GraphXML;
import hk.ust.cse.pag.cg.cfg.CFGDiff;
import hk.ust.cse.pag.cg.cfg.CFGDiffResult;
import hk.ust.cse.pag.cg.cfg.CFGUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import junit.framework.TestCase;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.CFGBuilderException;

public class CFGDiffTest extends TestCase {
	private static final String TEST_CLASS = "bin/hk/ust/cse/pag/cg/cfg/CFGExtracter.class";
	private static final String TEST2_CLASS = "bin/hk/ust/cse/pag/cg/cfg/CFGExtracter2.class";

	public void testSameCFG() throws CFGBuilderException, IOException {
		List<CFG> list = CFGUtil.getCFGList(new File(TEST_CLASS));
		System.out.println("--- phase 1 ---- ");
		for (CFG cfg : list) {
			System.out.println("Testing: " + cfg.getMethodName());
			CFGDiffResult result = CFGDiff.diff(cfg, cfg);
			assertSame(0, result.getChangedNodeCount());
		}

		System.out.println("--- phase 2 ---- ");
		for (int i = 1; i < list.size(); i++) {
			CFG prev = list.get(i - 1);
			CFG cfg = list.get(i);
			System.out.println("Testing: " + prev.getMethodName() + " <-> "
					+ cfg.getMethodName());
			CFGDiffResult result = CFGDiff.diff(prev, cfg);
			assertNotSame(0, result.getChangedNodeCount());
		}
	}

	public void testSameCFGList() throws CFGBuilderException, IOException {
		List<CFG> list = CFGUtil.getCFGList(new File(TEST_CLASS));

		List<CFGDiffResult> resultList = CFGDiff.diff(list, list);
		int diffCount = 0;
		for (CFGDiffResult result : resultList) {
			diffCount += result.getChangedNodeCount();
		}
		assertSame(0, diffCount);
	}

	public void testTwoCFGList() throws CFGBuilderException, IOException {
		List<CFG> list1 = CFGUtil.getCFGList(new File(TEST_CLASS));
		List<CFG> list2 = CFGUtil.getCFGList(new File(TEST2_CLASS));

		List<CFGDiffResult> resultList = CFGDiff.diff(list1, list2);
		int diffCount = 0;
		for (CFGDiffResult result : resultList) {
			diffCount += result.getChangedNodeCount();
			if (result.getChangedNodeCount() != 0
					&& result.getPrevColoredNodes().size() != 0) {
				System.out.println("Difference method: "
						+ result.getMethodName() + " diff: "
						+ result.getChangedNodeCount());
				PrintStream ps = new PrintStream(new FileOutputStream(
						"diff.xgml"));
				CFG2GraphXML.toGraphXML(result, ps, null, false, null);
				ps.close();
			}
		}
		assertNotSame(0, diffCount);
	}

	public void testCFGEmptyRemove() throws CFGBuilderException, IOException {
		List<CFG> list = CFGUtil.getCFGList(new File(TEST_CLASS));

		for (CFG cfg : list) {
			if (cfg.getMethodName().startsWith("main(")) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(
						"cfg.xgml"));
				CFG2GraphXML.toGraphXML(cfg, bw);
				bw.close();

				bw = new BufferedWriter(new FileWriter("cfg2.xgml"));
				CFG2GraphXML.toGraphXML(CFGUtil.removeEmptyBlocks(cfg), bw);
				bw.close();
			}
		}
	}
}
