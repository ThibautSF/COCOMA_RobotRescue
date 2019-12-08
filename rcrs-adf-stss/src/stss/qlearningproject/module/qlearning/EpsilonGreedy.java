/**
 *
 */
package stss.qlearningproject.module.qlearning;

import java.util.Random;

/**
 * @author tsimonfine
 *
 */
public class EpsilonGreedy extends Greedy implements IPolicy {
	private static final long serialVersionUID = 3188492833276330852L;

	protected double epsilon;

	protected Random r = new Random();
	protected IPolicy rand = new RandPolicy();

	/**
	 * @param epsilon
	 */
	public EpsilonGreedy(double epsilon) {
		super();
		this.epsilon = epsilon;
	}

	public double getEpsilon() {
		return this.epsilon;
	}

	/**
	 * @param epsilon epsilon à définir
	 */
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public int chooseAction(double[] actionValues) {
		int actionsCount = actionValues.length;

		int action = super.chooseAction(actionValues);

		if (r.nextDouble() < epsilon) {
			// explore
			int randomAction;

			do {
				randomAction = rand.chooseAction(actionValues);
			} while (randomAction == action && actionsCount > 2);

			action = randomAction;
		} else {
			// exploit
			action = super.chooseAction(actionValues);
		}

		return action;
	}
}
