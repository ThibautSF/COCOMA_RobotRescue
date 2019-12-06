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
		int action = -1;
		double[] actionSoftMax = softMax(actionValues);

		// Begin discrete prob
		double rand = r.nextDouble();

		double[] cumprob = new double[actionSoftMax.length + 1];
		cumprob[0] = 0;
		int total = 0;
		for (int i = 1; i < actionSoftMax.length + 1; i++) {
			total += actionSoftMax[i];
			cumprob[i] = total;
		}

		for (int i = 0; i < actionSoftMax.length; i++) {
			if (rand > cumprob[i] && rand <= cumprob[i + 1]) {
				action = i;
				break;
			}
		}
		// End discrete prob

		return action;
	}

	private double[] softMax(double[] actionValues) {
		double[] actionSoftMax = new double[actionValues.length];
		double sum = 0;

		for (int i = 0; i < actionValues.length; ++i) {
			actionSoftMax[i] = Math.exp(temperature * actionValues[i]);
			sum += actionSoftMax[i];
		}

		for (int i = 0; i < actionSoftMax.length; ++i)
			actionSoftMax[i] /= sum;

		return actionSoftMax;
	}
}
