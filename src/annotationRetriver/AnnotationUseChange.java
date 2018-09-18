package annotationRetriver;

public class AnnotationUseChange {
	
	private int annotationUseAddE; // the annotated element already exists
	private int annotationUseAddN; // the annotated element does not exist in the previous version
	
	private int annotationUseDeleteE; // the annotated element still exists
	private int annotationUseDeleteN; // the annotated element does not exist anymore
	
	private int annotationUseChange; // annotate the same program element, but uses different annotations
	
	private int annotationUseUpdate; // annotate the same program element and same annotation, but the value has changed
	
	private int updateAnnotationRelated; // the updated element is involved with changes made to annotation use
	
	private int annotationunrelatededit; // AST edit operations that are not changes to annotation use
	
	private int changeSpecificAnnotation=0;
	
	public AnnotationUseChange() {
		
		this.annotationUseAddE=0;
		this.annotationUseAddN=0;
		this.annotationUseDeleteE=0;
		this.annotationUseDeleteN=0;
		this.annotationUseChange=0;
		this.annotationUseUpdate=0;
		this.updateAnnotationRelated=0;
		this.annotationunrelatededit=0;
		this.changeSpecificAnnotation=0;
	}
	
	public void setChangeSpecificAnnotation(int numberOfSpecificAnnotationChange) {
		this.changeSpecificAnnotation=numberOfSpecificAnnotationChange;
	}
	
	public int getChangeSpecificAnnotation() {
		return changeSpecificAnnotation;
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
}
