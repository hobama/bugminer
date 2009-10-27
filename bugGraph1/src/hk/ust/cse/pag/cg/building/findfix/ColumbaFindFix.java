package hk.ust.cse.pag.cg.building.findfix;

public class ColumbaFindFix implements IFindFix {

	public FixInfo findBugIds(String log) {
		if (log == null) {
			return null;
		}

		if (log.contains("[bug") || log.contains("[fix")) {
			FixInfo info = new FixInfo();
			info.isFix = true;
			return info;
		}

		return null;
	}

}
