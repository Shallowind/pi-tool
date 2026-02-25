package com.zmmxkid.pitool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class thecharts extends AppCompatActivity {
    static final String PREFS_NAME = "MyPrefs";
    static final String PREF_QQ = "qq";
    private EditText qqEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thecharts);
        ConstraintLayout backLayout = findViewById(R.id.backlayout); // 假设你有一个 LinearLayout 用作背景
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
        qqEditText = findViewById(R.id.editTextText4);
        loadSavedQQ();
    }

    public void drawscore(View view) {
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();

        // Construct the URL with the provided qq
        String url = "http://47.93.57.125:521/WebAPI/public/Bests/" + qq + ".txt";
        saveQQ(qq);

        // Execute AsyncTask to perform network operation
        new DownloadDataTask().execute(url);
    }

    public void hot(View view) {
        Intent intent = new Intent(this, charts.class);
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();
        saveQQ(qq);
        intent.putExtra("url", "http://47.93.57.125:521/WebAPI/public/scenarioPopularity-PITool.html?userId="+qq+"&bg="+randomNumber);
        startActivity(intent);
    }

    public void seehistory(View view) {
        Intent intent = new Intent(this, charts.class);
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();
        saveQQ(qq);
        intent.putExtra("url", "http://47.93.57.125:521/WebAPI/public/chartQuery-PITool.html?userId="+qq+"&bg="+randomNumber);
        startActivity(intent);
    }

    public void dating(View view) {
        Intent intent = new Intent(this, charts.class);
        intent.putExtra("url", "http://47.93.57.125:521/WebAPI/public/dashboard.html?bg="+ randomNumber);
        startActivity(intent);
    }

    public void chifen(View view) {
        Intent intent = new Intent(this, charts.class);
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();
        saveQQ(qq);
        intent.putExtra("url", "http://47.93.57.125:521/WebAPI/public/scenarioContribution-PITool.html"+"?bg="+randomNumber);
        startActivity(intent);
    }
    public void cjkptt(View view) {
        Intent intent = new Intent(this,cjkptt.class);
        startActivity(intent);
    }
    public void fazhan(View view) {
        Intent intent = new Intent(this, charts.class);
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();
        saveQQ(qq);
        intent.putExtra("url", "http://47.93.57.125:521/WebAPI/public/globalHistory-PITool.html?userId="+qq+"&bg="+randomNumber);
        startActivity(intent);
    }
    public void huoyue(View view) {
        Intent intent = new Intent(this, charts.class);
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();
        saveQQ(qq);
        intent.putExtra("url", "http://47.93.57.125:521/WebAPI/public/activePlayerQuery-PITool.html?userId="+qq+"&bg="+randomNumber);
        startActivity(intent);
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
                Toast.makeText(thecharts.this, "该QQ不存在，请到相应QQ群输入相关指令", Toast.LENGTH_LONG).show();
            } else {
                // Save the downloaded file to the app's private directory
                EditText qqEditText = findViewById(R.id.editTextText4);
                String qq = qqEditText.getText().toString();
                String filename = qq + ".txt";
                File file = new File(getExternalFilesDir(null), filename);

                try {
                    // 写入其他响应内容（假设response是你要写入的内容）
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(response.getBytes());
                    outputStream.close();

                    // 创建QQ.txt文件并写入QQ号
                    File qqFile = new File(getExternalFilesDir(null), "QQ.txt");
                    FileOutputStream qqOutputStream = new FileOutputStream(qqFile);
                    qqOutputStream.write(qq.getBytes()); // 将QQ号写入文件
                    qqOutputStream.close(); // 关闭流

                    // 显示账号绑定成功的Toast提示
                    Toast.makeText(thecharts.this, "账号绑定成功", Toast.LENGTH_LONG).show();

                    // 启动personview Activity并传递必要的数据
                    Intent intent = new Intent(thecharts.this, personview.class);
                    intent.putExtra("qq", qq);
                    intent.putExtra("filename", filename); // 将filename传递到下一个活动
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
