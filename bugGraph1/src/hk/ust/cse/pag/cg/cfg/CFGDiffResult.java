package hk.ust.cse.pag.cg.cfg;

import hk.ust.cse.pag.cg.jar.ClassRep;
import hk.ust.cse.pag.cg.util.HashCount;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CFGDiffResult {
	int changedNodeCount;
	String methodName;
	
	// add version info for tracing back from pattern to source code
	File preVersion;
	File newVersion;
	
	// add t# info
	int tNumberBugCFG;
	int tNumberFixCFG;	
	
	// add method path info
	String pathPrev;
	String pathNew;
	
	ClassRep preRep;
	ClassRep newRep;
	

	Set<Node> prevColoredNodes = new HashSet<Node>();
	Set<Node> newColoredNodes = new HashSet<Node>();

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public int getChangedNodeCount() {
		return changedNodeCount;
	}

	public void setChangedNodeCount(int changedNodeCount) {
		this.changedNodeCount = changedNodeCount;
	}

	public Set<Node> getPrevColoredNodes() {
		return prevColoredNodes;
	}

	public void setPrevColoredNodes(Set<Node> prevColoredNodes) {
		this.prevColoredNodes = prevColoredNodes;
	}

	public Set<Node> getNewColoredNodes() {
		return newColoredNodes;
	}

	public void setNewColoredNodes(Set<Node> newColoredNodes) {
		this.newColoredNodes = newColoredNodes;
	}

	/**
	 * update count for nodes
	 * 
	 * @param nodes
	 * @param count
	 */
	private void updateCount(Set<Node> nodes, HashCount<String> count) {
		for (Node node : nodes) {
			count.increase(node.name);
		}
	}

	/**
	 * Update list
	 * 
	 * @param bugCount
	 * @param fixCount
	 */
	public void updateCount(HashCount<String> bugCount,
			HashCount<String> fixCount) {

		// FIXME: we only care changed case (No added/deleted)
		if (changedNodeCount == 0) {
			return;
		}

		// previous is considered as bug
		updateCount(prevColoredNodes, bugCount);

		// new is considered as fix
		updateCount(newColoredNodes, fixCount);
	}
	
	public void setPreVersion(File preVersion){
		this.preVersion=preVersion;
	}
	
	public File getPreVersion(){
		return this.preVersion;
	}
	
	public void setNewVersion(File newVersion){
		this.newVersion=newVersion;
	}
	
	public File getNewVersion(){
		return this.newVersion;
	}
	public void setTNumberBugCFG(int tNumberBugCFG){
		this.tNumberBugCFG=tNumberBugCFG;
	}
	
	public int getTNumberBugCFG(){
		return this.tNumberBugCFG;
	}
	
	public void setTNumberFixCFG(int tNumberFixCFG){
		this.tNumberFixCFG=tNumberFixCFG;
	}
	
	public int getTNumberFixCFG(){
		return this.tNumberFixCFG;
	}
	
	public void setPreClass(ClassRep preRep){
		this.preRep=preRep;
	}
	
	public ClassRep getPreClass(){
		return this.preRep;
	}
	public void setNewClass(ClassRep newRep){
		this.newRep=newRep;
	}
	
	public ClassRep getNewClass(){
		return this.newRep;
	}
}
