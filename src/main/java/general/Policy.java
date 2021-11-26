package general;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;


public class Policy {

	private State initState;
	private HashMap<State, ActionName> bestActions;
	private static Logger logger = LogManager.getLogger(Policy.class.getName());

	public Policy(State initState) {
		super();
		this.initState = initState;
		this.bestActions = new HashMap<>();
	}

	public Policy(State initState, HashMap<State, ActionName> bestActions) {
		super();
		this.initState = initState;
		this.bestActions = bestActions;
	}

	public State getInitState() {
		return initState;
	}

	public void setInitState(State initState) {
		this.initState = initState;
	}

	public HashMap<State, ActionName> getBestActions() {
		return bestActions;
	}

	public void setBestActions(HashMap<State, ActionName> bestActions) {
		this.bestActions = bestActions;
	}

	public State throughWhichStateCanIGetHereByPolicy(State endState) {
//		for (State s : bestActions.keySet()) {
//			HashMap<State, PossibleTransition> prob = this.getBestActions().get(s).getProbabilities();
//			for (State i : prob.keySet())
//				if (i.equals(endState))
//					return s;
//		}
		logger.warn("using OLD generic.Policy.throughWhichStateCanIGetHereByPolicy should #NOT");
		return null;
	}

	public Policy computePolicyUsingUtilites( HashMap<State, Double> U) {
		//TODO make a policy out of utilities where should i go ?
		return null;

	}

	@Override
	public String toString() {
		return "Policy{" +
				"initState=" + initState +
				",\n bestActions=" + bestActions +
				'}';
	}

	public String toLineString() {
		StringBuilder sb = new StringBuilder();
		sb.append("POLICY = ");
		for (State s : bestActions.keySet()){
			sb.append(" ").append(s.getId()).append(":").append(bestActions.get(s))
			.append(",");
		}
		System.out.println(sb);
		return sb.toString();
	}
}
