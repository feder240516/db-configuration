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
	
	String[] TEST_RESULTS_HEADERS = {"RDMS", "Component Instance ID", "DBInstance GUID", "Time", "Variable", "Value of variable", "Is default", "Query Profile"};
	
	private static final CSVService _instance = new CSVService();
	public static CSVService getInstance() {
		return _instance;
	}
	
	public List<TestResult> testResults;
	public List<TestResult> failedTests;
	public CSVService() {
		failedTests = new ArrayList<>();
		testResults = new ArrayList<>();
	}
	
	public synchronized void writeTest(TestResult testResult) {
		if (testResult != null) testResults.add(testResult);
	}
	
	public synchronized void addFailedTest(TestResult testResult) {
		if (testResult != null) failedTests.add(testResult);
	}
	
	public void dumpToDisk(String suffix) throws IOException {
		FileWriter fw = new FileWriter(String.format("testResults%s.csv",suffix));
		BufferedWriter bw = new BufferedWriter(fw);
		try (CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT
		    .withHeader(TEST_RESULTS_HEADERS))) {
				for(TestResult testResult: testResults) {
					printer.printRecord(testResult.getRdms(), 
										testResult.getComponentInstanceID(), 
										testResult.getDbInstance(), 
										testResult.getTime(), 
										testResult.getVariable(), 
										testResult.getVariableValue(),
										testResult.isDefault(),
										testResult.getQueryProfileID());
			}
	    }
		bw.close();
		FileWriter fwError = new FileWriter("testFailures.log");
		BufferedWriter bwError = new BufferedWriter(fwError);
		for(TestResult fail: failedTests) {
			bwError.write(String.format("Failed test for db %s, instance %s, testing variable %s with value %f for query profile [%s].\r\n", fail.getRdms(), fail.getComponentInstanceID(), fail.getVariable(), fail.getVariableValue(), fail.getQueryProfileID()));
		}
		bwError.close();
	}
}
