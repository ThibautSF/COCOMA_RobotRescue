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
	private static final long serialVersionUID = 601482317381529662L;

	protected double temperature;
	protected Random r = new Random();
	protected IPolicy rand = new RandPolicy();

	/**
	 * @param temperature
	 */
	public Boltzmann(double temperature) {
		this.temperature = temperature;
	}

	/**
	 * @return temperature
	 */
	public double getTemperature() {
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

		// System.out.println(Arrays.toString(actionSoftMax));

		// Begin discrete prob
		double d = r.nextDouble();

		double[] cumprob = new double[actionSoftMax.length + 1];
		cumprob[0] = 0;
		double total = 0;
		for (int i = 0; i < actionSoftMax.length; i++) {
			total += actionSoftMax[i];
			cumprob[i + 1] = total;
		}

		// System.out.println(Arrays.toString(cumprob));

		for (int i = 0; i < actionSoftMax.length; i++) {
			if (d > cumprob[i] && d <= cumprob[i + 1]) {
				action = i;
				break;
			}
		}
		// End discrete prob

		if (action == -1) {
			action = this.rand.chooseAction(actionValues);
		}

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
