package annotationRetriver;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class TestFileFilter {
	
	/* the file filter for java files*/
    final FilenameFilter javaFileFilter = new FilenameFilter(){
	@Override
	public boolean accept(File dir, String name) {
		String lowercaseName = name.toLowerCase();
		return lowercaseName.endsWith(".java");			
	   }
    }; 
	
    private String repositoryPath="";
    
	public TestFileFilter(String pathOfRepository) {
		this.repositoryPath=pathOfRepository;
	}
	
	public void setRepositoryPath (String pathOfRepository) {
		this.repositoryPath=pathOfRepository;
	}
	
	public String getRepositoryPath () {
		return this.repositoryPath;
	}
	
	public ArrayList<File> getSourceJavaFiles() {
		ArrayList<File> javaSourceFileOneRepository = new ArrayList<File>();
		getSourceJavaFilesForOneRepository(new File(getRepositoryPath()),javaFileFilter,javaSourceFileOneRepository);
		return javaSourceFileOneRepository;
	}
	
	/* function used to retrieve the complete set of java files in a repository */
	public void getSourceJavaFilesForOneRepository(File dir, FilenameFilter searchSuffix, ArrayList<File> al) {

		File[] files = dir.listFiles();		
		for(File f : files) {
			String lowercaseName = f.getName().toLowerCase();
			if(lowercaseName.indexOf("test")!=-1)
			{
				/* we do not consider test files in our study */
			}
			else
			{
			   if(f.isDirectory()){
				    /* iterate over every directory */
				   getSourceJavaFilesForOneRepository(f, searchSuffix, al);		
			   } else {
				    /* returns the desired java files */
				   if(searchSuffix.accept(dir, f.getName())){
					  al.add(f);
				  }
			  }
			}
		}
	}
}
