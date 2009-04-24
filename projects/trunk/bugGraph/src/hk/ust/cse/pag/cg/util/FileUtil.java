/*
 * Created on 2003. 5. 29.
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package hk.ust.cse.pag.cg.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hunkim
 * 
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FileUtil extends File {

	private static final String EMPTY = "";

	private static final String CR = "\r";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1213856407240310327L;

	public FileUtil(String filename) {
		super(filename);
	}

	public FileUtil(File f, String filename) {
		super(f, filename);
	}

	public boolean deleteDir() {
		// Directory needs recursive
		if (this.isDirectory() == true) {
			String files[] = this.list();
			for (int i = 0; i < files.length; i++) {
				// Don't remove itself yet
				if (!files[i].equals(".") && !files[i].equals("..")) {
					FileUtil f = new FileUtil(this, files[i]);
					f.deleteDir();
				}
			}
		}
		// Delete itself
		return this.delete();
	}

	/**
	 * Static method for deleteDir
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean deleteDir(String filename) {
		FileUtil ivafile = new FileUtil(filename);
		return ivafile.deleteDir();
	}

	/**
	 * Copy a file or directory
	 * 
	 * @param src
	 * @param dest
	 * @throws Exception
	 */
	public static void copy(File src, File dest) throws Exception {

		if (!src.exists()) {
			throw new FileNotFoundException(src.getAbsolutePath());
		}

		if (src.isDirectory()) {
			// Regulat file, delete it
			if (dest.isFile()) {
				dest.delete();
			}
			if (!dest.exists()) {
				if (dest.mkdirs() == false) {
					throw new Exception("Cannot make a directory:"
							+ dest.getAbsolutePath());
				}
			}

			File list[] = src.listFiles();
			for (int i = 0; i < list.length; i++) {
				copy(list[i], new File(dest, list[i].getName()));
			}
		} else if (src.isFile()) {
			copyFileContent(src, dest);
		}
	}

	/**
	 * TODO: This stream is not working on Limux
	 * 
	 * @param src
	 * @param dest
	 * @throws Exception
	 */
	static void copyFileStream(File src, File dest) throws Exception {
		// Make sure src is a regular file
		if (!src.isFile())
			return;

		FileChannel sourceChannel = new FileInputStream(src).getChannel();
		FileChannel destinationChannel = new FileOutputStream(dest)
				.getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		sourceChannel.close();
		destinationChannel.close();
	}

	static private void copyFileContent(File src, File dest) throws Exception {
		// Make sure src is a regular file
		if (!src.isFile())
			return;

		FileInputStream fis = new FileInputStream(src);
		FileOutputStream fos = new FileOutputStream(dest);
		byte[] buf = new byte[1024];
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			fos.write(buf, 0, i);
		}
		fis.close();
		fos.close();
	}

	/**
	 * @param dummyLocation
	 * @param workspace
	 */
	public static void copy(String dummyLocation, String workspace)
			throws Exception {
		copy(new File(dummyLocation), new File(workspace));
	}

	/**
	 * Count LOC of File
	 * 
	 * @param fileContent
	 * @return
	 * @throws IOException
	 */
	public static int getFileLOC(File fileContent) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileContent));
		int count = 0;
		while (br.readLine() != null) {
			count++;
		}
		br.close();
		return count;
	}

	/**
	 * Get each line in the file and return the List
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static List<String> fileLineToList(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		List<String> lineList = new ArrayList<String>();
		lineList.add("Don't read"); // list starts from 1. O is null;

		String line;
		while ((line = br.readLine()) != null) {
			line = line.replaceAll(CR, EMPTY);
			lineList.add(line);
		}
		br.close();
		return lineList;
	}

	static public void DOS2UNIX(File file) throws Exception {
		// Make sure src is a regular file
		if (!file.isFile())
			return;

		List<String> lines = fileLineToList(file);

		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for (String line : lines) {
			bw.write(line);
			bw.newLine();
		}

		bw.close();
	}
}