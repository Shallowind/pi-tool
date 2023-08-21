package com.zmmxkid.pitool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class personscore extends AppCompatActivity {
    private EditText qqEditText;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String PREF_QQ = "qq";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personscore);
        qqEditText = findViewById(R.id.editTextText4);
        loadSavedQQ();
    }

    public void drawscore(View view) {
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();

        // Construct the URL with the provided qq
        String url = "http://47.241.0.156:34567/WebAPI/public/Bests/" + qq + ".txt";
        saveQQ(qq);

        // Execute AsyncTask to perform network operation
        new DownloadDataTask().execute(url);
    }

    public void b30(View view) {
        Intent intent = new Intent(this,b30.class);
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();
        saveQQ(qq);
        intent.putExtra("qq", qq);
        startActivity(intent);
    }

    public void overall(View view) {
        Intent intent = new Intent(this,Overall.class);
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();
        saveQQ(qq);
        intent.putExtra("qq", qq);
        startActivity(intent);
    }

    public void qingchu(View view) {
        EditText qqEditText = findViewById(R.id.editTextText4);
        qqEditText.setText("");
    }

    private class DownloadDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        // Inside the onPostExecute method of DownloadDataTask
        @Override
        protected void onPostExecute(String response) {
            if (response.isEmpty()) {
                // Show a popup indicating the server doesn't have the user's data
                Toast.makeText(personscore.this, "该QQ不存在，请到相应QQ群输入相关指令", Toast.LENGTH_LONG).show();
            } else {
                // Save the downloaded file to the app's private directory
                EditText qqEditText = findViewById(R.id.editTextText4);
                String qq = qqEditText.getText().toString();
                String filename = qq + ".txt";
                File file = new File(getFilesDir(), filename);
                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(response.getBytes());
                    outputStream.close();

                    // Start the scoreview activity
                    Toast.makeText(personscore.this, "服务器请求成功", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(personscore.this, personview.class);
                    intent.putExtra("qq", qq);
                    intent.putExtra("filename", filename); // Pass the filename to the next activity
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    private void saveQQ(String qq) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_QQ, qq);
        editor.apply();
    }

    private void loadSavedQQ() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedQQ = settings.getString(PREF_QQ, "");
        qqEditText.setText(savedQQ);
    }
}
