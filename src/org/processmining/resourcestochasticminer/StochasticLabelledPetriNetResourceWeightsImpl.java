package org.processmining.resourcestochasticminer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetImpl;

import gnu.trove.list.array.TDoubleArrayList;

public class StochasticLabelledPetriNetResourceWeightsImpl extends StochasticLabelledPetriNetImpl
		implements StochasticLabelledPetriNetResourceWeightsEditable {

	private List<String> resources;
	private TDoubleArrayList transitionBaseWeights;
	private TDoubleArrayList transitionUtilisationWeights;
	private List<BitSet> transition2resources;

	public StochasticLabelledPetriNetResourceWeightsImpl() {
		resources = new ArrayList<>();
		transitionBaseWeights = new TDoubleArrayList();
		transitionUtilisationWeights = new TDoubleArrayList();
		transition2resources = new ArrayList<>();
	}

	@Override
	public int addTransition(String label, double weight) {
		int result = super.addTransition(label, weight);
		transitionBaseWeights.add(weight);
		transitionUtilisationWeights.add(Double.POSITIVE_INFINITY);
		transition2resources.add(new BitSet());
		return result;
	}
	
	public double getTransitionBaseWeight(int transition) {
		return transitionBaseWeights.get(transition);
	}

	public double getTransitionResourceWeight(int transition) {
		return transitionUtilisationWeights.get(transition);
	}

	public BitSet getTransitionResources(int transition) {
		return transition2resources.get(transition);
	}

	public int getNumberOfResources() {
		return resources.size();
	}

	public String getResourceName(int resource) {
		return resources.get(resource);
	}

	public StochasticLabelledLifeCyclePetriNetSemantics getDefaultSemantics() {
		return new StochasticLabelledLifeCyclePetriNetSemanticsImpl(this);
	}

	public void setTransitionBaseWeight(int transition, double weight) {
		transitionBaseWeights.set(transition, weight);
	}

	public void setTransitionUtilisationWeight(int transition, double weight) {
		transitionUtilisationWeights.set(transition, weight);
	}

	public void setTransitionResources(int transition, int resource, boolean required) {
		transition2resources.get(transition).set(resource, required);
	}

	public int addResource(String name) {
		resources.add(name);
		return resources.size() - 1;
	}

}