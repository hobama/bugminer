package edu.mit.csail.pag.bugzilla.miner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import edu.mit.csail.pag.bugzilla.util.HashCount;

public class GenerateBugMetaData {
	List<String> metaDataKeyList = null;
	HashSet<String> keywords = new HashSet<String>();
	List<String> keywordList = new ArrayList<String>();

	public void fillData(File XMLFile) throws JDOMException, IOException {

		System.err.println("Working on: " + XMLFile);

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				XMLFile));
		InputSource is = new InputSource(
				new InputStreamReader(bis, "ISO8859_1"));
		is.setSystemId(XMLFile.getAbsolutePath());
		// document = builder.parse(is);

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);

		// Get the root element
		Element root = doc.getRootElement();

		// Get all bug Elements
		List<Element> bugElementList = root.getChildren("bug");

		for (Element bugElement : bugElementList) {
			BugzillaData bData = new BugzillaData();
			bData.parseXML(bugElement);

			Set<String> keywordSet = bData.getShortDescWordSet();
			System.out.print(bData.toCSVString());
			for (String key : keywordList) {
				if (keywordSet.contains(key)) {
					System.out.print("1, ");
				} else {
					System.out.print("0, ");
				}
			}			
			System.out.println(bData.getShortDesc());
		}		
	}

	public void fillKeywords(File XMLFile) throws JDOMException, IOException {
		System.err.println("Collecting keywords from " + XMLFile);

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				XMLFile));
		InputSource is = new InputSource(
				new InputStreamReader(bis, "ISO8859_1"));
		is.setSystemId(XMLFile.getAbsolutePath());
		// document = builder.parse(is);

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);

		// Get the root element
		Element root = doc.getRootElement();

		// Get all bug Elements
		List<Element> bugElementList = root.getChildren("bug");

		// First iteration, collect only keywords
		for (Element bugElement : bugElementList) {
			BugzillaData bData = new BugzillaData();
			bData.parseXML(bugElement);
			keywords.addAll(bData.getShortDescWordSet());
		}		
	}

	public void fillDataFromDir(String dirName) {
		File dir = new File(dirName);
		if (!dir.isDirectory()) {
			return;
		}

		File[] XMLFileList = dir.listFiles();
		for (File xmlFile : XMLFileList) {
			if (xmlFile.getName().endsWith(".xml")) {
				try {
					fillKeywords(xmlFile);
				} catch (JDOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		keywordList.addAll(keywords);
		System.err.println("We have " + keywordList.size() + " keys!");

		System.out.print(BugzillaData.toCSVHeadString());
		Collections.sort(keywordList);
	
		for (String key : keywordList) {
			System.out.print(key + ", ");
		}
		
		System.out.println();

		for (File xmlFile : XMLFileList) {
			if (xmlFile.getName().endsWith(".xml")) {
				try {
					fillData(xmlFile);
				} catch (JDOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String args[]) throws JDOMException, IOException {
		new GenerateBugMetaData().fillDataFromDir(args[0]);
	}
}
