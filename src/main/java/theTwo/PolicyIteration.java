package theTwo;


import general.ActionName;
import general.Policy;
import general.PossibleTransition;
import general.State;
import general.useful.SumResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import world.Environment;
import world.Main;

import java.util.HashMap;

public class PolicyIteration {

    private HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> model;
    private HashMap<State, Double> U;
    private static Logger logger = LogManager.getLogger(PolicyIteration.class.getName());


    public PolicyIteration(HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> model, HashMap<State, Double> calculatedU) {
        this.model = model;
        this.U = calculatedU;
    }

    public Policy policyIteration(Policy policy) {
        boolean changed;
        HashMap<State, Integer> timesStateChanged = new HashMap<>();
        do {
            U = policyEvaluation(policy);
            changed = false;
            for (State s : model.keySet()) {
                SumResult maxSum= computeMaxSum(s);
                if (maxSum.getMaxSum() - computeSumByAction(model.get(s).get(policy.getBestActions().get(s))) > Main.TOLERANCE) {
                    changed = true;
                    timesStateChanged.computeIfPresent(s, (st, ss) -> ++ss);
                    timesStateChanged.putIfAbsent(s, 1);
                    System.out.println("PI: State " + s.getId() + ": New best action " + policy.getBestActions().get(s) + "-> " + maxSum.getBestAction());
                    policy.getBestActions().put(s, maxSum.getBestAction());
                }

                if (timesStateChanged.get(s) != null && timesStateChanged.get(s) == 10) {
                    System.out.println(s.getId() + "!!!!!!!!!!!!!!!!!!!!Exceeded max allowed times of changing one state!!!!!!!!!!!!!!!!!!!!");
                    logger.warn("Exceeded max allowed times of changing one state!!!!!");
                }
            }

        } while (changed && timesStateChanged.values().stream().allMatch(x -> x < 10));
        return policy;
    }

    private void printUtilities(HashMap<State, Double> U) {
        for (State s : U.keySet()) {
            System.out.println("PI: State " + s.getId() + ", Utility " + U.get(s));

        }
    }


    private HashMap<State, Double> policyEvaluation(Policy policy) {
        HashMap<State, PossibleTransition> nextStates;
        for (State state : model.keySet()) {
            try {
                nextStates = model.get(state).get(policy.getBestActions().get(state));
                if (nextStates != null)
                    U.put(state, (double) Environment.amICloseReward(state, null, policy.getBestActions().get(state)) +
                                    Main.GAMA * computeSumByAction(nextStates));

                            //here I calculate for next action reward I generally do this this previous state.
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return U;
    }

    private SumResult computeMaxSum(State s) {
        SumResult maxSumR = new SumResult(-110000.0, null, false);
        boolean firstLoop = true;
        for (ActionName action : model.get(s).keySet()) {
            SumResult possibleSum = new SumResult();
            possibleSum.setMaxSum(computeSumByAction(model.get(s).get(action)));
            possibleSum.setBestAction(action);
            if (firstLoop || possibleSum.getMaxSum() > maxSumR.getMaxSum()  /*Main.TOLERANCE*/) {
                maxSumR = possibleSum;
                firstLoop = false;
            }
        }
        return maxSumR;
    }

    private double computeSumByAction(HashMap<State, PossibleTransition> possibleNextStates) {
        return  possibleNextStates==null? 0.0 : possibleNextStates.entrySet().stream().mapToDouble(e -> e.getValue().getProbability() * (U.get(e.getKey()) == null ? 0.0 : U.get(e.getKey()))).sum();

    }

    public HashMap<State, Double> getU() {
        return U;
    }

    public void setU(HashMap<State, Double> u) {
        U = u;
    }

}
