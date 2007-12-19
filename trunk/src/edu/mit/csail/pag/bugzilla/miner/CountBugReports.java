package edu.mit.csail.pag.bugzilla.miner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import edu.mit.csail.pag.bugzilla.util.HashCount;

public class CountBugReports {
	HashCount bugReportNumber = new HashCount();

	HashSet<String> interestingComponentSet = new HashSet<String>();

	String interestingComponets[] = { "Equinox.Bundles", "Equinox.Framework",
			"Equinox.Incubator", "Equinox.Website", "JDT.APT", "JDT.Core",
			"JDT.Debug", "JDT.Doc", "JDT.Text", "JDT.UI", "PDE.Build",
			"PDE.Doc", "PDE.UI", "Platform.Ant", "Platform.Compare",
			"Platform.CVS", "Platform.Debug", "Platform.Doc", "Platform.IDE",
			"Platform.Releng", "Platform.Resources", "Platform.Runtime",
			"Platform.Scripting", "Platform.Search", "Platform.SWT",
			"Platform.Team", "Platform.Text", "Platform.UI", "Platform.Update",
			"Platform.UserAssistance", "Platform.WebDAV", "Platform.Website" };

	public void fillData(File XMLFile) throws JDOMException, IOException {
		System.out.println("Working on: " + XMLFile);

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				XMLFile));
		InputSource is = new InputSource(
				new InputStreamReader(bis, "ISO8859_1"));
		is.setSystemId(XMLFile.getAbsolutePath());
		//document = builder.parse(is);

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);

		// Get the root element
		Element root = doc.getRootElement();

		// Get all bug Elements
		List<Element> bugElementList = root.getChildren("bug");

		for (Element bugElement : bugElementList) {
			BugzillaData bData = new BugzillaData();
			bData.parseXML(bugElement);

			if (bData.component != null && bData.component != null) {
				String compName = bData.product + "." + bData.component;
				if (interestingComponentSet.contains(compName)) {
					bugReportNumber.increase(compName);
				}
			}

			System.out.println(bData);
		}
	}

	public void fillDataFromDir(String dirName) {
		// fill interesting components
		for (String comp : interestingComponets) {
			interestingComponentSet.add(comp);
		}

		File dir = new File(dirName);
		if (!dir.isDirectory()) {
			return;
		}

		File[] XMLFileList = dir.listFiles();
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

		List<Object> keyList = bugReportNumber.getKeyList();
		for (Object key : keyList) {
			System.out.println(key + ": " + bugReportNumber.getCount(key));
		}
	}

	public static void main(String args[]) throws JDOMException, IOException {
		new CountBugReports().fillDataFromDir(args[0]);
	}
}
