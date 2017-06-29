package ingenuity.sky.currencyalert;

/**
 * Created by saymon on 21.06.17.
 */
public class EUR  extends XECurrency{
    static boolean isActive;
    public EUR(String name, String to) {
        super(name, to);
        from = "EUR";
    }

    public static void setActive(boolean active) {
        isActive = active;
    }
}
