package extras;

import java.util.Map;

import ai.libs.jaicore.components.model.Component;
import ai.libs.jaicore.components.model.Parameter;
import benchmark.core.api.input.IOptimizationTask;


public interface IPlanningOptimizationTask<M> extends IOptimizationTask<M> {

	public Map<Component, Map<Parameter, ParameterRefinementConfiguration>> getParameterRefinementConfiguration();

}
