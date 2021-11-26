package general;

import java.util.List;

public class PossibleTransition {

	private Double probability;
	private State nextState;
	private Double reward;

	public static PossibleTransition findByCurrentState(State nextState, List<PossibleTransition> pts ){
		for (PossibleTransition pt : pts )
			if (pt.getNextState().equals(nextState))
				return pt;
		return null;
	}

	public PossibleTransition(Double probability, Double reward) {
		super();
		this.probability = probability;
		this.reward = reward;
	}

	public PossibleTransition(Double probability, State nextState, Double reward) {
		super();
		this.probability = probability;
		this.nextState = nextState;
	}

	public PossibleTransition(Double probability) {
		super();
		this.probability = probability;
	}

	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
	}

	public State getNextState() {
		return nextState;
	}

	public void setNextState(final State nextState) {
		this.nextState = nextState;
	}

	public Double getReward() {
		return reward;
	}

	public void setReward(Double reward) {
		this.reward = reward;
	}

	@Override
	public String toString() {
		return "PossibleTransition{ \n" +
				"\n\tprobability=" + probability +
				", \n\tnextState=" + nextState +
				", \n\treward=" + reward +
				'}';
	}
}
