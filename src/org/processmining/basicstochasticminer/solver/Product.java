package org.processmining.basicstochasticminer.solver;

public class Product implements Function {

	private final Function[] functions;

	public Product(Function... functions) {
		this.functions = functions;
	}

	public double getValue(double[] parameters) {
		double product = 1;
		for (Function function : functions) {
			product *= function.getValue(parameters);
		}
		return product;
	}

	public double getPartialDerivative(int parameterIndex, double[] parameters) {
		double sum = 0;

		for (int pivot = 0; pivot < functions.length; pivot++) {
			double product = 1;
			for (int f = 0; f < pivot; f++) {
				product *= functions[f].getValue(parameters);
			}
			product *= functions[pivot].getPartialDerivative(parameterIndex, parameters);
			for (int f = pivot + 1; f < functions.length; f++) {
				product *= functions[f].getValue(parameters);
			}
			sum += product;
		}

		return sum;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("(");
		for (int f = 0; f < functions.length; f++) {
			result.append(functions[f]);

			if (f < functions.length - 1) {
				result.append(") * (");
			}
		}
		result.append(")");

		return result.toString();
	}

	public String toLatex() {
		StringBuilder result = new StringBuilder();

		for (int f = 0; f < functions.length; f++) {
			result.append(functions[f].toLatex());

			if (f < functions.length - 1) {
				result.append("");
			}
		}

		return result.toString();
	}

	public boolean isConstant() {
		for (Function function : functions) {
			if (!function.isConstant()) {
				return false;
			}
		}
		return true;
	}
}