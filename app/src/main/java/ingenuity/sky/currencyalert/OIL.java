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
        Document doc = Jsoup.connect("http://www.finanz.ru/birzhevyye-tovary/grafik-v-realnom-vremeni/neft-cena?type=Brent").userAgent(" Chrome/58.0.3029.81").timeout(5000).get();
        String resultAmountString = null;
        try {

            resultAmountString = doc.getElementsByAttribute("data-animation").first().text();
            resultAmountString = resultAmountString.replace(",", ".");
            resultAmount = Double.parseDouble(resultAmountString);
        } catch (Exception e) {
            resultAmount = -1;
        }




        return resultAmount;
    }
}
