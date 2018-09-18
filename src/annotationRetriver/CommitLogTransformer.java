package annotationRetriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import difflib.Delta;
import difflib.DiffRow;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.DiffImpl;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.InsertOperation;
import gumtree.spoon.diff.operations.MoveOperation;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import spoon.Launcher;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtAnnotationMethod;
import spoon.reflect.declaration.CtAnnotationType;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

public class CommitLogTransformer {
	
	private String repositoryName;
	private String repositoryPath;
	
	ArrayList<FlushCommitInfo> commitInfoToFlush=new ArrayList<FlushCommitInfo>();
	
	Triple<LogInfo,LogInfo,Integer> tocalcuateinfo;
	
	int indexOfCommit=0;
		
	private static ArrayList<String> specificannotations=new ArrayList<String>(Arrays.asList("java.lang.Override",
			"java.lang.SuppressWarnings", "java.lang.Deprecated", "java.lang.SafeVarargs", "java.lang.FunctionalInterface")); 
	
	private static ArrayList<String> specificmetaannotations=new ArrayList<String>(Arrays.asList("java.lang.annotation.Documented",
			"java.lang.annotation.Inherited", "java.lang.annotation.Repeatable", "java.lang.annotation.Target", 
			"java.lang.annotation.Retention")); 
	
	private StanfordLemmatizer commitTypeAnalyzer; 
	
	public static final String[] error_keyword_array = {"error", "bug", "fix", "issue", "fixup", "bugfix", "npe",
			"mistake", "blunder", "incorrect", "fault", "defect", "flaw", "glitch", "gremlin", "erroneous"};
	
	public CommitLogTransformer (String pathOfRepository, StanfordLemmatizer analyzer, 
			String nameOfRepository) {
		this.repositoryPath=pathOfRepository;
		this.commitTypeAnalyzer=analyzer;
		this.repositoryName = nameOfRepository;
	}
	
	public void initialize() {
		this.indexOfCommit=0;
		tocalcuateinfo=Triple.of(new LogInfo(), new LogInfo(), 0);
	}

	public void transform() {
		
		Boolean whetherNewCommmit=false;
		String contentForACommit = "";
		try {
			for (String line : FileUtils.readLines(new File(this.repositoryPath+"/log.txt"))) {
				whetherNewCommmit = line.startsWith("commit ")? true:false;
				if(whetherNewCommmit) {
					if(!contentForACommit.equals("")) {
						analyzeCommitInfo(contentForACommit);
						contentForACommit="";
					}
				}	
				contentForACommit+=line;
				contentForACommit+=System.getProperty("line.separator");
			}
			analyzeCommitInfo(contentForACommit);
		//	calcuateCommitInfo(true);
		} catch (IOException e) {
             e.printStackTrace();
		}
	}
	
	public void analyzeCommitInfo(String commitContent) {
		
		String[] info = commitContent.split(System.getProperty("line.separator"));
		String commitId="",authorName="",commentContent="",commitdate="";
		ArrayList<String> infectedJavaClass = new ArrayList<String>();
		ArrayList<Pair<Integer, Integer>> changedLine = new ArrayList<Pair<Integer, Integer>>();  
		Boolean commentStart=false, modifySourceStart=false;

		for (int index=0; index<info.length; index++) {
			if(info[index].startsWith("commit "))
				commitId=(info[index].replace("commit ", "")).trim();
			if(info[index].startsWith("Author: "))
				authorName=(info[index].replace("Author: ", "")).trim();
			if(info[index].startsWith("Date:   ")) {
				commitdate=(info[index].replace("Date:   ", "")).trim();
				commentStart=true;
				continue;
			}
			if(info[index].matches("\\d.*")||info[index].startsWith("-")) {		
				modifySourceStart=true;
			}
			
			if(commentStart&&!modifySourceStart) {
				commentContent+=info[index];
				commentContent+=System.getProperty("line.separator");
			}
			
			if(modifySourceStart) {
				String[] splittedString=info[index].trim().split("\\s+");	
				String fileName="";
				if(splittedString.length>0)
					fileName =splittedString[splittedString.length-1].trim();
				else fileName="";
				
				if(fileName.endsWith(".java")&&(fileName.toLowerCase()).indexOf("test")==-1) {
					infectedJavaClass.add(fileName);
					int added=0;
					int deleted=0;
					try {
						added=Integer.parseInt(splittedString[0]);
					} catch (Exception e) {
						added=-1;
					}
					
					try {
						deleted=Integer.parseInt(splittedString[1]);
					} catch (Exception e) {
						deleted=-1;
					}
					
					changedLine.add(Pair.of(added, deleted));
				}
			}
		}
		
		if(infectedJavaClass.size()>0) {
			LogInfo commitInfoHere=new LogInfo();
			commitInfoHere.setCommitId(commitId);
			commitInfoHere.setAuthorName(authorName);
			commitInfoHere.setCommitDate(commitdate);
			commitInfoHere.setCommentContent(commentContent);
			commitInfoHere.setBugFixingCommitType(false);
			commitInfoHere.setJavaClassList(infectedJavaClass);
			commitInfoHere.setChangedLine(changedLine);
			this.indexOfCommit+=1;
			updateToCalcuateinfo(commitInfoHere,this.indexOfCommit);
			calcuateCommitInfo(false);
		}
	}
	
	public void updateToCalcuateinfo(LogInfo incomecommitinfo, int index) {
		if(index==1) {
			tocalcuateinfo=Triple.of(incomecommitinfo, new LogInfo(), index);
		}
		else if(index==2) {
			LogInfo current=tocalcuateinfo.getLeft();
			LogInfo previous=incomecommitinfo;
			tocalcuateinfo = Triple.of(current, previous, index);
		}
		else {
			LogInfo current=tocalcuateinfo.getMiddle();
			LogInfo previous=incomecommitinfo;
			tocalcuateinfo = Triple.of(current, previous, index);
		}
	}
	
