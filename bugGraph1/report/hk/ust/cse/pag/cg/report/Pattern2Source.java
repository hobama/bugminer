package hk.ust.cse.pag.cg.report;

import hk.ust.cse.pag.cg.cfg.NodeTable;
import hk.ust.cse.pag.cg.util.Const;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;

import edu.umd.cs.findbugs.ba.CFGBuilderException;

public class Pattern2Source {
	
	/**
	 * this function maps the pattern in "bug_pat.txt" or "bug-pat2.txt" with 
	 * the corresponding version,class and method info 
	 * output: "bug_pat2_expl"
	 */
	
	private void pattern2Method( File checkOutDir) throws FileNotFoundException, IOException,
			CFGBuilderException {
		//input:
		File bugPatFile = new File(checkOutDir, "bug-pat_OM.txt");		
		BufferedReader br = new BufferedReader(new FileReader(bugPatFile));
		
		//output:
		File bugPatExpl = new File(checkOutDir, "bug-pat_OMExp.txt");		
		PrintStream psBugPatExpl = new PrintStream(new FileOutputStream(bugPatExpl));
				
		//another input:
		//build a hashtable <CFG#, mappingInfo> for fast search
		//using the bug_graph.expl.txt
		Hashtable<String, String> bugGraphExpl=new Hashtable<String, String>();
		
		File bugExpl=new File(checkOutDir, "bug_graph_OMExp.txt");
		BufferedReader brBugExpl = new BufferedReader(new FileReader(bugExpl));
		while (true) {
			String line = brBugExpl.readLine();
			if (line == null) {
				break;
			}

			// starts with t
			if (!line.startsWith("t")){
				continue;
			}
			//seperate CFG and explanation
				String splits[] = line.trim().split(";;");
				if (splits.length != 2) {
				continue;
				}
				String mappingInfo=splits[1];
			//get CFG#, e.g.:  t # 234
				String splits2[]=splits[0].trim().split(" ");
				if(splits2.length !=3){
					continue;
				}
				String CFGNumber=splits2[2];
				bugGraphExpl.put(CFGNumber, mappingInfo);
				
		}
		brBugExpl.close();
		
		//find in "bug-pat.txt" the pattern list
		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}
			
			// println other lines start with t or e;
			if (line.startsWith("t")||line.startsWith("e")){
				psBugPatExpl.println(line);
			}
						
			else if (line.startsWith("v")){
				String splits[] = line.trim().split(" ");		
				if (splits.length != 3) {
					continue;
				}
				NodeTable nt=new NodeTable(Const.CHECKOUT_DIR + "/columba-svn");				
				psBugPatExpl.println(line+" :"+nt.get(Integer.parseInt(splits[2])));
				
			}			
			// must start with empty space
			else if (!line.startsWith(" ")) {
				continue;
			}
			// get pattern list according to output form of the patterns
			String splits[] = line.trim().split("] ");		
			if (splits.length != 2) {
				continue;
			}
			// get the pattern list
			String patternList[]=((String) splits[1].subSequence(1, (splits[1].length())-1)).trim().split(",");
				
			for (int i=0;i<=patternList.length-1;i++){
				String pattern=patternList[i];
				String expl=bugGraphExpl.get(pattern);
				psBugPatExpl.println(pattern+": "+expl);
			}

		}
		psBugPatExpl.close();
		br.close();
	}
	

	public static void main(String[] args) throws FileNotFoundException, CFGBuilderException, IOException{
		Pattern2Source p2s=new Pattern2Source();
		p2s.pattern2Method(new File(Const.CHECKOUT_DIR
				+ "/columba-svn"));
		p2s.pattern2Method(new File(Const.CHECKOUT_DIR
				+ "/jedit"));
		p2s.pattern2Method(new File(Const.CHECKOUT_DIR
				+ "/scarab"));
		p2s.pattern2Method(new File(Const.CHECKOUT_DIR
				+ "/argouml"));
	}
}


