package edu.mit.csail.pag.bugzilla.miner;

import java.util.Date;

import org.jdom.Element;

public class BugzillaData {
	long id;
	String product;
	String component;
	Date reportedDate;
	boolean isFixed;

	public void parseXML(Element bugElement) {
		Element bugIdElement = bugElement.getChild("bug_id");
		id = Long.parseLong(bugIdElement.getTextTrim());

		Element productElement = bugElement.getChild("product");
		if (productElement == null) {
			product = null;
		} else {
			product = productElement.getTextTrim();
		}

		Element componentElement = bugElement.getChild("component");
		if (componentElement == null) {
			component = null;
		} else {
			component = componentElement.getTextTrim();
		}
	}

	public String toString() {
		return id + ": " + product + "." + component;
	}
}
