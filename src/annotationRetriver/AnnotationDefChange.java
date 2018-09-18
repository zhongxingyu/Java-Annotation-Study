package annotationRetriver;

public class AnnotationDefChange {
	
	private int annotationDefAdd;
	private int annotationDefDelete;
	private int DefMetaAnnotationAdd;
	private int DefMetaAnnotationDelete;
	private int DefMetaAnnotationValueChange;
	private int DefMetaAnnotationReplace;
	private int ChangetoRentionOrTarget;
	private int ChangetoSpecificAnnotation;
	private int DefMethodAdd;
	private int DefMethodDelete;
	private int DefMethodChange;
	private int DefMethodReplace;
	
	public AnnotationDefChange() {
		this.annotationDefAdd=0;
		this.annotationDefDelete=0;
		this.DefMetaAnnotationAdd=0;
		this.DefMetaAnnotationDelete=0;
		this.DefMetaAnnotationValueChange=0;
		this.DefMetaAnnotationReplace=0;
		this.ChangetoRentionOrTarget=0;
		this.ChangetoSpecificAnnotation=0;
		this.DefMethodAdd=0;
		this.DefMethodDelete=0;
		this.DefMethodChange=0;
		this.DefMethodReplace=0;
	}
	
	public void setChangetoSpecificAnnotation(int numberOfChangetoSpecificAnnotation) {
		this.ChangetoSpecificAnnotation=numberOfChangetoSpecificAnnotation;
	}
	
	public int getChangetoSpecificAnnotation() {
		return ChangetoSpecificAnnotation;
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
}
