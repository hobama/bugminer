package hk.ust.cse.pag.cg.building;

import java.util.List;

/**
 * SVN log
 * 
 * @author hunkim
 * 
 */
public class SVNLog implements Comparable<SVNLog> {
	int revision;
	String author;
	String msg;

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	List<String> changedFiles;

	@Override
	public String toString() {
		return revision + " by " + author + " for: " + msg;
	}

	public int compareTo(SVNLog o) {
		if (revision > o.revision) {
			return 1;
		}
		if (revision < o.revision) {
			return -1;
		}

		return 0;
	}
}
