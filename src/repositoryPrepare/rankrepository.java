package repositoryPrepare;

public class rankrepository {

	public static void main(String[] args) {
		GetAndPrioritizeRepositoryCommit a=new GetAndPrioritizeRepositoryCommit(System.getProperty("user.home")
				+"/annotation-repository"); //Path which contains the downloaded Github repository
		a.initializeRepositoryInfo();
		a.getAccurateCommiAndContributorCount();
		a.bubbleSort();
		a.writeRepositoryNameToShFile(System.getProperty("user.home")+"/result.sh");
	}

}
