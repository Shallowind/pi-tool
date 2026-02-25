package com.zmmxkid.pitool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class totalrank extends AppCompatActivity {

    private TextView headText, headText2;
    private TextView[] ppaiming = new TextView[20];
    private TextView[] sscore = new TextView[20];
    private TextView[] pptt = new TextView[20];
    private TextView[] nname = new TextView[20];
    private DiagonalCutImageView[] iimg = new DiagonalCutImageView[20];
    private DiagonalCutImageView headImg;
    private String num,num1,qq;
    private int currentPage = 0;
    private LoadDataTask currentTask3;
    private boolean buttondef=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainperson);
        ConstraintLayout backLayout = findViewById(R.id.background); // 假设你有一个 LinearLayout 用作背景
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
        // 获取屏幕尺寸
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        int screenWidth = size.x;
        int screenHeight = size.y;
        int width = 400;
        int height = 750;

        double targetAspectRatio = (double) width / height;
        double screenAspectRatio = (double) screenWidth / screenHeight;

        int targetWidth;
        int targetHeight;

// 根据屏幕比例来计算新的宽高
        if (screenAspectRatio < targetAspectRatio) {
            // 屏幕更宽，以屏幕宽度为基准计算高度
            targetWidth = screenWidth;
            targetHeight = (int) (targetWidth / targetAspectRatio);
        } else {
            // 屏幕更高，以屏幕高度为基准计算宽度
            targetHeight = screenHeight;
            targetWidth = (int) (targetHeight * targetAspectRatio);
        }

        LinearLayout linearLayout = findViewById(R.id.screenlay);
