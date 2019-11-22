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

	private double[][] qValues;

	private float alpha;
	private float beta;
	private float gamma;

	/**
	 *
	 */
	public QLearning(int states, int actions) {
		this(states, actions, 0.4f, 8, 0.9f);
	}

	/**
	 *
	 */
	public QLearning(int states, int actions, float alpha, float beta, float gamma) {
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

	public int getNbStates() {
		return this.nbStates;
	}

	public int getNbActions() {
		return this.nbActions;
	}

	public int getAction(int state) {
		// TODO get action from this.qValues[states]
		return -1;
	}

	public void update(int prevS, int action, double reward, int state) {
		double[] nextActionEstim = this.qValues[state];

		// Find max expected reward for next state
		double maxExpectedRew = nextActionEstim[0];
		for (double e : nextActionEstim) {
			if (e > maxExpectedRew) {
				maxExpectedRew = e;
			}
		}

		double[] prevActionEstim = this.qValues[prevS];
		prevActionEstim[action] *= (1 - alpha);
		prevActionEstim[action] += (this.alpha * (reward + this.gamma * maxExpectedRew));
	}
}
