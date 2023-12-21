package org.processmining.resourcestochasticminer;

import java.util.BitSet;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;

public class MultiRun2Observations {
	public static Observations getObservations(List<Event> multiRun, int numberOfTraces, int numberOfResources,
			StochasticLabelledPetriNetResourceWeightsEditable model) {
		Observations result;
		{
			StochasticLabelledLifeCyclePetriNetSemantics semantics = model.getDefaultSemantics();
			result = new Observations(semantics.getNumberOfTransitions(), semantics);
		}

		//create a semantics for each trace
		StochasticLabelledLifeCyclePetriNetSemantics[] semanticss = new StochasticLabelledLifeCyclePetriNetSemantics[numberOfTraces];
		for (int trace = 0; trace < numberOfTraces; trace++) {
			semanticss[trace] = model.getDefaultSemantics();
		}

		//initialise busy array showing for each resource whether it is busy
		BitSet busyResources = new BitSet();

		//walk through the multi-run
		for (Event event : multiRun) {

			int transition = event.getMove().getTreeNode();

			StochasticLabelledLifeCyclePetriNetSemantics semantics = semanticss[event.getTrace()];
			BitSet enabledLowLevelTransitions = semantics.getEnabledTransitions();
			double[] highLevelTransition2utilisation = getUtilisations(busyResources, semantics);

			//mask utilisations that are not relevant
			for (int highLevelTransition = 0; highLevelTransition < semantics
					.getNumberOfHighLevelTransitions(); highLevelTransition++) {
				if (!enabledLowLevelTransitions
						.get(semantics.highLevel2lowLevelTransition(highLevelTransition, PerformanceTransition.enqueue))
						&& //
						!enabledLowLevelTransitions.get(semantics.highLevel2lowLevelTransition(highLevelTransition,
								PerformanceTransition.start))) {
					highLevelTransition2utilisation[highLevelTransition] = -1;
				}
			}

			//add observation
			result.see(enabledLowLevelTransitions, highLevelTransition2utilisation,
					semantics.highLevel2lowLevelTransition(transition, event.getMove().getLifeCycleTransition()),
					semantics);

			//update the resources
			if (event.hasResource()) {
				if (event.getMove().isComplete()) {
					busyResources.set(event.getResource(), false);
				} else if (event.getMove().isStart()) {
					busyResources.set(event.getResource(), true);
				}
			}

			//fire transition
			semantics.executeTransition(
					semantics.highLevel2lowLevelTransition(transition, event.getMove().getLifeCycleTransition()));
		}

		return result;
	}

	public static double[] getUtilisations(BitSet resourcesBusy,
			StochasticLabelledLifeCyclePetriNetSemantics semantics) {

		double[] result = new double[semantics.getNumberOfHighLevelTransitions()];
		for (int highLevelTransition = 0; highLevelTransition < semantics
				.getNumberOfHighLevelTransitions(); highLevelTransition++) {
			if (!semantics.isHighLevelTransitionSilent(highLevelTransition)) {

				//get the resources that can execute this high-level transition
				BitSet resourcesThatCanExecute = (BitSet) semantics.getHighLevelTransitionResources(highLevelTransition)
						.clone();

				int canExecute = resourcesThatCanExecute.cardinality();

				resourcesThatCanExecute.and(resourcesBusy);
				int canExecuteAndBusy = resourcesThatCanExecute.cardinality();

				result[highLevelTransition] = canExecuteAndBusy / (canExecute * 1.0);
			}
		}
		return result;
	}
}