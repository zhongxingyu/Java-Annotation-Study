package repositoryPrepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.json.JSONObject;
import org.json.JSONArray;

public class GithubRepositoryDownloader {
	
	private JsonObject[] repositoryInfoJSON;
	
	public void searchRepositories (String langauageType, String desiredLatesetPushDate, String sortType,
		  boolean fork, String orderType, int maximunPageNumber) {
		
		  repositoryInfoJSON=new JsonObject[maximunPageNumber*100];
		  
		   /* due to the limination of github search api, we can at most return 1000 search results for each 
		    * run of the program, during the next run, we will add the constraint on the number of stars to get the nex
		    * 1000 popular (defined by the number of stars) repositories, for instance, during a certain run of this class, the last 
		    * returned repository (among the 1000 returned repositories) has 937 stars, to get the next 1000 popular 
		    * repositories, the basic search query string will be:
		    * 
		    * String queryString="q=language:"+langauageType+"+stars:<937"+"+fork:"+fork+"+pushed:>="+desiredLatesetPushDate;
		    * 
		    * for the initial run, we do not impose any constraint on the number of stars +stars:500..1115
		    */
		   String queryString="q=language:"+langauageType+"+stars:70..126"+"+fork:"+fork+"+pushed:>="+desiredLatesetPushDate;
		   String baseSearchURL="https://api.github.com/search/repositories?"+queryString+"&sort="+sortType+"&order="+orderType+"&per_page=100";
		   	
		   for (int index=1; index<=maximunPageNumber; index++) {
			  String realSerarchURL= baseSearchURL+"&page="+index+
					  "&access_token=b388ef33418fb289232d9aecf4957d8bf9ac9cfe"; // access_token: the Github access token
			  readRepositoryInfoFromUrl(realSerarchURL, index);
	      }
		   
		  writeRepositoryInfoToJSONFile(System.getProperty("user.home")+"/annotation_study.json");
		  getDownloadShellFile(System.getProperty("user.home")+"/downloadRepository.tcsh");
	} 
	
	private String readAllText(BufferedReader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    String inputLine;
	    while ((inputLine = rd.readLine()) != null) {
	      sb.append(inputLine);
	    }
	    return sb.toString();
	}
	
    private void readRepositoryInfoFromUrl(String url, int originalIndex) {	  
    	
	    try {
	      InputStream is = new URL(url).openStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAllText(rd);
	      JSONArray libsArray = (JSONArray)new JSONObject (jsonText).get("items");
	      for (int index=0; index<libsArray.length();index++) {
	    	  JSONObject currentJson =(JSONObject)libsArray.get(index); 
	    	  String repository_Name= currentJson.get("full_name").toString(); 
	    	  int[] contributorAndCommit= getRepositoryContributorsAndCommit(repository_Name);
	  		  JsonObject jsonObject = Json.createObjectBuilder()
	  			.add("Repository_index", (originalIndex-1)*100+index)
	  		    .add("Repository_Name", currentJson.get("full_name").toString())
	  		    .add("Repository_URL", currentJson.get("html_url").toString())
	  		    .add("Repository_Stars", Integer.valueOf(currentJson.get("stargazers_count").toString()))
	  		    .add("Repository_Forks", Integer.valueOf(currentJson.get("forks_count").toString()))
	  		    .add("Repository_Clone_Address", currentJson.get("clone_url").toString())
	  		    .add("Repository_Creation_Date", currentJson.get("created_at").toString())
	  		    .add("Repository_Commit_Date", currentJson.get("pushed_at").toString())
	  		    .add("Repository_Update_Date", currentJson.get("updated_at").toString())
	  		    .add("Contributors_Number", contributorAndCommit[0])
	  		    .add("Commits_Number", contributorAndCommit[1])
	  			   .build();
	  		    repositoryInfoJSON[(originalIndex-1)*100+index]=jsonObject;
	       }
	      is.close();
	     } catch (IOException e) {
  			e.printStackTrace();
  		}
     }
    
