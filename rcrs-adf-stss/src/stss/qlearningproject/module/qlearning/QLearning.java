/**
 *
 */
package stss.qlearningproject.module.qlearning;

/**
 * @author tsimonfine
 *
 */
public class QLearning {
	// Number of possible states
	private int nbStates;
	// Number of possible actions
	private int nbActions;
	private IPolicy explorePolicy;

	private double[][] qValues;

	// learning rate
	private float alpha;
	private float beta;
	// discount factor
	private float gamma;

	/**
	 * Initialize QLearning with default values
	 *
	 * @param states  amount of states
	 * @param actions amount of actions
	 */
	public QLearning(int states, int actions) {
		this(new Greedy(), states, actions, 0.4f, 8, 0.9f);
	}

	/**
	 * @param states  amount of states
	 * @param actions amount of actions
	 * @param alpha   learning rate
	 * @param beta
	 * @param gamma   discount factor
	 */
	public QLearning(IPolicy explorationPolicy, int states, int actions, float alpha, float beta, float gamma) {
		this.nbStates = states;
		this.nbActions = actions;
		this.qValues = new double[states][];
		for (int i = 0; i < states; i++) {
			this.qValues[i] = new double[actions];
		}

		this.alpha = alpha;
		this.beta = beta;
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
