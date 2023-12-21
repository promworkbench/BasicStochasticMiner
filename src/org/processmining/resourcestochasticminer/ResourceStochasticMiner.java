package org.processmining.resourcestochasticminer;

import java.util.Arrays;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.basicstochasticminer.BasicStochasticMinerParameters;
import org.processmining.basicstochasticminer.solver.Equation;
import org.processmining.basicstochasticminer.solver.Solver;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.plugins.InductiveVisualMinerAlignmentComputation;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

public class ResourceStochasticMiner {

//	public static void main(String[] args) throws Exception {
//		String name = "BPI_Challenge_2013_incidents";
//
//		PluginContext context = new FakeContext();
//		XLog log = (XLog) new OpenLogFileLiteImplPlugin().importFile(context,
//				new File("/home/sander/Documents/work/svn/60 - inter-trace stochastics - Jana/" + name + ".xes.gz"));
//
//		AcceptingPetriNet net = AcceptingPetriNetFactory.createAcceptingPetriNet();
//		net.importFromStream(context, new FileInputStream(
//				new File("/home/sander/Documents/work/svn/60 - inter-trace stochastics - Jana/" + name + ".apnml")));
//
//		System.out.println(name);
//		StochasticLabelledPetriNetResourceWeightsEditable snet = mine(log, net,
//				new BasicStochasticMinerParametersDefault(), new ProMCanceller() {
//					public boolean isCancelled() {
//						return false;
//					}
//				});
//
//		StochasticLabelledPetriNetResourceWeightsExportPlugin.export(snet,
//				new File("/home/sander/Documents/work/svn/60 - inter-trace stochastics - Jana/" + name + ".slrpn"));
//
//		StochasticLabelledPetriNetResourceWeightsImportPlugin.read(new FileInputStream(
//				new File("/home/sander/Documents/work/svn/60 - inter-trace stochastics - Jana/" + name + ".slrpn")));
//	}

	public static StochasticLabelledPetriNetResourceWeightsEditable mine(XLog xLog, AcceptingPetriNet net,
			BasicStochasticMinerParameters parameters, ProMCanceller canceller) throws Exception {
		//create the result
		StochasticLabelledPetriNetResourceWeightsImpl result = new StochasticLabelledPetriNetResourceWeightsImpl();
		AcceptingPetriNet2StochasticLabelledPetriNet.convert(net.getNet(), net.getInitialMarking(), result);

		//align the log
		long startAlignment = System.currentTimeMillis();
		debug("align");
		IvMLogFiltered ivmLog = new IvMLogFilteredImpl(InductiveVisualMinerAlignmentComputation.align(new IvMModel(net),
				xLog, parameters.getClassifier(), canceller));
		long timeAlignment = (System.currentTimeMillis() - startAlignment) / 1000;

		long start = System.currentTimeMillis();

		//get resource model
		debug("get resource model");
		setResourceModel(ivmLog, result);
		debug(ResourceUtils.toString(result));

		//log to multi-run
		debug("get multi-run");
		StochasticLabelledLifeCyclePetriNetSemantics semantics = result.getDefaultSemantics();
		List<Event> multiRun = IvMLog2MultiRun.convert(ivmLog, parameters.getClassifier(), semantics, result);
		//		debug(multiRun);

		//create observations
		debug("create observations");
		Observations observations = MultiRun2Observations.getObservations(multiRun, xLog.size(),
				result.getNumberOfResources(), result);
		//		debug(observations);

		//create equations
		debug("create equations");
		List<Equation> equations = Observations2Functions.convert(observations, result, semantics);
		//		debug(equations);

		//create initial guess
		debug("create initial guess");
		double[] initialParameterGuesses = new double[result.getNumberOfTransitions() * 2];
		Arrays.fill(initialParameterGuesses, 1);

		//solve
		debug("solve");
		TIntArrayList nonZeroParameters = new TIntArrayList();
		for (int transition = 0; transition < result.getNumberOfTransitions(); transition++) {
			nonZeroParameters.add(transition * 2);
			nonZeroParameters.add(transition * 2 + 1);
		}
		double[] parameterValues = Solver.solve(equations, result.getNumberOfTransitions() * 2, new int[0],
				nonZeroParameters.toArray(), initialParameterGuesses);

		debug(Arrays.toString(parameterValues));

		//put the results into the net
		debug("output");
		for (int transition = 0; transition < result.getNumberOfTransitions(); transition++) {
			result.setTransitionBaseWeight(transition, parameterValues[transition * 2]);
			result.setTransitionUtilisationWeight(transition, parameterValues[transition * 2 + 1]);
		}

		long time = (System.currentTimeMillis() - start) / 1000;

		System.out.println(
				"weight parameters & multi-run size & observations & equations & alignment time (s) & run time (s)");
		System.out.println(result.getNumberOfTransitions() * 2 + "&" + multiRun.size() + "&" + observations.size() + "&"
				+ equations.size() + "&" + timeAlignment + "&" + time);

		return result;
	}

	public static void setResourceModel(IvMLogFiltered log, StochasticLabelledPetriNetResourceWeightsEditable result) {
		TObjectIntHashMap<String> resource2id = new TObjectIntHashMap<>(10, 0.5f, -1);

		for (IteratorWithPosition<IvMTrace> it = log.iterator(); it.hasNext();) {
			IvMTrace trace = it.next();

			for (IvMMove move : trace) {
				if (move.isModelSync() && move.isComplete() && !result.isTransitionSilent(move.getTreeNode())) {
					int transition = move.getTreeNode();
					if (!result.isTransitionSilent(transition)) {
						String resourceName = move.getResource();

						int resource = resource2id.putIfAbsent(resourceName, resource2id.size());
						if (resource == resource2id.getNoEntryValue()) {
							resource = result.addResource(resourceName);
						}

						result.setTransitionResources(transition, resource, true);
					}
				}
			}
		}
	}

	public static void debug(Object object) {
		//System.out.println(object.toString());
	}
}