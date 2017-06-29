package ingenuity.sky.currencyalert;

/**
 * Created by saymon on 21.06.17.
 */
public class XBT extends XECurrency {
    static boolean isActive;

    public XBT(String name, String to) {
        super(name, "USD");
        from = "XBT";
    }


    public void setActive(boolean active) {
        isActive = active;
    }
}
