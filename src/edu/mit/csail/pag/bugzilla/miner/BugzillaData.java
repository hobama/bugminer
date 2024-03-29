package edu.mit.csail.pag.bugzilla.miner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.jdom.Element;
import org.tartarus.snowball.ext.englishStemmer;

import weka.core.Stopwords;

public class BugzillaData {
	long id;

	String product;

	String component;

	Date reportedDate;

	boolean isFixed;

	Hashtable<String, String> metaData = new Hashtable<String, String>();

	private static String interestingKeys[] = { "bug_id", "assigned_to",
			"attachment", "bug_severity", "bug_status", "cclist_accessible",
			"classification", "classification_id", "component", "creation_ts",
			"delta_ts", "everconfirmed",/* "long_desc", */
			"op_sys", "priority", "product", "rep_platform", "reporter",
			"reporter_accessible", "resolution", "target_milestone", "version",
	/* "short_desc" */};

	public void parseXML(Element bugElement) {
		Element bugIdElement = bugElement.getChild("bug_id");
		id = Long.parseLong(bugIdElement.getTextTrim());
		product = bugElement.getChildTextTrim("product");
		component = bugElement.getChildTextTrim("component");
		List<Element> elments = bugElement.getChildren();
		for (Element one_elem : elments) {
			metaData.put(one_elem.getName(), one_elem.getTextTrim());
		}
	}

	public static String[] getHeads() {
		return interestingKeys;
	}

	public List<String> toCSVStringList() {
		List<String> ret = new ArrayList<String>();

		for (String key : interestingKeys) {
			String value = metaData.get(key);
			if (value == null) {
				value = "[null]";
			}
			ret.add(value);
		}

		return ret;
	}

	public String getShortDesc() {
		return metaData.get("short_desc");
	}

	public HashSet<String> getShortDescWordSet() {
		HashSet<String> retSet = new HashSet<String>();
		String shortDesc = getShortDesc();
		if (shortDesc == null) {
			return retSet;
		}

		for (String key : shortDesc.split("\\W")) {
			if (key.length() > 1 && !Character.isDigit(key.charAt(0))) {
				retSet.add(key.toLowerCase());
			}
		}

		return retSet;
	}

	/**
	 * Return stemmed words
	 * 
	 * @return
	 */
	public HashSet<String> getStemmedShortDescWordSet() {
		HashSet<String> retSet = new HashSet<String>();
		HashSet<String> workingSet = getShortDescWordSet();
		englishStemmer stemmer = new englishStemmer();
		for (String word : workingSet) {
			stemmer.setCurrent(word);
			stemmer.stem();
			stemmer.stem();

			// remove all stop words
			word = stemmer.getCurrent();
			if (!Stopwords.isStopword(word)) {
				retSet.add(word);
			}
		}

		return retSet;
	}

	public String getLongDesc() {
		return metaData.get("long_desc");
	}

	public List<String> getKeys() {
		List<String> keyList = new ArrayList<String>();
		Enumeration<String> en = metaData.keys();
		while (en.hasMoreElements()) {
			keyList.add(en.nextElement());
		}

		Collections.sort(keyList);
		return keyList;
	}

	public String getMetaValue(String metaKey) {
		return metaData.get(metaKey);
	}

	public String toString() {
		return id + ": " + product + "." + component;
	}
}
