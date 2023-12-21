package org.processmining.resourcestochasticminer.plugins;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;

import org.apache.commons.text.StringEscapeUtils;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.resourcestochasticminer.StochasticLabelledPetriNetResourceWeights;

@Plugin(name = "Stochastic labelled resource Petri net exporter", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Inductive visual Miner alignment", "File" }, userAccessible = true)
@UIExportPlugin(description = "Stochastic labelled resource Petri net", extension = "slrpn")
public class StochasticLabelledPetriNetResourceWeightsExportPlugin {

	@PluginVariant(variantLabel = "Dfg export (Directly follows graph)", requiredParameterLabels = { 0, 1 })
	public void exportDefault(UIPluginContext context, StochasticLabelledPetriNetResourceWeights net, File file)
			throws IOException {
		export(net, file);
	}

	public static void export(StochasticLabelledPetriNetResourceWeights net, File file) throws IOException {
		PrintWriter w = null;
		try {
			w = new PrintWriter(file);
			w.println("# number of places");
			w.println(net.getNumberOfPlaces());

			w.println("# initial marking");
			for (int place = 0; place < net.getNumberOfPlaces(); place++) {
				w.println(net.isInInitialMarking(place));
			}

			w.println("# number of resources");
			w.println(net.getNumberOfResources());
			for (int resource = 0; resource < net.getNumberOfResources(); resource++) {
				w.println(StringEscapeUtils.escapeJava(net.getResourceName(resource)));
			}

			w.println("# number of transitions");
			w.println(net.getNumberOfTransitions());
			for (int transition = 0; transition < net.getNumberOfTransitions(); transition++) {
				w.println("# transition " + transition);
				if (net.isTransitionSilent(transition)) {
					w.println("silent");
				} else {
					w.println("label " + StringEscapeUtils.escapeJava(net.getTransitionLabel(transition)));
				}
				w.println("# base weight ");
				w.println(net.getTransitionBaseWeight(transition));

				if (!net.isTransitionSilent(transition)) {
					w.println("# utilisation weight ");
					w.println(net.getTransitionResourceWeight(transition));
				}

				w.println("# number of input places");
				w.println(net.getInputPlaces(transition).length);
				for (int place : net.getInputPlaces(transition)) {
					w.println(place);
				}

				w.println("# number of output places");
				w.println(net.getOutputPlaces(transition).length);
				for (int place : net.getOutputPlaces(transition)) {
					w.println(place);
				}

				w.println("# number of resources");
				w.println(net.getTransitionResources(transition).cardinality());
				BitSet r = net.getTransitionResources(transition);
				for (int i = r.nextSetBit(0); i >= 0; i = r.nextSetBit(i + 1)) {
					w.println(i);
					if (i == Integer.MAX_VALUE) {
						break; // or (i+1) would overflow
					}
				}
			}
		} finally

		{
			if (w != null) {
				w.close();
			}
		}
	}
}