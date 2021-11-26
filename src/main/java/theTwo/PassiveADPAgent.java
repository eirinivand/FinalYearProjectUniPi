package theTwo;

import general.ActionName;
import general.Policy;
import general.PossibleTransition;
import general.State;
import world.Environment;
import world.Main;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 *
 */
public class PassiveADPAgent extends Agent {

    private HashMap<State, Double> rewards;
    private HashMap<State, HashMap<ActionName, Integer>> stateActionVisits;
    //HashMap<HashMap<currentState, nextState>, HashMap<ActionName, numberOfVisits>>
    private HashMap<Map<State, State>, HashMap<ActionName, Integer>> stateActionStateVisits;
    public HashMap<State, Integer> stateVisits;

    private double shouldExploreLoopNumber;
    private final double shouldExploreProbability = 1.0005;
    private boolean shouldExplore;

    /**
     * @param policy
     * @param mdp
     * @param utilities
     * @param stateActionVisits
     * @param stateActionStateVisits
     */
    public PassiveADPAgent(Policy policy, int loopNumber, HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> mdp,
                           HashMap<State, Double> utilities, HashMap<State, HashMap<ActionName, Integer>> stateActionVisits,
                           HashMap<Map<State, State>, HashMap<ActionName, Integer>> stateActionStateVisits) {
        super();
        this.policy = policy;
        this.mdp = mdp;
        this.utilities = utilities;
        this.stateActionVisits = stateActionVisits;
        this.stateVisits = new HashMap<>();
        this.rewards = new HashMap<>();
        this.stateActionStateVisits = stateActionStateVisits;
        this.passedTime = System.currentTimeMillis();
        this.shouldExploreLoopNumber = 2;
        this.shouldExplore = true;
        this.loopNumber = loopNumber;
    }

    public PassiveADPAgent(Policy policy, HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> mdp,
                           HashMap<State, Double> utilities, HashMap<State, HashMap<ActionName, Integer>> stateActionVisits,
                           HashMap<Map<State, State>, HashMap<ActionName, Integer>> stateActionStateVisits) {
        super();
        this.policy = policy;
        this.mdp = mdp;
        this.utilities = utilities;
        this.stateActionVisits = stateActionVisits;
        this.stateVisits = new HashMap<>();
        this.rewards = new HashMap<>();
        this.stateActionStateVisits = stateActionStateVisits;
        this.passedTime = System.currentTimeMillis();
        this.shouldExploreLoopNumber = 2;
        this.shouldExplore = true;
        this.loopNumber = 1;
    }

