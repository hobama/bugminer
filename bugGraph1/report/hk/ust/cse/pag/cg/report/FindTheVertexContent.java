package hk.ust.cse.pag.cg.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import hk.ust.cse.pag.cg.cfg.NodeTable;
import hk.ust.cse.pag.cg.util.Const;

public class FindTheVertexContent {
	String vertexNum;
	
	public FindTheVertexContent(String vertexNum){
		this.vertexNum=vertexNum;
	}
	
	public void findVertexContent(String vertexNumber, File checkOutDir)throws FileNotFoundException,IOException{	
	
		File graphNodeFile = new File(checkOutDir, "graph_node.txt");
		BufferedReader br = new BufferedReader(new FileReader(graphNodeFile));
		while (true) {
			String line = br.readLine();
						
			if (line == null) {			
				break;
			}
		
			//String str=line.replaceFirst(" ", "&");		
			String splits[] = line.trim().split(" ");
		
			if (splits.length != 2) {
				continue;
			}
	
			String lineNumber=splits[0];		
			if (vertexNumber.equals(lineNumber)) { 
				//== will only return true when the two are the same object,
				//have to change it to be equal
				System.out.println("the code content for vertex number "+ vertexNumber+" is: ");
				System.out.println(this.vertexNum+" "+splits[1]);
				break;
			}
		}
	}
	
	public static void main(String[] args)throws IOException{
		FindTheVertexContent findVertex=new FindTheVertexContent("42");
		findVertex.findVertexContent(findVertex.vertexNum, new File(Const.CHECKOUT_DIR + "/jedit"));
		NodeTable nt=new NodeTable("c:/scratch/hunkim/checkouts/jedit");
		System.out.println(nt.get(42));
		
	}

}