	public void calcuateCommitInfo(Boolean whetherFinalCommit) { 
		
		if(tocalcuateinfo.getRight()==1) {
		   final String FILE_HEADER_USE = "pName,ID,PE,bType1,bType2,cName,cType,Size,nbCC,nbUse,nbOver,nbCS,nbUseAE,"
				+ "nbUseAN,nbUseDE,nbUseDN,nbUseC,nbUseU,nbUpAnRe,nbAnUnEd,aName,Date";
		   final String FILE_HEADER_Def = "pName,ID,PE,bType1,bType2,cName,cType,Size,nbCC,"
				+ "nbDef,nbDA,nbDD,nbmetaA,nbmetaD,nbmetaVC,"
				+ "nbmetaR,nbRTC,nbCSMA,nbMA,nbMD,nbMC,nbMR,aName,Date";
		   final String NEW_LINE_SEPARATOR = "\n";
		   FileWriter fileWriter1 = null;
		   FileWriter fileWriter2 = null;
		   try {
			   fileWriter1 = new FileWriter(this.repositoryPath+"/"+"info_for_all_commits_use.csv",true);
			   //Write the CSV file header
			   fileWriter1.append(FILE_HEADER_USE.toString());
			   //Add a new line separator after the header
			   fileWriter1.append(NEW_LINE_SEPARATOR);
			   
			   fileWriter2 = new FileWriter(this.repositoryPath+"/"+"info_for_all_commits_def.csv",true);
			   //Write the CSV file header
			   fileWriter2.append(FILE_HEADER_Def.toString());
			   //Add a new line separator after the header
			   fileWriter2.append(NEW_LINE_SEPARATOR);
		   } catch (Exception e) {
			    e.printStackTrace();
		   } finally {	
			  try {
				   fileWriter1.flush();
				   fileWriter1.close();
				   fileWriter2.flush();
				   fileWriter2.close();
			  } catch (IOException e) {
                e.printStackTrace();
			 }	
		   }	
		}
		else {
			LogInfo currentcommitinfo=tocalcuateinfo.getLeft();
			LogInfo previouscommitinfo=tocalcuateinfo.getMiddle();
			FlushCommitInfo infoEveryCommit;
			
			if(!whetherFinalCommit) {
				List<List<AnnotationShortDesc>> currentAnnotationUse=new ArrayList<List<AnnotationShortDesc>>();
				List<List<AnnotationShortDesc>> lastAnnotationUse=new ArrayList<List<AnnotationShortDesc>>();
				List<List<AnnotationDefinition>> currentAnnotationdef=new ArrayList<List<AnnotationDefinition>>();
				List<List<AnnotationDefinition>> lastAnnotationdef=new ArrayList<List<AnnotationDefinition>>();
				Boolean[] parseErrorCurrent=new Boolean[currentcommitinfo.getJavaClassList().size()];
				Boolean[] parseErrorLast=new Boolean[currentcommitinfo.getJavaClassList().size()];
				Boolean[] whetherExistCurrent=new Boolean[currentcommitinfo.getJavaClassList().size()];
				Boolean[] whetherExistLast=new Boolean[currentcommitinfo.getJavaClassList().size()];
				
				List<CtPackage> currentPackage=new ArrayList<CtPackage>();
				List<CtPackage> lastPackage=new ArrayList<CtPackage>();
				boolean[] committyperesult= determineCommitType(this.repositoryName, currentcommitinfo.getCommentContent());

				List<List<String>> currentcodeline=new ArrayList<List<String>>();
				List<List<String>> lastcodeline=new ArrayList<List<String>>();
				
				switchToCommit(this.repositoryPath,currentcommitinfo.getCommitId());		
				for (int javaFileIndexCurrent=0; javaFileIndexCurrent<currentcommitinfo.getJavaClassList().size();
						javaFileIndexCurrent++) { 
					
					parseErrorCurrent[javaFileIndexCurrent]=false;
					String fileNameSuffix=currentcommitinfo.getJavaClassList().get(javaFileIndexCurrent);
			
					String realFileName=this.repositoryPath+"/"+fileNameSuffix;
					File f = new File(realFileName);
					if(f.exists() && !f.isDirectory()) {
						 whetherExistCurrent[javaFileIndexCurrent]=true;
						 CtPackage toCalcuatePackage;
					     if(estimatelinemumber(realFileName)<6000)
					    	 toCalcuatePackage=getDesiredPackage(realFileName);
					     else toCalcuatePackage=null;
					     currentPackage.add(toCalcuatePackage);
					     if(toCalcuatePackage==null) {
					    	 parseErrorCurrent[javaFileIndexCurrent]=true;	
					    	 currentAnnotationUse.add(new ArrayList<AnnotationShortDesc>());
					    	 currentAnnotationdef.add(new ArrayList<AnnotationDefinition>());
					    	 currentcodeline.add(new ArrayList<String>());
					    	 deleteFile(); 
					     }
					     else {
					    	 currentAnnotationUse.add(getAnnotationUseDescPackage(toCalcuatePackage));
					    	 currentAnnotationdef.add(getAnnotationDefinion(toCalcuatePackage));
					    	 currentcodeline.add(fileToLines(fileNameSuffix));
					    	 deleteFile();
					     }
					}
					else {
						whetherExistCurrent[javaFileIndexCurrent]=false;
						currentAnnotationUse.add(new ArrayList<AnnotationShortDesc>());
						currentAnnotationdef.add(new ArrayList<AnnotationDefinition>());
						currentcodeline.add(new ArrayList<String>());
						currentPackage.add(null);
					}
				}
				
				switchToCommit(this.repositoryPath,previouscommitinfo.getCommitId());	
				for (int javaFileIndexLast=0; javaFileIndexLast<currentcommitinfo.getJavaClassList().size();
						javaFileIndexLast++) { 
					parseErrorLast[javaFileIndexLast]=false;			
					String fileNameSuffix=currentcommitinfo.getJavaClassList().get(javaFileIndexLast);
			
					String realFileName=this.repositoryPath+"/"+fileNameSuffix;
					
					File f = new File(realFileName);
					if(f.exists() && !f.isDirectory()) {
						 whetherExistLast[javaFileIndexLast]=true;
						 CtPackage toCalcuatePackage;
					     if(estimatelinemumber(realFileName)<6000)
					    	 toCalcuatePackage=getDesiredPackage(realFileName);
					     else toCalcuatePackage=null;					 
					     lastPackage.add(toCalcuatePackage);
					     if(toCalcuatePackage==null) {
					    	 parseErrorLast[javaFileIndexLast]=true;
						     lastAnnotationUse.add(new ArrayList<AnnotationShortDesc>());
						     lastAnnotationdef.add(new ArrayList<AnnotationDefinition>());
						     lastcodeline.add(new ArrayList<String>());
						     deleteFile();
					     }
					     else {
						     lastAnnotationUse.add(getAnnotationUseDescPackage(toCalcuatePackage));
						     lastAnnotationdef.add(getAnnotationDefinion(toCalcuatePackage));
						     lastcodeline.add(fileToLines(fileNameSuffix));
					    	 deleteFile();
					     }
					}
					else {
						whetherExistLast[javaFileIndexLast]=false;
						lastAnnotationUse.add(new ArrayList<AnnotationShortDesc>());
					    lastAnnotationdef.add(new ArrayList<AnnotationDefinition>());
					    lastcodeline.add(new ArrayList<String>());
					    lastPackage.add(null);
					}
				}
				
				ArrayList<Pair<Integer, Integer>> filePair=new ArrayList<Pair<Integer, Integer>>();
				ArrayList<Integer> consideredIndex=new ArrayList<Integer>();
				consideredIndex.clear();
				Pair<Integer, Integer> toAddIndexPair;
				int totalNBFile = currentcommitinfo.getJavaClassList().size();
	
				for(int fileIndex=0;fileIndex<totalNBFile; fileIndex++) {
					
						if(parseErrorCurrent[fileIndex]==true || parseErrorLast[fileIndex]==true) {
							consideredIndex.add(fileIndex);
						}  // if we have parse error, ignore
						else {  
							 if(whetherExistCurrent[fileIndex]==true && whetherExistLast[fileIndex]==true) { // both files exist
							   toAddIndexPair=Pair.of(fileIndex, fileIndex);
							   filePair.add(toAddIndexPair);
							   consideredIndex.add(fileIndex);
						     } else if(whetherExistCurrent[fileIndex]==false && whetherExistLast[fileIndex]==false) {
						    	 consideredIndex.add(fileIndex);
						     } else {    		     
							   if(whetherExistCurrent[fileIndex]==true) { //file add
								  Boolean whetherFound=false;
								  int linenumber=currentcodeline.get(fileIndex).size();
								  
								  String currentFileName=currentcommitinfo.getJavaClassList().get(fileIndex);
							      String[] splittedFileName=currentFileName.split("/");
							      String considerName=splittedFileName[splittedFileName.length-1];
							      int linechange=currentcommitinfo.getChangedLine().get(fileIndex).getLeft();
								  
								  for (int annotherindex=0; annotherindex<totalNBFile; annotherindex++) {
									if(consideredIndex.contains(annotherindex)||annotherindex==fileIndex||whetherExistLast[annotherindex]==false) {
										continue;
									} 
									else {
										if(whetherExistCurrent[annotherindex]==true) {
											// ignore
										} 
										else {
											String lastFileName=currentcommitinfo.getJavaClassList().get(annotherindex);
										    String[] splittedName=lastFileName.split("/");
										    String considerNameLast=splittedName[splittedName.length-1];
										    int linechangelast=currentcommitinfo.getChangedLine().get(annotherindex).getRight();
                                            double linechangepercen=(double)Math.abs(linechange-linechangelast)/(double)linechange;
										    if(linechangepercen<=0.1 && considerName.equals(considerNameLast)) {
										    	 toAddIndexPair=Pair.of(fileIndex, annotherindex);
												 filePair.add(toAddIndexPair);
										     	 consideredIndex.add(fileIndex);
										     	 consideredIndex.add(annotherindex);
										     	 whetherFound=true;
										    } else if(linechangepercen>=0.5) {
										    	// abouslutely different file
										    } else {
												int diffsize=getCodeChurn(currentcodeline.get(fileIndex),lastcodeline.get(annotherindex));
												double percentage= (double)diffsize/(double)linenumber;
												if(Math.abs(percentage)<=0.3) {
												 toAddIndexPair=Pair.of(fileIndex, annotherindex);
												 filePair.add(toAddIndexPair);
										     	 consideredIndex.add(fileIndex);
										     	 consideredIndex.add(annotherindex);
										     	 whetherFound=true;
												}
										    }	
										}
									}
									if (whetherFound==true) {
										break;
									}
								}
								 if(whetherFound==false) {
									 toAddIndexPair=Pair.of(fileIndex, -1);
									 filePair.add(toAddIndexPair);
								     consideredIndex.add(fileIndex);
								 }
							} // file add
						}	
					}
				}
					
	            for(int fileIndex=0;fileIndex<totalNBFile; fileIndex++) {	// file delete
	                	if(!consideredIndex.contains(fileIndex) && whetherExistLast[fileIndex]==true) {
	                		 toAddIndexPair=Pair.of(-1,fileIndex);
							 filePair.add(toAddIndexPair);
						     consideredIndex.add(fileIndex);
						}
	             } // file delete
                
                for (int index=0; index<filePair.size(); index++) {
                	Pair<Integer, Integer> toComparePair=filePair.get(index);
                	
					infoEveryCommit = new FlushCommitInfo();		
					infoEveryCommit.setWhetherSpoonParseError(false);
					infoEveryCommit.setProjectName(this.repositoryName);
					infoEveryCommit.setAuthorName(currentcommitinfo.getAuthorName());
					infoEveryCommit.setCommitId(currentcommitinfo.getCommitId());
					infoEveryCommit.setCommitDate(currentcommitinfo.getCommitDate());
					int overideAnnotationNumber=0;
					int particularAnnotationNumber=0;
					
					if(toComparePair.getLeft()>=0 && toComparePair.getRight()>=0) {
						
						if(currentcodeline.get(toComparePair.getLeft()).size()>1000 || lastcodeline.get(toComparePair.getRight()).size()>1000
								||currentcodeline.get(toComparePair.getLeft()).equals(lastcodeline.get(toComparePair.getRight()))) {
							// due to time constraint, we currently ignore annotation use change calcuation on large files
							// also ignore situation where no changes have been made to the real source code file
						}
						else {
							infoEveryCommit.setChangeType("M");
							infoEveryCommit.setBugFixingCommitType1(committyperesult[0]);
							infoEveryCommit.setBugFixingCommitType2(committyperesult[1]);
							infoEveryCommit.setClassName(currentcommitinfo.getJavaClassList().get(toComparePair.getLeft()));
							infoEveryCommit.setAnnotationUseNumber(currentAnnotationUse.get(toComparePair.getLeft()).size());
							infoEveryCommit.setAnnotationDefNumber(currentAnnotationdef.get(toComparePair.getLeft()).size());
							for (int nbAnnotationUse=0; nbAnnotationUse<currentAnnotationUse.get(toComparePair.getLeft()).size();nbAnnotationUse++) {
								if((((currentAnnotationUse.get(toComparePair.getLeft())).get(nbAnnotationUse)).getQualifiedName()).equals("java.lang.Override"))
									overideAnnotationNumber+=1;
							}
							infoEveryCommit.setOverideAnnotationNumber(overideAnnotationNumber);
						//	System.out.println(fileNameSuffix);
							infoEveryCommit.setClassSize(currentcodeline.get(toComparePair.getLeft()).size());
							infoEveryCommit.setCodechurnNumber(getCodeChurn(lastcodeline.get(toComparePair.getRight()),currentcodeline.get(toComparePair.getLeft())));
							if (currentAnnotationUse.get(toComparePair.getLeft()).size() >0 || lastAnnotationUse.get(toComparePair.getRight()).size() >0 ) {
									
								  CtPackage currentpackage=currentPackage.get(toComparePair.getLeft());
							      CtPackage lastpackage=lastPackage.get(toComparePair.getRight());
							      
							      AnnotationUseChange usechange=compareAnnotationUse(lastpackage,currentpackage);
							      infoEveryCommit.setAnnotationUseAddE(usechange.getAnnotationUseAddE());
							      infoEveryCommit.setAnnotationUseAddN(usechange.getAnnotationUseAddN());
							      infoEveryCommit.setAnnotationUseChange(usechange.getAnnotationUseChange());
							      infoEveryCommit.setAnnotationUseDeleteE(usechange.getAnnotationUseDeleteE());
							      infoEveryCommit.setAnnotationUseDeleteN(usechange.getAnnotationUseDeleteN());
							      infoEveryCommit.setAnnotationUseUpdate(usechange.getAnnotationUseUpdate());
							      infoEveryCommit.setChangeSpecificAnnotation(usechange.getChangeSpecificAnnotation());
							      infoEveryCommit.setUpdateAnnotationRelated(usechange.getUpdateAnnotationRelated());
							      infoEveryCommit.setAnnotationUnrelatedEdit(usechange.getAnnotationUnrelatedEdit());
							
							}
							
							AnnotationDefChange defchange=compareAnnotationDef(currentAnnotationdef.get(toComparePair.getLeft()),
									lastAnnotationdef.get(toComparePair.getRight()));
							
							infoEveryCommit.setAnnotationDefAdd(defchange.getAnnotationDefAdd());
							infoEveryCommit.setAnnotationDefDelete(defchange.getAnnotationDefDelete());
							infoEveryCommit.setMetaAnnotationAdd(defchange.getMetaAnnotationAdd());
							infoEveryCommit.setMetaAnnotationDelete(defchange.getMetaAnnotationDelete());
							infoEveryCommit.setMetaAnnotationValueChange(defchange.getMetaAnnotationValueChange());
							infoEveryCommit.setAnnotationReplace(defchange.getAnnotationReplace());
							infoEveryCommit.setRentionTargetChange(defchange.getRentionTargetChange());
							infoEveryCommit.setChangeSpecificMetaAnnotation(defchange.getChangetoSpecificAnnotation());
							infoEveryCommit.setMethodAdd(defchange.getMethodAdd());
							infoEveryCommit.setMethodDelete(defchange.getMethodDelete());
							infoEveryCommit.setMethodChange(defchange.getMethodChange());
							infoEveryCommit.setMethodReplace(defchange.getMethodReplace());
							
							commitInfoToFlush.add(infoEveryCommit);
						}
					} else if (toComparePair.getLeft()>=0 && toComparePair.getRight()==-1) {
						infoEveryCommit.setChangeType("A");
						infoEveryCommit.setBugFixingCommitType1(false);			
						infoEveryCommit.setBugFixingCommitType2(false);
						infoEveryCommit.setClassName(currentcommitinfo.getJavaClassList().get(toComparePair.getLeft()));
						infoEveryCommit.setClassSize(currentcodeline.get(toComparePair.getLeft()).size());
						infoEveryCommit.setCodechurnNumber(currentcodeline.get(toComparePair.getLeft()).size());
						infoEveryCommit.setAnnotationUseNumber(currentAnnotationUse.get(toComparePair.getLeft()).size());
						infoEveryCommit.setAnnotationDefNumber(currentAnnotationdef.get(toComparePair.getLeft()).size());
						infoEveryCommit.setAnnotationUseAddN(currentAnnotationUse.get(toComparePair.getLeft()).size());
						infoEveryCommit.setAnnotationDefAdd(currentAnnotationdef.get(toComparePair.getLeft()).size());
						for (int nbAnnotationUse=0; nbAnnotationUse<currentAnnotationUse.get(toComparePair.getLeft()).size();nbAnnotationUse++) {
							String name=((currentAnnotationUse.get(toComparePair.getLeft())).get(nbAnnotationUse)).getQualifiedName();
							if(name.equals("java.lang.Override"))
								overideAnnotationNumber+=1;
							if(specificannotations.contains(name))
								particularAnnotationNumber+=1;
						}
						infoEveryCommit.setOverideAnnotationNumber(overideAnnotationNumber);
						infoEveryCommit.setChangeSpecificAnnotation(particularAnnotationNumber);
						
						commitInfoToFlush.add(infoEveryCommit);
					} else if (toComparePair.getLeft()==-1 && toComparePair.getRight()>=0) {
						// delete file
						infoEveryCommit.setChangeType("D");
						
						infoEveryCommit.setBugFixingCommitType1(committyperesult[0]);
						infoEveryCommit.setBugFixingCommitType2(committyperesult[1]);
						infoEveryCommit.setClassName(currentcommitinfo.getJavaClassList().get(toComparePair.getRight()));
						infoEveryCommit.setClassSize(0);
						infoEveryCommit.setCodechurnNumber(lastcodeline.get(toComparePair.getRight()).size());
						infoEveryCommit.setAnnotationDefDelete(lastAnnotationdef.get(toComparePair.getRight()).size());
						infoEveryCommit.setAnnotationUseDeleteN(lastAnnotationUse.get(toComparePair.getRight()).size());
						for (int nbAnnotationUse=0; nbAnnotationUse<lastAnnotationUse.get(toComparePair.getRight()).size();nbAnnotationUse++) {
							String name=((lastAnnotationUse.get(toComparePair.getRight())).get(nbAnnotationUse)).getQualifiedName();
							if(specificannotations.contains(name))
								particularAnnotationNumber+=1;
						}
						infoEveryCommit.setChangeSpecificAnnotation(particularAnnotationNumber);
						
						commitInfoToFlush.add(infoEveryCommit);
					}
                }
            }
			if(commitInfoToFlush.size()>100) {
				writeToCsvFile(this.repositoryPath);
				commitInfoToFlush.clear();
			}
		}
		
		if(whetherFinalCommit) {
		    writeToCsvFile(this.repositoryPath);
		    commitInfoToFlush.clear();
		}
	}
	
