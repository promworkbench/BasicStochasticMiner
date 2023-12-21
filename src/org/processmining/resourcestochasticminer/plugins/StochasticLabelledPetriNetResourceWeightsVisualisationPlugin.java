package org.processmining.resourcestochasticminer.plugins;

import java.text.DecimalFormat;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.resourcestochasticminer.StochasticLabelledPetriNetResourceWeights;
import org.processmining.stochasticlabelledpetrinets.plugins.StochasticLabelledPetriNetVisualisationPlugin;

public class StochasticLabelledPetriNetResourceWeightsVisualisationPlugin
		extends StochasticLabelledPetriNetVisualisationPlugin<StochasticLabelledPetriNetResourceWeights> {

	@Plugin(name = "Stochastic labelled Petri net (resource weights) visualisation", returnLabels = {
			"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
					"stochastic labelled Petri net", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Stochastic labelled Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, StochasticLabelledPetriNetResourceWeights net,
			ProMCanceller canceller) {
		return visualise(net);
	}

	public void decoratePlace(StochasticLabelledPetriNetResourceWeights net, int place, DotNode dotNode) {

	}

	public void decorateTransition(StochasticLabelledPetriNetResourceWeights net, int transition, DotNode dotNode) {
		DecimalFormat f = new DecimalFormat("0.0000");
		f.setMaximumFractionDigits(4);
		f.setMinimumFractionDigits(0);

		StringBuilder label = new StringBuilder();
		label.append("<");

		label.append(f.format(net.getTransitionBaseWeight(transition)));

		double adjustmentFactor = net.getTransitionResourceWeight(transition);
		if (adjustmentFactor != 1.0 && !net.isTransitionSilent(transition)) {
			label.append("*");
			label.append(f.format(adjustmentFactor));
			label.append("^util");
		}

		label.append(">");

		dotNode.setOption("xlabel", label.toString());
	}
}
