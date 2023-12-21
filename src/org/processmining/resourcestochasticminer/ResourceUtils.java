package org.processmining.resourcestochasticminer;

import java.util.BitSet;

public class ResourceUtils {
	public static String toString(StochasticLabelledPetriNetResourceWeights net) {
		StringBuilder result = new StringBuilder();
		for (int transition = 0; transition < net.getNumberOfTransitions(); transition++) {
			result.append("transition " + net.getTransitionLabel(transition));

			if (!net.isTransitionSilent(transition)) {
				result.append(" can be executed by:\n");
				BitSet resources = net.getTransitionResources(transition);
				for (int resource = resources.nextSetBit(0); resource >= 0; resource = resources
						.nextSetBit(resource + 1)) {

					result.append("\t" + net.getResourceName(resource) + "\n");

					if (resource == Integer.MAX_VALUE) {
						break; // or (i+1) would overflow
					}
				}
			}
		}
		return result.toString();
	}
}