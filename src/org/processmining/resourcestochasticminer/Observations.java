package org.processmining.resourcestochasticminer;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;
import org.processmining.resourcestochasticminer.Observations.ResourceMarkingState;

import gnu.trove.map.hash.THashMap;

public class Observations implements Iterable<Entry<ResourceMarkingState, int[]>> {

	public class ResourceMarkingState {

		final BitSet enabledLowLevelTransitions;
		final double[] highLevelTransition2resourceUtilisation;

		public ResourceMarkingState(BitSet enabledLowLevelTransitions2,
				double[] highLevelTransition2resourceUtilisation) {
			this.enabledLowLevelTransitions = enabledLowLevelTransitions2;
			this.highLevelTransition2resourceUtilisation = highLevelTransition2resourceUtilisation;
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(highLevelTransition2resourceUtilisation);
			result = prime * result + Objects.hash(enabledLowLevelTransitions);
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ResourceMarkingState other = (ResourceMarkingState) obj;
			return Objects.equals(enabledLowLevelTransitions, other.enabledLowLevelTransitions) && Arrays
					.equals(highLevelTransition2resourceUtilisation, other.highLevelTransition2resourceUtilisation);
		}

		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("$\\{");
			for (int lowLevelTransition = enabledLowLevelTransitions
					.nextSetBit(0); lowLevelTransition >= 0; lowLevelTransition = enabledLowLevelTransitions
							.nextSetBit(lowLevelTransition + 1)) {

				result.append(semantics.getTransitionLabel(lowLevelTransition));
				result.append(lifeCycle(semantics.getLifeCycle(lowLevelTransition)));

				if (enabledLowLevelTransitions.nextSetBit(lowLevelTransition + 1) != -1) {
					result.append(", ");
				}

				if (lowLevelTransition == Integer.MAX_VALUE) {
					break; // or (i+1) would overflow
				}
			}
			result.append("\\}$");
			result.append("&");
			result.append("$\\{");
			//utilisation
			for (int highLevelTransition = 0; highLevelTransition < semantics
					.getNumberOfHighLevelTransitions(); highLevelTransition++) {
				if (highLevelTransition2resourceUtilisation[highLevelTransition] > -1) {
					result.append(semantics.getHighLevelTransitionLabel(highLevelTransition));
					result.append("\\rightarrow ");
					result.append(highLevelTransition2resourceUtilisation[highLevelTransition]);
					if (hasNext(highLevelTransition, highLevelTransition2resourceUtilisation)) {
						result.append(",");
					}
				}
			}
			result.append("\\}$");

			return result.toString();
		}
	}

	private boolean hasNext(int start, double[] highLevelTransition2resourceUtilisation) {
		for (int highLevelTransition = start + 1; highLevelTransition < semantics
				.getNumberOfHighLevelTransitions(); highLevelTransition++) {
			if (highLevelTransition2resourceUtilisation[highLevelTransition] > -1) {
				return true;
			}
		}
		return false;
	}

	final int numberOfLowLevelTransitions;
	final THashMap<ResourceMarkingState, int[]> enabled2fired;
	final StochasticLabelledLifeCyclePetriNetSemantics semantics;

	public Observations(int numberOfLowLevelTransitions, StochasticLabelledLifeCyclePetriNetSemantics semantics) {
		this.numberOfLowLevelTransitions = numberOfLowLevelTransitions;
		enabled2fired = new THashMap<>();
		this.semantics = semantics;
	}

	public Iterator<Entry<ResourceMarkingState, int[]>> iterator() {
		return enabled2fired.entrySet().iterator();
	}

	public void see(BitSet enabledLowLevelTransitions, double[] lowLevelTransition2utilisation,
			int lowLevelTransitionFired, StochasticLabelledLifeCyclePetriNetSemantics semantics) {
		ResourceMarkingState key = new ResourceMarkingState(enabledLowLevelTransitions, lowLevelTransition2utilisation);
		enabled2fired.putIfAbsent(key, new int[numberOfLowLevelTransitions]);
		enabled2fired.get(key)[lowLevelTransitionFired]++;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		for (Entry<ResourceMarkingState, int[]> e : enabled2fired.entrySet()) {
			result.append(e.getKey().toString());
			result.append(toStringExecuted(e.getValue()));
			result.append("\\\\\n");
		}

		return result.toString();
	}

	public static String lifeCycle(PerformanceTransition t) {
		switch (t) {
			case complete :
				return "^\\text{c}";
			case enqueue :
				return "^\\text{e}";
			case other :
				break;
			case start :
				return "^\\text{s}";
			default :
				break;
		}
		return null;
	}

	public String toStringExecuted(int[] fired) {
		StringBuilder result = new StringBuilder();
		result.append("&$[");

		for (int lowLevelTransition = 0; lowLevelTransition < semantics
				.getNumberOfTransitions(); lowLevelTransition++) {
			if (fired[lowLevelTransition] > 0) {
				result.append("{");
				result.append(semantics.getTransitionLabel(lowLevelTransition));
				result.append(lifeCycle(semantics.getLifeCycle(lowLevelTransition)));
				result.append("}");
				if (fired[lowLevelTransition] > 1) {
					result.append("^{");
					result.append(fired[lowLevelTransition]);
					result.append("}");
				}

				if (hasNext(lowLevelTransition, fired)) {
					result.append(",");
				}
			}
		}
		result.append("]$");
		return result.toString();
	}

	private boolean hasNext(int start, int[] fired) {
		for (int lowLevelTransition = start + 1; lowLevelTransition < semantics
				.getNumberOfTransitions(); lowLevelTransition++) {
			if (fired[lowLevelTransition] > 0) {
				return true;
			}
		}
		return false;
	}

	public int size() {
		return enabled2fired.size();
	}
}