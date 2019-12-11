package stss.qlearningproject.module.qlearning;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import stss.qlearningproject.extaction.ActionFireFightingLight2;

public class Tests {

	public static void main(String[] args) {
		IPolicy explorationPolicy = new EpsilonGreedy(0.5);
		QLearning myq = new QLearning(explorationPolicy, 5, 3, 0.1, 0.2);
		QExportImport ei = new QExportImport("testdump");

		myq.update(0, 1, 25, 1);

		System.out.println(ei.getQlearning());

		ei.saveQlearning(myq);

		myq.update(0, 2, 50, 1);
		myq.setExplorationPolicy(new Greedy());

		QLearning testq = ei.getQlearning();

		System.out.println(myq.getExplorationPolicy());
		System.out.println(myq.getAction(0));
		System.out.println(testq.getExplorationPolicy());
		System.out.println(testq.getAction(0));

		List<String> states = computeStates(new ArrayList<>(), 0);

		QLearningFactory.initInstance(ActionFireFightingLight2.class, myq);
		QLearning q = QLearningFactory.getInstance(ActionFireFightingLight2.class);

		NumberFormat formatter = new DecimalFormat("#0.00000");

		System.out.println(states.size() + " possible states");
		for (int i = 0; i < states.size(); i++) {
			String s = states.get(i);
			double[] qvalues = q.getQTable(i);

			for (double d : qvalues) {

				s += "\t" + ((d < 0) ? formatter.format(d) : "+" + formatter.format(d));
			}
			System.out.println(s);
		}

	}

	// private static int[] state_descriptors = new int[] { 2, 3, 2, 2 };
	private static int[] state_descriptors = new int[] { 2, 2, 3, 2, 2 };

	private static List<String> computeStates(List<String> states, int step) {
		if (step < state_descriptors.length) {
			List<String> localstates = new ArrayList<>();

			if (!states.isEmpty()) {
				for (String string : states) {
					subComputeStates(step, localstates, string);
				}
			} else {
				subComputeStates(step, localstates, "");
			}

			step += 1;
			return computeStates(localstates, step);
		}

		return states;
	}

	private static void subComputeStates(int step, List<String> localstates, String str) {
		for (int i = 0; i < state_descriptors[step]; i++) {
			StringBuilder sb = new StringBuilder(str);
			sb.append(i);
			localstates.add(sb.toString());
		}
	}

}
