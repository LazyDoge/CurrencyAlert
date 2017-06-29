package ingenuity.sky.currencyalert;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.Callable;

/**
 * Created by saymon on 17.06.17.
 */
public class USD extends XECurrency {
    static boolean isActive;

    public USD(String name, String to) {
        super(name, to);
        from = "USD";
    }


    public void setActive(boolean active) {
        isActive = active;
    }
}
