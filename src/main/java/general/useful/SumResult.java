package general.useful;

import general.ActionName;

import java.util.Objects;

public class SumResult {

	private Double maxSum;
	private boolean changeBestAction;
	private ActionName bestAction;
//	private double reward;

	public SumResult() {
		super();
		this.maxSum = -Double.MAX_VALUE;
		this.bestAction = null;
//		this.reward = 0;
	}

//	public double getReward() {
//		return reward;
//	}
//
//	public void setReward(double reward) {
//		this.reward = reward;
//	}

	public SumResult(Double maxSum, ActionName bestAction, boolean changeBestAction) {
		super();
		this.maxSum = maxSum;
		this.bestAction = bestAction;
		this.changeBestAction = changeBestAction;
	}

	public Double getMaxSum() {
		return maxSum;
	}

	public void setMaxSum(Double maxSum) {
		this.maxSum = maxSum;
	}

	public ActionName getBestAction() {
		return bestAction;
	}

	public void setBestAction(ActionName bestAction) {
		this.bestAction = bestAction;
	}

	public boolean getChangeBestAction() {
		return changeBestAction;
	}

	public void setChangeBestAction(boolean changeBestAction) {
		this.changeBestAction = changeBestAction;
	}

	@Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof SumResult)) {
            return false;
        }
        SumResult sr = (SumResult) o;
        return maxSum==sr.maxSum &&
        		bestAction.equals(sr.bestAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxSum, bestAction);
    }
	
	public static SumResult getEndStateResult(){
		return  new SumResult( 0.0 , ActionName.THE_END, false);
	}
}
