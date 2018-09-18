package repositoryPrepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class GetAndPrioritizeRepositoryCommit {
	private String[] repositoryName;
	private int[] commitCount;
	private int[] contributorCount;
	String repositoryBase;

	public GetAndPrioritizeRepositoryCommit(String directoryOfRepositories) {
		this.repositoryBase = directoryOfRepositories;
	}

	public void setRepositoryBase(String directoryOfRepositories) {
		this.repositoryBase = directoryOfRepositories;
	}

	public void initializeRepositoryInfo() {
		
		File folder = new File(this.repositoryBase);
		File[] listOfFiles = folder.listFiles();
		ArrayList<String> downloadedRepositories = new ArrayList<String>();
		for (int i=0; i<listOfFiles.length; i++) {

			if (listOfFiles[i].isDirectory()) {
				String fileNameHere = this.repositoryBase + "/"+ listOfFiles[i].getName();
				File folderHere = new File(fileNameHere);
				File[] listOfFilesHere = folderHere.listFiles();
				for (int j = 0; j < listOfFilesHere.length; j++)
				{
					if (listOfFilesHere[j].isDirectory())
						downloadedRepositories.add(listOfFiles[i].getName() + "/" + listOfFilesHere[j].getName());
				}
			}
		}
		
		repositoryName = new String[downloadedRepositories.size()];
		commitCount = new int[downloadedRepositories.size()];
		contributorCount = new int[downloadedRepositories.size()];
		for (int i = 0; i < downloadedRepositories.size(); i++) {
		//	System.out.println(downloadedRepositories.get(i));
			repositoryName[i] = downloadedRepositories.get(i);
		}
	}

	public void getAccurateCommiAndContributorCount() {

		String getCommitCommand = "git rev-list --all --count";
		String getContributorsCommand = "git log --all --format='%aN' | sort -u";

		for (int index = 0; index < repositoryName.length; index++) {
			String repositoryPath = this.repositoryBase + "/" + repositoryName[index];
			commitCount[index] = getCommitOrContributorNumber(repositoryPath, getCommitCommand, true);
			contributorCount[index] = getCommitOrContributorNumber(repositoryPath, getContributorsCommand, false);
		}
	}

	public void bubbleSort() {
		int temp;
		String tempString = "";
		int size = commitCount.length;
		for (int i = 0; i < size - 1; i++) {
			for (int j = i + 1; j < size; j++) {
				if (commitCount[i] < commitCount[j]) {
					temp = commitCount[i];
					commitCount[i] = commitCount[j];
					commitCount[j] = temp;
					temp = contributorCount[i];
					contributorCount[i] = contributorCount[j];
					contributorCount[j] = temp;
					tempString = repositoryName[i];
					repositoryName[i] = repositoryName[j];
					repositoryName[j] = tempString;
				}
			}
		}
	}

	@SuppressWarnings("unused")
	public void writeRepositoryNameToShFile(String filePathForPriorizedRepository) {
		File fout = new File(filePathForPriorizedRepository);
		String toWriteReal = "";
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout)));
			for (int index = 0; index < repositoryName.length; index++) {
				toWriteReal = repositoryName[index] + "   " + commitCount[index] + "   " + contributorCount[index];
				bw.write(toWriteReal);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getCommitOrContributorNumber(String directory, String commitCommand, Boolean whethercommit) {

		int count = 0;
		String commandString = "cd " + directory + " && ";
		commandString += commitCommand;

		String[] cmd = { "/bin/bash", "-c", commandString };
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		Process p = null;
		try {
			p = pb.start();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = null;
			while ((s = stdInput.readLine()) != null) {
				if(whethercommit)
					count = Integer.parseInt(s);
				else count+= 1;
			}
			p.waitFor();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return count;
	}
}
