package tkzy.mealy_rc.models;

@SuppressWarnings("FieldCanBeLocal")
public class PreviousMeal {

    // Variables
    private boolean previousDayMeal;
    private boolean previousNightMeal;

    public PreviousMeal(boolean previousDayMeal, boolean previousNightMeal) {
        this.previousDayMeal = previousDayMeal;
        this.previousNightMeal = previousNightMeal;
    }

    public PreviousMeal() {
    }

    public boolean isPreviousDayMeal() {
        return previousDayMeal;
    }

    public void setPreviousDayMeal(boolean previousDayMeal) {
        this.previousDayMeal = previousDayMeal;
    }

    public boolean isPreviousNightMeal() {
        return previousNightMeal;
    }

    public void setPreviousNightMeal(boolean previousNightMeal) {
        this.previousNightMeal = previousNightMeal;
    }

}
