/**
 *
 */
package stss.qlearningproject.module.qlearning;

import java.io.Serializable;

/**
 * @author tsimonfine
 *
 */
public interface IPolicy extends Serializable {
	/**
	 * @param actionValues The qvalues/reward estimation for an state
	 * @return an action id
	 */
	public int chooseAction(double[] actionValues);
}
