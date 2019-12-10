package stss.qlearningproject.extaction;

import static rescuecore2.standard.entities.StandardEntityURN.REFUGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import adf.agent.action.Action;
import adf.agent.action.common.ActionMove;
import adf.agent.action.fire.ActionExtinguish;
import adf.agent.action.fire.ActionRefill;
import adf.agent.communication.MessageManager;
import adf.agent.develop.DevelopData;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.agent.module.ModuleManager;
import adf.agent.precompute.PrecomputeData;
import adf.component.extaction.ExtAction;
import adf.component.module.algorithm.PathPlanning;
import rescuecore2.config.NoSuchConfigOptionException;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;
import stss.qlearningproject.module.qlearning.EpsilonGreedy;
import stss.qlearningproject.module.qlearning.IPolicy;
import stss.qlearningproject.module.qlearning.QLearning;
import stss.qlearningproject.module.qlearning.QLearningFactory;

public class ActionFireFightingLight extends ExtAction {
	private PathPlanning pathPlanning;

	private int maxExtinguishDistance;
	private int maxExtinguishPower;
	private int thresholdRest;
	private int kernelTime;
	private int refillCompleted;
	private int refillRequest;
	private boolean refillFlag;

	private EntityID target;

	// Begin customs
	// ----
	// States:
	// param1 fireBuildingCloseRange
	// param2 waterFull
	// param3 refugeInRange
	// param4 InRefuge
	// param5 waterEmpty
	//
	// Number of states = 2*2*2*2*2
	//
	// The status number of values for each parameter
	private int[] state_descriptors = new int[] { 2, 2, 2, 2, 2 };
	// The list of all possible states (computed from state_descriptors)
	private List<String> states;
	// ----
	// End customs

	public ActionFireFightingLight(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
			ModuleManager moduleManager, DevelopData developData) {
		super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);
		this.maxExtinguishDistance = scenarioInfo.getFireExtinguishMaxDistance();
		this.maxExtinguishPower = scenarioInfo.getFireExtinguishMaxSum();
		this.thresholdRest = developData.getInteger("ActionFireFighting.rest", 100);
		int maxWater = scenarioInfo.getFireTankMaximum();
		this.refillCompleted = (maxWater / 10) * developData.getInteger("ActionFireFighting.refill.completed", 10);
		this.refillRequest = this.maxExtinguishPower * developData.getInteger("ActionFireFighting.refill.request", 1);
		this.refillFlag = false;

		this.target = null;

		switch (scenarioInfo.getMode()) {
		case PRECOMPUTATION_PHASE:
			this.pathPlanning = moduleManager.getModule("ActionFireFighting.PathPlanning",
					"adf.sample.module.algorithm.SamplePathPlanning");
			break;
		case PRECOMPUTED:
			this.pathPlanning = moduleManager.getModule("ActionFireFighting.PathPlanning",
					"adf.sample.module.algorithm.SamplePathPlanning");
			break;
		case NON_PRECOMPUTE:
			this.pathPlanning = moduleManager.getModule("ActionFireFighting.PathPlanning",
					"adf.sample.module.algorithm.SamplePathPlanning");
			break;
		}

		// Begin customs
		// ----
		this.states = computeStates(new ArrayList<String>(), 0);

		// Create a default empty qlearning (in case storage file don't exist)
		IPolicy policy = new EpsilonGreedy(0.2);
		QLearning qlearning = new QLearning(policy, this.states.size(), 5);

