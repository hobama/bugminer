package hk.ust.cse.pag.cg.building.eclipse2ant;

import hk.ust.cse.pag.cg.util.Const;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

public class Classpath2Ant {
	ArrayList<Entry> entryList = new ArrayList<Entry>();

	public List<Entry> parse(File xmlFile) throws IOException, SAXException {
		Digester digester = new Digester();
		digester.push(entryList);

		digester.addObjectCreate("*/classpathentry", Entry.class);
		digester.addSetProperties("*/classpathentry");

		// do List add
		digester.addSetNext("*/classpathentry", "add");

		digester.parse(xmlFile);

		return entryList;
	}

	public void toBuildXML(PrintStream ps) {

		ps.println("<project name=\"gen\" default=\"compile\" basedir=\".\">");

		ps.println("<target name=\"init\">");
		ps.println("<mkdir dir=\"bin\" />");
		ps.println("</target>");

		ps.println("<path id=\"classpath\">");
		ps.println("<fileset dir=\"lib\">\n"
				+ "<include name=\"**/*.jar\" />\n" + "</fileset>");

		ps.println("<pathelement path=\"bin\" />");

		for (Entry entry : entryList) {
			if ("lib".equals(entry.getKind())) {
				ps.println("<pathelement path=\"" + entry.path + "\" />");
			}
		}
		ps.println("</path>");

		String compileDepends = "init";
		// one by one
		for (int i = 0; i < entryList.size(); i++) {
			Entry entry = entryList.get(i);
			if ("src".equals(entry.getKind())) {
				String name = "compile-" + i;
				compileDepends += ", " + name;
				ps.println("<target name=\"" + name + "\">");
				ps.println("<javac destdir=\"bin\">");
				ps.println("  <src path=\"" + entry.path + "\" />");
				ps.println("<classpath refid=\"classpath\" />");
				ps.println("</javac>");
				ps.println("</target>");

			}
		}

		// all together
		ps.println("<target name=\"compile\" depends=\"" + compileDepends
				+ "\">");
		ps.println("<javac destdir=\"bin\">");

		for (Entry entry : entryList) {
			if ("src".equals(entry.getKind())) {
				ps.println("  <src path=\"" + entry.path + "\" />");
			}
		}
		ps.println("<classpath refid=\"classpath\" />");
		ps.println("</javac>");
		ps.println("</target>");

		ps.println("</project>");
	}

	public static void main(String[] args) throws Exception {
		Classpath2Ant parser = new Classpath2Ant();
		parser.parse(new File(Const.CHECKOUT_DIR
				+ "/columba/workspace/.classpath"));

		parser.toBuildXML(System.out);
	}
}
