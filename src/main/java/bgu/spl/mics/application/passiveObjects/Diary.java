package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.application.GsonDiary;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the diary where all reports are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Diary {
	private List<Report> reports;
	private AtomicInteger total;

	private static class DiaryHolder{
		private static Diary instance = new Diary();
	}

	private Diary() {
		reports = new LinkedList<Report>();
		total = new AtomicInteger(0);
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Diary getInstance() {
		return DiaryHolder.instance;
	}

	public List<Report> getReports() {
		return reports;
	}

	/**
	 * adds a report to the diary
	 * @param reportToAdd - the report to add
	 */
	public synchronized void addReport(Report reportToAdd){
		reports.add(reportToAdd);
	}

	/**
	 *
	 * <p>
	 * Prints to a file name @filename a serialized object List<Report> which is a
	 * List of all the reports in the diary.
	 * This method is called by the main method in order to generate the output.
	 */
	public void printToFile(String filename){
		try (FileWriter writer = new FileWriter(filename)) {
			GsonDiary.GsonReport[] gsonReports = new GsonDiary.GsonReport[this.reports.size()];
			for(int i = 0; i < this.reports.size(); i++) {
				Report currReport = this.reports.get(i);
				//Creating a GsonReport Object according to the current report
				GsonDiary.GsonReport gsonReport = new GsonDiary.GsonReport(currReport.getMissionName(),
						currReport.getM(), currReport.getMoneypenny(), currReport.getAgentsSerialNumbers(),
						currReport.getAgentsNames(), currReport.getGadgetName(), currReport.getTimeCreated(),
						currReport.getTimeIssued(), currReport.getQTime());
				gsonReports[i] = gsonReport;
			}
			//Create a GsonDiary object
			GsonDiary gsonDiary = new GsonDiary();
			gsonDiary.reports = gsonReports;
			gsonDiary.total = this.total.intValue();

			//write the GsonDiary object to a json file
			Gson gson = new Gson();
			String json = gson.toJson(gsonDiary);
			writer.write(json);
		}
		catch (IOException e) { }
	}

	/**
	 * Gets the total number of received missions (executed / aborted) be all the M-instances.
	 * @return the total number of received missions (executed / aborted) be all the M-instances.
	 */
	public int getTotal(){
		return total.intValue();
	}

	/**
	 * Increments the total number of received missions by 1
	 */
	public void incrementTotal(){
		//save the current total value into a local variable
		int value = total.intValue();
		//while total != value keep checking and then update total = total+1
		while (!total.compareAndSet(value, value + 1))
				value = total.intValue();
	}
}