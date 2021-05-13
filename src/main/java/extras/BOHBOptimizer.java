package extras;

import benchmark.core.api.input.IOptimizerConfig;
import extras.IPlanningOptimizationTask;
import extras.AHBLikeOptimizer;

/**
 *
 * @author mwever
 *
 */
public class BOHBOptimizer<M> extends AHBLikeOptimizer<M> {

	private static final String NAME = "bohb";

	public BOHBOptimizer(final String id, final IOptimizerConfig config, final IPlanningOptimizationTask<M> builder) {
		super(id, config, builder);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
