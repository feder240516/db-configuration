package services;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import helpers.TestResult;

public class CSVService {
	
	String[] TEST_RESULTS_HEADERS = {"RDMS", "Component Instance ID", "DBInstance GUID", "Time"};
	
	private static final CSVService _instance = new CSVService();
	public static CSVService getInstance() {
		return _instance;
	}
	
	public List<TestResult> testResults;
	public CSVService() {
		testResults = new ArrayList<>();
	}
	
	public synchronized void writeTest(TestResult testResult) {
		if (testResult != null) testResults.add(testResult);
	}
	
	public void dumpToDisk() throws IOException {
		FileWriter fw = new FileWriter("testResults.csv");
		BufferedWriter bw = new BufferedWriter(fw);
		try (CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT
		    .withHeader(TEST_RESULTS_HEADERS))) {
				for(TestResult testResult: testResults) {
					printer.printRecord(testResult.getRdms(), testResult.getComponentInstanceID(), testResult.getDbInstance(), testResult.getTime());
			}
	    }
	}
}
