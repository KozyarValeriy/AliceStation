package ru.kozyar.alicestation;

import android.app.Activity;
import android.widget.ProgressBar;
import android.widget.Toast;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;


public class LoginToYandex implements Runnable {

    private static final String DEVICE_URL = "https://quasar.yandex.ru/devices_online_stats";
    private static final String TOKEN_URL = "https://frontend.vh.yandex.ru/csrf_token";
    private static final String YANDEX_URL = "https://passport.yandex.ru/passport?mode=auth";

    private String login;
    private String password;
    private ProgressBar loading;
    private Activity activity;
    private String fileName;

    private String token;
    private String deviceId;
    private boolean isConnect;

    public String getToken() {
        return token;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public boolean isConnect() {
        return isConnect;
    }

    LoginToYandex(String login, String password, Activity activity, ProgressBar loading, String fileName) {
        this.login = login;
        this.password = password;
        this.loading = loading;
        this.activity = activity;
        this.fileName = fileName;
    }

    private void message(String text) {
        final String textMessage = text;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, textMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void load(boolean visible) {
        final boolean loadVisible = visible;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (loadVisible)
                    loading.setVisibility(ProgressBar.VISIBLE);
                else
                    loading.setVisibility(ProgressBar.INVISIBLE);
            }
        });
    }

    @Override
    public void run() {
        final int TIMEOUT_VALUE = 5000;

        try {
            load(true);
            this.isConnect = true;

            // First connections to start session
            HttpsURLConnection connectToAuthYandex = (HttpsURLConnection) new URL(YANDEX_URL).openConnection();
            connectToAuthYandex.setRequestMethod("POST");
            connectToAuthYandex.setDoOutput(true);

            connectToAuthYandex.setConnectTimeout(TIMEOUT_VALUE);
            connectToAuthYandex.setReadTimeout(TIMEOUT_VALUE);

            // Sending password and login
            String myData = "login=" + login + "&passwd=" + password;
            connectToAuthYandex.getOutputStream().write(myData.getBytes());
            connectToAuthYandex.connect();

            // answer code
            int authResult = connectToAuthYandex.getResponseCode();

            if (authResult != 200) {
                message(String.format(Locale.ENGLISH,"First step (ERROR): %d", authResult));

                throw new IOException("Error first connection");
            }

            // Second connections to get all devices
            HttpsURLConnection connetToGetDevices = (HttpsURLConnection) new URL(DEVICE_URL).openConnection();
            connetToGetDevices.setRequestMethod("GET");

            connetToGetDevices.setConnectTimeout(TIMEOUT_VALUE);
            connetToGetDevices.setReadTimeout(TIMEOUT_VALUE);

            connetToGetDevices.connect();
            // answer code
            int devicesResult = connetToGetDevices.getResponseCode();
            String deviceId;
            if (devicesResult == 200) {
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(connetToGetDevices.getInputStream()));
                String answer = rd.readLine();
                deviceId = JsonParser.getDeviceId(answer);

            } else {
                message(String.format(Locale.ENGLISH,"Second step (ERROR): %d", devicesResult));
                throw new IOException("Error second connection");
            }

            // Third connections to get x-csrf-token
            HttpsURLConnection connectToGetToken = (HttpsURLConnection) new URL(TOKEN_URL).openConnection();
            connectToGetToken.setRequestMethod("GET");

            connectToGetToken.setConnectTimeout(TIMEOUT_VALUE);
            connectToGetToken.setReadTimeout(TIMEOUT_VALUE);

            connectToGetToken.connect();
            // answer code
            int tokenResult = connectToGetToken.getResponseCode();
            String token;
            if (tokenResult == 200) {
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(connectToGetToken.getInputStream()));
                token = rd.readLine();
                message(String.format(Locale.ENGLISH,"OK: %d", tokenResult));
            } else {
                message(String.format(Locale.ENGLISH,"Third step (ERROR): %d", tokenResult));
                throw new IOException("Error third connection");
            }

            this.token = token;
            this.deviceId = deviceId;


            JSONObject deviceAndToken = JsonParser.getJsonTemp(deviceId, token, isConnect, login);
            Filer.setFile(fileName, deviceAndToken.toString());

        } catch (IOException e) {
            message("IOerr - " + e.toString());
        } catch (Exception e) {
            message(e.toString());
        }
        load(false);

    }
}
