package extras;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.processes.ProcessIDNotRetrievableException;
import ai.libs.jaicore.processes.ProcessUtil;
import benchmark.core.api.input.IOptimizerConfig;
import extras.IPlanningOptimizationTask;

public abstract class AHBLikeOptimizer<M> extends APCSBasedOptimizer<M> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AHBLikeOptimizer.class);

	private static final String GRPC_OPT_RUN_SCRIPT = "run.py";
	private static final String GRPC_OPT_WORKER_SCRIPT = "evalworker.py";

	protected AHBLikeOptimizer(final String id, final IOptimizerConfig config, final IPlanningOptimizationTask<M> task) {
		super(id, config, task);
	}

	@Override
	public JsonNode getClientConfig() {
		ObjectMapper om = new ObjectMapper();
		ObjectNode root = om.createObjectNode();
		root.put("gRPC_port", this.getConfig().getPort());
		return root;
	}

	@Override
	public String getRunScript() {
		return GRPC_OPT_RUN_SCRIPT;
	}

	@Override
	public String getWorkerScript() {
		return GRPC_OPT_WORKER_SCRIPT;
	}

	@Override
	protected void runOptimizer() throws Exception {
		ProcessBuilder pb = new ProcessBuilder(AHBLikeOptimizer.this.getCommand()).directory(AHBLikeOptimizer.this.getWorkingDirectory()).redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
		Process p = null;
		int processID = -1;
		try {
			p = pb.start();
			try {
				processID = ProcessUtil.getPID(p);
			} catch (ProcessIDNotRetrievableException e1) {
				LOGGER.warn("Could not get process id.");
			}
			p.waitFor(AHBLikeOptimizer.this.getInput().getGlobalTimeout().milliseconds(), TimeUnit.MILLISECONDS);
		} catch (IOException e) {
			LOGGER.warn("Could not spawn smac process.", e);
		} catch (InterruptedException e) {
			LOGGER.warn("Got interrupted while waiting for the process to finish.");
			e.printStackTrace();
			if (p.isAlive()) {
				if (processID >= 0) {
					try {
						ProcessUtil.killProcess(processID);
					} catch (IOException e1) {
						e1.printStackTrace();
						p.destroyForcibly();
					}
				} else {
					p.destroyForcibly();
				}
			}
		}
	}

	public List<String> getCommand() {
		return Arrays.asList(CONFIG.getPythonCommand(), GRPC_OPT_RUN_SCRIPT, "--min_budget", "1", "--max_budget", "5", "--n_iterations", "100", "--n_workers", this.getConfig().cpus() + "", "--id", this.getID() + "");
	}

}
