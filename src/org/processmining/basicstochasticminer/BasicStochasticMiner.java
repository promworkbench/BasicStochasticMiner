package org.processmining.basicstochasticminer;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.plugins.InductiveVisualMinerAlignmentComputation;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsEditable;

public class BasicStochasticMiner {

	public static StochasticLabelledPetriNetSimpleWeightsEditable mine(XLog xLog, AcceptingPetriNet model,
			BasicStochasticMinerParameters parameters, ProMCanceller canceller) throws Exception {

		StochasticLabelledPetriNetSimpleWeightsEditable resultNet = AcceptingPetriNet2StochasticLabelledPetriNet
				.convert(model.getNet(), model.getInitialMarking());
		IvMModel iModel = new IvMModel(model);

		//align log and model
		IvMLogFiltered ivmLog = new IvMLogFilteredImpl(
				InductiveVisualMinerAlignmentComputation.align(iModel, xLog, parameters.getClassifier(), canceller));

		//create choice data
		Enablements enablementData = ComputeEnablements.compute(ivmLog, resultNet, canceller);

		for (int node : iModel.getAllNodes()) {
			double weight = 1;
			if (enablementData.getEnabled(node) > 0) {
				weight = enablementData.getExecuted(node) / (enablementData.getEnabled(node) * 1.0);
			}

			resultNet.setTransitionWeight(node, weight);
		}

		return resultNet;
	}

}