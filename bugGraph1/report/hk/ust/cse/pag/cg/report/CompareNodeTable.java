package hk.ust.cse.pag.cg.report;

import hk.ust.cse.pag.cg.util.Const;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CompareNodeTable {

	/**
	 * @param args
	 */
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
		if (result.isEmpty()) {System.out.println("no similar lines found");}//it seems that
		//can only use isEmpty to judge whether an ListArray is empty or not, but can not use
		// ==null to judge whether an array is empty
		return result;
		
	}
	public static void main(String[] args) throws IOException {
		CompareNodeTable cnt=new CompareNodeTable();
		cnt.findTheSameNode(new File(Const.CHECKOUT_DIR + "/jedit"));

	}

}
