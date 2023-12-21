package org.processmining.resourcestochasticminer;

import java.util.BitSet;

import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemantics;

/**
 * This semantics serves a double-role: it operates on a high (collapsed) level,
 * but also still supports the calls to the low-level transitions.
 * 
 * @author sander
 *
 */
public interface StochasticLabelledLifeCyclePetriNetSemantics extends StochasticLabelledPetriNetSemantics {

	public StochasticLabelledLifeCyclePetriNetSemantics clone();

	public int lowLevel2highLevelTransition(int lowLevelTransition);

	public int highLevel2lowLevelTransition(int highLevelTransition, PerformanceTransition lifeCycleTransition);

	public PerformanceTransition getLifeCycle(int lowLevelTransition);

	public int getResourceOfName(String resourceName);

	public BitSet getLowLevelTransitionResources(int lowLevelTransition);

	/**
	 * 
	 * @param transition
	 * @param resources
	 *            For each resource, whether that resource is currently busy.
	 * @return the weight of the transition.
	 */
	public double getLowLevelTransitionWeight(int transition, BitSet resources);

	public int getNumberOfHighLevelTransitions();

	public boolean isHighLevelTransitionSilent(int highLevelTransition);

	public BitSet getHighLevelTransitionResources(int highLevelTransition);

	public String getHighLevelTransitionLabel(int highLevelTransition);

}