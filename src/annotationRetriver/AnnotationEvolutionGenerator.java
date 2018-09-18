package annotationRetriver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class AnnotationEvolutionGenerator {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		String filePath="";
		String annotaionRelativePath="annotation-data";
		
	    filePath = AnnotationEvolutionGenerator.class.getResource("").toString().replace("file:","").
	    		replace("target/classes","src");
	    filePath = filePath.substring(0,filePath.lastIndexOf("/"));
	    filePath = filePath.substring(0,filePath.lastIndexOf("/"));
	    filePath = filePath.substring(0,filePath.lastIndexOf("/"));
	    
	    downloadRepository(filePath, annotaionRelativePath); 
	    
	    List<String> repositoryPathSet = new ArrayList<String>();
	    
	    List<String> repositoryname = new ArrayList<String>();
	    
		try {
			for (String line : FileUtils.readLines(new File(filePath+"/download-repository.sh"))) {
				    String refactoredline=(line.trim()).split(" ")[3];
				    refactoredline=refactoredline.replace("./", "");
				    repositoryname.add(refactoredline);
				    repositoryPathSet.add(filePath+"/"+annotaionRelativePath+"/"+refactoredline);
			}
		} catch (IOException e) {

		}
	    
	    commitLogGeneration(repositoryPathSet);
		
		StanfordLemmatizer commitTypeAnalyzer=new StanfordLemmatizer();
		
        String repositoryBase=filePath+"/"+annotaionRelativePath+"/";
		
		for (String repository : repositoryname) {
			
			CommitLogTransformer transformer=new CommitLogTransformer(repositoryBase+"/"+repository, 
					commitTypeAnalyzer, repository);
			transformer.initialize();
			transformer.transform();
			transformer.commitInfoToFlush.clear();
		}
	}
	
   public static void commitLogGeneration(List<String> repositoryPath) {
		
		String shellcommand="";
		for (int index=0; index<repositoryPath.size(); index++) {
			shellcommand="cd "+repositoryPath.get(index)+" && "+"git log --after=\"2014-10-28\" --before=\"2017-10-28\" --no-merges --numstat > log.txt"
		  +" && " + "chmod -R 777 ./log.txt";
			executeCommand(shellcommand);
		}	
	}
	
	public static void downloadRepository(String basePath, String annotaionRelPath) {
		
		String annotationPath=basePath+"/"+annotaionRelPath;
		new File(annotationPath).mkdirs();
		String downloadCommand="cd "+annotationPath+" && cp " +basePath+"/download-repository.sh ."+" && chmod 777 ./download-repository.sh"+" && ./download-repository.sh";
		executeCommand(downloadCommand);
	}
	
    private static void executeCommand(String command) {	
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
}