package annotationRetriver;

import java.util.ArrayList;
import java.util.List;

public class AnnotationShortDesc {

	private String qualifiedName;
	private List<String> keySet;
	private List<String> valueSet;
	
	public AnnotationShortDesc() {
		this.qualifiedName="";
		this.keySet=new ArrayList<String>();
		this.valueSet=new ArrayList<String>();
	}
	
	public void setQualifiedName(String annotationName) {
		this.qualifiedName=annotationName;
	}
	
	public String getQualifiedName() {
		return qualifiedName;
	}
	
	public void setAnnotationKeySet(List<String> setOfAnnotationKey) {
		this.keySet=setOfAnnotationKey;
	}
	
	public List<String> getAnnotationKeySet() {
		return keySet;
	}
	
	public void setAnnotationValueSet(List<String> setOfAnnotationValue) {
		this.valueSet=setOfAnnotationValue;
	}
	
	public List<String> getAnnotationValueSet() {
		return valueSet;
	}
}
