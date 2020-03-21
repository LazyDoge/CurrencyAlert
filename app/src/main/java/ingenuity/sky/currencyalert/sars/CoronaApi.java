package ingenuity.sky.currencyalert.sars;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CoronaApi  {

    @GET("/all")
    Call<CountryStats> all();

    @GET("/countries/{country}")
    Call<CountryStats> getCountryStats(@Path("country") String country);

}
