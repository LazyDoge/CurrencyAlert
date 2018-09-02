package ingenuity.sky.currencyalert;

public class CustomCurrency extends XECurrency {
    static boolean isActive;

    public CustomCurrency(String from, String to) {
        super(from+"-"+to, to);
        setFrom(from);

    }

    public static void setActive(boolean active) {
      isActive = active;
    }
}
