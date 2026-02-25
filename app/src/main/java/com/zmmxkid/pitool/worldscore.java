package com.zmmxkid.pitool;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zmmxkid.pitool.MainActivity.randomNumber;
import static com.zmmxkid.pitool.personscore.PREFS_NAME;
import static com.zmmxkid.pitool.personscore.PREF_QQ;

public class worldscore extends AppCompatActivity {
    private EditText qqEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worldscore);
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
        Spinner spinnerzhu = findViewById(R.id.spinnerzhu);
        Spinner spinnerju = findViewById(R.id.spinnerju);
        Spinner spinnerji = findViewById(R.id.spinnerji);
        Spinner spinnerjie = findViewById(R.id.spinnerjie);

        // 定义选项列表数据
        String[] optionszhu = {"细菌", "病毒", "真菌", "寄生虫", "朊病毒", "纳米病毒", "生化武器", "Neurax蠕虫", "Necroa病毒"};
        String[] optionsju = {"虚假消息", "科学否定", "狂牛怪疫", "地旷人稀", "航空时代", "圣诞老人的小助手", "遥传", "人造器官", "冰冻病毒", "来历不明", "立百病毒", "不闻不问", "人人平等", "债务危机", "全球暖化", "关闭一切", "冰河时代", "天花", "恐外症", "海盗瘟疫", "火山喷发", "猪流感", "颠倒乾坤", "黄金时代", "黑死病"};
        String[] optionsji = {"细菌", "病毒", "真菌", "寄生虫", "朊病毒", "纳米病毒", "生化武器", "Neurax蠕虫", "Necroa病毒", "猩猩流感", "暗影瘟疫"};
        String[] optionsjie = {"细菌", "病毒", "真菌", "寄生虫", "朊病毒", "纳米病毒", "生化武器","冰冻病毒"};
        // 创建适配器
        ArrayAdapter<String> adapterzhu = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionszhu);
        adapterzhu.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapterju = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionsju);
        adapterju.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapterji = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionsji);
        adapterji.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapterjie = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionsjie);
        adapterjie.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 设置适配器到 Spinner
        spinnerzhu.setAdapter(adapterzhu);
        spinnerju.setAdapter(adapterju);
        spinnerji.setAdapter(adapterji);
        spinnerjie.setAdapter(adapterjie);
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

    public void overall(View view) {
        Intent intent = new Intent(this,caculate.class);
        startActivity(intent);
    }
    public void totalrank(View view) {
        Intent intent = new Intent(this,totalrank.class);
        startActivity(intent);
    }
    public void personal(View view) {
        Intent intent = new Intent(this, PlayerDetailActivity.class);
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();
        saveQQ(qq);
        intent.putExtra("qq", qq);
        startActivity(intent);
    }
    public void calculator(View view) {
        Intent intent = new Intent(this,caculate.class);
        startActivity(intent);
    }
    public void guide(View view) {
        Intent intent = new Intent(this, charts.class);
        EditText qqEditText = findViewById(R.id.editTextText4);
        String qq = qqEditText.getText().toString();
        saveQQ(qq);
        intent.putExtra("url", "http://47.93.57.125:521/WebAPI/public/pdf/viewer.html");
        startActivity(intent);
    }

    public void zhuyouxi(View view) {
        Spinner spinnerzhu = findViewById(R.id.spinnerzhu);
        //Toast.makeText(this, "功能在开发中", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(worldscore.this, mainperson.class);
        intent.putExtra("key",spinnerzhu.getSelectedItem().toString());
        intent.putExtra("key1","主游戏");
        startActivity(intent);
    }

    public void juqing(View view) {
        Spinner spinnerju = findViewById(R.id.spinnerju);
        //Toast.makeText(this, "功能在开发中", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(worldscore.this, mainperson.class);
        String str=spinnerju.getSelectedItem().toString();
        if(str.equals("虚假消息"))str="虚假信息";
        if(str.equals("地旷人稀"))str="地广人稀";
        if(str.equals("遥传"))str="谣传";
        if(str.equals("立百病毒"))str="尼帕病毒";
        if(str.equals("全球暖化"))str="全球变暖";
        if(str.equals("火山喷发"))str="火山爆发";
        if(str.equals("颠倒乾坤"))str="乾坤颠倒";
        if(str.equals("圣诞老人的小助手"))str="圣诞蠕虫";

        intent.putExtra("key",str);
        intent.putExtra("key1","剧情");
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
    public void jisu(View view) {
        Spinner spinnerji = findViewById(R.id.spinnerji);
        //Toast.makeText(this, "功能在开发中", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(worldscore.this, mainperson.class);
        intent.putExtra("key",spinnerji.getSelectedItem().toString());
        intent.putExtra("key1","急速");
        startActivity(intent);
    }

    public void jieyao(View view) {
        Spinner spinnerjie = findViewById(R.id.spinnerjie);
        //Toast.makeText(this, "功能在开发中", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(worldscore.this, mainperson.class);
        intent.putExtra("key",spinnerjie.getSelectedItem().toString());
        intent.putExtra("key1","解药");
        startActivity(intent);
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
                Toast.makeText(worldscore.this, "该QQ不存在，请到相应QQ群输入相关指令", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(worldscore.this, "账号绑定成功", Toast.LENGTH_LONG).show();

                    // 启动personview Activity并传递必要的数据
                    Intent intent = new Intent(worldscore.this, personview.class);
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