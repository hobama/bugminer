package hk.ust.cse.pag.cg.building.eclipse2ant;

public class Entry {
	String kind;
	String path;
	String exported;
	String sourcepath;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getExported() {
		return exported;
	}

	public void setExported(String exported) {
		this.exported = exported;
	}

	public String getSourcepath() {
		return sourcepath;
	}

	public void setSourcepath(String sourcepath) {
		this.sourcepath = sourcepath;
	}

	@Override
	public String toString() {
		return kind + " " + path;
	}

}
