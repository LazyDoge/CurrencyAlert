package ingenuity.sky.currencyalert.sars;

public class CountryStats {

    private String country;
    private long cases;
    private long deaths;
    private long recovered;
    private float mortality;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getCases() {
        return cases;
    }

    public void setCases(long cases) {
        this.cases = cases;
    }

    public long getDeaths() {
        return deaths;
    }

    public void setDeaths(long deaths) {
        this.deaths = deaths;
    }

    public long getRecovered() {
        return recovered;
    }

    public void setRecovered(long recovered) {
        this.recovered = recovered;
    }

    public float getMortality() {
        return mortality ;
    }

    public void setMortality(float mortality) {
        this.mortality = mortality;
    }

    public void setMortality() {
        this.mortality = (((float) deaths) * 100)/cases;
    }


    @Override
    public String toString() {
        return "CountryStats{" +
                "cases=" + cases +
                ", deaths=" + deaths +
                ", recovered=" + recovered +
                ", mortality=" + mortality +
                '}';
    }
}
