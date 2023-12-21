package org.processmining.basicstochasticminer;

import java.util.BitSet;

public class Enablements {

	private int[] executed;
	private int[] enabled;

	public Enablements(int numberOfTransitions) {
		executed = new int[numberOfTransitions];
		enabled = new int[numberOfTransitions];
	}

	public void addExecution(int executeNext, BitSet enabledt) {
		executed[executeNext]++;

		for (int i = enabledt.nextSetBit(0); i >= 0; i = enabledt.nextSetBit(i + 1)) {
			enabled[i]++;
			if (i == Integer.MAX_VALUE) {
				break;
			}
		}
	}

	public int getExecuted(int transition) {
		return executed[transition];
	}

	public int getEnabled(int transition) {
		return enabled[transition];
	}
}