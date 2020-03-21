package ingenuity.sky.currencyalert.sars;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ingenuity.sky.currencyalert.MainActivity;
import ingenuity.sky.currencyalert.R;
import ingenuity.sky.currencyalert.constants.LocalPreferencesKeys;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//https://habr.com/ru/post/428736/
public class Controller implements Callback<CountryStats> {

    private MainActivity mainActivity;
    private Map<String, TextView> all;
    private Map<String, TextView> country;

    public Controller(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        all = new HashMap<>();
        all.put("cases", (TextView) mainActivity.findViewById(R.id.trow2));
        all.put("deaths", (TextView) mainActivity.findViewById(R.id.trow3));
        all.put("recovered", (TextView) mainActivity.findViewById(R.id.trow4));
        all.put("mortality", (TextView) mainActivity.findViewById(R.id.trow5));

        country = new HashMap<>();
        country.put("cases", (TextView) mainActivity.findViewById(R.id.trow22));
        country.put("deaths", (TextView) mainActivity.findViewById(R.id.trow23));
        country.put("recovered", (TextView) mainActivity.findViewById(R.id.trow24));
        country.put("mortality", (TextView) mainActivity.findViewById(R.id.trow25));
    }

    public static final String BASE_URL = "https://coronavirus-19-api.herokuapp.com";

    @Override
    public void onResponse(Call<CountryStats> call, Response<CountryStats> response) {
        if (response.isSuccessful()) {
            CountryStats stats = response.body();
            stats.setMortality();
            if (stats.getCountry() != null) {
                pushData(stats, country);
            } else {
                pushData(stats, all);
            }

        }
    }

    @SuppressLint("DefaultLocale")
    private void pushData(CountryStats stats, Map<String, TextView> map) {
        map.get("cases").setText(String.valueOf(stats.getCases()));
        map.get("deaths").setText(String.valueOf(stats.getDeaths()));
        map.get("recovered").setText(String.valueOf(stats.getRecovered()));
        map.get("mortality").setText(String.format("%.3f %%", stats.getMortality()));

    }

    public void start() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        CoronaApi coronaApi = retrofit.create(CoronaApi.class);

        String countryName =
                MainActivity.dataCountry[mainActivity.getLocalPreferences()
                        .getInt(LocalPreferencesKeys.COUNTRY, 0)];

        Call<CountryStats> russia = coronaApi.getCountryStats(countryName);
        Call<CountryStats> all = coronaApi.all();

        all.enqueue(this);
        russia.enqueue(this);
    }

    @Override
    public void onFailure(Call<CountryStats> call, Throwable t) {
        t.printStackTrace();
    }
}
