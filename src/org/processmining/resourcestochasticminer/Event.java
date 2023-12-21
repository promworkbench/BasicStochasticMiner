package org.processmining.resourcestochasticminer;

import java.util.Objects;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;

public class Event {

	private StochasticLabelledPetriNetResourceWeights model;

	public Event(StochasticLabelledPetriNetResourceWeights model, IvMMove move, int trace, int resource) {
		this.model = model;
		this.move = move;
		this.trace = trace;
		this.resource = resource;

	}

	public IvMMove getMove() {
		return move;
	}

	public int getTrace() {
		return trace;
	}

	public int getResource() {
		return resource;
	}

	public boolean hasResource() {
		return resource >= 0;
	}

	private final IvMMove move;
	private final int trace;
	private final int resource;

	public int hashCode() {
		return Objects.hash(move, resource, trace);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		return Objects.equals(move, other.move) && resource == other.resource && trace == other.trace;
	}

	public String toString() {
		return model.getTransitionLabel(move.getTreeNode()) + "(" + move.getLifeCycleTransition() + ",r" + resource
				+ ",t" + trace + ")";
	}

}