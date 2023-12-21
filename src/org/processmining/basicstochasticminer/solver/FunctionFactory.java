package org.processmining.basicstochasticminer.solver;

public interface FunctionFactory {
	public Function constant(double value);

	public Function variablePower(int parameterIndex, double power);

	public Function variablePower(int parameterIndex, String name, double power);

	public Function product(Function... functions);

	public Function division(Function functionA, Function functionB);

	public Function sum(Function... functions);

	public Function variable(int parameterIndex);

	public Function variable(int parameterIndex, String name);
}