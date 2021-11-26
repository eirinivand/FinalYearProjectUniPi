package theTwo;

import general.ActionName;
import general.Policy;
import general.PossibleTransition;
import general.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import world.Environment;
import world.Main;

import java.util.HashMap;
import java.util.Random;

public class TDAgent extends Agent  {

    private HashMap<State, Double> rewards;
    private HashMap<State, Double> stateFrequencyVisits;
    private HashMap<State, Integer> stateNumberVisits;
    private Integer sumOfAllStateVisits;
    private int shouldExploreLoopNumber;
    private Random random = new Random(2254359156778L);
    public static Logger logger = LogManager.getLogger(TDAgent.class.getName());

    public TDAgent() {
        super();
        this.rewards = new HashMap<>();
        this.shouldExploreLoopNumber = 1;
        this.stateFrequencyVisits = new HashMap<>();
        this.stateNumberVisits = new HashMap<>();
        this.sumOfAllStateVisits = 0;
    }

    /**
     * @param policy
     * @param mdp
     * @param utilities
     */
    public TDAgent(Policy policy, HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> mdp,
                   HashMap<State, Double> utilities, HashMap<State,Integer> stateNumberVisits,
                   HashMap<State,Double> stateFrequencyVisits, Integer sumOfAllStateVisits) {
        super(policy, mdp, utilities);
        this.rewards = new HashMap<>();
        this.stateNumberVisits = stateNumberVisits;
        this.sumOfAllStateVisits = sumOfAllStateVisits;
        this.stateFrequencyVisits = stateFrequencyVisits;
        this.shouldExploreLoopNumber = 1;
    }


    private ActionName percept() {
        if (nextState!=null && !utilities.containsKey(nextState)) {
            double nextStateReward = Environment.amICloseReward(currentState, nextState, action);
            utilities.put(nextState, nextStateReward);
            rewards.put(nextState, nextStateReward);
        }
        if (currentState != null) {
            sumOfAllStateVisits++;

            stateNumberVisits.computeIfPresent(currentState, (state, number) -> number++);
            stateNumberVisits.computeIfAbsent(currentState, state -> 1);

            stateFrequencyVisits.computeIfPresent(currentState, (state, frequency) -> (double) stateNumberVisits.get(state)/sumOfAllStateVisits);
            stateFrequencyVisits.computeIfAbsent(currentState, state -> (double) stateNumberVisits.get(state)/sumOfAllStateVisits);

            //1. is it present? compute. (this goes first or else it will always exist and make miscalculations)
            utilities.computeIfPresent(currentState, (state, actionsStatesPTs) -> {
                 //1. is it present? compute.
                try {
                    return actionsStatesPTs + (1 / stateNumberVisits.get(state)) *
                            (stateFrequencyVisits.get(state)) *
                            (Environment.amICloseReward(null, state, action) + Main.GAMA * utilities.get(nextState) - actionsStatesPTs);
                }catch (Exception e){
                    logger.info("nextState" +nextState);
                    logger.info("state" +state);
                    logger.info("action" +action);
                    return Environment.amICloseReward(null, state, action);
                }

            });

        }
        if (Environment.isEndState(nextState)) {
            System.out.println("PADP: percet isEndState");
            return null;
        } else {
            currentState =  (nextState==null)? Environment.getCurrentStateAfterActionForTDAgent(this, action) :nextState;
            action = policy.getBestActions().get(nextState);
            if (action == null || loopNumber == shouldExploreLoopNumber) {
                action = ActionName.getRandomAction();
                shouldExploreLoopNumber = (loopNumber + 5);
            }
        }
        return action;

    }

    @Override
    public void run() {
        // ok so the agent knows how many states there are as he does have action
        // policy. but i don't want to use the policy'nextState knowledge cause i might
        // not have it in another case, so i'll run up to the point that the
        // environment tells me i'm in state 5
        double reward = 0;
        currentState = null;
        loopNumber = 0;
        action = percept();
        do {
            nextState = Environment.getCurrentStateAfterActionForTDAgent(this, action);
            action = percept();
            loopNumber++;
        } while (!Environment.isEndState(nextState));

        System.out.println("TDA: Finished TD");
        this.stopped = true;
    }



    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public HashMap<State, Double> getUtilities() {
        return utilities;
    }

    public void setUtilities(HashMap<State, Double> utilities) {
        this.utilities = utilities;
    }

    public HashMap<State, Double> getRewards() {
        return rewards;
    }

    public void setRewards(HashMap<State, Double> rewards) {
        this.rewards = rewards;
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

    public HashMap<State, Double> getStateFrequencyVisits() {
        return stateFrequencyVisits;
    }

    public void setStateFrequencyVisits(HashMap<State, Double> stateFrequencyVisits) {
        this.stateFrequencyVisits = stateFrequencyVisits;
    }

    public HashMap<State, Integer> getStateNumberVisits() {
        return stateNumberVisits;
    }

    public void setStateNumberVisits(HashMap<State, Integer> stateNumberVisits) {
        this.stateNumberVisits = stateNumberVisits;
    }

    public Integer getSumOfAllStateVisits() {
        return sumOfAllStateVisits;
    }

    public void setSumOfAllStateVisits(Integer sumOfAllStateVisits) {
        this.sumOfAllStateVisits = sumOfAllStateVisits;
    }
}
