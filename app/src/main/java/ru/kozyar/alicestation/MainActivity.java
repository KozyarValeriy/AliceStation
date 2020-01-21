package ru.kozyar.alicestation;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    public EditText url;
    public EditText login;
    public EditText password;

    public String updateUrl = "";
    public String cookieFile;
    public String deviceAndTokenFile;

    public String token = "";
    public String deviceId = "";
    public boolean isConnect = false;
    public LoginToYandex newLogin;

    public String cookie;
    public MyCookieStore cookieStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        cookieFile = getFilesDir().toString() + "/cookies.json";
        deviceAndTokenFile = getFilesDir().toString() + "/deviceAndToken.json";

        cookie = Filer.getFile(cookieFile);

        cookieStore = new MyCookieStore(cookieFile);
        CookieManager cookieManager = new CookieManager(cookieStore, new MyCookiePolicy());
        CookieHandler.setDefault(cookieManager);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_tools)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        try {
            if (Intent.ACTION_SEND.equals(action) && type != null) {
                if ("text/plain".equals(type)) {
                    handleSendText(intent);
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        url = findViewById(R.id.editText1);
        if (!updateUrl.equals("")) {
            url.setText(updateUrl);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    void handleSendText(Intent intent) {
        url = findViewById(R.id.editText1);
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null && sharedText.startsWith("http")) {
            updateUrl = sharedText;
        }
    }

    public void onButtonClickSend(View v){
        url = findViewById(R.id.editText1);
        if (token.equals("") || deviceId.equals("")) {
            String deviceAndToken = Filer.getFile(deviceAndTokenFile);
            if (deviceAndToken.equals("")) {
                deviceAndToken = "{}";
            }
            JSONObject deviceAndTokenJson;

            try {
                deviceAndTokenJson = new JSONObject(deviceAndToken);
                token = deviceAndTokenJson.getString("token");
                deviceId = deviceAndTokenJson.getString("deviceid");
                isConnect = deviceAndTokenJson.getBoolean("isconnect");

                if (isConnect) {
                    String newUrl = clearUrl(url.getText().toString());
                    url.setText(newUrl);
                    SendVideo newVideo = new SendVideo(newUrl, deviceId, token, this);
                    Thread thread = new Thread(newVideo);
                    thread.start();
                } else {
                    throw new Exception("Error sending video");
                }

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "JSONerr " + e.toString(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "MAINerr " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public String clearUrl(String url) {
        String newUrl;
        // removing arguments in url
        if (url.contains("www.youtube")) {
            newUrl = url.split("&")[0];
        } else {
            newUrl = url;
        }
        // convert in normal url
        if (url.contains("https://youtu.be")) {
            String[] allParam = url.split("/");
            newUrl = "https://www.youtube.com/watch?v=" + allParam[allParam.length - 1];
        }
        return newUrl;
    }

    public void onButtonLoginClick(View v){
        login = findViewById(R.id.editTextLogin);
        password = findViewById(R.id.editTextPassword);

        ProgressBar loading = findViewById(R.id.progressBar1);
        try {
            this.newLogin = new LoginToYandex(login.getText().toString(),
                    password.getText().toString(), this, loading, deviceAndTokenFile);
            Thread thread = new Thread(this.newLogin);
            thread.start();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
    }

}
