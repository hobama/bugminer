package hk.ust.cse.pag.cg.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Common library
 * 
 * @author hunkim
 * 
 * @param <T>
 */
public class HashCount<T> {
	int totalCount = 0;
	Hashtable<T, Integer> hashtable = new Hashtable<T, Integer>();

	public int getTotalCount() {
		return totalCount;
	}

	public void increase(T key) {
		update(key, 1);
	}

	public void update(T key, int delta) {
		Integer integer = (Integer) hashtable.get(key);
		if (integer == null) {
			integer = Integer.valueOf("" + 0);
		}

		integer = Integer.valueOf("" + (integer.intValue() + delta));
		hashtable.put(key, integer);

		// update the total count
		totalCount += delta;
	}

	@SuppressWarnings("unchecked")
	/*
	 * @deprecated
	 */
	public void increaseWithNoSpaceKey(T key) {
		String noSpaceKey = key.toString().replace(' ', '_');
		increase((T) noSpaceKey);
	}

	public void decrease(T key) {
		update(key, -1);
	}

	public int getCount(T key) {
		Integer integer = (Integer) hashtable.get(key);
		if (integer == null)
			return 0;

		return integer.intValue();
	}

	/**
	 * Compute rate
	 * 
	 * @param key
	 * @return
	 */
	public double getRate(T key) {
		// FIXME" What to do for 0 or negative total count
		if (totalCount <= 0) {
			return -1;
		}

		return (double) getCount(key) * 100 / totalCount;
	}

	public List<T> getKeyList() {
		List<T> list = new ArrayList<T>();
		Enumeration<T> en = hashtable.keys();

		while (en.hasMoreElements()) {
			list.add(en.nextElement());
		}

		return list;
	}

	public List<T> getDescendingKeyList() {
		return getOrderedKeyList(false);
	}

	public List<T> getAscendingKeyList() {
		return getOrderedKeyList(true);
	}

	private List<T> getOrderedKeyList(boolean ascendingOrder) {
		List<CountItem> countItemList = new ArrayList<CountItem>();
		Enumeration<T> en = hashtable.keys();

		while (en.hasMoreElements()) {
			T key = en.nextElement();
			CountItem countItem = new CountItem(key, getCount(key));
			countItem.ascendingOrder = ascendingOrder;
			countItemList.add(countItem);
		}

		Collections.sort(countItemList);

		List<T> retList = new ArrayList<T>();

		for (CountItem countItem : countItemList) {
			retList.add(countItem.key);
		}

		return retList;
	}

	class CountItem implements Comparable<CountItem> {
		boolean ascendingOrder = true;

		public CountItem(T key, int count) {
			this.key = key;
			this.count = count;
		}

		T key;

		int count;

		public int compareTo(CountItem obj) {
			CountItem item = obj;
			if (ascendingOrder) {
				return count - item.count;
			}
			return item.count - count;
		}
	}

	// test
	public static void main(String args[]) {
		System.out.println("Hashcount test");

		HashCount<String> hashCount = new HashCount<String>();
		hashCount.increase("hunkim");
		hashCount.increase("pankai");
		hashCount.increase("hunkim");
		hashCount.increase("pankai");
		hashCount.increase("ejw");
		hashCount.increase("pankai");

		List<String> keys = hashCount.getKeyList();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			System.out.println("Ket: " + key + " count: "
					+ hashCount.getCount(key));
		}

		System.out.println("asc ---------------");
		keys = hashCount.getAscendingKeyList();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			System.out.println("Ket: " + key + " count: "
					+ hashCount.getCount(key));
		}

		System.out.println("desc ---------------");
		keys = hashCount.getDescendingKeyList();
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			System.out.println("Ket: " + key + " count: "
					+ hashCount.getCount(key));
		}
	}
}