	private int estimatelinemumber(String filename) {
		
	    int linenumber = 0;
        try{
   		File file =new File(filename);
   		if(file.exists()){
   		    FileReader fr = new FileReader(file);
   		    LineNumberReader lnr = new LineNumberReader(fr);
   	        while (lnr.readLine() != null){
   	        	linenumber++;
   	        }
   	     	lnr.close();
   		}else{
   			 System.out.println("File does not exists!");
   		}
   	} catch(IOException e){
   		e.printStackTrace();
   	}
       return linenumber;
	}
	
	private List<AnnotationShortDesc> getAnnotationUseDescPackage(CtPackage packagename) {	
		
		List<AnnotationShortDesc> annotationUseInfo = new ArrayList<AnnotationShortDesc>();
		
		@SuppressWarnings("rawtypes")
		List<CtAnnotation> annotations=packagename.getElements(new TypeFilter<>(CtAnnotation.class));	
		for (@SuppressWarnings("rawtypes") CtAnnotation a : annotations) { 
			if(!(a.getAnnotatedElement() instanceof CtAnnotationType)) {
				AnnotationShortDesc desc= getAnnotationShortDesc(a);
				if(!desc.getQualifiedName().isEmpty())
					annotationUseInfo.add(desc);
			}
		}
        return annotationUseInfo;
	}
	
