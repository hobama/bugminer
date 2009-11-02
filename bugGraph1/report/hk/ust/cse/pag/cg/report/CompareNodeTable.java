package hk.ust.cse.pag.cg.report;

import hk.ust.cse.pag.cg.cfg.NodeTable;
import hk.ust.cse.pag.cg.util.Const;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class CompareNodeTable {


	/**
	 *  find whether two "graph_node.txt" for different projects are the same
	 * @throws IOException 
	 * 
	 * 
	 * */
	public boolean isSubSet(NodeTable nt1, NodeTable nt2, String checkOutDir1, String checkOutDir2) throws IOException{
		
		Set<Integer> keys1=nt1.getGraphNodeTable().keySet();
		Iterator<Integer> iter=keys1.iterator();
		Integer i;
		String line1;
		String line2;
		
		while (iter.hasNext()){
			i=(Integer) iter.next();
			line1=nt1.get(i);
			line2=nt2.get(i);
			if(line2==null){
				System.out.println(checkOutDir1+"/graph_node.txt"+" is not subset of "+checkOutDir2+"/graph_node.txt");
				System.out.println(i+" "+line1 + "exists in "+checkOutDir1+"/graph_node.txt" );	
				System.out.println("but not exists in "+checkOutDir2+"/graph_node.txt" );
				
				return false;
			}
			else if(!line1.equals(line2)){
				System.out.println(checkOutDir1+"/graph_node.txt"+" is not subset of "+checkOutDir2+"/graph_node.txt");
				System.out.println(i+" "+line1 + "exists in "+checkOutDir1+"/graph_node.txt" );
				System.out.println("yet"+i+" "+line2 + "exists in "+checkOutDir2+"/graph_node.txt" );
				return false;
			}			
		}
		
		System.out.println(checkOutDir1+"/graph_node.txt"+" is subset of "+checkOutDir2+"/graph_node.txt");
		return true;
	}
	
	public boolean compareGraphNodeFile (String checkOutDir1, String checkOutDir2) throws IOException{
		NodeTable nt1=new NodeTable(checkOutDir1);
		NodeTable nt2=new NodeTable(checkOutDir2);
		
		return isSubSet(nt1,nt2, checkOutDir1, checkOutDir2)&& isSubSet(nt2,nt1,checkOutDir2,checkOutDir1);
	}
	
	
	public ArrayList<String> findTheSameNode(File checkOutDir) throws IOException{
		ArrayList<String> result= new ArrayList<String>();
		
		File graphNodeFile = new File(checkOutDir, "graph_node.txt");
		BufferedReader br = new BufferedReader(new FileReader(graphNodeFile));
		
		File graphNodeFile1 = new File(checkOutDir, "graph_node1.txt");
		BufferedReader scanSecondTime=new BufferedReader(new FileReader(graphNodeFile1));
		
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
	
			String fixLineNumber=splits[0];	
			String fixLineContent=splits[1];
			
			while (true){
				String scanLine = scanSecondTime.readLine();
				
				if (scanLine == null) {			
					break;
				}
			
						
				String scanSplits[] = line.trim().split(" ");
			
				if (scanSplits.length != 2) {
					continue;
				}
		
				String scanLineNumber=scanSplits[0];	
				String scanLineContent=scanSplits[1];
				if ((!fixLineNumber.equals(scanLineNumber))&&(fixLineContent.equals(scanLineContent))) { 
				//== will only return true when the two are the same object,
				//have to change it to be equal
					System.out.println("the same contents found: ");
					System.out.println(fixLineNumber+ " :"+scanLineNumber);
					result.add(fixLineNumber+" "+fixLineContent);
					result.add(scanLine);
					break;
			}
				
				
			}
			System.out.println("find no pairs for "+ line);
		}
		System.out.println("result= "+result.size());
		if (result.isEmpty()) {System.out.println("no similar lines found");}
		//can only use isEmpty to judge whether an ListArray is empty or not, but can not use
		// ==null to judge whether an array is empty
		return result;
		
	}
	
	
	public static void main(String[] args) throws IOException {
		CompareNodeTable cnt=new CompareNodeTable();
		boolean b=cnt.compareGraphNodeFile( "c:/scratch/hunkim/checkouts/jedit","c:/scratch/hunkim/checkouts/argouml");
		if (b){
			System.out.println("two are equal");
		}
		else{
			System.out.println("two are not equal");
		}
		
		//NodeTable nt=new NodeTable("c:/scratch/hunkim/checkouts/jedit");
		//System.out.println(nt.get(970));		
		//cnt.findTheSameNode(new File(Const.CHECKOUT_DIR + "/jedit"));
		//////////////////////////////////////////////////////////////
		//result is the graph_node.txt argouml project is the largest which includes all the nodes in allNodesTable, includes also
		//the other three graph_node.txt files
	}

}
