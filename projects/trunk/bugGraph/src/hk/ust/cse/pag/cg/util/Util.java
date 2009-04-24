package hk.ust.cse.pag.cg.util;

public class Util {

	static public boolean isAllDigit(String str) {
		if (str == null || str.length() == 0)
			return false;

		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i)))
				return false;
		}
		return true;
	}

}
