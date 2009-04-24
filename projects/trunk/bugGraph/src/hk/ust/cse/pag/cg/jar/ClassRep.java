package hk.ust.cse.pag.cg.jar;

import java.util.Arrays;

public class ClassRep {
	String name;
	byte[] body;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClassRep)) {
			return false;
		}
		ClassRep rep = (ClassRep) obj;

		return name.equals(rep.name) && Arrays.equals(body, rep.body);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
}
