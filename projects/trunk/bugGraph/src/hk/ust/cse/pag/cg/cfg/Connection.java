package hk.ust.cse.pag.cg.cfg;

public class Connection {
	String source;
	String target;
	Node sourceNode;
	Node targetNode;

	public Connection(Node source, Node target) {
		this.source = source.name;
		this.target = target.name;
		
		// keep the node object
		sourceNode = source;
		targetNode = target;
	}
	
	public Node getSourceNode()
	{
	    return sourceNode;
	}
	
	public Node getTargetNode()
	{
	    return targetNode;
	}

	@Override
	public String toString() {
		return source + " -> " + target;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
