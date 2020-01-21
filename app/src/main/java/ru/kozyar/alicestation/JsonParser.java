package ru.kozyar.alicestation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
    public static String getDeviceId(String response) throws JSONException {
        JSONObject answer = new JSONObject(response);
        JSONArray devices = answer.getJSONArray("items");

        answer = new JSONObject(devices.get(0).toString());
        return answer.getString("id");
    }

    public static JSONObject getJsonAnswer(String deviceId, String videoUrl) throws JSONException {
        JSONObject answer = new JSONObject();
        JSONObject insider = new JSONObject();
        insider.put("provider_item_id", videoUrl);
        if (videoUrl.startsWith("https://www.youtube")) {
            insider.put("player_id", "youtube");
        }
        answer.put("device", deviceId);
        answer.put("msg", insider);
        return answer;
    }

    public static JSONObject getJsonTemp(String deviceId, String token, boolean isConnect, String login) throws JSONException {
        JSONObject answer = new JSONObject();
        answer.put("deviceid", deviceId);
        answer.put("token", token);
        answer.put("isconnect", isConnect);
        answer.put("login", login);
        return answer;
    }
}
