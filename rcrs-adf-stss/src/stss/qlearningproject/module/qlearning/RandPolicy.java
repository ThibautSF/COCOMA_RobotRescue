/**
 *
 */
package stss.qlearningproject.module.qlearning;

import java.util.Random;

/**
 * @author tsimonfine
 *
 */
public class RandPolicy implements IPolicy {
	protected Random r = new Random();

	/**
	 * Constructor
	 */
	public RandPolicy() {
	}

	@Override
	public int chooseAction(double[] actionValues) {
		int actionsCount = actionValues.length;

		int randomAction = r.nextInt(actionsCount - 1);

		return randomAction;
	}

}
