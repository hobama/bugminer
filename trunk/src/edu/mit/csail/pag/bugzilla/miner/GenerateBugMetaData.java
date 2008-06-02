package edu.mit.csail.pag.bugzilla.miner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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

import au.com.bytecode.opencsv.CSVWriter;

import edu.mit.csail.pag.bugzilla.util.HashCount;

public class GenerateBugMetaData {
	private static final int KEYWORD_MIN_COUNT = 100;

	List<String> metaDataKeyList = null;

	HashCount stemmedKeywords = new HashCount();

	List<String> keywordList = new ArrayList<String>();

	public void fillData(File XMLFile, CSVWriter writer) throws JDOMException,
			IOException {

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

			List<String> valueList = bData.toCSVStringList();

			for (int i = 0; i < Math.min(keywordList.size(), KEYWORD_MIN_COUNT); i++) {
				String key = keywordList.get(i);
				if (keywordSet.contains(key)) {
					valueList.add("1");
				} else {
					valueList.add("0");
				}
			}

			// write the result to CSV
			String valueArray[] = new String[valueList.size()];
			writer.writeNext(valueList.toArray(valueArray));
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
			stemmedKeywords.addAll(bData.getStemmedShortDescWordSet());
		}
	}

	public void fillDataFromDir(String dirName, String csvOut)
			throws IOException {
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

		keywordList = stemmedKeywords.getDescendingKeyList();
		System.err.println("We have " + keywordList.size() + " !");

		// initiate CSVWriter and print heads
		CSVWriter writer = new CSVWriter(new FileWriter(csvOut));
		List<String> csvHead = new ArrayList<String>();
		for (String head : BugzillaData.getHeads()) {
			csvHead.add(head);
		}

		for (int i = 0; i < Math.min(keywordList.size(), KEYWORD_MIN_COUNT); i++) {
			String key = keywordList.get(i);
			csvHead.add("k_" + key + "(" + stemmedKeywords.getCount(key) + ")");
		}

		String csvHeadArray[] = new String[csvHead.size()];
		writer.writeNext(csvHead.toArray(csvHeadArray));

		for (File xmlFile : XMLFileList) {
			if (xmlFile.getName().endsWith(".xml")) {
				try {
					fillData(xmlFile, writer);
				} catch (JDOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		writer.close();
	}

	public static void main(String args[]) throws JDOMException, IOException {
		if (args.length != 2) {
			System.err.println("Usage: program <bug-xml-dir> <out.csv>");
			return;
		}
		new GenerateBugMetaData().fillDataFromDir(args[0], args[1]);
	}
}
