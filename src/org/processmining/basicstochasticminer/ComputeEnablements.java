package org.processmining.basicstochasticminer;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemantics;

public class ComputeEnablements {
	public static Enablements compute(IvMLogFiltered log, StochasticLabelledPetriNet net, ProMCanceller canceller) {
		Enablements result = new Enablements(net.getNumberOfTransitions());

		StochasticLabelledPetriNetSemantics semantics = net.getDefaultSemantics();

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			semantics.setInitialState();

			for (IvMMove move : trace) {
				if (move.isModelSync() && move.isComplete()) {
					int transition = move.getTreeNode();

					result.addExecution(transition, semantics.getEnabledTransitions());

					semantics.executeTransition(transition);
				}
			}

			if (canceller.isCancelled()) {
				return null;
			}

		}
		return result;
	}
}