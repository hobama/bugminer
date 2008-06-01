package edu.mit.csail.pag.bugzilla.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class HashCount {
	Hashtable hashtable = new Hashtable();

	public void increase(Object key) {
		update(key, 1);
	}

	public void addAll(Set set) {
		for (Object obj : set) {
			increase(obj);
		}
	}

	public void update(Object key, int delta) {
		Integer integer = (Integer) hashtable.get(key);
		if (integer == null) {
			integer = Integer.valueOf("" + 0);
		}

		integer = Integer.valueOf("" + (integer.intValue() + delta));
		hashtable.put(key, integer);
	}

	public void increaseWithNoSpaceKey(String key) {
		String noSpaceKey = key.replace(' ', '_');
		increase(noSpaceKey);
	}

	public void decrease(Object key) {
		update(key, -1);
	}

	public int getCount(Object key) {
		Integer integer = (Integer) hashtable.get(key);
		if (integer == null)
			return 0;

		return integer.intValue();
	}

	public List getKeyList() {
		List list = new ArrayList();
		Enumeration en = hashtable.keys();

		while (en.hasMoreElements()) {
			list.add(en.nextElement());
		}

		return list;
	}

	public List getDescendingKeyList() {
		return getOrderedKeyList(false);
	}

	public List getAscendingKeyList() {
		return getOrderedKeyList(true);
	}

	private List getOrderedKeyList(boolean ascendingOrder) {
		List list = new ArrayList();
		Enumeration en = hashtable.keys();

		while (en.hasMoreElements()) {
			Object key = en.nextElement();
			CountItem countItem = new CountItem(key, getCount(key));
			countItem.ascendingOrder = ascendingOrder;
			list.add(countItem);
		}

		Collections.sort(list);

		List retList = new ArrayList();

		for (int i = 0; i < list.size(); i++) {
			CountItem countItem = (CountItem) list.get(i);
			retList.add(countItem.key);

		}

		return retList;
	}

	class CountItem implements Comparable {
		boolean ascendingOrder = true;

		public CountItem(Object key, int count) {
			this.key = key;
			this.count = count;
		}

		Object key;

		int count;

		public int compareTo(Object obj) {
			CountItem item = (CountItem) obj;
			if (ascendingOrder) {
				return count - item.count;
			}
			return item.count - count;
		}
	}

	// test
	public static void main(String args[]) {
		System.out.println("Hashcount test");

		HashCount hashCount = new HashCount();
		hashCount.increase("hunkim");
		hashCount.increase("pankai");
		hashCount.increase("hunkim");
		hashCount.increase("pankai");
		hashCount.increase("ejw");
		hashCount.increase("pankai");

		List keys = hashCount.getKeyList();
		for (int i = 0; i < keys.size(); i++) {
			Object key = keys.get(i);
			System.out.println("Ket: " + key + " count: "
					+ hashCount.getCount(key));
		}

		System.out.println("asc ---------------");
		keys = hashCount.getAscendingKeyList();
		for (int i = 0; i < keys.size(); i++) {
			Object key = keys.get(i);
			System.out.println("Ket: " + key + " count: "
					+ hashCount.getCount(key));
		}

		System.out.println("desc ---------------");
		keys = hashCount.getDescendingKeyList();
		for (int i = 0; i < keys.size(); i++) {
			Object key = keys.get(i);
			System.out.println("Ket: " + key + " count: "
					+ hashCount.getCount(key));
		}
	}
}
