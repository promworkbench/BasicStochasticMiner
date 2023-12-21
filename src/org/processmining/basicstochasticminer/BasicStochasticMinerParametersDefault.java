package org.processmining.basicstochasticminer;

import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class BasicStochasticMinerParametersDefault extends BasicStochasticMinerParametersAbstract {

	public BasicStochasticMinerParametersDefault() {
		super(MiningParameters.getDefaultClassifier());
	}

}