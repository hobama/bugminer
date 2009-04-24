package hk.ust.cse.pag.cg.jar;

import hk.ust.cse.pag.cg.cfg.CFGDiffResult;
import hk.ust.cse.pag.cg.util.HashCount;

import java.util.ArrayList;
import java.util.List;

public class JarDiffResult {
	List<CFGDiffResult> changedCFGList = new ArrayList<CFGDiffResult>();
	List<CFGDiffResult> addedCFGList = new ArrayList<CFGDiffResult>();
	List<CFGDiffResult> removedCFGList = new ArrayList<CFGDiffResult>();

	public void updateCount(HashCount<String> bugCount,
			HashCount<String> fixCount) {
		// FIXME: we only consider changed ones
		for (CFGDiffResult cfgDiffResult : changedCFGList) {
			cfgDiffResult.updateCount(bugCount, fixCount);
		}
	}
}