// 设置 LinearLayout 的布局参数
        ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
        layoutParams.width = targetWidth;
        layoutParams.height = targetHeight;
        linearLayout.setLayoutParams(layoutParams);
        if (currentTask3 != null) {
            currentTask3.cancel(true);
        }

        headText = findViewById(R.id.headtext);
        headText2 = findViewById(R.id.headtext2);
        headImg = findViewById(R.id.headimg);
        //隐藏button30
        findViewById(R.id.button33).setVisibility(View.GONE);

        for (int i = 0; i < 20; i++) {
            ppaiming[i] = findViewById(getResources().getIdentifier("ppaiming" + (i + 1), "id", getPackageName()));
            sscore[i] = findViewById(getResources().getIdentifier("sscore" + (i + 1), "id", getPackageName()));
            pptt[i] = findViewById(getResources().getIdentifier("pptt" + (i + 1), "id", getPackageName()));
            nname[i] = findViewById(getResources().getIdentifier("nname" + (i + 1), "id", getPackageName()));
            iimg[i] = findViewById(getResources().getIdentifier("iimg" + (i + 1), "id", getPackageName()));
        }
        for (int j = 0; j < 20; j++) {
            iimg[j].setImageResource(R.drawable.rating_off);
            int finalJ = j;
            iimg[j].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(totalrank.this, b30.class);
                    TextView qqt = findViewById(getResources().getIdentifier("pptt" + (finalJ + 1), "id", getPackageName()));
                    String qq = qqt.getText().toString();
                    // 去除 "qq: " 前缀
                    if (qq.startsWith("qq: ")) {
                        qq = qq.substring(4);
                    }
                    intent.putExtra("qq", qq);
                    startActivity(intent);
                }
            });
        }
        num = getIntent().getStringExtra("key");
        num1 = getIntent().getStringExtra("key1");
        qq = "123456789"; // Replace with the actual QQ value

        updateUI();

        findViewById(R.id.up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 0) {
                    currentPage--;
                    for (int j = 0; j < 20; j++) {
                        iimg[j].setImageResource(R.drawable.rating_off);
                    }
                    buttondef=true;
                    updateUI();
                }
            }
        });

        findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttondef) {
                    currentPage++;
                    for (int j = 0; j < 20; j++) {
                        iimg[j].setImageResource(R.drawable.rating_off);
                    }
                    updateUI();
                }
                else{
                    Toast.makeText(totalrank.this, "最后一页了", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateUI() {
        if (currentTask3 != null) {
            if (currentTask3.getStatus() != AsyncTask.Status.FINISHED) {
                currentTask3.cancel(true);
            }
        }
        currentTask3 = new LoadDataTask(getApplicationContext());
        currentTask3.execute();
    }
    public void chakanpl(View view) {
        Intent intent = new Intent(totalrank.this, pinglun.class);
        intent.putExtra("xuhao", headText.getText().toString() + "-" + headText2.getText().toString());
        intent.putExtra("bg", String.valueOf(randomNumber));
        startActivity(intent);
    }

    public class LoadDataTask extends AsyncTask<Void, Integer, Void> {

        private String headTextData, headText2Data;
        private String[] ppaimingData = new String[20];
        private String[] sscoreData = new String[20];
        private String[] ppttData = new String[20];
        private String[] nnameData = new String[20];
        private int validDataCount = 0;
        private boolean isLastPage = false;

        public LoadDataTask(Context context) {

        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                // 使用新的 JSON API 数据源
                URL url = new URL("http://47.93.57.125:4321/vanillaLeaderboardQuery");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoInput(true);

                reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                connection.disconnect();

                // 解析 JSON 数组
                JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                // 计算当前页的起始索引
                int startIndex = currentPage * 20;

                // 如果起始索引已经超过数组长度，说明没有更多数据了
                if (startIndex >= jsonArray.length()) {
                    isLastPage = true;
                    validDataCount = 0;
                    publishProgress(-2); // 特殊标记：无更多数据
                    return null;
                }

                // 计算结束索引（最多20条）
                int endIndex = Math.min(startIndex + 20, jsonArray.length());
                validDataCount = 0;

                // 提取数据
                for (int i = startIndex; i < endIndex; i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    // 映射字段：Position(排名), Id(QQ), Username(用户名), RankScore(分数)
                    ppaimingData[validDataCount] = obj.getString("Position");
                    ppttData[validDataCount] = obj.getString("Id");
                    nnameData[validDataCount] = obj.getString("Username");

                    // RankScore 可能是数字类型，转换为字符串
                    Object scoreObj = obj.get("RankScore");
                    sscoreData[validDataCount] = scoreObj.toString();

                    validDataCount++;
                }

                // 清空剩余槽位，防止数据残留
                for (int i = validDataCount; i < 20; i++) {
                    ppaimingData[i] = null;
                }

                // 检查是否是最后一页
                isLastPage = (endIndex >= jsonArray.length());

                headTextData = "主游戏";
                headText2Data = "总分排行榜";

                publishProgress(-1);

            } catch (Exception e) {
                e.printStackTrace();
                publishProgress(-3); // 错误标记
            } finally {
                if (reader != null) {
                    try { reader.close(); } catch (Exception ignored) {}
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int code = values[0];

            // 处理无更多数据的情况
            if (code == -2) {
                buttondef = false;
                Toast.makeText(totalrank.this, "最后一页了", Toast.LENGTH_SHORT).show();
                currentPage--; // 回退页码
                return;
            }

            // 处理错误
            if (code == -3) {
                Toast.makeText(totalrank.this, "数据加载失败", Toast.LENGTH_SHORT).show();
                return;
            }

            if (code == -1) {
                headText.setText(headTextData);
                headText2.setText(headText2Data);

                // 加载头图 - 使用指定 URL
                Glide.with(totalrank.this)
                        .load("http://47.93.57.125:521/WebAPI/icons/evolved.png")
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.rating_gold_)
                        .error(R.drawable.rating_gold_)
                        .into(headImg);

                for (int i = 0; i < 20; i++) {
                    if (ppaimingData[i] == null) {
                        // 清空该位置的数据（用于最后一页不足20条时）
                        ppaiming[i].setText("");
                        sscore[i].setText("");
                        pptt[i].setText("");
                        nname[i].setText("");
                        iimg[i].setImageResource(R.drawable.rating_off);
                        continue;
                    }

                    // 设置排名（添加 # 号）
                    ppaiming[i].setText("#" + ppaimingData[i]);
                    // 设置分数
                    sscore[i].setText(sscoreData[i]);
                    // 设置 QQ（添加 "qq: " 前缀）
                    pptt[i].setText("qq: " + ppttData[i]);
                    // 设置用户名
                    nname[i].setText(nnameData[i]);

                    // 构建头像 URL 并加载
                    String qq = ppttData[i];
                    String avatarUrl = "https://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=640";

                    Glide.with(totalrank.this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.rating_off)
                            .error(R.drawable.rating_off)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(iimg[i]);
                }

                // 更新按钮状态：如果是最后一页，禁用下一页按钮
                buttondef = !isLastPage;
            }
        }
    }
}