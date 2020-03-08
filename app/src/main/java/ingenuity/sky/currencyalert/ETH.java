package ingenuity.sky.currencyalert;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.Callable;

/**
 * Created by saymon on 30.06.17.
 */
public class ETH extends Currency implements Callable<Double> {
    static boolean isActive;

    public ETH(String name) {
        super(name);
    }

    @Override
    public Double call() throws Exception {

        double resultAmount = -1;
        try {
            Document doc = Jsoup.connect("https://ru.investing.com/crypto/ethereum/eth-usd").userAgent(" Chrome/58.0.3029.81").timeout(5000).get();
            String resultAmountString = null;

            resultAmountString = doc.getElementsByClass("arial_26 inlineblock pid-1058142-last")
                    .first().text();
            resultAmountString = resultAmountString.replace(".", "");
            resultAmountString = resultAmountString.replace(",", ".");
            resultAmount = Double.parseDouble(resultAmountString);
        } catch (Exception ignored) {
        }

        return resultAmount;
    }
}
