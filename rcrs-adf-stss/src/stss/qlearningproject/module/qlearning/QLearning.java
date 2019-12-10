/**
 *
 */
package stss.qlearningproject.module.qlearning;

import java.io.Serializable;

/**
 * @author tsimonfine
 *
 */
public class QLearning implements Serializable {
	private static final long serialVersionUID = -7562112486267395988L;

	// Number of possible states
	private int nbStates;
	// Number of possible actions
	private int nbActions;
	private IPolicy explorePolicy;

	private double[][] qValues;

	// learning rate
	private double alpha;
	// discount factor
	private double gamma;

	/**
	 * Initialize QLearning with default values (Random policy)
	 *
	 * @param states  amount of states
	 * @param actions amount of actions
	 */
	public QLearning(int states, int actions) {
		this(new RandPolicy(), states, actions, 0.9, 0.75);
	}

	/**
	 * Initialize QLearning with default values (custom policy)
	 *
	 * @param explorationPolicy the policy
	 * @param states            amount of states
	 * @param actions           amount of actions
	 */
	public QLearning(IPolicy explorationPolicy, int states, int actions) {
		this(explorationPolicy, states, actions, 0.9, 0.75);
	}

	/**
	 * @param states  amount of states
	 * @param actions amount of actions
	 * @param alpha   learning rate
	 * @param gamma   discount factor
	 */
	public QLearning(IPolicy explorationPolicy, int states, int actions, double alpha, double gamma) {
		this.explorePolicy = explorationPolicy;
		this.nbStates = states;
		this.nbActions = actions;
		this.qValues = new double[states][];
		for (int i = 0; i < states; i++) {
			this.qValues[i] = new double[actions];
		}

		this.alpha = alpha;
		this.gamma = gamma;
	}

	/**
	 * @return
	 */
	public int getNbStates() {
		return this.nbStates;
	}

	/**
	 * @return
	 */
	public int getNbActions() {
		return this.nbActions;
	}

	/**
	 * @return Exploration Policy
	 */
	public IPolicy getExplorationPolicy() {
		return explorePolicy;
	}

	/**
	 * @param explorationPolicy Exploration Policy
	 */
	public void setExplorationPolicy(IPolicy explorationPolicy) {
		this.explorePolicy = explorationPolicy;
	}

	/**
	 * @return the learning rate
	 */
	public double getLearningRate() {
		return alpha;
	}

	/**
	 * @param alpha new learning rate
	 */
	public void setLearningRate(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @return the discount factor
	 */
	public double getDiscountFactor() {
		return gamma;
	}

	/**
	 * @param gamma new discount factor
	 */
	public void setDiscountFactor(double gamma) {
		this.gamma = gamma;
	}

	/**
	 * @return qTable
	 */
	public double[][] getQTable() {
		return qValues;
	}

	/**
	 * @return qValues for the state
	 */
	public double[] getQTable(int state) {
		return qValues[state];
	}

	public int getAction(int state) {
		return this.explorePolicy.chooseAction(this.qValues[state]);
	}

	/**
	 * @param prevS  The previous state
	 * @param action The action done
	 * @param reward The reward obtained
	 * @param state  The actual state
	 */
	public void update(int prevS, int action, double reward, int state) {
		double[] nextActionEstim = this.qValues[state];

		// Find max expected reward for next state
		double maxExpectedRew = nextActionEstim[0];
		for (double e : nextActionEstim) {
			if (e > maxExpectedRew) {
				maxExpectedRew = e;
			}
		}

		// Edit qvalue
		double[] prevActionEstim = this.qValues[prevS];
		prevActionEstim[action] *= (1 - alpha);
		prevActionEstim[action] += (this.alpha * (reward + this.gamma * maxExpectedRew));
	}
}
