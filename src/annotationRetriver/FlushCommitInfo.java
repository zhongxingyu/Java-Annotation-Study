package annotationRetriver;

public class FlushCommitInfo {

	private String commitId;
	private String authorName;
	private Boolean whetherBugFixingCommit1;
	private Boolean whetherBugFixingCommit2;
	private String projectName;
	private String className;
	private int classSize;
	private int codechurnNumber;
	private String commitDate;
	private int annotationUseNumber;
	private Boolean pureAnnotationDefinion;
	private int overideAnnotationNumber;
	private Boolean whetherSpoonParseError;
	private int annotationDefNumber;
	private int annotationDefAdd;
	private int annotationDefDelete;
	private int DefMetaAnnotationAdd;
	private int DefMetaAnnotationDelete;
	private int DefMetaAnnotationValueChange;
	private int DefMetaAnnotationReplace;
	private int ChangetoRentionOrTarget;
	private int ChangetoSpecificMetaAnnotation;
	private int DefMethodAdd;
	private int DefMethodDelete;
	private int DefMethodChange;
	private int DefMethodReplace;
	private String changetype;
	private int changeSpecificAnnotation=0;
	private int annotationUseAddE; // the annotated element already exists in the previous version
	private int annotationUseAddN; // the annotated element does not exist in the previous version
	private int annotationUseDeleteE; // the annotated element still exists in the new version
	private int annotationUseDeleteN; // the annotated element does not exist anymore in the new version
	private int annotationUseChange; // annotate the same program element, but uses different annotations
	private int annotationUseUpdate; // annotate the same program element and same annotation, but the value has changed
    private int updateAnnotationRelated; // the updated element is involved with changes made to annotation use	
	private int annotationunrelatededit; // AST edit operations that are not changes to annotation use
	
	public FlushCommitInfo() {
		this.commitId="";
		this.authorName="";
		this.whetherBugFixingCommit1=false;
		this.whetherBugFixingCommit2=false;
		this.projectName="";
		this.className="";
		this.classSize=0;
		this.commitDate="";
		this.annotationUseNumber=0;
		this.annotationDefNumber=0;
		this.annotationDefAdd=0;
		this.annotationDefDelete=0;
		this.pureAnnotationDefinion=false;
		this.overideAnnotationNumber=0;
		this.whetherSpoonParseError=false;
		this.DefMetaAnnotationAdd=0;
		this.DefMetaAnnotationDelete=0;
		this.DefMetaAnnotationValueChange=0;
		this.DefMetaAnnotationReplace=0;
		this.ChangetoRentionOrTarget=0;
		this.ChangetoSpecificMetaAnnotation=0;
		this.DefMethodAdd=0;
		this.DefMethodDelete=0;
		this.DefMethodChange=0;
		this.DefMethodReplace=0;
		this.changetype="M";
		this.annotationUseAddE=0;
		this.annotationUseAddN=0;
		this.annotationUseDeleteE=0;
		this.annotationUseDeleteN=0;
		this.annotationUseChange=0;
		this.annotationUseUpdate=0;
		this.updateAnnotationRelated=0;	
		this.annotationunrelatededit=0;
		this.codechurnNumber=0;
		this.changeSpecificAnnotation=0;
	}
	
	public void setChangeSpecificMetaAnnotation(int numberOfSpecificMetaAnnotationChange) {
		this.ChangetoSpecificMetaAnnotation=numberOfSpecificMetaAnnotationChange;
	}
	
	public int getChangeSpecificMetaAnnotation() {
		return ChangetoSpecificMetaAnnotation;
	}
	
	public void setChangeSpecificAnnotation(int numberOfSpecificAnnotationChange) {
		this.changeSpecificAnnotation=numberOfSpecificAnnotationChange;
	}
	
	public int getChangeSpecificAnnotation() {
		return changeSpecificAnnotation;
	}
	
	public void setCodechurnNumber(int numberOfCodechurn) {
		this.codechurnNumber=numberOfCodechurn;
	}
	
	public int getCodechurnNumber() {
		return codechurnNumber;
	}
	
	public void setMethodReplace(int numberOfMethodReplace) {
		this.DefMethodReplace=numberOfMethodReplace;
	}
	
	public int getMethodReplace() {
		return DefMethodReplace;
	}
	
	public void setRentionTargetChange(int numberOfRentionTargetChange) {
		this.ChangetoRentionOrTarget=numberOfRentionTargetChange;
	}
	
	public int getRentionTargetChange() {
		return ChangetoRentionOrTarget;
	}
	
	public void setAnnotationReplace(int numberOfAnnotationReplace) {
		this.DefMetaAnnotationReplace=numberOfAnnotationReplace;
	}
	
	public int getAnnotationReplace() {
		return DefMetaAnnotationReplace;
	}
	
	public void setAnnotationUseAddE(int numberOfAnnotationUseAddE) {
		this.annotationUseAddE=numberOfAnnotationUseAddE;
	}
	
	public int getAnnotationUseAddE() {
		return annotationUseAddE;
	}
	
	public void setAnnotationUseAddN(int numberOfAnnotationUseAddN) {
		this.annotationUseAddN=numberOfAnnotationUseAddN;
	}
	
	public int getAnnotationUseAddN() {
		return annotationUseAddN;
	}
	
	public void setAnnotationUseDeleteE(int numberOfAnnotationUseDeleteE) {
		this.annotationUseDeleteE=numberOfAnnotationUseDeleteE;
	}
	
	public int getAnnotationUseDeleteE() {
		return annotationUseDeleteE;
	}
	
	public void setAnnotationUseDeleteN(int numberOfAnnotationUseDeleteN) {
		this.annotationUseDeleteN=numberOfAnnotationUseDeleteN;
	}
	
