package ingenuity.sky.currencyalert;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.Callable;

/**
 * Created by saymon on 18.06.17.
 */
public class XECurrency extends  Currency implements Callable<Double> {
    String from;
    String to;

    public XECurrency(String name, String to) {
        super(name);
        this.to = to;
    }

    @Override
    public Double call() throws Exception {
        double resultAmount = -1;
        Document doc = Jsoup.connect(String.format("http://www.xe.com/currencyconverter/convert/?Amount=1&From=%s&To=%s", from, to)).userAgent(" Chrome/58.0.3029.81").timeout(5000).get();

        String resultAmountString = null;
        try {
            resultAmountString = doc.getElementsByClass("uccResultAmount").first().text();
            resultAmountString = resultAmountString.replace(",", "");

            resultAmount = Double.parseDouble(resultAmountString);
        } catch (Exception e) {
            resultAmount = -1;
        }




        return resultAmount;
    }
}