    private List<AnnotationShortDesc> getAnnotationUseDescElement(CtElement elementname) {	
		
		List<AnnotationShortDesc> annotationUseInfo = new ArrayList<AnnotationShortDesc>();
		
		List<CtAnnotation<? extends Annotation>> annotations=elementname.getAnnotations();	
		for (@SuppressWarnings("rawtypes") CtAnnotation a : annotations) { 
			if(!(a.getAnnotatedElement() instanceof CtAnnotationType)) {
				AnnotationShortDesc desc= getAnnotationShortDesc(a);
				if(!desc.getQualifiedName().isEmpty())
					annotationUseInfo.add(desc);
			}
		}
        return annotationUseInfo;
	}

	private AnnotationShortDesc getAnnotationShortDesc(@SuppressWarnings("rawtypes") CtAnnotation annotationname) {
		 
		AnnotationShortDesc infoForThisUse=new AnnotationShortDesc();
		String annotationNameHere = "";
		annotationNameHere=getAnnotationName(annotationname);	
		if (!annotationNameHere.isEmpty()) {
			infoForThisUse.setQualifiedName(annotationNameHere);		
			Map maphere=annotationname.getValues();
			@SuppressWarnings("unchecked")
			Set<Object> keyset= maphere.keySet();
			ArrayList<String> keyused=new ArrayList<String>();
			ArrayList<String> valueForKey=new ArrayList<String>();
			for(Object key: keyset) 
			{
				keyused.add(key.toString());
				if(maphere.get(key) instanceof CtNewArray) {
				   CtNewArray names=(CtNewArray)(annotationname.getValue(key.toString()));
				   valueForKey.add("arrayelement+"+names.getElements().size());
				}
				else {
					try {
						valueForKey.add(maphere.get(key).toString());
					} catch (Exception e) {
						valueForKey.add("error:unnormalLiteralImpl");
					}
				}
			}		
			infoForThisUse.setAnnotationKeySet(keyused);
			infoForThisUse.setAnnotationValueSet(valueForKey);
		}		    
		return infoForThisUse;
	}
	
    private AnnotationUseChange compareAnnotationUse(CtPackage previousPackage, CtPackage currentPackage) {	
    	
		AnnotationUseChange targetusechange=new AnnotationUseChange();	
		int annotationUseAddE=0;
		int annotationUseAddN=0;
		int annotationUseDeleteE=0;
		int annotationUseDeleteN=0;
		int annotationUseChange=0;
		int annotationUseUpdate=0;
		int particularannotation=0;
		int updateAnnotationRelated=0;
		int annotationunrelatededit=0;
		
		AstComparator diffCompare = new AstComparator();
        DiffImpl editScript = diffCompare.compare(previousPackage, currentPackage); 
        List<Pair<CtElement, CtElement>> elementPairList = editScript.getElementMapping();
        List<CtElement> updatedElement = getUpdateorMovedElement(editScript);
        annotationunrelatededit=editScript.getRootOperations().size();
        
        for(int pairindex=0;pairindex<elementPairList.size();pairindex++) {
        	Pair<CtElement, CtElement> elementPair=elementPairList.get(pairindex);
        	CtElement previous=elementPair.getLeft();
        	CtElement current=elementPair.getRight();
        	List<AnnotationShortDesc> previousAnnoDesc=getAnnotationUseDescElement(previous);
        	List<AnnotationShortDesc> currentAnnoDesc=getAnnotationUseDescElement(current);
        	int[] compareResult=compareAnnotationLists(previousAnnoDesc,currentAnnoDesc);
        	
        	if(compareResult[0]>0 || compareResult[1]>0 || compareResult[2]>0 || compareResult[3]>0) {
        		for(int innerindex=0; innerindex<updatedElement.size(); innerindex++) {
        			if (updatedElement.get(innerindex)==previous) {
        				updateAnnotationRelated+=(compareResult[0]+compareResult[1]+compareResult[2]+compareResult[3]);
        				break;
        			}
        		}
        	}
      	
			annotationUseDeleteE+=compareResult[0];
			annotationUseChange+=compareResult[1];
			annotationUseUpdate+=compareResult[2];
			annotationUseAddE+=compareResult[3];
			particularannotation+=compareResult[4];  		
        }
        
		List<CtElement> insertedElement = new ArrayList<CtElement>();
		List<CtElement> deletedElement = new ArrayList<CtElement>();
		insertedElement.clear();
		deletedElement.clear();
        
        for (int index = 0; index < editScript.getAllOperations().size(); index++) {
			@SuppressWarnings("rawtypes")
			Operation operation = editScript.getAllOperations().get(index);
			if (operation instanceof InsertOperation) {
				
				CtElement dstNodeInsert;
				try {
					dstNodeInsert = operation.getSrcNode();
				} catch (Exception error) {
					dstNodeInsert = null;
				} 
				
				if(dstNodeInsert!=null && dstNodeInsert.getAnnotations().size()>0) {
					Boolean whetherFoundInsert=false;
					for (int innerInsert=0; innerInsert<insertedElement.size(); innerInsert++) {
						CtElement innerelementInsert= insertedElement.get(innerInsert);
						if(innerelementInsert==dstNodeInsert) {
							whetherFoundInsert=true;
							break;
						}
					}
					if(!whetherFoundInsert) {
						annotationUseAddN+=dstNodeInsert.getAnnotations().size();
						insertedElement.add(dstNodeInsert);
					}
				}		
			} else if(operation instanceof DeleteOperation) {
				
				CtElement dstNodeDelete;
				try {
				    dstNodeDelete = operation.getSrcNode();
				} catch (Exception error) {
					dstNodeDelete = null;
				}
				
				if(dstNodeDelete!=null && dstNodeDelete.getAnnotations().size()>0) {
					Boolean whetherFoundDelete=false;
					for (int innerDelete=0; innerDelete<deletedElement.size(); innerDelete++) {
						CtElement innerelementDelete= deletedElement.get(innerDelete);
						if(innerelementDelete==dstNodeDelete) {
							whetherFoundDelete=true;
							break;
						}
					}
					if(!whetherFoundDelete) {
						annotationUseDeleteN+=dstNodeDelete.getAnnotations().size();
						deletedElement.add(dstNodeDelete);
					}
				}		
			}
		}
			
		targetusechange.setAnnotationUseAddE(annotationUseAddE);
		targetusechange.setAnnotationUseAddN(annotationUseAddN);
		targetusechange.setAnnotationUseDeleteE(annotationUseDeleteE);
		targetusechange.setAnnotationUseDeleteN(annotationUseDeleteN);
		targetusechange.setAnnotationUseChange(annotationUseChange);
		targetusechange.setAnnotationUseUpdate(annotationUseUpdate);
		targetusechange.setChangeSpecificAnnotation(particularannotation);
		targetusechange.setUpdateAnnotationRelated(updateAnnotationRelated);
		targetusechange.setAnnotationUnrelatedEdit(annotationunrelatededit);
		
		return targetusechange;
	}
    