    private int[] getRepositoryContributorsAndCommit (String repositoryName) { 
    	
    	int[] actualRepositoryData = new int[] {0,0};
    	
    	String searchURL= "https://api.github.com/repos/"+repositoryName+"/contributors?&per_page=100&page=";
    	Boolean callAgain=true;
    	int initialPageNumber=1;
    	
    	while(callAgain) {
    	  int[] pageRepositoryData=actualCalcuateRepositoryContributorAndCommit(searchURL, initialPageNumber);
    	  actualRepositoryData[0]+=pageRepositoryData[0];
    	  actualRepositoryData[1]+=pageRepositoryData[1];
    	  if(pageRepositoryData[0]<100) {
    		callAgain=false;
    	  }
    	  else{
    		callAgain=true;
    		initialPageNumber+=1;
    	  }
    	}
    	
    	return actualRepositoryData; 
    }
    
    private int[] actualCalcuateRepositoryContributorAndCommit (String basicURL, int pageNumber) { 
        
    	int[] repositoryData = new int[2]; 
    	String realSearchURL= basicURL+Integer.toString(pageNumber)+"&access_token=b388ef33418fb289232d9aecf4957d8bf9ac9cfe";
    	int nbContributor=0, nbCommit=0;

		try {
			InputStream is = new URL(realSearchURL).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		    JSONArray json = new JSONArray (readAllText(rd));
		    nbContributor=json.length();
		    for (int index=0; index<json.length();index++) {
		    	  JSONObject currentJson =(JSONObject)json.get(index);  
		    	  nbCommit+= Integer.valueOf(currentJson.get("contributions").toString());
		       }
		      is.close();
		      
		      repositoryData[0]=nbContributor;
			  repositoryData[1]=nbCommit;
		 } catch (IOException e) {
			e.printStackTrace();
		} 

    	return repositoryData;   	
    }
    
    private void getDownloadShellFile(String filaName) {
		
		File fout = new File(filaName);
	    try {
	    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout)));
		    for (int index=0; index<repositoryInfoJSON.length; index++) {
				bw.write("git clone "+repositoryInfoJSON[index].get("Repository_Clone_Address"));
				bw.newLine();
				bw.flush();
			 } 
			 bw.close();
		}
	    catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeRepositoryInfoToJSONFile (String jsonFileName) {
		
		Map<String, Object> properties = new HashMap<String, Object>(1);
		properties.put(JsonGenerator.PRETTY_PRINTING, true);

		try {	
		PrintWriter pw = new PrintWriter(jsonFileName);
		JsonGeneratorFactory jgf = Json.createGeneratorFactory(properties);
		JsonGenerator jg=jgf.createGenerator(pw).writeStartObject();

		 for (int index=0; index<repositoryInfoJSON.length; index++) {
			 jg.writeStartObject(Integer.toString(index)) 
	        .write("Repository_Name", repositoryInfoJSON[index].get("Repository_Name"))     
	        .write("Repository_URL", repositoryInfoJSON[index].get("Repository_URL"))   
	        .write("Repository_Stars", repositoryInfoJSON[index].get("Repository_Stars"))
	        .write("Repository_Forks", repositoryInfoJSON[index].get("Repository_Forks"))
	        .write("Repository_Clone_Address", repositoryInfoJSON[index].get("Repository_Clone_Address"))
	        .write("Repository_Creation_Date", repositoryInfoJSON[index].get("Repository_Creation_Date"))
	        .write("Repository_Commit_Date", repositoryInfoJSON[index].get("Repository_Commit_Date"))
	        .write("Repository_Update_Date", repositoryInfoJSON[index].get("Repository_Update_Date"))
	        .write("Contributors_Number", repositoryInfoJSON[index].get("Contributors_Number"))
 	        .write("Commits_Number", repositoryInfoJSON[index].get("Commits_Number"))
	        .writeEnd();
			 jg.flush();
			 if((index+1)==repositoryInfoJSON.length)	
			{
				jg.writeEnd();
				jg.close();
			}
		  }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
