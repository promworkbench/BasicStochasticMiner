package org.processmining.resourcestochasticminer;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetEditable;

public interface StochasticLabelledPetriNetResourceWeightsEditable
		extends StochasticLabelledPetriNetResourceWeights, StochasticLabelledPetriNetEditable {

	public void setTransitionBaseWeight(int transition, double weight);

	public void setTransitionUtilisationWeight(int transition, double weight);

	public void setTransitionResources(int transition, int resource, boolean required);

	public int addResource(String name);

}