    private List<CtElement> getUpdateorMovedElement(DiffImpl edit) {
    	
    	List<CtElement> toadd = new ArrayList<CtElement>();
    	for (int i = 0; i < edit.getRootOperations().size(); i++) {
			Operation operation = edit.getRootOperations().get(i);
			if (operation instanceof MoveOperation || operation instanceof UpdateOperation) {
				CtElement dstNode = operation.getSrcNode();
				toadd.add(dstNode);
			} 
		} 	
    	return toadd;
    }
    
    private int[] compareAnnotationLists(List<AnnotationShortDesc> previousdesc, 
			List<AnnotationShortDesc> currentdesc) {
		
		int[] result=new int[5]; 
		int annotationUseAddE=0;
		int annotationUseDeleteE=0;
		int annotationUseChange=0;
		int annotationUseUpdate=0;
		
		int equalannotationnumber=0;
		
		int certainannotation=0;
		
		List<String> previousnameset=new ArrayList<String>();
		List<String> currentnameset=new ArrayList<String>();
		
		for(int previousindex=0; previousindex<previousdesc.size(); previousindex++) {
			previousnameset.add(previousdesc.get(previousindex).getQualifiedName());
		}
		
		for(int currentindex=0; currentindex<currentdesc.size(); currentindex++) {
			currentnameset.add(currentdesc.get(currentindex).getQualifiedName());
		}
		
		for(int previousindex=0; previousindex<previousdesc.size(); previousindex++) {
			
			AnnotationShortDesc annotationprevious=previousdesc.get(previousindex);
			
			for(int currentindex=0;currentindex<currentdesc.size();currentindex++) {
				AnnotationShortDesc annotationcurrent=currentdesc.get(currentindex);
				
				if(annotationprevious.getQualifiedName().equals(annotationcurrent.getQualifiedName())) {
					
					equalannotationnumber+=1;
					previousnameset.remove(annotationprevious.getQualifiedName());
					currentnameset.remove(annotationprevious.getQualifiedName());
					if((annotationprevious.getAnnotationKeySet()).
							equals(annotationcurrent.getAnnotationKeySet()) &&
							(annotationprevious.getAnnotationValueSet()).
							equals(annotationcurrent.getAnnotationValueSet())) {
						// same annotation, same value
					}
					else {
						annotationUseUpdate+=1;
						if(specificannotations.contains(annotationprevious.getQualifiedName()))
							certainannotation+=1;
					}		
					break;
				}
			}
		}
		
		int remainingpreviousannotation=previousdesc.size()-equalannotationnumber;
		int remainingcurrentannotation=currentdesc.size()-equalannotationnumber;
		
		if(remainingpreviousannotation<=remainingcurrentannotation) {
			annotationUseChange=remainingpreviousannotation;
			annotationUseDeleteE=0;
			annotationUseAddE=remainingcurrentannotation-remainingpreviousannotation;
		}
		else {
			annotationUseChange=remainingcurrentannotation;
			annotationUseDeleteE=remainingpreviousannotation-remainingcurrentannotation;
			annotationUseAddE=0;
		}
		
		int typea=0;
		for (int innerindex=0; innerindex<currentnameset.size();innerindex++) {
			  if(specificannotations.contains(currentnameset.get(innerindex)))
				  typea+=1;
		  }
		
		int typeb=0;
		for (int innerindex=0; innerindex<previousnameset.size();innerindex++) {
			  if(specificannotations.contains(previousnameset.get(innerindex)))
				  typeb+=1;
		  }
		
		if(typea>=typeb)
			certainannotation+=typea;
		else
			certainannotation+=typeb;
		
		result[0]=annotationUseDeleteE;
		result[1]=annotationUseChange;
		result[2]=annotationUseUpdate;
		result[3]=annotationUseAddE;
		result[4]=certainannotation;
		
		return result;
	}
	
