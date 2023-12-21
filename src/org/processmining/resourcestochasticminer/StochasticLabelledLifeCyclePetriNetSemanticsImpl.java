package org.processmining.resourcestochasticminer;

import java.util.BitSet;

import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemanticsImpl;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class StochasticLabelledLifeCyclePetriNetSemanticsImpl extends StochasticLabelledPetriNetSemanticsImpl
		implements StochasticLabelledLifeCyclePetriNetSemantics {

	private StochasticLabelledPetriNetResourceWeights highLevelNet;
	private TObjectIntMap<String> resource2id = new TObjectIntHashMap<>(10, 0.5f, -1);
	private Wrap wrap;

	private static class Wrap extends StochasticLabelledPetriNetResourceWeightsImpl {
		private int[] highLevel2lowLevelEnqueue;
		private int[] highLevel2lowLevelStart;
		private int[] highLevel2lowLevelComplete;
		private int[] highLevel2lowLevelSilent;
		private TIntArrayList lowLevel2highLevel;
	}

	public StochasticLabelledLifeCyclePetriNetSemanticsImpl(StochasticLabelledPetriNetResourceWeights net) {
		super(expandNet(net));
		wrap = expandNet(net);
		this.highLevelNet = net;

		for (int resource = 0; resource < net.getNumberOfResources(); resource++) {
			resource2id.put(net.getResourceName(resource), resource);
		}
	}

	@Override
	public StochasticLabelledLifeCyclePetriNetSemanticsImpl clone() {
		StochasticLabelledLifeCyclePetriNetSemanticsImpl result;
		result = (StochasticLabelledLifeCyclePetriNetSemanticsImpl) super.clone();

		result.highLevelNet = highLevelNet;

		return result;
	}

	@Override
	public double getTransitionWeight(int transition) {
		assert false;
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public double getTotalWeightOfEnabledTransitions() {
		assert false;
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public int lowLevel2highLevelTransition(int lowLevelTransition) {
		return wrap.lowLevel2highLevel.get(lowLevelTransition);
	}

	@Override
	public int highLevel2lowLevelTransition(int highLevelTransition, PerformanceTransition lifeCycleTransition) {
		if (highLevelNet.isTransitionSilent(highLevelTransition)) {
			return wrap.highLevel2lowLevelSilent[highLevelTransition];
		}
		switch (lifeCycleTransition) {
			case complete :
				return wrap.highLevel2lowLevelComplete[highLevelTransition];
			case enqueue :
				return wrap.highLevel2lowLevelEnqueue[highLevelTransition];
			case start :
				return wrap.highLevel2lowLevelStart[highLevelTransition];
			case other :
			default :
				break;
		}
		assert false;
		return Integer.MAX_VALUE;
	}

	@Override
	public PerformanceTransition getLifeCycle(int lowLevelTransition) {
		int highLevelTransition = lowLevel2highLevelTransition(lowLevelTransition);

		if (highLevelNet.isTransitionSilent(highLevelTransition)) {
			return PerformanceTransition.complete;
		}

		if (wrap.highLevel2lowLevelComplete[highLevelTransition] == lowLevelTransition) {
			return PerformanceTransition.complete;
		}
		if (wrap.highLevel2lowLevelStart[highLevelTransition] == lowLevelTransition) {
			return PerformanceTransition.start;
		}
		if (wrap.highLevel2lowLevelEnqueue[highLevelTransition] == lowLevelTransition) {
			return PerformanceTransition.enqueue;
		}

		return null;
	}

	@Override
	public int getResourceOfName(String resourceName) {
		if (resourceName == null) {
			return -1;
		}
		return resource2id.get(resourceName);
	}

	public static Wrap expandNet(StochasticLabelledPetriNetResourceWeights net) {
		Wrap result = new Wrap();
		result.highLevel2lowLevelEnqueue = new int[net.getNumberOfTransitions()];
		result.highLevel2lowLevelStart = new int[net.getNumberOfTransitions()];
		result.highLevel2lowLevelComplete = new int[net.getNumberOfTransitions()];
		result.highLevel2lowLevelSilent = new int[net.getNumberOfTransitions()];
		result.lowLevel2highLevel = new TIntArrayList();

		//add places (numbers stay the same)
		for (int place = 0; place < net.getNumberOfPlaces(); place++) {
			result.addPlace();
			result.addPlaceToInitialMarking(place, net.isInInitialMarking(place));
		}

		//add transitions
		for (int highLevelTransition = 0; highLevelTransition < net.getNumberOfTransitions(); highLevelTransition++) {
			if (net.isTransitionSilent(highLevelTransition)) {
				int silent = result.addTransition(net.getTransitionBaseWeight(highLevelTransition));
				result.highLevel2lowLevelSilent[highLevelTransition] = silent;
				result.lowLevel2highLevel.add(highLevelTransition);

				for (int place : net.getInputPlaces(highLevelTransition)) {
					result.addPlaceTransitionArc(place, silent);
				}
				for (int place : net.getOutputPlaces(highLevelTransition)) {
					result.addTransitionPlaceArc(silent, place);
				}
			} else {
				//enqueue
				int enqueue = result.addTransition(net.getTransitionLabel(highLevelTransition),
						net.getTransitionBaseWeight(highLevelTransition));
				result.highLevel2lowLevelEnqueue[highLevelTransition] = enqueue;
				result.lowLevel2highLevel.add(highLevelTransition);

				//start
				int start = result.addTransition(net.getTransitionLabel(highLevelTransition),
						net.getTransitionBaseWeight(highLevelTransition));
				result.highLevel2lowLevelStart[highLevelTransition] = start;
				result.lowLevel2highLevel.add(highLevelTransition);

				//complete
				int complete = result.addTransition(net.getTransitionLabel(highLevelTransition),
						net.getTransitionBaseWeight(highLevelTransition));
				result.highLevel2lowLevelComplete[highLevelTransition] = complete;
				result.lowLevel2highLevel.add(highLevelTransition);

				int enqPsta = result.addPlace();
				result.addTransitionPlaceArc(enqueue, enqPsta);
				result.addPlaceTransitionArc(enqPsta, start);

				int staPcom = result.addPlace();
				result.addTransitionPlaceArc(start, staPcom);
				result.addPlaceTransitionArc(staPcom, complete);

				for (int place : net.getInputPlaces(highLevelTransition)) {
					result.addPlaceTransitionArc(place, enqueue);
				}
				for (int place : net.getOutputPlaces(highLevelTransition)) {
					result.addTransitionPlaceArc(complete, place);
				}
			}
		}

		return result;
	}

	public double getUtilisation(BitSet resourcesBusy, int transition) {
		BitSet resourcesThatCanExecute = (BitSet) highLevelNet.getTransitionResources(transition).clone();
		resourcesThatCanExecute.and(resourcesBusy);
		int canExecuteAndBusy = resourcesThatCanExecute.cardinality();

		int canExecute = highLevelNet.getTransitionResources(transition).cardinality();

		return canExecuteAndBusy / (canExecute * 1.0);
	}

	@Override
	public double getLowLevelTransitionWeight(int transition, BitSet resources) {
		if (highLevelNet.isTransitionSilent(transition)) {
			return highLevelNet.getTransitionBaseWeight(transition);
		}
		return highLevelNet.getTransitionBaseWeight(transition)
				* Math.pow(highLevelNet.getTransitionResourceWeight(transition), getUtilisation(resources, transition));
	}

	public BitSet getLowLevelTransitionResources(int lowLevelTransition) {
		if (getLifeCycle(lowLevelTransition) == PerformanceTransition.enqueue
				|| isTransitionSilent(lowLevelTransition)) {
			return new BitSet();
		}

		int transition = lowLevel2highLevelTransition(lowLevelTransition);
		return highLevelNet.getTransitionResources(transition);
	}

	public int getNumberOfHighLevelTransitions() {
		return highLevelNet.getNumberOfTransitions();
	}

	public boolean isHighLevelTransitionSilent(int highLevelTransition) {
		return highLevelNet.isTransitionSilent(highLevelTransition);
	}

	public BitSet getHighLevelTransitionResources(int highLevelTransition) {
		return highLevelNet.getTransitionResources(highLevelTransition);
	}

	public String getHighLevelTransitionLabel(int highLevelTransition) {
		return highLevelNet.getTransitionLabel(highLevelTransition);
	}
}