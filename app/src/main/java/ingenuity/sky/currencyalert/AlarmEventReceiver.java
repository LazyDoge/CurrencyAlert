package ingenuity.sky.currencyalert;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by saymon on 17.06.17.
 */
public class AlarmEventReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "1011";
    private static AlarmManager alarmManager;
    private static PendingIntent pendingIntent;
    static ArrayList<Currency> currencyList;
    static SharedPreferences localPreferences;
    static int period;
    static TextView currentTextView;

    String[] dataFrom = {"RUB", "UAH", "KZT", "BYN", "GBP", "CNY", "JPY", "KRW", "AUD", "USD", "EUR", };
    String[] dataTo = {"RUB", "UAH", "KZT", "BYN", "GBP", "CNY", "JPY", "KRW", "AUD", "USD", "EUR", };

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
                break;
            case "RUB":
                currentTextView = MainActivity.RUBText;
                break;
            case "ETH":
                currentTextView = MainActivity.ETHText;
                break;
            default:
                currentTextView = MainActivity.custText;
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


            @SuppressLint("StaticFieldLeak")
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
                    if (currency instanceof XECurrency && !(currency instanceof CustomCurrency)) {
                        futures.add(executorService.submit((XECurrency) currency));
                    } else if (currency instanceof XBT) {
                        futures.add(executorService.submit((XBT) currency));
                    } else if (currency instanceof ETH) {
                        futures.add(executorService.submit((ETH) currency));
                    } else if (currency instanceof OIL) {
                        futures.add(executorService.submit((OIL) currency));
                    } else if (currency instanceof CustomCurrency) {
                        futures.add(executorService.submit((CustomCurrency) currency));
                    }

                }

                localPreferencesInit(context);

                int failCounter = localPreferences.getInt("fail", 0);

