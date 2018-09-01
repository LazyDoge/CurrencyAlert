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
        if (from.equalsIgnoreCase(to)) {
            return 1.0;
        }

//        Document doc = Jsoup.connect(String.format("http://www.xe.com/currencyconverter/convert/?Amount=1&From=%s&To=%s", from, to)).userAgent(" Chrome/58.0.3029.81").timeout(5000).get();
        Document doc = Jsoup.connect(String.format("https://transferwise.com/gb/currency-converter/%s-to-%s-rate?amount=1", from.toLowerCase(), to.toLowerCase())).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.42 Safari/537.36").timeout(5000).get();

        String resultAmountString = null;
        try {
//            resultAmountString = doc.getElementsByClass("uccResultAmount").first().text();
//            resultAmountString = resultAmountString.replace(",", "");

            resultAmountString = doc.getElementsByClass("text-success").first().text();
            resultAmountString = resultAmountString.replace(",", "").substring(0, resultAmountString.length()-4);

            resultAmount = Double.parseDouble(resultAmountString);
        } catch (Exception e) {
            resultAmount = -1;
        }




        return resultAmount;
    }
}
