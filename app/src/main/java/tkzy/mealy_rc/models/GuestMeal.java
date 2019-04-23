package tkzy.mealy_rc.models;

@SuppressWarnings("FieldCanBeLocal")
public class GuestMeal {

    private boolean dayMeal, nightMeal;

    public GuestMeal(boolean dayMeal, boolean nightMeal) {
        this.dayMeal = dayMeal;
        this.nightMeal = nightMeal;
    }

    public GuestMeal() {
    }

    public boolean isDayMeal() {
        return dayMeal;
    }

    public void setDayMeal(boolean dayMeal) {
        this.dayMeal = dayMeal;
    }

    public boolean isNightMeal() {
        return nightMeal;
    }

    public void setNightMeal(boolean nightMeal) {
        this.nightMeal = nightMeal;
    }

}
