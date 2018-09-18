package repositoryPrepare;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DownloadRepository {

	public static void main(String[] args) {
		System.setProperty("http.agent", "Chrome");
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		cal.add(Calendar.DATE, -180);
		String dateString = dateFormat.format(cal.getTime());
		// new downloadRepository().searchRepositories("CSharp", dateString,
		// "stars", "desc", 5);
		new GithubRepositoryDownloader().searchRepositories("Java", dateString, "stars", false, "desc", 10);
	}
}
