/**
 *
 */
package stss.qlearningproject.module.qlearning;

/**
 * @author tsimonfine
 *
 */
public class Greedy implements IPolicy {

	/**
	 * Constructor
	 */
	public Greedy() {
	}

	@Override
	public int chooseAction(double[] actionValues) {
		int actionsCount = actionValues.length;

		// find best action (greedy)
		double maxReward = actionValues[0];
		int greedy = 0;

		for (int i = actionsCount - 1; i >= 0; i--) {
			if (actionValues[i] > maxReward) {
				maxReward = actionValues[i];
				greedy = i;
			}
		}

		return greedy;
	}

}
