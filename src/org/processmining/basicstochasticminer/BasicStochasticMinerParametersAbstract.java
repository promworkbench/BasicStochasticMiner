package org.processmining.basicstochasticminer;

import org.deckfour.xes.classification.XEventClassifier;

public abstract class BasicStochasticMinerParametersAbstract implements BasicStochasticMinerParameters {

	private XEventClassifier classifier;

	public BasicStochasticMinerParametersAbstract(XEventClassifier classifier) {
		this.setClassifier(classifier);
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

}
