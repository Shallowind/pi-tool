package com.zmmxkid.pitool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    public static double version=1.63;
    public static scnperson.LoadDataTask currentTask;
    public static mainperson.LoadDataTask currentTask2;
    public static b30.DownloadImageTaskForImageView currentTask1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new CheckVersionTask().execute();
    }

    public void gonglue(View view) {
        Intent intent = new Intent(this,gonglue.class);
        startActivity(intent);
    }


    private class CheckVersionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL("http://101.42.49.54:8888/down/EpkgtBS8VpPY");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                StringBuilder content = new StringBuilder();
                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    content.append(new String(buffer, 0, bytesRead));
                }
                inputStream.close();

                double txtVersion = Double.parseDouble(content.toString());
                return txtVersion != version;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean shouldUpdate) {
            if (shouldUpdate) {
                Toast.makeText(MainActivity.this, "有可用更新", Toast.LENGTH_LONG).show();
                // Open the default browser with the update URL
                Uri updateUri = Uri.parse("http://101.42.49.54:8888/down/NxKd3k8EI7pS");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, updateUri);
                startActivity(browserIntent);
            }
        }
    }
    public void chaxun(View view) {
        Intent intent = new Intent(this,caculate.class);
        startActivity(intent);
    }

    public void jisuanqi(View view) {
        //Toast.makeText(this, "功能在开发中", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,worldscore.class);
        startActivity(intent);
    }


    public void personscore(View view) {
        Intent intent = new Intent(this,personscore.class);
        startActivity(intent);
    }

    public void scn(View view) {
        Intent intent = new Intent(this,scn.class);
        startActivity(intent);
    }

    public void author(View view) {
        Intent intent = new Intent(this, authors.class);
        startActivity(intent);
    }
}