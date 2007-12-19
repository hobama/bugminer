package edu.mit.csail.pag.bugzilla.util;

/*
 * $Id: XMLUtils.java,v 1.3 2004/11/20 18:59:42 zimmerth Exp $
 * 
 * LICENSE:
 */
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom.Verifier;

/**
 * @author Thomas Zimmermann <zimmerth@cs.uni-sb.de>
 * @author Sung Kim <hunkim@mit.edu>
 */
public class XMLUtils {

	private static final int DEFAULT_BUFFER_SIZE = 4096;

	public static void removeInvalidXMLCharactersInDir(File oldDir, File newDir)
			throws IOException {

		if (!oldDir.isDirectory()) {
			System.err.println(oldDir + " is not a directory!");
			return;
		}

		// create new directory
		newDir.mkdirs();

		File[] XMLFileList = oldDir.listFiles();
		for (File xmlFile : XMLFileList) {
			if (xmlFile.getName().endsWith(".xml")) {
				File newFile = new File(newDir, xmlFile.getName());
				removeInvalidXMLCharactersInFile(xmlFile, newFile);
			}
		}
	}

	public static void removeInvalidXMLCharactersInFile(File oldFile,
			File newFile) throws IOException {

		System.out.println("removing invalid XML char in " + oldFile + " to "
				+ newFile);
		FileReader reader = null;
		FileWriter writer = null;
		try {
			reader = new FileReader(oldFile);
			writer = new FileWriter(newFile);
			char[] buffer = new char[DEFAULT_BUFFER_SIZE];
			int n = 0;
			while (-1 != (n = reader.read(buffer))) {
				int invalid = 0;
				for (int j = 0; j < n; j++) {
					if (invalid > 0) {
						buffer[j - invalid] = buffer[j];
					}
					if (!Verifier.isXMLCharacter(buffer[j])) {
						invalid++;
					}
				}
				writer.write(buffer, 0, n - invalid);
			}
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				// ignore
			}
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static void encodeInvalidXMLCharacters(String oldFile, String newFile)
			throws IOException {
		FileReader reader = null;
		FileWriter writer = null;
					
		try {
			reader = new FileReader(oldFile);
			writer = new FileWriter(newFile);
			char[] buffer = new char[DEFAULT_BUFFER_SIZE];
			int n = 0;
			while (-1 != (n = reader.read(buffer))) {
				int pos = 0;
				for (int j = 0; j < n; j++) {
					if (!Verifier.isXMLCharacter((buffer[j]))) {
						writer.write(buffer, pos, j - pos);
						pos = j + 1;
						String escape = XMLUtils.escapeCharater(buffer[j]);
						writer.write(escape);
					}
				}
				if (pos < n) {
					writer.write(buffer, pos, n - pos);
				}
			}
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				// ignore
			}
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public static String encodeInvalidXMLCharacters(String string) {
		StringBuffer result = new StringBuffer(DEFAULT_BUFFER_SIZE);
		int n = string.length();
		int pos = 0;
		for (int j = 0; j < n; j++) {
			if (!Verifier.isXMLCharacter(string.charAt(j))) {
				result.append(string.substring(pos, j));
				pos = j + 1;
				String escape = XMLUtils.escapeCharater(string.charAt(j));
				result.append(escape);
			}
		}
		if (pos < n) {
			result.append(string.substring(pos, n));
		}
		return result.toString();
	}

	public static String removeInvalidXMLCharacters(String string) {
		StringBuffer result = new StringBuffer(DEFAULT_BUFFER_SIZE);
		int n = string.length();
		int pos = 0;
		for (int j = 0; j < n; j++) {
			if (!Verifier.isXMLCharacter(string.charAt(j))) {
				result.append(string.substring(pos, j));
				pos = j + 1;
			}
		}
		if (pos < n) {
			result.append(string.substring(pos, n));
		}
		return result.toString();
	}

	public static boolean hasInvalidXMLCharacters(String fileName)
			throws IOException {
		boolean invalid = false;
		FileReader reader = null;
		try {
			reader = new FileReader(fileName);
			char[] buffer = new char[DEFAULT_BUFFER_SIZE];
			int n = 0;
			while (!invalid && (-1 != (n = reader.read(buffer)))) {
				for (int j = 0; j < n; j++) {
					if (!Verifier.isXMLCharacter(buffer[j])) {
						invalid = true;
						break;
					}
				}
			}
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return invalid;
	}	
	

	/**
	 * @param ch
	 * @return String
	 */
	public static String escapeCharater(char ch) {
		return "&#" + ((int) ch) + ";";
	}
}
