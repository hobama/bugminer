package hk.ust.cse.pag.cg.building.findfix;

import hk.ust.cse.pag.cg.util.Util;

public class ArgoUMLFindFix implements IFindFix {

	private boolean isBugNum(String log) {
		String tokens[] = log.split("\\s+");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (token.equals("#") && i < tokens.length - 1) {
				if (Util.isAllDigit(tokens[i + 1])) {
					return true;
				}
			}

			if (token.startsWith("#") && token.length() > 1) {
				if (Util.isAllDigit(token.substring(1))) {
					return true;
				}
			}
		}

		return false;
	}

	public FixInfo findBugIds(String log) {
		if (log == null) {
			return null;
		}

		if (log.contains("bug") || log.contains("fix") || isBugNum(log)) {
			FixInfo info = new FixInfo();
			info.isFix = true;
			return info;
		}

		return null;
	}

	public static void main(String[] args) {
		String logs[] = { "Fixed #123 234", " bug ", " haha", " # 1234",
				" I don't know" };
		ArgoUMLFindFix findFix = new ArgoUMLFindFix();
		for (String log : logs) {
			System.out.println(log + ":" + findFix.findBugIds(log));
		}
	}

}
