package org.processmining.resourcestochasticminer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.processmining.basicstochasticminer.solver.Constant;
import org.processmining.basicstochasticminer.solver.Equation;
import org.processmining.basicstochasticminer.solver.Function;
import org.processmining.basicstochasticminer.solver.FunctionFactory;
import org.processmining.basicstochasticminer.solver.FunctionFactoryImpl;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceTransition;
import org.processmining.resourcestochasticminer.Observations.ResourceMarkingState;

public class Observations2Functions {

	public static List<Equation> convert(Observations observations, StochasticLabelledPetriNetResourceWeights net,
			StochasticLabelledLifeCyclePetriNetSemantics semantics) {
		FunctionFactory functionFactory = new FunctionFactoryImpl(1, new int[] {});
		//FunctionFactory functionFactory = new FunctionFactoryNonReducing(0, new int[0]);
		List<Equation> result = new ArrayList<>();

		StringBuilder string = new StringBuilder();

		for (Entry<ResourceMarkingState, int[]> e : observations) {
			ResourceMarkingState state = e.getKey();
			int[] fired = e.getValue();

			int sumOfEnabledLowLevelTransitions = sum(fired);

			//			System.out.println();
			//			System.out.println(e.getKey());
			//			System.out.println("executed: " + observations.toStringExecuted(e.getValue()));

			for (int lowLevelTransition = state.enabledLowLevelTransitions
					.nextSetBit(0); lowLevelTransition >= 0; lowLevelTransition = state.enabledLowLevelTransitions
							.nextSetBit(lowLevelTransition + 1)) {
				double targetFraction = fired[lowLevelTransition] / (sumOfEnabledLowLevelTransitions * 1.0);
				int highLevelTransition = semantics.lowLevel2highLevelTransition(lowLevelTransition);
				double highLevelUtilisation = state.highLevelTransition2resourceUtilisation[highLevelTransition];

				Function transitionWeight = getTransitionWeight(functionFactory, lowLevelTransition,
						highLevelUtilisation, semantics);
				Function sum;
				{
					List<Function> arr = new ArrayList<>();
					for (int enabledLowLevelTransition = state.enabledLowLevelTransitions.nextSetBit(
							0); enabledLowLevelTransition >= 0; enabledLowLevelTransition = state.enabledLowLevelTransitions
									.nextSetBit(enabledLowLevelTransition + 1)) {

						int enabledHighLevelTransition = semantics
								.lowLevel2highLevelTransition(enabledLowLevelTransition);
						double highLevelEnabledUtilisation = state.highLevelTransition2resourceUtilisation[enabledHighLevelTransition];
						arr.add(getTransitionWeight(functionFactory, enabledLowLevelTransition,
								highLevelEnabledUtilisation, semantics));

						if (enabledLowLevelTransition == Integer.MAX_VALUE) {
							break; // or (i+1) would overflow
						}
					}
					Function[] arrr = new Function[arr.size()];
					sum = functionFactory.sum(arr.toArray(arrr));
				}

				Function division = functionFactory.division(transitionWeight, sum);
				int cardinality = fired[lowLevelTransition];

				Equation equation = new Equation(targetFraction, division, cardinality);

				string.append(state.toString() + //
						observations.toStringExecuted(fired) + "&");

				if (!(equation.getFunction() instanceof Constant) && cardinality > 0) {
					//					System.out.println(equation);

					string.append("$\\frac{" + fired[lowLevelTransition] + "}{" + sumOfEnabledLowLevelTransitions
							+ "}$&" + "${}=" + division.toLatex() + "$" + "&" + cardinality);

					result.add(equation);
				}

				string.append("\\\\\n");
			}
		}
//		System.out.println(string.toString());
		return result;
	}

	private static int sum(int[] arr) {
		int result = 0;
		for (int a : arr) {
			result += a;
		}
		return result;
	}

	private static Function getTransitionWeight(FunctionFactory functionFactory, int lowLevelTransition,
			double highLevelUtilisation, StochasticLabelledLifeCyclePetriNetSemantics semantics) {

		int transition = semantics.lowLevel2highLevelTransition(lowLevelTransition);
		PerformanceTransition lifeCycle = semantics.getLifeCycle(lowLevelTransition);
		String label = semantics.getHighLevelTransitionLabel(transition);

		if (lifeCycle == PerformanceTransition.complete || semantics.isHighLevelTransitionSilent(transition)
				|| semantics.getHighLevelTransitionResources(transition).cardinality() == 0) {
			return functionFactory.variable(transition * 2, "b(" + label + ")");
		} else {
			return functionFactory.product(//
					functionFactory.variable(transition * 2, "b(" + label + ")"), //b 
					functionFactory.variablePower(transition * 2 + 1, "u(" + label + ")", highLevelUtilisation)); // u^y
		}
	}

}