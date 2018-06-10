package ingenuity.sky.currencyalert;

/**
 * Created by saymon on 30.06.17.
 */
public class RUB extends  XECurrency {
    static boolean isActive;

    public RUB(String name, String to) {
        super(name, to);
        from = "RUB";
        swap();
    }

    private void swap() {
        String tmp = from;
        from = this.to;
        this.to = tmp;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
