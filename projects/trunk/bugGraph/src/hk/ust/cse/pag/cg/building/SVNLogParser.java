package hk.ust.cse.pag.cg.building;

import hk.ust.cse.pag.cg.building.findfix.ArgoUMLFindFix;
import hk.ust.cse.pag.cg.building.findfix.ColumbaFindFix;
import hk.ust.cse.pag.cg.building.findfix.IFindFix;
import hk.ust.cse.pag.cg.building.findfix.JEditFindFix;
import hk.ust.cse.pag.cg.building.findfix.ScarabFindFix;
import hk.ust.cse.pag.cg.util.Const;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class SVNLogParser {
	public List<SVNLog> parse(File xmlFile) throws IOException, SAXException {
		Digester digester = new Digester();
		ArrayList<SVNLog> r = new ArrayList<SVNLog>();
		digester.push(r);

		digester.addObjectCreate("*/logentry", SVNLog.class);
		digester.addSetProperties("*/logentry");
		digester.addBeanPropertySetter("*/logentry/author");
		digester.addBeanPropertySetter("*/logentry/msg");
		digester.addBeanPropertySetter("*/logentry/resolution", "status");

		// do List add
		digester.addSetNext("*/logentry", "add");

		digester.parse(xmlFile);

		// sort them
		// Collections.sort(r);

		return r;
	}

	public void findFixRevisions(File checkOutDir, String jarName,
			IFindFix findFox) throws Exception {

		File fixOut = new File(checkOutDir, "fix_rev.txt");
		PrintStream ps = new PrintStream(new FileOutputStream(fixOut));
		File logXML = new File(checkOutDir, "log.xml");
		List<SVNLog> logList = parse(logXML);

		int count = 0;
		int missing = 0;
		for (int i = 0; i < logList.size(); i++) {
			SVNLog log = logList.get(i);
			if (i > 0 && findFox.findBugIds(log.getMsg()) != null) {
				SVNLog prevLog = logList.get(i - 1);
				File oldJar = new File(checkOutDir, "jar_repos/" + jarName
						+ "_r" + prevLog.getRevision() + ".jar");
				File newJar = new File(checkOutDir, "jar_repos/" + jarName
						+ "_r" + log.getRevision() + ".jar");

				count++;
				String bang = "!";
				if (oldJar.exists() && newJar.exists()) {
					bang = " ";
				} else {
					missing++;
				}

				ps.println(bang + prevLog.getRevision() + ":"
						+ log.getRevision());

				ps.println("\t" + log.getMsg() + "\n");
			}
		}

		String summary = jarName + " Total " + count + " bug fixes. We miss "
				+ missing + " revisions.";

		System.out.println(summary);
		ps.println(summary);
		ps.close();
	}

	public static void main(String[] args) throws Exception {
		SVNLogParser parser = new SVNLogParser();
		parser.findFixRevisions(new File(Const.CHECKOUT_DIR + "/columba-svn"),
				"columba", new ColumbaFindFix());
		parser.findFixRevisions(new File(Const.CHECKOUT_DIR
				+ "/jedit"), "jedit", new JEditFindFix());
		parser.findFixRevisions(new File(Const.CHECKOUT_DIR
				+ "/scarab"), "scarab", new ScarabFindFix());
		parser.findFixRevisions(new File(Const.CHECKOUT_DIR
				+ "/argouml"), "argouml", new ArgoUMLFindFix());
	}

}
