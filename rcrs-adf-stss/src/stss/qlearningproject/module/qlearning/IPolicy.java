/**
 *
 */
package stss.qlearningproject.module.qlearning;

/**
 * @author tsimonfine
 *
 */
public interface IPolicy {
	/**
	 * @param actionValues The qvalues/reward estimation for an state
	 * @return an action id
	 */
	public int chooseAction(double[] actionValues);
}
