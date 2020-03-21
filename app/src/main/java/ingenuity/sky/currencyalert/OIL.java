package ingenuity.sky.currencyalert;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.Callable;

/**
 * Created by saymon on 21.06.17.
 */
public class OIL extends Currency implements Callable<Double> {
    static boolean isActive;


    public OIL(String name) {
        super(name);
    }

    @Override
    public Double call() throws Exception {
        double resultAmount = -1;
        try {
//            Document doc = Jsoup.connect("https://www.finanz.ru/birzhevyye-tovary/grafik-v-realnom-vremeni/neft-cena?type=Brent").userAgent(" Chrome/58.0.3029.81").timeout(5000).get();
            Document doc = Jsoup.connect("https://ru.investing.com/commodities/brent-oil").userAgent(" Chrome/58.0.3029.81").timeout(5000).get();
            String resultAmountString = null;

//            resultAmountString = doc.getElementsByAttribute("data-animation").first().text();
            resultAmountString = doc.getElementById("last_last").text();
            resultAmountString = resultAmountString.replace(",", ".");
            resultAmount = Double.parseDouble(resultAmountString);
        } catch (Exception ignored) {
        }

        return resultAmount;
    }
}
