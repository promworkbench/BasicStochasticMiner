package org.processmining.resourcestochasticminer.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.resourcestochasticminer.StochasticLabelledPetriNetResourceWeights;
import org.processmining.resourcestochasticminer.StochasticLabelledPetriNetResourceWeightsImpl;

@Plugin(name = "Stochastic labelled resource Petri net", parameterLabels = { "Filename" }, returnLabels = {
		"Stochastic labelled resource Petri net" }, returnTypes = { StochasticLabelledPetriNetResourceWeights.class })
@UIImportPlugin(description = "Stochastic labelled resource Petri net files", extensions = { "slrpn" })
public class StochasticLabelledPetriNetResourceWeightsImportPlugin extends AbstractImportPlugin {
	public StochasticLabelledPetriNetResourceWeightsImpl importFromStream(PluginContext context, InputStream input,
			String filename, long fileSizeInBytes) throws Exception {
		return read(input);
	}

	public static StochasticLabelledPetriNetResourceWeightsImpl read(InputStream input)
			throws NumberFormatException, IOException {

		StochasticLabelledPetriNetResourceWeightsImpl result = new StochasticLabelledPetriNetResourceWeightsImpl();

		BufferedReader r = new BufferedReader(new InputStreamReader(input));

		int numberOfPlaces = Integer.parseInt(getNextLine(r));
		for (int place = 0; place < numberOfPlaces; place++) {
			result.addPlace();

			int inInitialMarking = Integer.parseInt(getNextLine(r));
			if (inInitialMarking > 0) {
				result.addPlaceToInitialMarking(place, inInitialMarking);
			}
		}

		int numberOfResources = Integer.parseInt(getNextLine(r));
		for (int resource = 0; resource < numberOfResources; resource++) {
			String resourceName = getNextLine(r);
			result.addResource(resourceName);
		}

		int numberOfTransitions = Integer.parseInt(getNextLine(r));
		for (int transition = 0; transition < numberOfTransitions; transition++) {
			String line = getNextLine(r);
			double baseWeight = Double.parseDouble(getNextLine(r));

			if (line.startsWith("silent")) {
				result.addTransition(baseWeight);
			} else if (line.startsWith("label ")) {
				result.addTransition(line.substring(6), baseWeight);

				//utilisation weight
				{
					double utilisationWeight = Double.parseDouble(getNextLine(r));
					result.setTransitionUtilisationWeight(transition, utilisationWeight);
				}
			} else {
				throw new RuntimeException("invalid transition");
			}

			//incoming places
			{
				int numberOfIncomingPlaces = Integer.parseInt(getNextLine(r));
				for (int p = 0; p < numberOfIncomingPlaces; p++) {
					int place = Integer.parseInt(getNextLine(r));
					result.addPlaceTransitionArc(place, transition);
				}
			}

			//outgoing places
			{
				int numberOfOutgoingPlaces = Integer.parseInt(getNextLine(r));
				for (int p = 0; p < numberOfOutgoingPlaces; p++) {
					int place = Integer.parseInt(getNextLine(r));
					result.addTransitionPlaceArc(transition, place);
				}
			}

			//resources
			{
				int numberOfTransitionResources = Integer.parseInt(getNextLine(r));
				for (int p = 0; p < numberOfTransitionResources; p++) {
					int resource = Integer.parseInt(getNextLine(r));
					result.setTransitionResources(transition, resource, true);
				}
			}
		}

		r.close();

		return result;
	}

	public static String getNextLine(BufferedReader r) throws IOException {
		String line = r.readLine();
		while (line != null && line.startsWith("#")) {
			line = r.readLine();
		}
		return line;
	}
}