    private void writeToCsvFile(String fileName) {
    	
		FileWriter fileWriter1 = null;
    	FileWriter fileWriter2 = null;
		final String COMMA_DELIMITER = ",";
		final String NEW_LINE_SEPARATOR = "\n";
		try {
			fileWriter1 = new FileWriter(fileName+"/"+"info_for_all_commits_use.csv",true);
			fileWriter2 = new FileWriter(fileName+"/"+"info_for_all_commits_def.csv",true);
			//Write a new student object list to the CSV file
			for (int commitindex=0; commitindex<commitInfoToFlush.size(); commitindex++) {
				FlushCommitInfo infoForACertainCommit=commitInfoToFlush.get(commitindex);
				
				fileWriter1.append(infoForACertainCommit.getProjectName());
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(infoForACertainCommit.getCommitId());
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getWhetherSpoonParseError()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getBugFixingCommitType1()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getBugFixingCommitType2()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(infoForACertainCommit.getClassName());
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(infoForACertainCommit.getChangeType());
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getClassSize()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getCodechurnNumber()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getAnnotationUseNumber()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getOverideAnnotationNumber()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getChangeSpecificAnnotation()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getAnnotationUseAddE()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getAnnotationUseAddN()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getAnnotationUseDeleteE()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getAnnotationUseDeleteN()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getAnnotationUseChange()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getAnnotationUseUpdate()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getUpdateAnnotationRelated()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(String.valueOf(infoForACertainCommit.getAnnotationUnrelatedEdit()));
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(infoForACertainCommit.getAuthorName());
				fileWriter1.append(COMMA_DELIMITER);
				fileWriter1.append(infoForACertainCommit.getCommitDate());
				fileWriter1.append(NEW_LINE_SEPARATOR);
				
				fileWriter2.append(infoForACertainCommit.getProjectName());
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(infoForACertainCommit.getCommitId());
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getWhetherSpoonParseError()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getBugFixingCommitType1()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getBugFixingCommitType2()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(infoForACertainCommit.getClassName());
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(infoForACertainCommit.getChangeType());
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getClassSize()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getCodechurnNumber()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getAnnotationDefNumber()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getAnnotationDefAdd()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getAnnotationDefDelete()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getMetaAnnotationAdd()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getMetaAnnotationDelete()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getMetaAnnotationValueChange()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getAnnotationReplace()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getRentionTargetChange()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getChangeSpecificMetaAnnotation()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getMethodAdd()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getMethodDelete()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getMethodChange()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(String.valueOf(infoForACertainCommit.getMethodReplace()));
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(infoForACertainCommit.getAuthorName());
				fileWriter2.append(COMMA_DELIMITER);
				fileWriter2.append(infoForACertainCommit.getCommitDate());
				fileWriter2.append(NEW_LINE_SEPARATOR);
			}		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {	
			try {
				fileWriter1.flush();
				fileWriter1.close();
				fileWriter2.flush();
				fileWriter2.close();
			} catch (IOException e) {
                e.printStackTrace();
			}	
		}
	}
	
	@SuppressWarnings("rawtypes")
	private String getAnnotationName(CtAnnotation annotation) {
		String annotationNameHere = "";
		try {
			try {
				annotationNameHere = annotation.getType().toString();
			} catch (Exception e) {
				annotationNameHere = annotation.getAnnotationType().toString();
			}
		  } catch (Exception e) {
			annotationNameHere = "";
		  }
		return annotationNameHere;
	}
	
    private AnnotationDefChange compareAnnotationDef(List<AnnotationDefinition> currentAnnotationDef, 
    		List<AnnotationDefinition> previousAnnotationDef) {
		
    	AnnotationDefChange targetchange=new AnnotationDefChange();
    	
    	int annotationDefAdd = 0;
    	int annotationDefDelete = 0;
    	int DefMetaAnnotationAdd = 0;
    	int DefMetaAnnotationDelete = 0;
    	int DefMetaAnnotationValueChange = 0;
    	int DefMetaAnnotationReplace = 0;
    	int ChangetoRentionOrTarget = 0;
    	int ChangetoSpecificAnnotation = 0;
    	int DefMethodAdd = 0;
    	int DefMethodDelete = 0;
    	int DefMethodChange = 0;
    	int DefMethodReplace = 0;
    	
    	if(currentAnnotationDef.size()==0 && previousAnnotationDef.size()==0) {
    		return targetchange;
    	}
    	else if(currentAnnotationDef.size()==0 && previousAnnotationDef.size()!=0) {
    		targetchange.setAnnotationDefDelete(previousAnnotationDef.size());
    		return targetchange;
    	}
    	else if(currentAnnotationDef.size()!=0 && previousAnnotationDef.size()==0) {
    		targetchange.setAnnotationDefAdd(currentAnnotationDef.size());
    		return targetchange;
    	}
    	else {
    		List<String> currentAnnotationName=new ArrayList<String>();
        	for(int indexcurrent=0; indexcurrent<currentAnnotationDef.size(); indexcurrent++)
        		currentAnnotationName.add((currentAnnotationDef.get(indexcurrent)).getQualifiedName());
        	
        	Boolean whetherAnnotationStillExist=false;
    		for(int indexprevious=0; indexprevious<previousAnnotationDef.size();indexprevious++) {
    			
    			whetherAnnotationStillExist=false;
    			String qualifiednameprevious=(previousAnnotationDef.get(indexprevious)).getQualifiedName();
    			
    			for(int indexnow=0; indexnow<currentAnnotationDef.size(); indexnow++) {
    				String qualifiednamenow=(currentAnnotationDef.get(indexnow)).getQualifiedName();
    				if(qualifiednameprevious.equals(qualifiednamenow)) {
    					currentAnnotationName.remove(qualifiednamenow);
    					int[] result=compareTwoDefinitions(currentAnnotationDef, indexnow, previousAnnotationDef, indexprevious);
    					DefMetaAnnotationAdd+=(result[0]>0? 1:0);
    					DefMetaAnnotationDelete+=(result[1]>0? 1:0);
    					DefMetaAnnotationValueChange+=(result[2]>0? 1:0);
    					DefMetaAnnotationReplace+=(result[3]>0? 1:0);
    					ChangetoRentionOrTarget+=(result[4]>0? 1:0);
    					ChangetoSpecificAnnotation+=(result[5]>0? 1:0);
    					
    					DefMethodAdd+=(result[6]>0? 1:0);
    					DefMethodDelete+=(result[7]>0? 1:0);
    					DefMethodChange+=(result[8]>0? 1:0);
    					DefMethodReplace+=(result[9]>0? 1:0);
    					
    					whetherAnnotationStillExist=true;
        				break;
    				}
    			}
    			if(!whetherAnnotationStillExist)
    				annotationDefDelete+=1;
    		}
    		annotationDefAdd+=currentAnnotationName.size();
    		
    		targetchange.setAnnotationDefAdd(annotationDefAdd);
    		targetchange.setAnnotationDefDelete(annotationDefDelete);
    		targetchange.setMetaAnnotationAdd(DefMetaAnnotationAdd);
    		targetchange.setMetaAnnotationDelete(DefMetaAnnotationDelete);
    		targetchange.setMetaAnnotationValueChange(DefMetaAnnotationValueChange);
    		targetchange.setAnnotationReplace(DefMetaAnnotationReplace);
    		targetchange.setRentionTargetChange(ChangetoRentionOrTarget);
    		targetchange.setChangetoSpecificAnnotation(ChangetoSpecificAnnotation);
    		targetchange.setMethodAdd(DefMethodAdd);
    		targetchange.setMethodDelete(DefMethodDelete);
    		targetchange.setMethodChange(DefMethodChange);
    		targetchange.setMethodReplace(DefMethodReplace);
    		
    		return targetchange;
    	}
	}
    
    private int[] compareTwoDefinitions(List<AnnotationDefinition> currentAnnotationDef, int indexnow, 
    		List<AnnotationDefinition> previousAnnotationDef, int indexprevious) {
    	
    	List<Triple<String, ArrayList<String>, ArrayList<String>>> metaannotationinfoprevious =
    			(previousAnnotationDef.get(indexprevious)).getMethaAnnotationInfo();
		List<Triple<String, String, String>> annotationmethodInfoprevious =
				(previousAnnotationDef.get(indexprevious)).getAnnotationMethodInfo();
		
		List<Triple<String, ArrayList<String>, ArrayList<String>>> metaannotationinfonow =
    			(currentAnnotationDef.get(indexnow)).getMethaAnnotationInfo();
		List<Triple<String, String, String>> annotationmethodInfonow =
				(currentAnnotationDef.get(indexnow)).getAnnotationMethodInfo();
		
		int[] result=new int[10];
		int[] resultmetaannotation=getDiffMetaAnnotations(metaannotationinfoprevious, metaannotationinfonow);
		int[] resultannotationmethod=getDiffAnnotationMethods(annotationmethodInfoprevious,annotationmethodInfonow);
		
		result[0]=resultmetaannotation[0];
		result[1]=resultmetaannotation[1];
		result[2]=resultmetaannotation[2];
		result[3]=resultmetaannotation[3];
		result[4]=resultmetaannotation[4];
		result[5]=resultmetaannotation[5];
		
		
		result[6]=resultannotationmethod[0];
		result[7]=resultannotationmethod[1];
		result[8]=resultannotationmethod[2];
		result[9]=resultannotationmethod[3];
		
		return result;
    }
    
    private int[] getDiffAnnotationMethods(List<Triple<String, String, String>> annotationmethodInfoprevious, 
    	List<Triple<String, String, String>> annotationmethodInfonow) { 
    	
    	int[] result=new int[4];
    	
    	int DefMethodAdd = 0;
    	int DefMethodDelete = 0;
    	int DefMethodChange = 0;
    	int DefMethodReplace = 0;
    	
    	int equalmethod=0;
    	for(int indexbefore=0; indexbefore<annotationmethodInfoprevious.size(); indexbefore++) {
    		String methodnameBefore=annotationmethodInfoprevious.get(indexbefore).getLeft();
    		String typebefore=annotationmethodInfoprevious.get(indexbefore).getMiddle();
    		String defaultvaluebefore=annotationmethodInfoprevious.get(indexbefore).getRight();

    		for(int indexcurrent=0;indexcurrent<annotationmethodInfonow.size();indexcurrent++) {
    			String methodnameNow=annotationmethodInfonow.get(indexcurrent).getLeft();
        		String typenow=annotationmethodInfonow.get(indexcurrent).getMiddle();
        		String defaultvaluenow=annotationmethodInfonow.get(indexcurrent).getRight();
        		if(methodnameBefore.equals(methodnameNow)) {
        			equalmethod+=1;
        			if(typebefore.equals(typenow)&&defaultvaluebefore.equals(defaultvaluenow)) 
        			{}
        			else 
        			{
        				DefMethodChange++;
        			}
        			break;
        		}
    		}
    	}
    	
    	int remainingpreviousmethod=annotationmethodInfoprevious.size()-equalmethod;
		int remainingcurrentmethod=annotationmethodInfonow.size()-equalmethod;
		
		if(remainingpreviousmethod<=remainingcurrentmethod) {
			DefMethodReplace=remainingpreviousmethod;
			DefMethodDelete=0;
			DefMethodAdd=remainingcurrentmethod-remainingpreviousmethod;
		}
		else {
			DefMethodReplace=remainingcurrentmethod;
			DefMethodDelete=remainingpreviousmethod-remainingcurrentmethod;
			DefMethodAdd=0;
		}
    	
    	result[0]=DefMethodAdd;
    	result[1]=DefMethodDelete;
    	result[2]=DefMethodChange;
    	result[3]=DefMethodReplace;
    	
    	return result;
    }
    
    private int[] getDiffMetaAnnotations(List<Triple<String, ArrayList<String>, ArrayList<String>>> metaannotationinfoprevious,
    		List<Triple<String, ArrayList<String>, ArrayList<String>>> metaannotationinfonow) {
    	
    	int[] result=new int[6];
    	
    	int DefMetaAnnotationAdd = 0;
    	int DefMetaAnnotationDelete = 0;
    	int DefMetaAnnotationValueChange = 0;
    	int DefMetaAnnotationReplace=0;
    	int ChangetoRentionOrTarget=0;
    	int ChnagetoSpecificMetaAnnotation=0;
    	
    	List<String> previousnameset=new ArrayList<String>();
		List<String> currentnameset=new ArrayList<String>();
		
		for(int previousindex=0; previousindex<metaannotationinfoprevious.size(); previousindex++) {
			previousnameset.add(metaannotationinfoprevious.get(previousindex).getLeft());
		}
		
		for(int currentindex=0; currentindex<metaannotationinfonow.size(); currentindex++) {
			currentnameset.add(metaannotationinfonow.get(currentindex).getLeft());
		}
    	
    	int equalnumber=0;
    	for(int indexbefore=0; indexbefore<metaannotationinfoprevious.size(); indexbefore++) {
    		String metanameBefore=metaannotationinfoprevious.get(indexbefore).getLeft();
    		ArrayList<String> keybefore=metaannotationinfoprevious.get(indexbefore).getMiddle();
    		ArrayList<String> valuebefore=metaannotationinfoprevious.get(indexbefore).getRight();
    		for(int indexcurrent=0;indexcurrent<metaannotationinfonow.size();indexcurrent++) {
    			String metanameNow=metaannotationinfonow.get(indexcurrent).getLeft();
        		ArrayList<String> keynow=metaannotationinfonow.get(indexcurrent).getMiddle();
        		ArrayList<String> valuenow=metaannotationinfonow.get(indexcurrent).getRight();
        		if(metanameBefore.equals(metanameNow)) {
        			previousnameset.remove(metanameBefore);
        			currentnameset.remove(metanameBefore);
        			equalnumber+=1;
        			if(keybefore.equals(keynow)&&valuebefore.equals(valuenow)) 
        			{}
        			else 
        			{
        				DefMetaAnnotationValueChange++;
        				
        				if(metanameBefore.indexOf("java.lang.annotation.Target")!=-1||
        						metanameBefore.indexOf("java.lang.annotation.Retention")!=-1)
        					ChangetoRentionOrTarget+=1;
        			}
        			break;
        		}
    		}
    	}
    	
    	int remainingpreviousannotation=metaannotationinfoprevious.size()-equalnumber;
		int remainingcurrentannotation=metaannotationinfonow.size()-equalnumber;
		
		if(remainingpreviousannotation<=remainingcurrentannotation) {
			DefMetaAnnotationReplace=remainingpreviousannotation;
			DefMetaAnnotationDelete=0;
			DefMetaAnnotationAdd=remainingcurrentannotation-remainingpreviousannotation;
		}
		else {
			DefMetaAnnotationReplace=remainingcurrentannotation;
			DefMetaAnnotationDelete=remainingpreviousannotation-remainingcurrentannotation;
			DefMetaAnnotationAdd=0;
		}
		
		for (int innerindex=0; innerindex<currentnameset.size();innerindex++) {
			  if(specificmetaannotations.contains(currentnameset.get(innerindex)))
				  ChnagetoSpecificMetaAnnotation=1;
		  }
		for (int innerindex=0; innerindex<previousnameset.size();innerindex++) {
			  if(specificmetaannotations.contains(previousnameset.get(innerindex)))
				  ChnagetoSpecificMetaAnnotation=1;
		  }
		
    	result[0]=DefMetaAnnotationAdd;
    	result[1]=DefMetaAnnotationDelete;
    	result[2]=DefMetaAnnotationValueChange;
    	result[3]=DefMetaAnnotationReplace;
    	result[4]=ChangetoRentionOrTarget;
    	result[5]=ChnagetoSpecificMetaAnnotation;
    	
    	return result;
    }
	
	private void switchToCommit(String repositroyPath, String commitSha) { 
		String shellcommand;
		shellcommand="cd "+repositroyPath+" && "+"git config core.fileMode false"
				  +" && " + "git checkout "+commitSha;
		executeCommand(shellcommand);
	}

	private CtPackage getDesiredPackage(String inputSourceFile) {
		Launcher spoon = new Launcher();
		spoon.setArgs(new String[] { "--noclasspath" });
		spoon.addInputResource(inputSourceFile);
		spoon.setSourceOutputDirectory("/tmp/tempory");
		try {
			spoon.buildModel();
			spoon.prettyprint();
		} catch (Exception e) {	
			return null; // ignore source file that causes jdt compile errors
		}	
		
		Factory factory = spoon.getFactory();
		List<CtPackage> packageList = new ArrayList<CtPackage>(factory.Package().getAll());
		CtPackage desiredPackage=packageList.get(packageList.size()-1);
		return desiredPackage;
	}
	
	private void deleteFile() {
		try {
			FileUtils.deleteDirectory(new File("/tmp/tempory"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List<String> fileToLines(String filename) {
		
		String[] splittedstring=filename.split("/");
		String endfilename=splittedstring[splittedstring.length-1];
		
		List<File> files = (List<File>) FileUtils.listFiles(new File("/tmp/tempory"), 
				TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		String filepath="";
		if(files.size()==1&&files.get(0).getAbsolutePath().endsWith(endfilename)) {
			filepath=files.get(0).getAbsolutePath();
		}
		else {
		   for(int fileindex=0; fileindex<files.size(); fileindex++) {
			   if((files.get(fileindex).getAbsolutePath()).endsWith(endfilename))
			   {
				   filepath=files.get(fileindex).getAbsolutePath();
				   break;
			   }
		   }
		}
		
	    List<String> lines = new LinkedList<String>();
	    String line = "";
	    if(!files.isEmpty()&&!filepath.isEmpty()) {
	      try {
	         @SuppressWarnings("resource")
		     BufferedReader in = new BufferedReader(new FileReader(filepath));
	         while ((line = in.readLine()) != null) {
	    	     if(!line.isEmpty())
	               lines.add(line.trim());
	          }
	       } catch (IOException e) {
	        e.printStackTrace();
	       }
	     }
	    
	    return lines;
   }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public int getCodeChurn(List<String> original, List<String> revised) {

		int codechurn=0;
		Patch diff = DiffUtils.diff(original, revised);

	    DiffRowGenerator.Builder builder = new DiffRowGenerator.Builder();
	    builder.showInlineDiffs(false);
	    DiffRowGenerator generator = builder.build();

		List<Delta> deltaset= diff.getDeltas();
	    
	    for (Delta delta : deltaset) {
	        List<DiffRow> generateDiffRows = generator.generateDiffRows(
	                (List<String>) delta.getOriginal().getLines(),
	                (List<String>) delta.getRevised().getLines()
	                );
	        codechurn+=generateDiffRows.size();
	    }
	    
	    return codechurn;
	}
	
    private void executeCommand(String command) {	    	
		String[] cmd = { "/bin/bash", "-c", command};
	    ProcessBuilder pb = new ProcessBuilder(cmd);
	    pb.redirectErrorStream(true);
        Process p = null;
        try {
            p = pb.start();
            p.waitFor();
         } catch (IOException e1) {
            e1.printStackTrace();
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
     }
	
	public boolean[] determineCommitType (String projectName, String inputComment) {
		String processedInputComment=inputComment.toLowerCase();
		String inputComment1="";
		String inputComment2="";
		
		processedInputComment=processedInputComment.replaceAll("error handl", "");
		processedInputComment=processedInputComment.replaceAll("error cod", "");
		processedInputComment=processedInputComment.replaceAll("bug tree", "");
		inputComment1=processedInputComment;
		
		processedInputComment=processedInputComment.replaceAll("javadoc fix", "");
		processedInputComment=processedInputComment.replaceAll("fix javadoc", "");
		processedInputComment=processedInputComment.replaceAll("fixing javadoc", "");
		processedInputComment=processedInputComment.replaceAll("formatting fix", "");
		processedInputComment=processedInputComment.replaceAll("format fix", "");
		processedInputComment=processedInputComment.replaceAll("fix format", "");
		processedInputComment=processedInputComment.replaceAll("fixing format", "");
		processedInputComment=processedInputComment.replaceAll("grammar fix", "");
		processedInputComment=processedInputComment.replaceAll("fix grammar", "");
		processedInputComment=processedInputComment.replaceAll("fixing grammar", "");
		processedInputComment=processedInputComment.replaceAll("typo fix", "");
		processedInputComment=processedInputComment.replaceAll("fix typo", "");
		processedInputComment=processedInputComment.replaceAll("fixing typo", "");
		
		processedInputComment=processedInputComment.replaceAll("issue:", "");
		processedInputComment=processedInputComment.replaceAll("issues:", "");
		processedInputComment=processedInputComment.replaceAll("issue #", "");
		processedInputComment=processedInputComment.replaceAll("issue#", "");
		processedInputComment=processedInputComment.replaceAll("issues #", "");
		processedInputComment=processedInputComment.replaceAll("issues#", "");
		processedInputComment=processedInputComment.replaceAll("issue=", "");
		processedInputComment=processedInputComment.replaceAll("issues=", "");
		processedInputComment=processedInputComment.replaceAll("issue\\s+\\d+", "");
		processedInputComment=processedInputComment.replaceAll("issues\\s+\\d+", "");
		processedInputComment=processedInputComment.replaceAll("issue gh", "");
		processedInputComment=processedInputComment.replaceAll("issue ldap", "");
		processedInputComment=processedInputComment.replaceAll("issues gh", "");
		processedInputComment=processedInputComment.replaceAll("issues ldap", "");
		
		processedInputComment=processedInputComment.replaceAll("fix:", "");
		processedInputComment=processedInputComment.replaceAll("fixes:", "");
		processedInputComment=processedInputComment.replaceAll("fix #", "");
		processedInputComment=processedInputComment.replaceAll("fix#", "");
		processedInputComment=processedInputComment.replaceAll("fixes #", "");
		processedInputComment=processedInputComment.replaceAll("fixes#", "");
		processedInputComment=processedInputComment.replaceAll("fix=", "");
		processedInputComment=processedInputComment.replaceAll("fixes=", "");
		processedInputComment=processedInputComment.replaceAll("fixes gh", "");
		processedInputComment=processedInputComment.replaceAll("fixes ldap", "");
		processedInputComment=processedInputComment.replaceAll("fix gh", "");
		processedInputComment=processedInputComment.replaceAll("fix ldap", "");
		processedInputComment=processedInputComment.replaceAll("fix\\s+\\d+", "");
		processedInputComment=processedInputComment.replaceAll("fixes\\s+\\d+", "");
		if(projectName.indexOf("actor-platform")!=-1)
		   processedInputComment=processedInputComment.replaceAll("fix\\(", "");
		
		processedInputComment=processedInputComment.replaceAll("bug:", "");
		processedInputComment=processedInputComment.replaceAll("bugs:", "");
		processedInputComment=processedInputComment.replaceAll("bug #", "");
		processedInputComment=processedInputComment.replaceAll("bug#", "");
		processedInputComment=processedInputComment.replaceAll("bugs #", "");
		processedInputComment=processedInputComment.replaceAll("bugs#", "");
		processedInputComment=processedInputComment.replaceAll("bug=", "");
		processedInputComment=processedInputComment.replaceAll("bugs=", "");
		processedInputComment=processedInputComment.replaceAll("bug gh", "");
		processedInputComment=processedInputComment.replaceAll("bug ldap", "");
		processedInputComment=processedInputComment.replaceAll("bugs gh", "");
		processedInputComment=processedInputComment.replaceAll("bugs ldap", "");
		processedInputComment=processedInputComment.replaceAll("bug\\s+\\d+", "");
		processedInputComment=processedInputComment.replaceAll("bugs\\s+\\d+", "");
		// processedInputComment=processedInputComment.replaceAll("-bug-fix", "");
		inputComment2=processedInputComment;
		
		List<String> transformedArray1=this.commitTypeAnalyzer.lemmatize(inputComment1);
		boolean whtherBugFixingCommit1=false;
		for (String temp1 : transformedArray1) {
			for(String keyword1 : error_keyword_array) {
				if(temp1.trim().toLowerCase().equals(keyword1)) {
					whtherBugFixingCommit1=true;
					break;
				}
			}
			if(whtherBugFixingCommit1)
				break;
		}
		
		List<String> transformedArray2=this.commitTypeAnalyzer.lemmatize(inputComment2);
		boolean whtherBugFixingCommit2=false;
		for (String temp2 : transformedArray2) {
			for(String keyword2 : error_keyword_array) {
				if(temp2.trim().toLowerCase().equals(keyword2)) {
					whtherBugFixingCommit2=true;
					break;
				}
			}
			if(whtherBugFixingCommit2)
				break;
		}
		
		boolean[] resultarray=new boolean[2];
		resultarray[0] = whtherBugFixingCommit1;
		resultarray[1] = whtherBugFixingCommit2;
		return resultarray;
	}
	
	@SuppressWarnings("rawtypes")
	private List<AnnotationDefinition> getAnnotationDefinion(CtPackage packageName) {
		
		List<AnnotationDefinition> annotationDefinitionInfo = new ArrayList<AnnotationDefinition>();
		
		List<CtAnnotationType> annotations = packageName.getElements(new TypeFilter<>(CtAnnotationType.class));
		
		for (CtAnnotationType a : annotations) {
			AnnotationDefinition infoForThisDef=new AnnotationDefinition();
			infoForThisDef.setQualifiedName(a.getQualifiedName());
			
			List<CtAnnotation<? extends Annotation>> metaAnnotation = a.getAnnotations();
			List<Triple<String, ArrayList<String>, ArrayList<String>>> methaAnnotationInfo=new ArrayList<Triple<String, ArrayList<String>, ArrayList<String>>>();	
			for(int size=0; size<metaAnnotation.size();size++) {
				CtAnnotation metaAnnotationName=metaAnnotation.get(size);
				Map maphere=metaAnnotationName.getValues();
				@SuppressWarnings("unchecked")
				Set<Object> keyset= maphere.keySet();
				ArrayList<String> keyused=new ArrayList<String>();
				ArrayList<String> valueForKey=new ArrayList<String>();
				for(Object key: keyset) 
				{
					keyused.add(key.toString());
					if(maphere.get(key) instanceof CtNewArray) {
					   CtNewArray names=(CtNewArray)(metaAnnotationName.getValue(key.toString()));
					   valueForKey.add("arrayelement+"+names.getElements().size());
					}
					else {
						try {
							valueForKey.add(maphere.get(key).toString());
						} catch (Exception e) {
							valueForKey.add("error:unnormalLiteralImpl");
						}
					}
				}	
				
				Triple<String, ArrayList<String>, ArrayList<String>> metaInformation=Triple.of(
						getAnnotationName(metaAnnotationName), keyused, valueForKey);
				methaAnnotationInfo.add(metaInformation);
			}
			infoForThisDef.setMethaAnnotationInfo(methaAnnotationInfo);
			
			@SuppressWarnings("unchecked")
			Set<CtAnnotationMethod<?>> annotationMemeber = a.getAnnotationMethods(); 
			List<Triple<String, String, String>> annotationMethodInfo=new ArrayList<Triple<String, String, String>>();	
			for(CtAnnotationMethod name: annotationMemeber) {
				   String defaultValue;
				   try{
					   defaultValue=name.getDefaultExpression().toString();
				   } catch(Exception e) {
					   defaultValue="WD";
				   }
				   Triple<String, String, String> annotationMethod=Triple.of(name.getSimpleName(), name.getType().toString(), defaultValue);
				   annotationMethodInfo.add(annotationMethod);
			}
			
			infoForThisDef.setAnnotationMethodInfo(annotationMethodInfo);
			
			annotationDefinitionInfo.add(infoForThisDef);
		}
		
		return annotationDefinitionInfo;
	}
}
