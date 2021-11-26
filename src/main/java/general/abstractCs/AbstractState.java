package general.abstractCs;

import java.util.HashMap;

public abstract class AbstractState implements PropertyInterface {

    private String id;
    private HashMap<? extends Enum, ? extends Object> stateProperties;
    private Double utility;

    //TODO do I need this?
    private Double reward;

    protected abstract String setIdByProperties(HashMap<? extends Enum, ? extends Object> properties);

    public HashMap<? extends Enum, ? extends Object> getStateProperties() {
        return stateProperties;
    }

    public void setStateProperties(HashMap<? extends Enum, ? extends Object> stateProperties) {
        this.stateProperties = stateProperties;
    }

    public Double getUtility() {
        return utility;
    }

    public void setUtility(Double utility) {
        this.utility = utility;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getReward() {
        return reward;
    }

    public void setReward(Double reward) {
        this.reward = reward;
    }

    public String toString() {
       return this.getId();
    }
}
