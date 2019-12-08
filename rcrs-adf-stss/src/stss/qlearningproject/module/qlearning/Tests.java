package stss.qlearningproject.module.qlearning;

public class Tests {

	public static void main(String[] args) {
		IPolicy explorationPolicy = new EpsilonGreedy(0.5);
		QLearning myq = new QLearning(explorationPolicy, 5, 3, 0.1, 0.2);
		QExportImport ei = new QExportImport("testdump");

		myq.update(0, 1, 25, 1);

		ei.saveQlearning(myq);

		myq.update(0, 2, 50, 1);
		myq.setExplorationPolicy(new Greedy());

		QLearning testq = ei.getQlearning();

		System.out.println(myq.getExplorationPolicy());
		System.out.println(myq.getAction(0));
		System.out.println(testq.getExplorationPolicy());
		System.out.println(testq.getAction(0));
	}

}
