package annotationRetriver;

import java.util.ArrayList;
import org.apache.commons.lang3.tuple.Pair;

public class LogInfo {

	private String commitId;
	private String authorName;
	private String commentContent;
	private String commitDate;
	private ArrayList<String> infectedJavaClass;
	private ArrayList<Pair<Integer, Integer>> changedLine;  
	private Boolean whetherBugFixingCommit;
	
	public LogInfo() {
		this.commitId="";
		this.authorName="";
		this.commentContent="";
		this.commitDate="";
		this.infectedJavaClass=new ArrayList<String>();
		this.changedLine=new ArrayList<Pair<Integer, Integer>>();
		this.whetherBugFixingCommit=false;
	}
	
	public void setCommitDate(String dateOfCommit) {
		this.commitDate=dateOfCommit;
	}
	
	public String getCommitDate() {
		return commitDate;
	}
	
	public void setCommitId(String hashnumber) {
		this.commitId=hashnumber;
	}
	
	public String getCommitId() {
		return commitId;
	}
	
	public void setAuthorName(String name) {
		this.authorName=name;
	}
	
	public String getAuthorName() {
		return authorName;
	}
	
	public void setCommentContent(String comment) {
		this.commentContent=comment;
	}
	
	public String getCommentContent() {
		return commentContent;
	}
	
	public void setChangedLine(ArrayList<Pair<Integer, Integer>> lineChanged) {
		this.changedLine=lineChanged; 
	}
	
	public ArrayList<Pair<Integer, Integer>> getChangedLine() {
		return changedLine; 
	}
	
	public void setJavaClassList(ArrayList<String> classList) {
		this.infectedJavaClass=classList;
	}
	
	public ArrayList<String> getJavaClassList() {
		return infectedJavaClass;
	}
	
	public void setBugFixingCommitType(Boolean commitType) {
		this.whetherBugFixingCommit=commitType;
	}
	
	public Boolean getBugFixingCommitType() {
		return whetherBugFixingCommit;
	}
}
