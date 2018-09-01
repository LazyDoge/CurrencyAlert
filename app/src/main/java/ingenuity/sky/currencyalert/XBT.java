package ingenuity.sky.currencyalert;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.Callable;

/**
 * Created by saymon on 21.06.17.
 */
public class XBT extends Currency implements Callable<Double>{
    static boolean isActive;

    public XBT(String name) {
        super(name);
    }


    public void setActive(boolean active) {
        isActive = active;

    }

    @Override
    public Double call() throws Exception {
        double resultAmount = -1;
        Document doc = Jsoup.connect("https://myfin.by/crypto-rates/bitcoin").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.42 Safari/537.36").timeout(5000).get();

        String resultAmountString = null;
        try {
            resultAmountString = doc.getElementsByClass("birzha_info_head_rates").first().text();
            resultAmountString = resultAmountString.replace(",", "").substring(0, resultAmountString.length()-1);

            resultAmount = Double.parseDouble(resultAmountString);
        } catch (Exception e) {
            resultAmount = -1;
        }




        return resultAmount;
    }
}
