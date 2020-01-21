package ru.kozyar.alicestation;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;


public class SendVideo implements Runnable {

    private static final String STATION_URL = "https://yandex.ru/video/station";

    private String videoUrl;
    private String deviceId;
    private String token;
    private Activity activity;

    SendVideo(String videoUrl, String deviceId, String token, Activity activity) {
        this.videoUrl = videoUrl;
        this.deviceId = deviceId;
        this.token = token;
        this.activity = activity;
    }

    public void message(String text) {
        final String textMessage = text;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, textMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void run() {
        try {

            // Fourth connection to send video to station
            HttpsURLConnection connectToSendVideo = (HttpsURLConnection) new URL(STATION_URL).openConnection();
            connectToSendVideo.setRequestMethod("POST");
            connectToSendVideo.setConnectTimeout(5000);
            connectToSendVideo.setReadTimeout(5000);
            connectToSendVideo.setRequestProperty("x-csrf-token", token);
            // get sending data
            connectToSendVideo.setDoOutput(true);
            JSONObject sendingData = JsonParser.getJsonAnswer(deviceId, videoUrl);
            // write sending data
            OutputStreamWriter wr = new OutputStreamWriter(connectToSendVideo.getOutputStream());
            wr.write(sendingData.toString());
            wr.flush();

            connectToSendVideo.connect();

            int answerResult = connectToSendVideo.getResponseCode();
            if (answerResult == 200) {
                message(String.format(Locale.ENGLISH,"Fourth step: %d", answerResult));
            } else {
                message(String.format(Locale.ENGLISH,"Fourth step (ERROR): %d", answerResult));
                throw new IOException("Error fourth connection");
            }
        } catch (IOException e) {
            message("IOerr - " + e.toString());
        } catch (JSONException e) {
            message("JSONerr - " + e.toString());
        } catch (Exception e) {
            message(e.toString());
        }

    }
}
