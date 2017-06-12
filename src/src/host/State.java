package host;

/**
 * Created by TC_Yeh on 5/26/2017.
 */
public class State {
    private String stateName;
    private int stateValue;
    private boolean isByzaninte;

    /**
     * This constructor builds instance with input value.
     * @param stateName stateName
     * @param stateValue stateValue
     */
    State(String stateName, int stateValue){
        this.stateName = stateName;
        this.stateValue = stateValue;
        isByzaninte = false;
    }

    /**
     * This method changes stateValue.
     * @param newValue stateValue
     */
    public void changeState(int newValue){
        stateValue = newValue;
    }

    /**
     * This method returns stateName;
     * @return stateName
     */
    public String getStateName(){
        return stateName;
    }

    /**
     * This method returns stateValue.
     * @return stateVaule
     */
    public int getStateValue(){
        return stateValue;
    }

    /**
     * This method returns string:"stateName -> stateValue"
     * @return toString
     */
    public String toString(){
        return stateName + "->" + stateValue;
    }

    public State clone(){
        return new State(stateName, stateValue);
    }

    /**
     * This method compares two states, returns true if they have same stateName and stateValue.
     * @param aState another state
     * @return is equal
     */
    public boolean equals(State aState){
        if (this.stateName.equals(aState.getStateName()) && this.stateValue == aState.getStateValue()){
            return true;
        }
        return false;
    }

    public boolean isByzaninte(){
        return  isByzaninte;
    }

    public void setByzaninte(boolean isByzaninte){
        this.isByzaninte = isByzaninte;
    }
}
