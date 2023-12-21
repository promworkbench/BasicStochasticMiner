package org.processmining.resourcestochasticminer;

import java.util.BitSet;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;

public interface StochasticLabelledPetriNetResourceWeights extends StochasticLabelledPetriNet {

	public double getTransitionBaseWeight(int transition);

	public double getTransitionResourceWeight(int transition);

	/**
	 * Do not edit the result, but the caller should make a copy before editing.
	 * 
	 * @param transition
	 * @return
	 */
	public BitSet getTransitionResources(int transition);

	public int getNumberOfResources();

	public String getResourceName(int resource);

	@Override
	public StochasticLabelledLifeCyclePetriNetSemantics getDefaultSemantics();

}