	public int getAnnotationUseDeleteN() {
		return annotationUseDeleteN;
	}
	
	public void setAnnotationUseChange(int numberOfAnnotationUseChange) {
		this.annotationUseChange=numberOfAnnotationUseChange;
	}
	
	public int getAnnotationUseChange() {
		return annotationUseChange;
	}
	
	public void setAnnotationUseUpdate(int numberOfAnnotationUseUpdate) {
		this.annotationUseUpdate=numberOfAnnotationUseUpdate;
	}
	
	public int getAnnotationUseUpdate() {
		return annotationUseUpdate;
	}
	
	public void setUpdateAnnotationRelated(int nbAnnotationRelatedUpdate) {
		this.updateAnnotationRelated=nbAnnotationRelatedUpdate;
	}
	
	public int getUpdateAnnotationRelated() {
		return updateAnnotationRelated;
	}
	
	public void setAnnotationUnrelatedEdit(int nbannotationunrelatededit) {
		this.annotationunrelatededit=nbannotationunrelatededit;
	}
	
	public int getAnnotationUnrelatedEdit() {
		return annotationunrelatededit;
	}
	
	public void setChangeType(String typeOfChange) {
		this.changetype=typeOfChange;
	}
	
	public String getChangeType() {
		return changetype; // "M": modify, "A": add, "D": delete 
	}
	
	public void setMethodChange(int numberOfDefMethodChange) {
		this.DefMethodChange=numberOfDefMethodChange;
	}
	
	public int getMethodChange() {
		return DefMethodChange;
	}
	
	public void setMethodDelete(int numberOfDefMethodDelete) {
		this.DefMethodDelete=numberOfDefMethodDelete;
	}
	
	public int getMethodDelete() {
		return DefMethodDelete;
	}
	
	public void setMethodAdd(int numberOfDefMethodAdd) {
		this.DefMethodAdd=numberOfDefMethodAdd;
	}
	
	public int getMethodAdd() {
		return DefMethodAdd;
	}
	
	public void setMetaAnnotationValueChange(int numberOfDefMetaAnnotationValueChange) {
		this.DefMetaAnnotationValueChange=numberOfDefMetaAnnotationValueChange;
	}
	
	public int getMetaAnnotationValueChange() {
		return DefMetaAnnotationValueChange;
	}
	
	public void setMetaAnnotationDelete(int numberOfDefMetaAnnotationDelete) {
		this.DefMetaAnnotationDelete=numberOfDefMetaAnnotationDelete;
	}
	
	public int getMetaAnnotationDelete() {
		return DefMetaAnnotationDelete;
	}
	
	public void setMetaAnnotationAdd(int numberOfDefMetaAnnotationAdd) {
		this.DefMetaAnnotationAdd=numberOfDefMetaAnnotationAdd;
	}
	
	public int getMetaAnnotationAdd() {
		return DefMetaAnnotationAdd;
	}
	
	public void setAnnotationDefDelete(int numberOfAnnotationDefDelete) {
		this.annotationDefDelete=numberOfAnnotationDefDelete;
	}
	
	public int getAnnotationDefDelete() {
		return annotationDefDelete;
	}
	
	public void setAnnotationDefAdd(int numberOfAnnotationDefAdd) {
		this.annotationDefAdd=numberOfAnnotationDefAdd;
	}
	
	public int getAnnotationDefAdd() {
		return annotationDefAdd;
	}
	
	public void setAnnotationDefNumber(int numberOfAnnotationDef) {
		this.annotationDefNumber=numberOfAnnotationDef;
	}
	
	public int getAnnotationDefNumber() {
		return annotationDefNumber;
	}
	
	public void setWhetherSpoonParseError(Boolean whetherSpoonParseError) {
		this.whetherSpoonParseError=whetherSpoonParseError;
	}
	
	public Boolean getWhetherSpoonParseError() {
		return whetherSpoonParseError;
	}
	
	public void setOverideAnnotationNumber(int numberOfOverideAnnotation) {
		this.overideAnnotationNumber=numberOfOverideAnnotation;
	}
	
	public int getOverideAnnotationNumber() {
		return overideAnnotationNumber;
	}
	
	public void setWhetherPureAnnotationDefinion(Boolean whetherPureAnnotationDefinion) {
		this.pureAnnotationDefinion=whetherPureAnnotationDefinion;
	}
	
	public Boolean getWhetherPureAnnotationDefinion() {
		return pureAnnotationDefinion;
	}
	
	public void setAnnotationUseNumber(int numberOfAnnotationUse) {
		this.annotationUseNumber=numberOfAnnotationUse;
	}
	
	public int getAnnotationUseNumber() {
		return annotationUseNumber;
	}
	
	public void setCommitDate(String dateOfCommit) {
		this.commitDate=dateOfCommit;
	}
	
	public String getCommitDate() {
		return commitDate;
	}
	
	public void setClassSize(int sizeOfClass) {
		this.classSize=sizeOfClass;
	}
	
	public int getClassSize() {
		return classSize;
	}
	
	public void setClassName(String nameOfClass) {
		this.className=nameOfClass;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setProjectName(String nameOfProject) {
		this.projectName=nameOfProject;
	}
	
	public String getProjectName() {
		return projectName;
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
	
	public void setBugFixingCommitType1(Boolean commitType1) {
		this.whetherBugFixingCommit1=commitType1;
	}
	
	public Boolean getBugFixingCommitType1() {
		return whetherBugFixingCommit1;
	}
	
	public void setBugFixingCommitType2(Boolean commitType2) {
		this.whetherBugFixingCommit2=commitType2;
	}
	
	public Boolean getBugFixingCommitType2() {
		return whetherBugFixingCommit2;
	}
}
