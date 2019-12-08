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
	private static final long serialVersionUID = 2218381489137753699L;

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
