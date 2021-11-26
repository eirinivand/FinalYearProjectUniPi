package world;

import general.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import theTwo.PassiveADPAgent;
import theTwo.TDAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Environment {

    private HashMap<PassiveADPAgent, Thread> threads;
    private ArrayList<PassiveADPAgent> agents;
    private static ArrayList<Obstacle> obstacles;
    private static ArrayList<Obstacle> obstacles2;

    private static HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> model;
    private static HashMap<PassiveADPAgent, State> currentStatesOfAgents;
    private static HashMap<TDAgent, State> currentStatesOfTDAgents;
    private static State endState;
    private static int cols;
    private static int rows;
    static int MAX_POLICY_GRADE;

    private static Logger logger = LogManager.getLogger(Environment.class.getName());

    /**
     * @param rows
     * @param cols
     * @param numOfObst
     * @param endStateX
     * @param endStateY
     */
    public Environment(int rows, int cols, int numOfObst, int endStateX, int endStateY) {
        super();

        this.rows = rows;
        this.cols = cols;
        MAX_POLICY_GRADE = rows * cols;
        this.agents = new ArrayList<>(1);
        this.threads = new HashMap<>(1);
        this.obstacles = new ArrayList<Obstacle>(1);
        currentStatesOfAgents = new HashMap<>(1);
        currentStatesOfTDAgents = new HashMap<>(1);

        Random rand = new Random();

        for (int i = 0; i < numOfObst; i++) {
            int[] la = freeSpot(rand, endStateX, endStateY);
            obstacles.add(new Obstacle(la[0], la[1]));
//			generic.PaintWorld.table1.setValueAt("X", la[0], la[1]);

        }
        logger.info("			Successfully create world");

        for (int k = 0; k < obstacles.size(); k++) {
            logger
                    .info("			obstacles" + " x: " + obstacles.get(k).getX() + " y: " + obstacles.get(k).getY());
        }


//        obstacles.sort(new Comparator<Obstacle>() {
//            @Override
//            public int compare(Obstacle o1, Obstacle o2) {
//
//                if (o1.getX() <= o2.getX())
//                    return 1;
//                else if (o1.getX() == o2.getX())
//                    return 0;
//                return -1;
//            }
//        });

        generateMDP(rows, cols, endStateX, endStateY);
        setObstacles2(obstacles);
        logger.info("endState: " + endState.getId());
    }


    public static State getCurrentStateForAgent(PassiveADPAgent agent) {
        return currentStatesOfAgents.get(agent);
    }

    /**
     * based on the closest value.
     * TODO analyze it
     *
     * @param passiveADPAgent
     * @param action
     * @return
     */
    public static State getCurrentStateAfterActionForAgent(PassiveADPAgent passiveADPAgent, ActionName action) {
        Random r = new Random();
        int n = r.nextInt(100) + 1;
        Double minDiff = Double.MAX_VALUE;
        ArrayList<State> possibleNextS = new ArrayList<>();
        State currentStateOfAgent = currentStatesOfAgents.get(passiveADPAgent);
        if (currentStateOfAgent == null || action == null) {
            if (currentStateOfAgent == null) {
                System.out.println("Environment.getCurrentStateAfterActionForAgent State: " + currentStateOfAgent + " Did you just start ?");
                currentStatesOfAgents.put(passiveADPAgent, World.startState);
                return World.startState;
            }
            System.out.println("Environment.getCurrentStateAfterActionForAgent Action: " + action + " Did you just start ?");
            return currentStatesOfAgents.get(passiveADPAgent);

        }
        /*
         * for each probability in the state's possible transitions
         */
        HashMap<State, PossibleTransition> pts = model.get(currentStateOfAgent).get(action);
        do {
            //find a possible end state based on the current state and given action
            if (pts != null)
                for (State fs : pts.keySet()) {
                    Double diff = pts.get(fs).getProbability() * 100 - n;
                    if (Math.abs(diff) < Math.abs(minDiff) && pts.get(fs).getNextState() != null) {
                        minDiff = diff;
                        possibleNextS.clear();
                        possibleNextS.add(pts.get(fs).getNextState());
                    } else if (Math.abs(diff) == Math.abs(minDiff) && pts.get(fs).getNextState() != null) {
                        possibleNextS.add(pts.get(fs).getNextState());
                    }
                }
            else
                return currentStatesOfAgents.get(passiveADPAgent);


        } while (possibleNextS.isEmpty());
        // I think i can simply return the .put but not sure for previously associated with null values.
        currentStatesOfAgents.put(passiveADPAgent, possibleNextS.get(r.nextInt(possibleNextS.size())));
        //logger.warn(currentStatesOfAgents.get(passiveADPAgent));
        return currentStatesOfAgents.get(passiveADPAgent);
    }

    public static State getCurrentStateAfterActionForTDAgent(TDAgent tdAgent, ActionName action) {
//        Random r = new Random();
//        int n = r.nextInt(100) + 1;
//        Double minDiff = Double.MAX_VALUE;
//        ArrayList<State> possibleNextS = new ArrayList<>();
//        State currentStateOfAgent = currentStatesOfTDAgents.get(tdAgent);
//        if (currentStateOfAgent == null || action == null) {
//            if(currentStateOfAgent== null ) {
//                System.out.println("Environment.getCurrentStateAfterActionForAgent State: " + currentStateOfAgent+ " Did you just start ?");
//                currentStatesOfTDAgents.put(tdAgent, World.startState);
//                return World.startState;
//            }
//            System.out.println("Environment.getCurrentStateAfterActionForAgent Action: " + action + " Did you just start ?");
//            return currentStatesOfTDAgents.get(tdAgent);
//
//        }
//        /*
//         * for each probability in the state's possible transitions
//         */
//        HashMap<State, PossibleTransition> pts = model.get(currentStateOfAgent).get(action);
//        do {
//            if (pts != null)
//                for (State fs : pts.keySet()) {
//                    Double diff = pts.get(fs).getProbability() * 100 - n;
//                    if (Math.abs(diff) < Math.abs(minDiff) && pts.get(fs).getNextState() != null) {
//                        minDiff = diff;
//                        possibleNextS.clear();
//                        possibleNextS.add(pts.get(fs).getNextState());
//                    } else if (Math.abs(diff) == Math.abs(minDiff) && pts.get(fs).getNextState() != null) {
//                        possibleNextS.add(pts.get(fs).getNextState());
//                    }
//                }
//            else{
//                int i = r.nextInt(4);
//                ActionName possibleNestAction =null;
//                switch (i) {
//                    case 0: possibleNestAction =ActionName.DOWN;
//                        break;
//                    // - down -
//                    case 1: possibleNestAction = ActionName.UP;
//                        break;
//                    // - left -
//                    case 2: possibleNestAction = ActionName.LEFT;
//                        break;
//                    // - right -
//                    case 3: possibleNestAction = ActionName.RIGHT;
//                        break;
//                }
//                pts = model.get(currentStateOfAgent).get(possibleNestAction);
//            }
//
//        }while(possibleNextS.isEmpty());
//        // I think i can simply return the .put but not sure for previously associated with null values.
//        currentStatesOfTDAgents.put(tdAgent, possibleNextS.get(r.nextInt(possibleNextS.size())));
//        //logger.warn(currentStatesOfAgents.get(passiveADPAgent));
        return currentStatesOfTDAgents.get(tdAgent);
    }

    public static int evaluatePolicy(Policy policy) {
        int grade = Environment.MAX_POLICY_GRADE;
        for (State state : policy.getBestActions().keySet()) {
            if (isEndState(state))
                continue;
            if (fallsToWall(state, policy.getBestActions().get(state))) {
                grade--;
                state.setReward(-100d);
                continue;
            } else
                state.setReward(10d);
            for (Obstacle obstacle : obstacles) {
                if (fallsToObstacle(obstacle, state, policy.getBestActions().get(state))) {
                    state.setReward(-100d);
                    grade--;
                } else
                    state.setReward(10d);
            }

            if (lookingEachOther(state, policy.getBestActions().get(state), policy.getBestActions())) {
                state.setReward(-100d);
                grade--;
            } else
                state.setReward(10d);

        }
        return grade;
    }

    private static boolean lookingEachOther(State currentState, ActionName actionOfCurrentState, HashMap<State, ActionName> bestActions) {
        State surroundingState;
        if (actionOfCurrentState.getAxisName().equals(AxisName.X))
            surroundingState = new State(((Integer) currentState.getStateProperties().get(actionOfCurrentState.getAxisName()) + actionOfCurrentState.getMoveInAxis()), (Integer) currentState.getStateProperties().get(AxisName.getOposite(actionOfCurrentState.getAxisName())));
        else {
            surroundingState = new State((Integer) currentState.getStateProperties().get(AxisName.getOposite(actionOfCurrentState.getAxisName())), (Integer) currentState.getStateProperties().get(actionOfCurrentState.getAxisName()) + actionOfCurrentState.getMoveInAxis());
        }
        ActionName actionOfSurroundingState = bestActions.get(surroundingState);
        return actionOfSurroundingState != null && !Environment.isEndState(surroundingState) && actionOfSurroundingState.getAxisName().equals(actionOfCurrentState.getAxisName()) && actionOfSurroundingState.getMoveInAxis().intValue() != actionOfCurrentState.getMoveInAxis().intValue();

    }


    /**
     * @param rand
     * @param endStateX
     * @param endStateY
     * @return
     */
    public int[] freeSpot(Random rand, int endStateX, int endStateY) {
        int numX = -1;
        int numY = -1;
        // mexris otou kataferei na brei mia kenh 8esh sunexizei
        boolean occupied = true;
        while (occupied) {
            occupied = false;
            numX = rand.nextInt(cols);
            numY = rand.nextInt(rows);
            for (int i = 0; i < obstacles.size(); i++) {
                if (numX == obstacles.get(i).getX() && numY == obstacles.get(i).getY()) {
                    occupied = true;
                }
            }
            // TODO change the bellow values to macth the positions of the agents
            if ((numX == 0 && numY == 0) || (numX == endStateX && numY == endStateY)) {
                occupied = true;
            }

        }
        int[] la = {numX, numY};
        return la;
    }

    /**
     * @param rows
     * @param cols
     * @param endStateX
     * @param endStateY
     */
    private void generateMDP(int rows, int cols, int endStateX, int endStateY) {
        model = new HashMap<>(1);
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {

                boolean notOccupied = true;
                for (int k = 0; k < obstacles.size(); k++) {
                    if (i == obstacles.get(k).getX() && j == obstacles.get(k).getY()) {
                        notOccupied = false;
                        break;
                    }
                }
                if (notOccupied) {
                    model.put(new State(i, j), new HashMap<>());
                }
            }
        }
        endState = new State(endStateX, endStateY);
        // TODO DONE ? ? ? Make it rain actions and probabilities :P
        generateActionsForAllStates();
        //   logger.info(model);

    }

    /**
     * Generates Actions For All States. For all states the actions available are
     * up, down, left, right but depending on the action the the agent takes he
     * might end up in different states meaning getting a different reward.
     */
    private void generateActionsForAllStates() {
        for (State s : model.keySet()) {
            HashMap<ActionName, HashMap<State, PossibleTransition>> actions = new HashMap<>();
            Integer x = (Integer) s.getStateProperties().get(AxisName.X);
            Integer y = (Integer) s.getStateProperties().get(AxisName.Y);
            for (int i = 0; i < 4; i++) {
                // adding up to actions of state
                State wantedNextState = null;
                ActionName actionName = null;
                switch (i) {
                    case 0:
                        wantedNextState = findStateByXY(x, y + 1);
                        actionName = ActionName.UP;
                        break;
                    // - down -
                    case 1:
                        wantedNextState = findStateByXY(x, y - 1);
                        actionName = ActionName.DOWN;
                        break;
                    // - left -
                    case 2:
                        wantedNextState = findStateByXY(x - 1, y);
                        actionName = ActionName.LEFT;
                        break;
                    // - right -
                    case 3:
                        wantedNextState = findStateByXY(x + 1, y);
                        actionName = ActionName.RIGHT;
                        break;
                }
                if (wantedNextState != null)
                    actions.put(actionName, generatePossibleTransitions(s, wantedNextState));
                else {
                    HashMap<State, PossibleTransition> pts = new HashMap<>();
                    pts.put(s, new PossibleTransition(1d, s, -4d));
                    actions.put(actionName, pts);
                }
            }
            model.put(s, actions);
        }
    }

    /**
     * It will take you where you want to go with a probability of 0.7 but you will
     * slip with a probability of 0.1 , the rewards vary depending on
     * if it hits wall stays at the same point
     */
    private HashMap<State, PossibleTransition> generatePossibleTransitions(State currentState, State wantedNextState) {
        HashMap<State, PossibleTransition> pts = new HashMap<>();
        Integer x = (Integer) currentState.getStateProperties().get(AxisName.X);
        Integer y = (Integer) currentState.getStateProperties().get(AxisName.Y);

        // -- possible transitions--
        for (int i = 0; i < 4; i++) {
            // - up -

            State possibleNestState = null;
            switch (i) {
                case 0:
                    possibleNestState = findStateByXY(x, y + 1);
                    break;
                // - down -
                case 1:
                    possibleNestState = findStateByXY(x, y - 1);
                    break;
                // - left -
                case 2:
                    possibleNestState = findStateByXY(x - 1, y);
                    break;
                // - right -
                case 3:
                    possibleNestState = findStateByXY(x + 1, y);
                    break;
            }
            possibleNestState = possibleNestState == null ? currentState : possibleNestState;
            pts.put(possibleNestState, generateGenericProbabilities(possibleNestState));

        }
/*
        boolean nextStateExists = false;
		Iterator itr = possibleTransitionsForAction.iterator();
		while(itr.hasNext()) {
			PossibleTransition pt =(PossibleTransition) itr.next();
			if (pt.getNextState() !=null && pt.getNextState().equals(new State(successIfX,successIfY))) {
				pt.setProbability(0.7);
				nextStateExists = true;
			}
		}
*/
        for (State fs : pts.keySet()) {
            if (fs.getId().equals(wantedNextState.getId())) {
                pts.get(fs).setProbability(0.7);
            }

        }

        return pts;

    }

    public PossibleTransition generateGenericProbabilities(State nextState) {
        return new PossibleTransition(0.1, nextState, amICloseReward(nextState));
    }

    /**
     * This gives calculates the reward to be given based on the tiles away/close to
     * the end state. If I were to have negative rewards
     * <p>
     * update 17/07/2018
     * is it closer than before?
     *
     * @return
     */
    public static Double amICloseReward(Integer x, Integer y) {

        Double analogyForMaxReward= 100.0;
        Double maxRewardForWorld= rows + cols + 1.0;
        Double rewardPerState= maxRewardForWorld
               - Math.abs(Math.abs(x - endState.getStateProperties().get(AxisName.X))
                          + Math.abs(y - endState.getStateProperties().get(AxisName.Y)));
        Double percentageOfReward = rewardPerState * analogyForMaxReward / maxRewardForWorld;
        return percentageOfReward;
//        return -percentageOfReward * (analogyForMaxReward - percentageOfReward);
    }

    public static Double amICloseReward(State s) {
        return amICloseReward((Integer) s.getStateProperties().get(AxisName.X), (Integer) s.getStateProperties().get(AxisName.Y));
    }

    public static Double amICloseReward(State oldState, State currentState, ActionName actionMade) {
        /*
         *
         */
        if (currentState == null && oldState != null) {
            currentState = oldState;
            oldState = null;
        }

        if (isEndState(currentState)) {
            return Main.UtilityForEndState;
        }
        double rewardForState = amICloseReward(currentState);
        double rewardForOldState = oldState == null ? 0.0 : amICloseReward(oldState);

        if (isEndState(currentState))
            logger.info("reached end state.................................. " + rewardForState);


        if (rewardForOldState == rewardForState)
            rewardForState -= .5;
        else if (rewardForOldState < rewardForState) {
            rewardForState -= .5;
        } else{
            rewardForState -= .2;
        }
        for (Obstacle o : obstacles) {
            if (fallsToObstacle(o, currentState, actionMade)) {
                rewardForState *= 0.8;
                break;
            }
        }
        if (fallsToWall(currentState, actionMade)) {
            rewardForState *= 0.7;
        }
        return rewardForState;
    }

    private static boolean fallsToObstacle(Obstacle o, State oldState, ActionName actionMade) {
        return (Integer) o.getStateProperties().get(actionMade.getAxisName()) == ((Integer) oldState.getStateProperties().get(actionMade.getAxisName()) + actionMade.getMoveInAxis())
                && (Integer) o.getStateProperties().get(AxisName.getOposite(actionMade.getAxisName())) == ((Integer) oldState.getStateProperties().get(AxisName.getOposite(actionMade.getAxisName())));
    }

    //    private static boolean fallsToAgent(PassiveADPAgent agent, State oldState, ActionName actionMade) {
