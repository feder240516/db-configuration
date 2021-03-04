package managers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.xml.utils.UnImplNode;

import com.healthmarketscience.sqlbuilder.Query;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import ai.libs.jaicore.components.api.IComponentInstance;
import exceptions.UnavailablePortsException;
import handlers.ADatabaseHandle;
import helpers.TestDescription;

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
		double score = 0;
		ADatabaseHandle dbHandle = dbSystemFactory.createHandle(componentInstance, test);
		try {
			for(int i = 0; i < test.numberOfTests; ++i) {
				dbHandle.initiateServer();
				for(List<Query> lq: test.queries.values()) {
					if (lq.size() == 1) {
						score += dbHandle.benchmarkQuery(lq.get(0));
					}else {
						// TODO: Handle multiple concurrent queries
						throw new UnsupportedOperationException();
					}
				}
				dbHandle.stopServer();
			}
		} catch (IOException | SQLException | InterruptedException | UnavailablePortsException e) {
			e.printStackTrace();
			score = Double.MAX_VALUE;
		}
		dbHandle.cleanup();
        
        semaphore.release();
        return score;
	}
}
