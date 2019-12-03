/**
 *
 */
package stss.qlearningproject.module.qlearning;

import java.util.Random;

/**
 * Softmax
 *
 * @author tsimonfine
 *
 */
public class Boltzmann implements IPolicy {
	protected float temperature;
	protected Random r = new Random();

	/**
	 * @param temperature
	 */
	public Boltzmann(float temperature) {
		this.temperature = temperature;
	}

	/**
	 * @return temperature
	 */
	public float getTemperature() {
		return temperature;
	}

	/**
	 * @param temperature temperature à définir
	 */
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	@Override
	public int chooseAction(double[] actionValues) {
		int action = 0;
		double[] actionSoftMax = softMax(actionValues);

		// TODO discreteProb

		return action;
	}

	private double[] softMax(double[] actionValues) {
		double[] actionSoftMax = new double[actionValues.length];

		// TODO softmax

		return actionSoftMax;
	}

}
