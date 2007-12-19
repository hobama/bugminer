package edu.mit.csail.pag.bugzilla.miner;

import java.io.File;
import java.io.IOException;

import edu.mit.csail.pag.bugzilla.util.XMLUtils;

/**
 * Try to remove all invalid chars
 * 
 * @author hunkim
 * 
 */
public class RemoveInvalidCharInXML {
	public static void main(String args[]) throws IOException {
		if (args.length < 2) {
			System.err.println("remove invalid files : <src_dir> <dst_dir>");
			return;
		}

		XMLUtils.removeInvalidXMLCharactersInDir(new File(args[0]), new File(
				args[1]));

	}

}
