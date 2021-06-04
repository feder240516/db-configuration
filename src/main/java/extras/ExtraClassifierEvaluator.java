package extras;

import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import benchmark.core.api.IHyperoptObjectEvaluator;

public class ExtraClassifierEvaluator implements IHyperoptObjectEvaluator<IMekaClassifier>{
	@Override
	public Double evaluate(IMekaClassifier arg0, int arg1)
			throws ObjectEvaluationFailedException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxBudget() {
		// TODO Auto-generated method stub
		return 0;
	}

}
