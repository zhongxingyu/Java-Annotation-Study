package annotationRetriver;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

public class AnnotationDefinition {
	
	private String qualifiedName;
	private List<Triple<String, ArrayList<String>, ArrayList<String>>> methaAnnotationInfo;
	private List<Triple<String, String, String>> annotationMethodInfo;
	
	public AnnotationDefinition() {
		this.qualifiedName="";
		this.methaAnnotationInfo=new ArrayList<Triple<String, ArrayList<String>, ArrayList<String>>>();
		this.annotationMethodInfo=new ArrayList<Triple<String, String, String>>();
	}
	
	public void setQualifiedName(String annotationName) {
		this.qualifiedName=annotationName;
	}
	
	public String getQualifiedName() {
		return qualifiedName;
	}
	
	public void setMethaAnnotationInfo(List<Triple<String, ArrayList<String>, ArrayList<String>>> attributeForMetaAnnotation) {
		this.methaAnnotationInfo=attributeForMetaAnnotation;
	}
	
	public List<Triple<String, ArrayList<String>, ArrayList<String>>> getMethaAnnotationInfo() {
		return methaAnnotationInfo;
	}
	
	public void setAnnotationMethodInfo(List<Triple<String, String, String>> attributeForAnnotationMethod) {
		this.annotationMethodInfo=attributeForAnnotationMethod;
	}
	
	public List<Triple<String, String, String>> getAnnotationMethodInfo() {
		return annotationMethodInfo;
	}
}
