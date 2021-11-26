package general;


import general.abstractCs.AbstractState;

import java.util.HashMap;
import java.util.Objects;

public class State extends AbstractState {

    public State(Integer x, Integer y){
        super();
        HashMap<AxisName, Integer> properties = new HashMap<>() ;
        properties.put(AxisName.Y, y);
        properties.put(AxisName.X , x);
        this.setStateProperties(properties);
        this.setId(setIdByProperties(properties));
        this.setUtility(0.0);
    }

    public String setIdByProperties(HashMap<? extends Enum, ? extends Object> properties) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (Object property : properties.values()) {
                sb.append(((Integer) property).toString());
                sb.append(",");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("]");
            return sb.toString();

    }
    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof State)) {
            return false;
        }
        State s = (State) o;
        return ((State) o).getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public HashMap<? extends Enum, Integer> getStateProperties() {
        return (HashMap<? extends Enum, Integer>) super.getStateProperties();
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
