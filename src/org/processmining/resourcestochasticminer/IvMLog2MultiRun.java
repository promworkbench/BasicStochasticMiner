package org.processmining.resourcestochasticminer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;

public class IvMLog2MultiRun {
	public static List<Event> convert(IvMLogFiltered log, XEventClassifier classifier,
			StochasticLabelledLifeCyclePetriNetSemantics semantics, StochasticLabelledPetriNetResourceWeights model) {
		List<Event> result = new ArrayList<>();

		//create iterators
		List<TraceIterator> its = new ArrayList<>();
		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();
			TraceIterator itT = new TraceIterator(trace, it.getPosition(), semantics);
			its.add(itT);
		}

		while (hasNext(its)) {
			//find the earliest trace with the earliest timestamp
			TraceIterator winner = getEarliest(its);
			winner.next();
			result.add(new Event(model, winner.get(), winner.getTraceNr(), winner.getResource()));
		}
		return result;
	}

	private static TraceIterator getEarliest(List<TraceIterator> its) {
		Iterator<TraceIterator> itsi = its.iterator();
		TraceIterator winner = null;
		while ((winner == null || !winner.hasNext()) && itsi.hasNext()) {
			winner = itsi.next();
		}
		while (itsi.hasNext()) {
			TraceIterator it = itsi.next();
			if (it.hasNext() && !earlier(winner, it)) {
				winner = it;
			}
		}
		return winner;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return whether a should come before b
	 */
	private static boolean earlier(TraceIterator a, TraceIterator b) {
		assert a != null;
		assert b != null;

		if (a.peek().getLogTimestamp() != null && b.peek().getLogTimestamp() != null) {
			return a.peek().getLogTimestamp() <= b.peek().getLogTimestamp();
		}

		//if a has not timestamp, then return a
		return a.peek().getLogTimestamp() == null;
	}

	private static class TraceIterator implements Iterator<IvMMove> {

		final Iterator<IvMMove> parent;
		final IvMTrace trace;
		private final int traceNr;
		boolean hasNext;
		IvMMove now;
		int posNow;
		int posNext = -1;
		IvMMove next;
		final StochasticLabelledLifeCyclePetriNetSemantics semantics;

		public TraceIterator(IvMTrace trace, int traceNr, StochasticLabelledLifeCyclePetriNetSemantics semantics) {
			parent = trace.iterator();
			this.trace = trace;
			this.traceNr = traceNr;
			this.semantics = semantics;
			findNext();
		}

		public int getResource() {
			if (now.getLifeCycleTransition() == PerformanceTransition.enqueue
					|| now.getLifeCycleTransition() == PerformanceTransition.other
					|| semantics.isTransitionSilent(now.getTreeNode())) {
				return -1;
			}
			if (now.getLifeCycleTransition() == PerformanceTransition.complete) {
				return semantics.getResourceOfName(now.getResource());
			}
			/*
			 * For the start, we need to match "its" completion event. As that
			 * is not known, we need to walk forward to find the first matching
			 * completion event.
			 */
			for (int i = posNow + 1; i < trace.size(); i++) {
				IvMMove complete = trace.get(i);
				if (complete.isComplete() && complete.isModelSync() && complete.getTreeNode() == now.getTreeNode()) {
					return semantics.getResourceOfName(complete.getResource());
				}
			}
			assert false;
			return Integer.MIN_VALUE;
		}

		private void findNext() {
			while (parent.hasNext()) {
				next = parent.next();
				posNext++;
				if (next.isModelSync()) {
					hasNext = true;
					return;
				}
			}

			hasNext = false;
			next = null;
		}

		public boolean hasNext() {
			return hasNext;
		}

		public IvMMove next() {
			now = next;
			posNow = posNext;
			findNext();
			return now;
		}

		public IvMMove get() {
			return now;
		}

		public IvMMove peek() {
			return next;
		}

		public int getTraceNr() {
			return traceNr;
		}
	}

	private static boolean hasNext(List<TraceIterator> its) {
		for (Iterator<IvMMove> it : its) {
			if (it.hasNext()) {
				return true;
			}
		}
		return false;
	}
}