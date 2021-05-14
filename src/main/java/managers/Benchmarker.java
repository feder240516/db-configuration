package managers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.xml.utils.UnImplNode;
import org.jooq.Query;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.TooManyFailuresException;
import exceptions.UnavailablePortsException;
import handlers.ADatabaseHandle;
import helpers.TestDescription;
import helpers.TestResult;
import junit.framework.AssertionFailedError;
import services.CSVService;

class QueryCallable implements Callable<Double> {

	ADatabaseHandle dbHandle;
	String query;
	
	QueryCallable(ADatabaseHandle dbHandle, String query){
		this.dbHandle = dbHandle;
		this.query = query;
	}
	
	@Override
	public Double call() throws Exception {
		return dbHandle.benchmarkQuery(query);
	}
	
}

public class Benchmarker {
	private static final int MAX_QUERY_RETRIES = 5;
	DBSystemFactory dbSystemFactory;
	TestDescription test;
	Semaphore semaphore;
	
	public Benchmarker(TestDescription test, int maxThreads) {
		this(test,maxThreads,DBSystemFactory.getInstance());
	}
	
	public Benchmarker(TestDescription test, int maxThreads, DBSystemFactory dbSystemFactory) {
		semaphore = new Semaphore(maxThreads);
		this.test = test;
		this.dbSystemFactory = dbSystemFactory;
	}
	
	private double runSingleTest(List<String> lq, ADatabaseHandle dbHandle, ExecutorService executor) throws Exception {
		double singleScore = 0, repetitionScore = 0;
		
		if (lq.size() == 0) { }
		else if (lq.size() == 1) {
			String query = lq.get(0);
			singleScore = new QueryCallable(dbHandle, query).call();
			repetitionScore += singleScore;
		} else {
			List<Callable<Double>> taskList = new ArrayList<>();
			for (String query: lq) {
				taskList.add(new QueryCallable(dbHandle, query));
			}
			List<Future<Double>> results = executor.invokeAll(taskList);
			singleScore = 0;
			try {
				for(Future<Double> result: results) {
					double value = result.get();
					singleScore = Math.max(value, singleScore);
				}
			} catch (Exception e) {
				singleScore = Double.POSITIVE_INFINITY;
			}
			repetitionScore = singleScore == Double.MAX_VALUE ? Double.MAX_VALUE: repetitionScore + singleScore;
		}
		return repetitionScore;
	}
	
	private void writeTestResults(List<Double> testResults, UUID handleID, IComponentInstance componentInstance) {
		for(double repetitionScore: testResults) {
			CSVService.getInstance().writeTest(new TestResult(handleID.toString(),repetitionScore,componentInstance, test.ID));
		}
	}
	
	private void markFailure(UUID handleID, IComponentInstance componentInstance) {
		CSVService.getInstance().addFailedTest(new TestResult(handleID.toString(),-1,componentInstance, test.ID));
	}
	
	private List<Double> runAllTests(int numberOfTests, Map<Integer, List<String>> queries, ADatabaseHandle dbHandle, ExecutorService executor) throws Exception {
		int failures = 0;
		List<Double> scoresForReport = new ArrayList<Double>();
		while(scoresForReport.size() < test.numberOfTests) {
			boolean failed = false;
			double repetitionScore = 0;
			try {
				dbHandle.initiateServer();
				for(List<String> lq: queries.values()) {
					repetitionScore = runSingleTest(lq, dbHandle, executor);
				}
				dbHandle.stopServer();
				scoresForReport.add(repetitionScore);
			}catch(Exception e) {
				failed = true;
				failures++;
			}
			if(failures >= MAX_QUERY_RETRIES) { throw new TooManyFailuresException("TEST_FAILED"); }
		}
		return scoresForReport;
	}
	
	public double benchmark(IComponentInstance componentInstance) throws InterruptedException, ExecutionException, UnavailablePortsException, IOException, SQLException {
		semaphore.acquire();
		double score = 0, singleScore = 0, repetitionScore = 0;
		ADatabaseHandle dbHandle = null;
		UUID handleID = null;
		String dbSystem = componentInstance.getComponent().getName();
		Map<Integer, List<String>> queries = test.generateQueries(dbSystem);
		ExecutorService executor = null;
		try {
			executor = (ExecutorService) Executors.newCachedThreadPool();
			/*int threads = queries.values()
								.stream()
								.max((a,b) -> {return a.size() - b.size();})
								.orElse(new ArrayList<>())
								.size();
			
			if (threads > 1) { executor = (ExecutorService) Executors.newFixedThreadPool(threads); }*/
			dbHandle = dbSystemFactory.createHandle(componentInstance, test);
			handleID = dbHandle.getUUID();
			System.out.println(String.format("Number of tests programmed: %d", test.numberOfTests));
			List<Double> testResults = runAllTests(test.numberOfTests,queries,dbHandle, executor);
			writeTestResults(testResults, handleID, componentInstance);
			score = testResults.stream().reduce(0., (a,b)->{return a + b;});
		} catch (Exception e) {
			e.printStackTrace();
			score = Double.MAX_VALUE;
			if (handleID == null) handleID = UUID.randomUUID();
			markFailure(handleID, componentInstance);
			if (dbHandle != null) dbHandle.stopServer();
		} finally {
			if (dbHandle != null) { dbHandle.cleanup(); }
			if (executor != null) {
				executor.shutdown();
				executor.awaitTermination(999999, TimeUnit.DAYS);
			}
			semaphore.release();
			//System.out.println(String.format("Semaphore released"));
		}
		if (handleID != null) {
			// CSVService.getInstance().writeTest(new TestResult(handleID.toString(),score,componentInstance.getComponent().getName()));
		}
		
        return score;
	}
}
