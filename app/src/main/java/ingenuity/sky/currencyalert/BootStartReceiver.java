package ingenuity.sky.currencyalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by saymon on 17.06.17.
 */
public class BootStartReceiver extends BroadcastReceiver {


    private boolean serviceOn;
    private static SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            MainActivity.alarmEventReceiver = new AlarmEventReceiver();
            sharedPreferences = context.getSharedPreferences("mainPreferences", Context.MODE_PRIVATE);
            if ( sharedPreferences.getBoolean("service_on", false)){

                MainActivity.alarmEventReceiver.start(context);
            }
        } catch (Exception e) {
            sharedPreferences.edit().putString("удалить", e.toString());
        }


    }


    public boolean isServiceOn() {
        return serviceOn;
    }

    public void setServiceOn(boolean serviceOn) {
        this.serviceOn = serviceOn;
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static void setSharedPreferences(SharedPreferences sharedPreferences) {
        BootStartReceiver.sharedPreferences = sharedPreferences;
    }
}