//        return (Integer) currentStatesOfAgents.get(agent).getStateProperties().get(actionMade.getAxisName()) == ((Integer) currentStatesOfAgents.keySet().stream()..getStateProperties().get(actionMade.getAxisName()) + actionMade.getMoveInAxis())
//                && (Integer) currentStatesOfAgents.get(agent).getStateProperties().get(AxisName.getOposite(actionMade.getAxisName()))
//                .equals(((Integer) oldState.getStateProperties().get(AxisName.getOposite(actionMade.getAxisName())))));
//    }
    private static boolean fallsToWall(State oldState, ActionName actionMade) {
        return (actionMade.getAxisName().equals(AxisName.Y) && rows <= ((Integer) oldState.getStateProperties().get(actionMade.getAxisName()) + actionMade.getMoveInAxis()) && ((Integer) oldState.getStateProperties().get(actionMade.getAxisName()) + actionMade.getMoveInAxis()) > 0 )
                || (actionMade.getAxisName().equals(AxisName.X) && cols <= ((Integer) oldState.getStateProperties().get(actionMade.getAxisName()) + actionMade.getMoveInAxis()) && ((Integer) oldState.getStateProperties().get(actionMade.getAxisName()) + actionMade.getMoveInAxis())> 0);
    }

    public ArrayList<PassiveADPAgent> getAgents() {
        return agents;
    }

    public void setAgents(ArrayList<PassiveADPAgent> agents) {
        this.agents = agents;
    }

    public static HashMap<PassiveADPAgent, State> getCurrentStatesOfAgents() {
        return currentStatesOfAgents;
    }

    protected static void setCurrentStatesOfAgents(HashMap<PassiveADPAgent, State> currentStatesOfAgents1) {
        currentStatesOfAgents = currentStatesOfAgents1;
    }

    public HashMap<PassiveADPAgent, Thread> getThreads() {
        return threads;
    }

    public void setThreads(HashMap<PassiveADPAgent, Thread> threads) {
        this.threads = threads;
    }

    /**
     * THIS IS NOT WORKING
     * i compute the reward as if the agent would successfully follow the best
     * policy meaning the chances are with him
     * there is a loss if previous state is 0 it does count the reward from the
     * posibility of looping through state 0
     *
     * @param p
     * @param cursor
     * @return
     */
    @Deprecated
    public static double rewardFromPolicy(Policy p, State cursor) {
        if (cursor.equals(p.getInitState())) {
            return 0;
        }
        double reward = 0;
        State previousS = p.throughWhichStateCanIGetHereByPolicy(cursor);
        int loopedToManyTimes = 0;
        /**
         * this is problematic since 2018-05-21
         */
//		while (!cursor.equals(p.getInitState()) && !previousS.equals(cursor) && previousS.getX() != -4 && !mdpR.isEndState(cursor)) {
//			double rewardForStateFromState = 0;
//			HashMap<ActionName, PossibleTransition> actions= model.get(cursor);

//			if(actions==null){
//				System.out.println("action null");
//			}
//			actionsLoop:
//			for (PossibleTransition a : actions.values())

//				for (State k : a.getProbability())
//					if (k.equals(previousS)) {
//						rewardForStateFromState = a.getProbabilities().get(k).getReward();
//						break actionsLoop;
//					}
//			reward += rewardForStateFromState;

//			State previousBy2S = previousS;
//			previousS = cursor;
//			cursor = p.throughWhichStateCanIGetHereByPolicy(previousS);
//			//TODO specify how many loops are acceptable
//			if (previousBy2S.equals(cursor)) {
//				loopedToManyTimes++;
//				if (loopedToManyTimes >= 4) {

//					return -1;
//				}
//			}
//		}
        return reward;
    }

    /**
     * @param name
     * @param initS
     * @return
     */
    @Deprecated
    protected static ActionName getActionByNameAndInitState(ActionName name, State initS) {
//		for (State s: mdpR.getMdp().keySet())
//			if(s.equals(initS))
//				for (Action a : mdpR.getMdp().get(s).values())
//					if(a.getName().equals(name))
//						return a;
        logger.warn("using OLD generic.MDP.getActionByNameAndInitState should #NOT");
        return null;
    }

    /**
     * @param s
     * @return
     */
    public static boolean isEndState(State s) {
        if (s != null && Environment.endState.getId().equals(s.getId())) {
            logger.log(Level.INFO, "Reached is end state");
            return true;
        }
        return false;
    }

    /**
     * NOT USED ANYMORE
     *
     * @param endS
     * @return
     */
    @Deprecated
    public State througtWhichStateCanIGetHereByMDP(State endS) {
//		for (State s : model.keySet())
//			for (int i = 0; i < model.get(s).size(); i++)
//				for (State j : model.get.getMdp().get(s).get(i).getProbabilities().keySet())
//					if (j.equals(endS))
//						return s;
        logger.warn("using OLD generic.Environment.througtWhichStateCanIGetHereByMDP should #NOT");
        return null;

    }

    /**
     * @return
     */
    public static Policy randomPolicy() {
        HashMap<State, ActionName> bestActions = new HashMap<>(1);
        for (State state : model.keySet()) {
            if (!Environment.isEndState(state))
                bestActions.put(state, randomActionForState());
        }
        // TODO set init State
        Policy policy = new Policy(new State(0, 0));
        policy.setBestActions(bestActions);
        return policy;
    }


    public static ActionName randomActionForState() {
        Random r = new Random();
        return ActionName.getById(r.nextInt(ActionName.getCountOfActions()));
    }


    /**
     * @param rand
     * @param endStateX
     * @param endStateY
     * @return
     */
    @Deprecated
    public int[] freeSpot2(Random rand, int endStateX, int endStateY) {
        int numX = -1;
        int numY = -1;
        // mexris otou kataferei na brei mia kenh 8esh sunexizei

        while (true) {
            boolean occupied = false;
            // Packet(int x, int y, String color, int destinationX, int
            // destinationY)
            numX = rand.nextInt(cols);
            numY = rand.nextInt(rows);
            for (int i = 0; i < obstacles.size(); i++) {
                if (numX == obstacles.get(i).getX() && numY == obstacles.get(i).getY()) {
                    occupied = true;
                }
            }
            if ((numX == 0 && numY == 0) || (numX == endStateX && numY == endStateY)) {
                occupied = true;
            }


            if (!occupied) {
                break;
            }
        }
        // topo8ethsh antikeimenou sthn kenh 8esh
        //	putOneObject(rand, numX, numY);
        int[] freeSpot = {numX, numX};
        return freeSpot;
    }

    public State findStateByXY(int x, int y) {
        for (State s : model.keySet())
            if ((Integer) s.getStateProperties().get(AxisName.X) == x && (Integer) s.getStateProperties().get(AxisName.Y) == y)
                return s;
        return null;
    }

    public static State getEndState() {
        return endState;
    }

    public static void setEndState(State endState) {
        Environment.endState = endState;
    }

    public static HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> getModel() {
        return model;
    }

    public static void setModel(HashMap<State, HashMap<ActionName, HashMap<State, PossibleTransition>>> model) {
        Environment.model = model;
    }

    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    public static void setObstacles2(ArrayList<Obstacle> obstacles2) {
        Environment.obstacles2 = obstacles2;
    }

    public static ArrayList<Obstacle> getObstacles2() {
        return obstacles2;
    }


}
