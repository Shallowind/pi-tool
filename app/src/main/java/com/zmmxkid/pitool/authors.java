package com.zmmxkid.pitool;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zmmxkid.pitool.MainActivity.randomNumber;
import static com.zmmxkid.pitool.MainActivity.version;

public class authors extends AppCompatActivity {
    private TextView textView18;
    private TextView textView33;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authors);
        ConstraintLayout backLayout = findViewById(R.id.linearLayout);

        // 根据随机数选择相应的图片
        switch (randomNumber) {
            case 1:
                backLayout.setBackgroundResource(R.drawable.img11);
                break;
            case 2:
                backLayout.setBackgroundResource(R.drawable.img22);
                break;
            case 3:
                backLayout.setBackgroundResource(R.drawable.img33);
                break;
            case 4:
                backLayout.setBackgroundResource(R.drawable.img44);
                break;
            case 5:
                backLayout.setBackgroundResource(R.drawable.img55);
                break;
            default:
                // 默认处理
                backLayout.setBackgroundResource(R.drawable.img11); // 如果发生异常，设置 img1
                break;
        }
        // 获取 TextView 控件
        textView18 = findViewById(R.id.textView18);
        textView33 = findViewById(R.id.textView33);

        // 创建 Handler 以便从后台线程更新 UI
        handler = new Handler(Looper.getMainLooper());

        // 自动下载并读取内容
        new Thread(new DownloadTextRunnable()).start();

    }

    public void gengxin(View view) {
        new authors.CheckVersionTask().execute();
    }

    // 下载文本并在主线程更新UI
    private class DownloadTextRunnable implements Runnable {
        @Override
        public void run() {
            try {
                URL url = new URL("http://47.93.57.125:8888/down/MeZ3YXvhKWfN");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String firstLine = reader.readLine();  // 第一行
                StringBuilder otherLines = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    otherLines.append(line).append("\n");
                }

                inputStream.close();

                // 使用 Handler 在主线程更新 UI
                final String finalFirstLine = firstLine;
                final String finalOtherLines = otherLines.toString();
                handler.post(() -> {
                    // 更新 UI
                    textView18.setText(finalFirstLine);
                    textView33.setText(finalOtherLines);
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(authors.this, "下载失败", Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    private class CheckVersionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL("http://47.93.57.125:8888/down/10oLCfjinRJR");
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
                Uri updateUri = Uri.parse("http://47.93.57.125:8888/down/MOuOLRaJI0rE");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, updateUri);
                startActivity(browserIntent);
            } else {
                Toast.makeText(authors.this, "恭喜，是最新版本！", Toast.LENGTH_LONG).show();
            }
        }
    }
}