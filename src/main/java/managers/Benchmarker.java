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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.xml.utils.UnImplNode;
import org.jooq.Query;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.ADatabaseHandle;
import helpers.TestDescription;
import helpers.TestResult;
import junit.framework.AssertionFailedError;
import services.CSVService;

public class Benchmarker {
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
	
	public double benchmark(IComponentInstance componentInstance) throws InterruptedException, ExecutionException, UnavailablePortsException, IOException, SQLException {
		semaphore.acquire();
		double score = 0, singleScore = 0, repetitionScore = 0;
		ADatabaseHandle dbHandle = null;
		UUID handleID = null;
		String dbSystem = componentInstance.getComponent().getName();
		Map<Integer, List<String>> queries = test.generateQueries(dbSystem);
		try {
			
			dbHandle = dbSystemFactory.createHandle(componentInstance, test);
			handleID = dbHandle.getUUID();
			System.out.println(String.format("Number of tests programmed: %d", test.numberOfTests));
			for(int i = 0; i < test.numberOfTests; ++i) {
				repetitionScore = 0;
				dbHandle.initiateServer();
				for(List<String> lq: queries.values()) {
					if (lq.size() == 1) {
						System.out.println(String.format("query: %s",lq.get(0).toString()));
						singleScore = dbHandle.benchmarkQuery(lq.get(0));
						repetitionScore += singleScore;
					}else {
						// TODO: Handle multiple concurrent queries
						throw new UnsupportedOperationException();
					}
				}
				dbHandle.stopServer();
				score += repetitionScore;
				CSVService.getInstance().writeTest(new TestResult(handleID.toString(),repetitionScore,componentInstance));
			}
		} catch (Exception e) {
			e.printStackTrace();
			score = Double.MAX_VALUE;
			if (dbHandle != null) dbHandle.stopServer();
		} finally {
			if (dbHandle != null) { dbHandle.cleanup(); }
			semaphore.release();
			System.out.println(String.format("Semaphore released"));
		}
		if (handleID != null) {
			// CSVService.getInstance().writeTest(new TestResult(handleID.toString(),score,componentInstance.getComponent().getName()));
		}
		
        return score;
	}
}
