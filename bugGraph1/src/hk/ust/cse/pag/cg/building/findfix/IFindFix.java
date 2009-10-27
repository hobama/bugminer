package hk.ust.cse.pag.cg.building.findfix;

public interface IFindFix {
	/**
	 * Return null if given log is not a bug fix log
	 * @param log
	 * @return
	 */
	FixInfo findBugIds(String log);
}