//                System.out.println(String.format("curr = %d  futur =%d", currencyList.size(), futures.size()));

                boolean failure = false;

                for (int i = 0; i < futures.size(); i++) {


                    String name = currencyList.get(i).getName();
//                    System.out.println(name);

                    try {
                        setCurrentTextView(name);
                    } catch (Exception e) {

                    }
                    double lowBorder;
                    double hiBorder;

                    if (name.contains("-")) {
                        lowBorder = localPreferences.getFloat("custmin", 0);
                        hiBorder = localPreferences.getFloat("custmax", Integer.MAX_VALUE);
                    } else {
                        lowBorder = localPreferences.getFloat(name + "min", 0);
                        hiBorder = localPreferences.getFloat(name + "max", Integer.MAX_VALUE);
                    }
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
                                } catch (InterruptedException | ExecutionException ignored) {

                                }

                                return null;
                            }


                            @Override
                            protected void onProgressUpdate(Void... values) {
                                super.onProgressUpdate(values);
                                try {
                                    MainActivity.progressBar.setVisibility(ProgressBar.VISIBLE);
                                } catch (Exception ignored) {

                                }

                            }


                            @SuppressLint("DefaultLocale")
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                setaDoubleD(aDouble);

                                try {

                                    if (aDouble == -1.0 || aDouble == 0.0) {
                                        currentTextView.setText("неудача");

                                    } else {

//                                        if (aDouble < 1) {
                                        if (false) {
                                            currentTextView.setText(String.format("%(.4f", 1.0 / aDouble));

                                        } else {

                                            currentTextView.setText(String.valueOf(aDouble));
                                        }

                                    }
                                    MainActivity.progressBar.setVisibility(ProgressBar.INVISIBLE);
                                } catch (Exception ignored) {

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

                    changeTextColor(name);


                    if (aDoubleD == -1.0 || aDoubleD == 0.0) {
                        if (!failure) {
                            failCounter++;
                            localPreferences.edit().putInt("fail", failCounter).apply();
                            failure = true;
                        }
                        continue;

                    } else {
                        localPreferences.edit().putFloat(name + "value", (float) aDoubleD).apply();
                    }

                    if (aDoubleD < lowBorder || aDoubleD > hiBorder) {
                        notificationNeeded = true;
                        if (false) {
                            contentText.append(name.toUpperCase()).append(" = ").append(String.format("%(.4f", 1.0 / aDoubleD)).append("\r\n");

                        } else {

                            contentText.append(name.toUpperCase()).append(" = ").append(aDoubleD).append("\r\n");
                        }
                        contentTitle = "Валюта вышла за границы!";

                    }


                    if (i == futures.size() - 1 && !failure) {
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

    private void changeTextColor(String name) {
        if (aDoubleD == -1.0 || aDoubleD == 0.0) {
            return;
        }
        try {
            float aFloat = localPreferences.getFloat(name + "value", (float) aDoubleD);
            float aDouble = (float) aDoubleD;

            if (aDouble < 1) {
                aDouble = 1 / aDouble;
            }
            if (aFloat < 1) {
                aFloat = 1 / aFloat;
            }

//            if (new BigDecimal(aDouble).setScale(1, RoundingMode.UP).equals(new BigDecimal(aFloat).setScale(1, RoundingMode.UP))) {
            if (String.valueOf(aDouble).equals(String.valueOf(aFloat))) {
                currentTextView.setTextColor(Color.BLACK);

            } else if (aDouble > aFloat) {
                currentTextView.setTextColor(Color.parseColor("#00897B"));
            } else if (aDouble < aFloat) {
                currentTextView.setTextColor(Color.parseColor("#FF5722"));
            }

        } catch (Exception e) {

        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(Context context, CharSequence contentText, CharSequence contentTitle) {
        Intent notficationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notficationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Resources resources = context.getResources();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            CharSequence name = "signal $O$";
            String description = "testChannell for 26+";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
//                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);


            builder = new Notification.Builder(context, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(context);

        }

//        int icon = R.mipmap.ic_launcher;
        builder.setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
//                .setLights(Color.GREEN, 1000, 1000)
//                .setColor(Color.GREEN)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(contentText.toString())
                .setContentTitle(contentTitle);
        if (contentText.length() > 15) {
            builder.setStyle(new Notification.BigTextStyle().bigText(contentText.toString()));
        }


        Notification notification = builder.build();


//        Log.d("soundGetter  ", String.valueOf(soundGetter));
//        Log.d("vibroGetter  ", String.valueOf(vibroGetter));
//        Log.d("diodGetter  ", String.valueOf(diodGetter));
//        for (Map.Entry<String, ?> entry :
//                localPreferences.getAll().entrySet()) {
//            System.out.println(entry.toString());
//        }

//        System.out.println(localPreferences.toString());
        localPreferencesInit(context);
        int soundGetter = localPreferences.getInt("sound", 2);
        int vibroGetter = localPreferences.getInt("vibro", 2);
        int diodGetter = localPreferences.getInt("diod", 1);


        switch (soundGetter) {
            case 2:
                notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/raw/sound_2");
                break;
            case 3:
                notification.defaults = Notification.DEFAULT_SOUND;
                break;
        }

        switch (vibroGetter) {
            case 2:
                notification.vibrate = new long[]{0, 150, 0};
                break;
            case 3:
                notification.defaults = Notification.DEFAULT_VIBRATE;
                break;
        }

        switch (diodGetter) {
            case 2:
                notification.ledARGB = Color.GREEN;
                notification.ledOffMS = 5000;
                notification.ledOnMS = 500;
                notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS;
        }


//        notification.defaults =
//        notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/raw/sound_2");


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
        String to;
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

            XBT xbt = new XBT("XBT");
            if (localPreferences.getBoolean("XBT_is_active", false)) {

                currencyList.add(xbt);
            }

            OIL oil = new OIL("OIL");
            if (localPreferences.getBoolean("OIL_is_active", false)) {

                currencyList.add(oil);
            }

            RUB rub = new RUB("RUB", to);
            if (localPreferences.getBoolean("RUB_is_active", false)) {

                currencyList.add(rub);
            }

            ETH eth = new ETH("ETH");

            if (localPreferences.getBoolean("ETH_is_active", false)) {

                currencyList.add(eth);
            }

            CustomCurrency cust = new CustomCurrency(dataFrom[localPreferences.getInt("custom_from", 0)], dataTo[localPreferences.getInt("custom_to", 0)]);
            if (localPreferences.getBoolean("cust_is_active", false)) {

                currencyList.add(cust);
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
