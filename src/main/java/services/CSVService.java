package services;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import helpers.TestResult;

public class CSVService {
	
	String[] TEST_RESULTS_HEADERS = {"RDMS", "Component Instance ID", "DBInstance GUID", "Time", "Variable", "Value of variable", "Is default", "Query Profile"};
	private double startingPoint = System.currentTimeMillis();
	private String algorithm = "NOT_SPECIFIED";
	private String experimentUUID = "UNKNOWN";
	
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
		if (testResult != null) {
			testResults.add(testResult);
			testResult.setAlgorithm(algorithm);
			testResult.setExperimentUUID(experimentUUID);
		}
	}
	
	public synchronized void addFailedTest(TestResult testResult) {
		if (testResult != null) {
			failedTests.add(testResult);
			testResult.setAlgorithm(algorithm);
			testResult.setExperimentUUID(experimentUUID);
		}
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
	
	public void dumpWithVars(String suffix) throws IOException {
		FileWriter fw = new FileWriter(String.format("testResults%s.csv",suffix));
		BufferedWriter bw = new BufferedWriter(fw);
		Set<String> varNames = new HashSet<String>();
		testResults.forEach((result) -> {
			varNames.addAll(result.getParameterKeys());
		});
		List<String> finalHeaders = new ArrayList<>();
		finalHeaders.add("RDMS");
		finalHeaders.add("Component Instance ID");
		finalHeaders.add("DBInstance GUID");
		finalHeaders.add("Time");
		finalHeaders.addAll(varNames);
		String[] finalHeadersArr = finalHeaders.toArray(new String[0]);
		try (CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT
			    .withHeader(finalHeadersArr))) {
			for(TestResult result: testResults) {
				List<String> valuesToPrint = new ArrayList<>();
				valuesToPrint.add(result.getRdms());
				valuesToPrint.add(result.getComponentInstanceID());
				valuesToPrint.add(result.getDbInstance());
				valuesToPrint.add(String.valueOf(result.getTime()));
				for(int i = 4; i < finalHeadersArr.length; ++i) {
					valuesToPrint.add(result.getParameterValues(finalHeadersArr[i]));
				}
				printer.printRecord(valuesToPrint);
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
	
	public void setStartingPoint() {
		this.startingPoint = System.currentTimeMillis();
		this.testResults.clear();
		this.failedTests.clear();
	}
	
	public void dumpWithVars2(String suffix) throws IOException {
		int numTests = testResults.size(); 
		String algo = numTests > 0 ? testResults.get(testResults.size()-1).getExperimentUUID() : "UNKNOWN";
		FileWriter fw = new FileWriter(String.format("reports/testResults_%s_%d_experiment_%s.csv",suffix,(long)System.currentTimeMillis(),algo));
		BufferedWriter bw = new BufferedWriter(fw);
		Set<String> varNames = new HashSet<String>();
		testResults.forEach((result) -> {
			varNames.addAll(result.getParameterKeys());
		});
		List<String> finalHeaders = new ArrayList<>();
		finalHeaders.add("RDMS");
		finalHeaders.add("Component Instance ID");
		finalHeaders.add("DBInstance GUID");
		finalHeaders.add("Time");
		finalHeaders.add("Algorithm");
		finalHeaders.add("Timestamp");
		finalHeaders.add("Experiment number");
		finalHeaders.add("Query Profile")
		finalHeaders.addAll(varNames);
		String[] finalHeadersArr = finalHeaders.toArray(new String[0]);
		try (CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT
			    .withHeader(finalHeadersArr))) {
			for(TestResult result: testResults) {
				List<String> valuesToPrint = new ArrayList<>();
				valuesToPrint.add(result.getRdms());
				valuesToPrint.add(result.getComponentInstanceID());
				valuesToPrint.add(result.getDbInstance());
				valuesToPrint.add(String.valueOf(result.getTime()));
				valuesToPrint.add(result.getAlgorithm());
				valuesToPrint.add(String.valueOf(result.getTimestamp() - this.startingPoint));
				valuesToPrint.add(result.getExperimentUUID());
				valuesToPrint.add(result.getQueryProfileID())
				for(int i = 7; i < finalHeadersArr.length; ++i) {
					valuesToPrint.add(result.getParameterValues(finalHeadersArr[i]));
				}
				printer.printRecord(valuesToPrint);
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
	
	public void dumpWithVars3(String suffix) throws IOException {
		int numTests = testResults.size(); 
		String algo = numTests > 0 ? testResults.get(testResults.size()-1).getExperimentUUID() : "UNKNOWN";
		FileWriter fw = new FileWriter(String.format("reports/testResults_%s_%d_%s.csv",suffix,(long)System.currentTimeMillis(),algo));
		BufferedWriter bw = new BufferedWriter(fw);
		Set<String> varNames = new HashSet<String>();
		testResults.forEach((result) -> {
			varNames.addAll(result.getParameterKeys());
		});
		List<String> finalHeaders = new ArrayList<>();
		finalHeaders.add("RDMS");
		finalHeaders.add("Component Instance ID");
		finalHeaders.add("DBInstance GUID");
		finalHeaders.add("Time");
		finalHeaders.add("Algorithm");
		finalHeaders.add("Timestamp");
		finalHeaders.add("Experiment number");
		finalHeaders.addAll(varNames);
		String[] finalHeadersArr = finalHeaders.toArray(new String[0]);
		try (CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT
			    .withHeader(finalHeadersArr))) {
			for(TestResult result: testResults) {
				List<String> valuesToPrint = new ArrayList<>();
				valuesToPrint.add(result.getRdms());
				valuesToPrint.add(result.getComponentInstanceID());
				valuesToPrint.add(result.getDbInstance());
				valuesToPrint.add(String.valueOf(result.getTime()));
				valuesToPrint.add(result.getAlgorithm());
				valuesToPrint.add(String.valueOf(result.getTimestamp() - this.startingPoint));
				valuesToPrint.add(result.getExperimentUUID());
				for(int i = 7; i < finalHeadersArr.length; ++i) {
					valuesToPrint.add(result.getParameterValues(finalHeadersArr[i]));
				}
				printer.printRecord(valuesToPrint);
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

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setExperimentUUID(String experimentID) {
		this.experimentUUID = experimentID;
	}
}
