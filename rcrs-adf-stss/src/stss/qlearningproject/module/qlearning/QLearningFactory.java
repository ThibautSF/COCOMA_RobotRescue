/**
 *
 */
package stss.qlearningproject.module.qlearning;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tsimonfine
 *
 */
public class QLearningFactory {
	private static Map<Class<?>, QLearning> instances = new HashMap<>();
	private static Map<Class<?>, QExportImport> exporters = new HashMap<>();

	/**
	 * @param classOwner
	 * @param qlearning
	 * @return
	 */
	public static boolean initInstance(Class<?> classOwner, QLearning defaultQ) {
		if (!instances.containsKey(classOwner) || instances.get(classOwner) == null) {
			QExportImport ei = new QExportImport("qtables/qtable_" + classOwner.getSimpleName());
			QLearning qlearning = ei.getQlearning();

			if (qlearning == null)
				qlearning = defaultQ;

			instances.put(classOwner, qlearning);
			exporters.put(classOwner, ei);

			return true;
		}

		return false;
	}

	/**
	 * @param classOwner
	 * @return
	 */
	public static QLearning getInstance(Class<?> classOwner) {
		return instances.get(classOwner);
	}

	public static void saveInstance(Class<?> classOwner) {
		QExportImport ei = exporters.get(classOwner);
		QLearning qlearn = instances.get(classOwner);
		if (ei != null && qlearn != null) {
			ei.saveQlearning(qlearn);
		}
	}

	/**
	 * Set a new qlearning instance (WARNING: erase previous values)
	 *
	 * @param classOwner
	 * @param qlearning
	 */
	public static void setInstance(Class<?> classOwner, QLearning qlearning) {
		instances.put(classOwner, qlearning);
		QExportImport ei = new QExportImport("qtables/qtable_" + classOwner.getSimpleName());
		exporters.put(classOwner, ei);
	}

}
