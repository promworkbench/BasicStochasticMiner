package org.processmining.basicstochasticminer.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.basicstochasticminer.BasicStochasticMiner;
import org.processmining.basicstochasticminer.BasicStochasticMinerParameters;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeights;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsEditable;

public class BasicStochasticMinerPlugin {
	@Plugin(name = "Discover stochastic perspective of accepting Petri net with Basic Stochastic Miner", level = PluginLevel.Regular, returnLabels = {
			"Stochastic Labelled Petri Net" }, returnTypes = {
					StochasticLabelledPetriNetSimpleWeights.class }, parameterLabels = { "Log",
							"Accepting Petri net" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine a stochastic labelled Petri net, dialog", requiredParameterLabels = { 0, 1 })
	public StochasticLabelledPetriNetSimpleWeights mineGuiSLPN(final UIPluginContext context, XLog xLog,
			AcceptingPetriNet net) throws Exception {
		BasicStochasticMinerDialog dialog = new BasicStochasticMinerDialog(xLog);
		InteractionResult result = context.showWizard("Mine using Basic Stochastic Miner", true, true, dialog);

		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(false);
			return null;
		}

		BasicStochasticMinerParameters parameters = dialog.getMiningParameters();

		context.log("Mining...");

		return BasicStochasticMiner.mine(xLog, net, parameters, new ProMCanceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		});
	}

	public static StochasticLabelledPetriNetSimpleWeightsEditable mineSLPN(XLog xLog, AcceptingPetriNet net,
			BasicStochasticMinerParameters parameters, ProMCanceller canceller) throws Exception {
		return BasicStochasticMiner.mine(xLog, net, parameters, canceller);
	}
}