    private ActionName percept() {
        if (nextState != null && !mdp.containsKey(nextState)) {
            double nextStateReward = Environment.amICloseReward(currentState, nextState, action);
            utilities.put(nextState, nextStateReward);

        }
        if (currentState != null) {
//            if(!cS.equals(nextState)){
//                HashMap<ActionName, Set<PossibleTransition>> actions = mdp.get(nextState);
//                if(actions!=null) {
//                    if (actions.get(action) == null) {
//                        Set<PossibleTransition> pts = new HashSet<>(1);
//                        pts.add(new PossibleTransition(1.0, cS, nextStateReward));
//                        actions.put(action, pts);
//                    } else {
//                        System.out.println(actions + " " + action + " " + actions.get(action) + " " + cS.getId());
//                        for(PossibleTransition pt:  actions.get(action))
//                            pt.getNextState().equals(nextState)? pt.setProbability();
//                    }
//                    mdp.put(nextState, actions);
//                }
//            }
//			nS.put(nextState, nS.get(nextState) == null? 0 : nS.get(nextState) + 1);

            stateVisits.computeIfPresent(currentState, (state, visits) -> visits++);
            rewards.computeIfPresent(currentState, (state, reward) -> Environment.amICloseReward(currentState));
            stateVisits.computeIfAbsent(currentState, state -> 1);
            rewards.computeIfAbsent(currentState, state -> 0.0);

            //1. is it present? compute.
            stateActionVisits.computeIfPresent(currentState, (state, actionCount) -> {
                return increaseActionCounter(actionCount);
            });
            //2. is it not? compute
            stateActionVisits.computeIfAbsent(currentState, state -> {
                HashMap<ActionName, Integer> actionCount = new HashMap<>();
                actionCount.put(action, 1);
                return actionCount;
            });

            Map<State, State> statesMap = Collections.singletonMap(currentState, nextState);
            //1. is it present? compute.
            stateActionStateVisits.computeIfPresent(statesMap, (stateStateMap, actionCount) -> {
                return increaseActionCounter(actionCount);
            });
            //2. is it not? compute
            stateActionStateVisits.computeIfAbsent(statesMap, statesMapping -> {
                HashMap<ActionName, Integer> actionCount = new HashMap<>();
                actionCount.put(action, 1);
                return actionCount;
            });


            // for each t that is in nSAS ( = stateActionStateVisits) as next state of the currentState and has at least one action written in it.
            for (State t : stateActionStateVisits.keySet().stream().filter(statesMapping -> statesMapping.get(currentState) != null && stateActionStateVisits.get(statesMapping).get(action) != null)
                    .map(statesMapping -> statesMapping.get(currentState)).collect(Collectors.toList())) {
                //if currentState exists in mdp then compute its value, the actions HashMap < actionMade, < nextState( = t) , pt > > .
                mdp.computeIfPresent(currentState, (state, actionsStatesPTs) -> {
                    //if action is absent in actions of currentState in mdp ( = stateActionsPTs ) then compute its value, < t , pt >
                    //1. is it present? compute.
                    actionsStatesPTs.computeIfPresent(action, (actionName, statesPTs) -> {
                        statesPTs.computeIfPresent(t, (stateT, pt) -> {
                            pt.setProbability((double) stateActionStateVisits.get(Collections.singletonMap(state, stateT)).get(actionName) / stateActionVisits.get(state).get(actionName));
                            return pt;
                        });
                        statesPTs.computeIfAbsent(t, stateT ->
                                new PossibleTransition((double) stateActionStateVisits.get(Collections.singletonMap(state, t)).get(actionName) / stateActionVisits.get(state).get(actionName)
                                        , t, Environment.amICloseReward(t))
                        );
                        return statesPTs;
                    });
                    //2. is it not? compute
                    actionsStatesPTs.computeIfAbsent(action, actionName -> {
                        //TODO check if get(SingletonMap) works .. would surpise me to work..
                        PossibleTransition pt = new PossibleTransition((double) stateActionStateVisits.get(Collections.singletonMap(state, t)).get(actionName) / stateActionVisits.get(state).get(actionName)
                                , t, Environment.amICloseReward(t));
                        HashMap<State, PossibleTransition> statesPTs = new HashMap<>(1);
                        statesPTs.put(t, pt);
                        return statesPTs;
                    });
                    return actionsStatesPTs;
                });

                mdp.computeIfAbsent(currentState, state -> {
                    //if action is absent in actions of currentState in mdp ( = stateActionsPTs ) then compute its value, < t , pt > .
                    HashMap<ActionName, HashMap<State, PossibleTransition>> actionsStatesPTs = new HashMap<>();
                    HashMap<State, PossibleTransition> statesPTs = new HashMap<>();
                    statesPTs.put(t, new PossibleTransition((double) stateActionStateVisits.get(Collections.singletonMap(state, t)).get(action) / stateActionVisits.get(state).get(action)
                            , t, Environment.amICloseReward(t)));
                    actionsStatesPTs.put(action, statesPTs);
                    return actionsStatesPTs;
                });
            }

        }
        utilities = policyEvaluation();
        if (Environment.isEndState(nextState) || Environment.isEndState(currentState)) {
            System.out.println("PADP: percet isEndState");
            return ActionName.THE_END;
        } else {
            currentState = (nextState == null) ? Environment.getCurrentStateAfterActionForAgent(this, action) : nextState;
            action = policy.getBestActions().get(nextState);
            shouldExplore = (loopNumber >= Math.floor(shouldExploreLoopNumber));
            if (action == null || action == ActionName.NONE || shouldExplore) {
                action = ActionName.getRandomAction();
                shouldExploreLoopNumber = loopNumber;
                shouldExploreLoopNumber *= shouldExploreProbability;
            }
        }
        return action;

    }

    private HashMap<ActionName, Integer> increaseActionCounter(HashMap<ActionName, Integer> actionCount) {
        actionCount.computeIfPresent(action, (actionName, count) -> ++count);
        actionCount.putIfAbsent(action, 1);
        return actionCount;
    }



    private HashMap<State, Double> policyEvaluation() {
        HashMap<State, PossibleTransition> nextStates;
        for (State state : mdp.keySet()) {
            try {
                nextStates = mdp.get(state).get(policy.getBestActions().get(state));
                if (nextStates != null)
                    utilities.put(state, (double) Environment.amICloseReward(state, null, policy.getBestActions().get(state)) + Main.GAMA *
                            nextStates.entrySet().stream().mapToDouble(e -> e.getValue().getProbability() * (utilities.get(e.getKey()) == null ? 0.0 : utilities.get(e.getKey()))).sum());
                //here I calculate for next action reward I generally do this this previous state.
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return utilities;
    }

    @Override
    public void run() {
        // ok so the agent knows how many states there are as he does have action
        // policy. but i don't want to use the policy'nextState knowledge cause i might
        // not have it in another case, so i'll run up to the point that the
        // environment tells me i'm in state 5
        double reward = 0;
        currentState = null;
        do {
            action = percept();
            nextState = Environment.getCurrentStateAfterActionForAgent(this, action);
            loopNumber++;
        } while (!action.equals(ActionName.THE_END));

        System.out.println("PADP: Finished Passive ADP ");
        this.stopped = true;
    }

    public HashMap<State, HashMap<ActionName, Integer>> getStateActionVisits() {
        return stateActionVisits;
    }

    public void setStateActionVisits(HashMap<State, HashMap<ActionName, Integer>> stateActionVisits) {
        this.stateActionVisits = stateActionVisits;
    }

    public HashMap<Map<State, State>, HashMap<ActionName, Integer>> getStateActionStateVisits() {
        return stateActionStateVisits;
    }

    public void setStateActionStateVisits(HashMap<Map<State, State>, HashMap<ActionName, Integer>> stateActionStateVisits) {
        this.stateActionStateVisits = stateActionStateVisits;
    }

}