		QLearningFactory.initInstance(this.getClass(), qlearning);
		// ----
		// End customs
	}

	// Begin customs
	// ----
	/**
	 * Compute all the states possible outcomes
	 *
	 * @param states
	 * @param step
	 * @return
	 */
	private List<String> computeStates(List<String> states, int step) {
		if (step < state_descriptors.length) {
			List<String> localstates = new ArrayList<>();

			if (!states.isEmpty()) {
				for (String string : states) {
					subComputeStates(step, localstates, string);
				}
			} else {
				subComputeStates(step, localstates, "");
			}

			step += 1;
			return computeStates(localstates, step);
		}

		return states;
	}

	private void subComputeStates(int step, List<String> localstates, String str) {
		for (int i = 0; i < state_descriptors[step]; i++) {
			StringBuilder sb = new StringBuilder(str);
			sb.append(i);
			localstates.add(sb.toString());
		}
	}

	/**
	 * Transform status array to string
	 *
	 * @param params the status as array
	 * @return the status as a string
	 */
	private String paramToState(int[] params) {
		return Arrays.stream(params).mapToObj(String::valueOf).collect(Collectors.joining(""));
	}

	/**
	 * Transform status string to array
	 *
	 * @param str the status as string
	 * @return the status as array
	 */
	private int[] stringToParam(String str) {
		String[] strArray = str.split(",");
		int[] intArray = new int[strArray.length];

		for (int i = 0; i < strArray.length; i++) {
			intArray[i] = Integer.parseInt(strArray[i]);
		}

		return intArray;
	}

	/**
	 * Get the state of the agent
	 *
	 * @param agent agent to get the state
	 * @return the state as array of int
	 */
	private int[] getActualState(FireBrigade agent) {
		// States:
		// Building on fire (known) -> 0 or 1
		// Building on fire close (action range) -> 0 or 1
		// Water status -> 0 (empty) / 1 / 2 (full)
		// Water below 50% -> 0 or 1
		// Water below 25% -> 0 or 1
		// Refuge close -> 0 or 1
		// In refuge (action range) -> 0 or 1

		// fireBuildingCloseRange
		// waterStatus
		// refugeInRange
		// InRefuge

		int[] state = new int[this.state_descriptors.length];

		EntityID agentPosition = agent.getPosition();
		StandardEntity positionEntity = Objects.requireNonNull(this.worldInfo.getPosition(agent));

		// Is any building on fire ? (0 for none / 1 for at least one building
		Collection<Building> burnings = this.worldInfo.getFireBuildings();
		// state[0] = (burnings.isEmpty()) ? 0 : 1;

		// Is a building on fire next to me ?
		state[0] = 0;
		for (Building building : burnings) {
			if (this.worldInfo.getDistance(agentPosition, building.getID()) < this.maxExtinguishDistance) {
				state[0] = 1;
				break;
			}
		}

		// My water tank status
		int amount = agent.getWater();
		if (amount == this.refillCompleted) { // full or not
			state[1] = 1;
		} else {
			state[1] = 0;
		}
		if (amount == 0) { // empty or not
			state[4] = 1;
		} else {
			state[4] = 0;
		}

		System.out.println("WATER : " + amount);

		// Refuge close ?
		state[2] = 0;
		Collection<StandardEntity> refuges = this.worldInfo.getEntitiesOfType(REFUGE);
		for (StandardEntity refuge : refuges) {
			if (this.worldInfo.getDistance(agentPosition, refuge.getID()) < this.maxExtinguishDistance) {
				state[2] = 1;
				break;
			}
		}

		// In refuge ?
		state[3] = (StandardEntityURN.REFUGE == positionEntity.getStandardURN()) ? 1 : 0;

		return state;
	}
	// ----
	// End customs

	@Override
	public ExtAction precompute(PrecomputeData precomputeData) {
		super.precompute(precomputeData);
		if (this.getCountPrecompute() >= 2) {
			return this;
		}
		this.pathPlanning.precompute(precomputeData);
		try {
			this.kernelTime = this.scenarioInfo.getKernelTimesteps();
		} catch (NoSuchConfigOptionException e) {
			this.kernelTime = -1;
		}
		return this;
	}

	@Override
	public ExtAction resume(PrecomputeData precomputeData) {
		super.resume(precomputeData);
		if (this.getCountResume() >= 2) {
			return this;
		}
		this.pathPlanning.resume(precomputeData);
		try {
			this.kernelTime = this.scenarioInfo.getKernelTimesteps();
		} catch (NoSuchConfigOptionException e) {
			this.kernelTime = -1;
		}
		return this;
	}

	@Override
	public ExtAction preparate() {
		super.preparate();
		if (this.getCountPreparate() >= 2) {
			return this;
		}
		this.pathPlanning.preparate();
		try {
			this.kernelTime = this.scenarioInfo.getKernelTimesteps();
		} catch (NoSuchConfigOptionException e) {
			this.kernelTime = -1;
		}
		return this;
	}

	@Override
	public ExtAction updateInfo(MessageManager messageManager) {
		super.updateInfo(messageManager);
		if (this.getCountUpdateInfo() >= 2) {
			return this;
		}
		this.pathPlanning.updateInfo(messageManager);
		return this;
	}

	@Override
	public ExtAction setTarget(EntityID target) {
		this.target = null;
		if (target != null) {
			StandardEntity entity = this.worldInfo.getEntity(target);
			if (entity instanceof Building) {
				this.target = target;
			}
		}
		return this;
	}

	@Override
	public ExtAction calc() {
		this.result = null;
		FireBrigade agent = (FireBrigade) this.agentInfo.me();
		EntityID agentPosition = agent.getPosition();

		// Get the qlearning of my class
		QLearning qlearning = QLearningFactory.getInstance(this.getClass());
		// qlearning.setExplorationPolicy(new RandPolicy());

		// Get actual state
		int[] beginState = getActualState(agent);
		int beginStateID = this.states.indexOf(paramToState(beginState));

		int waterQuantity = agent.getWater();

		// TODO beginStateID == -1 error ?
		System.out.println("state : " + paramToState(beginState));
		int action = qlearning.getAction(beginStateID);

		Collection<Building> burnings = this.worldInfo.getFireBuildings();
		int min_distance = Integer.MAX_VALUE;
		Building b = null;

		System.out.println("Begin calc");
		int reward = 0;
		// Building on fire (known) -> 0 or 1
		// Building on fire close (action range) -> 0 or 1
		// Water status -> 0 (empty) / 1 / 2 (full)
		// Water below 50% -> 0 or 1
		// Water below 25% -> 0 or 1
		// Refuge close -> 0 or 1
		// In refuge (action range) -> 0 or 1

		// state 1 fireBuildingCloseRange
		// state 2 waterFull
		// state 3 refugeInRange
		// state 4 InRefuge
		// state 5 waterEmpty
		switch (action) {
		case 1:
			// action go to nearest building in fire
			System.out.println("choose action go building");
			for (Building building : burnings) {
				int distance = this.worldInfo.getDistance(agentPosition, building.getID());
				if (distance < min_distance) {
					min_distance = distance;
					b = building;
				}
			}

			if (b != null) {
				if (beginState[0] == 1) {
					reward -= 1; // Go to building on fire when already close
				} else {
					if (agent.getWater() == 0) {
						reward -= 1; // Go to building on fire when no water available
					} else {
						reward += 1; // Go to building on fire with water
					}
				}
				this.result = this.getMoveAction(pathPlanning, agentPosition, b.getID());
			}
			break;

		case 2:
			// action extinguish fire
			System.out.println("choose action extinguish fire");
			for (Building building : burnings) {
				int distance = this.worldInfo.getDistance(agentPosition, building.getID());
				if (distance < this.maxExtinguishDistance && distance < min_distance) {
					min_distance = distance;
					b = building;
				}
			}

			if (b != null) {
				if (agent.getWater() == 0) {
					reward -= 1; // Extinguish fire with no water availble
				} else {
					if (beginState[0] == 0) {
						// Extinguish fire when no fire in range
						reward -= 1;
					} else {
						reward += 3; // Extinguish fire with water availble
					}
				}
				this.result = new ActionExtinguish(b.getID(), this.maxExtinguishPower);
			}
			break;

		case 3:
			// action go to nearest refuge
			System.out.println("choose action go refuge");
			Collection<StandardEntity> refuges = this.worldInfo.getEntitiesOfType(REFUGE);
			System.out.println("NB REFUGES IN MAP : " + refuges.size());
			StandardEntity r = null;
			for (StandardEntity refuge : refuges) {
				int distance = this.worldInfo.getDistance(agentPosition, refuge.getID());
				if (distance < min_distance) {
					min_distance = distance;
					r = refuge;
					System.out.println("DISTANCE MIN TO REFUGE" + min_distance);
				}
			}

			if (r != null) {
				if (beginState[3] == 1) {
					reward -= 1; // Already in
				}
				if (agent.getWater() == 15000) {
					reward -= 1; // Already full of water
				}
				System.out.println(r);
				this.result = this.getMoveAction(pathPlanning, agentPosition, r.getID());
				System.out.println(r);
			}
			break;

		case 4:
			// action refill tank
			System.out.println("choose action refill tank");
			this.result = new ActionRefill();
			if (beginState[3] == 0) {
				reward -= 1; // NOT Already in
			} else {
				if (agent.getWater() == 15000) {
					reward -= 1; // Refilling when full
				} else {
					reward += 1; // Refilling
				}
			}
			break;

		case 0:
		default:
			// action idle
			System.out.println("choose action idle");
			// TODO : move randomly
			reward -= 1;
			break;
		}

		// Get state after action
		int[] endState = getActualState(agent);
		int endStateID = this.states.indexOf(paramToState(endState));

		// Update qtable with reward info
		qlearning.update(beginStateID, action, reward, endStateID);

		/*
		 * TODO maybe find another moment to save the instance (save only at end ? or at
		 * least only once for all agent) ?
		 */
		// Save the instance to file in qtables/
		QLearningFactory.saveInstance(this.getClass());
		System.out.println("###################################################");
		return this;
	}

	private Action getMoveAction(PathPlanning pathPlanning, EntityID from, EntityID target) {
		pathPlanning.setFrom(from);
		pathPlanning.setDestination(target);
		List<EntityID> path = pathPlanning.calc().getResult();
		if (path != null && path.size() > 0) {
			StandardEntity entity = this.worldInfo.getEntity(path.get(path.size() - 1));
			if (entity instanceof Building) {
				if (entity.getStandardURN() != StandardEntityURN.REFUGE) {
					path.remove(path.size() - 1);
				}
			}
			return new ActionMove(path);
		}
		return null;
	}
}
