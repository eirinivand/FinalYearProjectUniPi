package theTwo;

import general.ActionName;
import general.Policy;
import general.PossibleTransition;
import general.State;

import java.util.HashMap;

public class Agent implements Runnable {

    protected Policy policy ;
    protected HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> mdp ;
    protected HashMap<State, Double> utilities ;
    protected volatile boolean stopped = false;
    protected State nextState;
    protected ActionName action;
    protected long passedTime;
    protected int loopNumber;
    protected State currentState;

    public Agent() {
        this.mdp = new HashMap<>();
        this.utilities = new HashMap<>();
    }

    public Agent(Policy policy, HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> mdp, HashMap<State, Double> utilities) {
        this.policy = policy;
        this.mdp = mdp;
        this.utilities = utilities;
    }

    @Override
    public void run() {

    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> getMdp() {
        return mdp;
    }

    public void setMdp(HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> mdp) {
        this.mdp = mdp;
    }

    public HashMap<State, Double> getUtilities() {
        return utilities;
    }

    public void setUtilities(HashMap<State, Double> utilities) {
        this.utilities = utilities;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public State getNextState() {
        return nextState;
    }

    public void setNextState(State nextState) {
        this.nextState = nextState;
    }

    public ActionName getAction() {
        return action;
    }

    public void setAction(ActionName action) {
        this.action = action;
    }

    public long getPassedTime() {
        return passedTime;
    }

    public void setPassedTime(long passedTime) {
        this.passedTime = passedTime;
    }

    public int getLoopNumber() {
        return loopNumber;
    }

    public void setLoopNumber(int loopNumber) {
        this.loopNumber = loopNumber;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }
}
