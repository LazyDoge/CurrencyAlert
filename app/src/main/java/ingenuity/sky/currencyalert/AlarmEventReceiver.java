package ingenuity.sky.currencyalert;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by saymon on 17.06.17.
 */
public class AlarmEventReceiver extends BroadcastReceiver {
    private static AlarmManager alarmManager;
    private static PendingIntent pendingIntent;
    static ArrayList<Currency> currencyList;
    static SharedPreferences localPreferences;
    static int period;
    static TextView currentTextView;

    public static TextView getCurrentTextView() {
        return currentTextView;
    }

    public static void setCurrentTextView(String name) {
        switch (name) {
            case "usd":
                currentTextView = MainActivity.usdText;
                break;
            case "XBT":
                currentTextView = MainActivity.XBTText;
                break;
            case "EUR":
                currentTextView = MainActivity.EURText;
                break;
            case "OIL":
                currentTextView = MainActivity.OILText;
        }

    }

    private static boolean parsing;

    public static void setParsing(boolean parsing) {
        AlarmEventReceiver.parsing = parsing;
    }


    public void setaDoubleD(double aDoubleD) {
        this.aDoubleD = aDoubleD;
    }

    double aDoubleD;



    @SuppressWarnings("unchecked")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override

    public void onReceive(final Context context, Intent intent) {
//        localPreferencesInit(context);
//        if (!localPreferences.getBoolean("service_on", false)) {
//            return;
//        }


        new Thread(new Runnable() {


            @Override
            public void run() {


                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "currWakeLock");
                wakeLock.acquire();

                currencyListInit(context);

                if (currencyList.isEmpty()) {
                    wakeLock.release();
                    return;
                }

                StringBuilder contentText = new StringBuilder();
                CharSequence contentTitle = "title";
                boolean notificationNeeded = false;

                ArrayList<Future<Double>> futures = new ArrayList<>();
                for (Currency currency :
                        currencyList) {

                    ExecutorService executorService = Executors.newFixedThreadPool(2);
                    if (currency instanceof XECurrency) {
                        futures.add(executorService.submit((XECurrency) currency));
                    } else if (currency instanceof OIL) {
                        futures.add(executorService.submit((OIL) currency));
                    }

                }

                localPreferencesInit(context);

                int failCounter = localPreferences.getInt("fail", 0);

//                System.out.println(String.format("curr = %d  futur =%d", currencyList.size(), futures.size()));



                for (int i = 0; i<futures.size(); i++) {


                    String name = currencyList.get(i).getName();
//                    System.out.println(name);

                    try {
                        setCurrentTextView(name);
                    } catch (Exception e) {

                    }

                    double lowBorder = localPreferences.getFloat(name +"min", 0);
                    double hiBorder = localPreferences.getFloat(name + "max", Integer.MAX_VALUE);
                    double current = -1;
                    try {

                        parsing = true;
                        new AsyncTask<Future<Double>, Void, Void>() {
                            private double aDouble;



                            @Override
                            protected Void doInBackground(Future<Double>[] futures) {
                                publishProgress();

                                try {
                                    aDouble = futures[0].get();
                                } catch (InterruptedException | ExecutionException e) {

                                }

                                return null;
                            }


                            @Override
                            protected void onProgressUpdate(Void... values) {
                                super.onProgressUpdate(values);
                                try {
                                    MainActivity.progressBar.setVisibility(ProgressBar.VISIBLE);
                                } catch (Exception e) {

                                }

                            }


                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                setaDoubleD(aDouble);

                                try {


                                    currentTextView.setText(String.valueOf(aDouble));
                                    MainActivity.progressBar.setVisibility(ProgressBar.INVISIBLE);
                                } catch (Exception e) {

                                }

                                ////
                                setParsing(false);


                                ////



                            }
                        }.execute(futures.get(i));

                    } catch (Exception e) {

                        failCounter++;
                        localPreferences.edit().putInt("fail", failCounter).apply();
                        setParsing(false);

                        break;
                    }


                    while (parsing) {

                        //nop
                    }

                    if (aDoubleD == -1.0 || aDoubleD == 0.0) {
                        failCounter++;
                        localPreferences.edit().putInt("fail", failCounter).apply();
                        break;
                    }


                    if (aDoubleD < lowBorder || aDoubleD > hiBorder) {
                        notificationNeeded = true;
                        contentText.append(name.toUpperCase()).append(" = ").append(aDoubleD).append("\r\n");
                        contentTitle = "Валюта вышла за границы!";

                    }


                    if (i == futures.size() - 1 && !notificationNeeded) {
                        failCounter = 0;
                        localPreferences.edit().putInt("fail", failCounter).apply();
                    }

                }




                if (period == 0) {

                    period = localPreferences.getInt("period", 180);
                }

                if (failCounter > ((7 * 60) / period)) {
                    notificationNeeded = true;
                    contentText = new StringBuilder("Не удается обновить данные длительное время.");
                    contentTitle = "Неудача!";
                }

                if (notificationNeeded) {
                    sendNotification(context, contentText.toString(), contentTitle);

                }

                wakeLock.release();

            }
        }).start();








    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(Context context, CharSequence contentText, CharSequence contentTitle) {
        Intent notficationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notficationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Resources resources = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

//        int icon = R.mipmap.ic_launcher;
        builder.setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
//                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(contentText.toString())
                .setContentTitle(contentTitle);
        if (contentText.length() > 15) {
            builder.setStyle(new Notification.BigTextStyle().bigText(contentText.toString()));
        }


        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_ALL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        miuiIconNormalize(notification);
        notificationManager.notify(101, notification);
    }

    private void miuiIconNormalize(Notification notification) {
        try {
            Class miuiNotificationClass = Class.forName("android.app.MiuiNotification");
            Object miuiNotification = miuiNotificationClass.newInstance();
            Field field = miuiNotification.getClass().getDeclaredField("customizedIcon");
            field.setAccessible(true);

            field.set(miuiNotification, true);
            field = notification.getClass().getField("extraNotification");
            field.setAccessible(true);

            field.set(notification, miuiNotification);
        } catch (Exception e) {

        }
    }

    private void currencyListInit(Context context) {
        currencyList = new ArrayList<>();
        String to = "RUB";
        try {
            localPreferencesInit(context);
            to = localPreferences.getString("to", "RUB");

            USD usd = new USD("usd", to);
            if (localPreferences.getBoolean("usd_is_active", false)) {

                currencyList.add(usd);
            }


            EUR eur = new EUR("EUR", to);
            if (localPreferences.getBoolean("EUR_is_active", false)) {

                currencyList.add(eur);
            }

            XBT xbt = new XBT("XBT", "USD");
            if (localPreferences.getBoolean("XBT_is_active", false)) {

                currencyList.add(xbt);
            }

            OIL oil= new OIL("OIL");
            if (localPreferences.getBoolean("OIL_is_active", false)) {

                currencyList.add(oil);
            }
        } catch (Exception e) {
            //nop
        }
    }

    public void start(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmEventReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        period = 0;
        try {
            localPreferencesInit(context);
            period = localPreferences.getInt("period", 180);
        } catch (Exception e) {
            period = 180;
        }
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000 * period, pendingIntent);



    }

    private void localPreferencesInit(Context context) {
        if (localPreferences == null) {
            localPreferences = context.getSharedPreferences("mainPreferences", Context.MODE_PRIVATE);
        }
    }

    public void stop() {

        if (alarmManager != null && pendingIntent != null) {

            alarmManager.cancel(pendingIntent);
        }
//        new AsyncTask<Void, Void, Void>() {
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//
//                publishProgress();
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//
//                return null;
//            }
//
//            @Override
//            protected void onProgressUpdate(Void... values) {
//                super.onProgressUpdate(values);
//                MainActivity.progressBar.setVisibility(ProgressBar.VISIBLE);
//
//
//
//            }
//        }.execute();
//
//
//
//        new AsyncTask<Void, Void, Void>() {
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//
//                publishProgress();
//
//                return null;
//            }
//
//            @Override
//            protected void onProgressUpdate(Void... values) {
//                super.onProgressUpdate(values);
//                MainActivity.progressBar.setVisibility(ProgressBar.INVISIBLE);
//
//
//
//            }
//        }.execute();
    }
}
