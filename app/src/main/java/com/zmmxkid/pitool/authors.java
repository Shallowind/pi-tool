package com.zmmxkid.pitool;

import static com.zmmxkid.pitool.MainActivity.version;

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

public class authors extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authors);
    }
    public void gengxin(View view) {
        new authors.CheckVersionTask().execute();
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
                Toast.makeText(authors.this, "有可用更新", Toast.LENGTH_LONG).show();
                // Open the default browser with the update URL
                Uri updateUri = Uri.parse("http://101.42.49.54:8888/down/NxKd3k8EI7pS");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, updateUri);
                startActivity(browserIntent);
            }
            else{
                Toast.makeText(authors.this, "恭喜，是最新版本！", Toast.LENGTH_LONG).show();
            }
        }
    }
}