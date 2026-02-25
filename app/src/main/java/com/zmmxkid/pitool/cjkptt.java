package com.zmmxkid.pitool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class cjkptt extends AppCompatActivity {

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
                    Intent intent = new Intent(cjkptt.this, b30.class);
                    TextView qqt = findViewById(getResources().getIdentifier("pptt" + (finalJ + 1), "id", getPackageName()));
                    String qq = qqt.getText().toString();
                    qq = qq.substring(4);
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
                    Toast.makeText(cjkptt.this, "最后一页了", Toast.LENGTH_LONG).show();
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
        Intent intent = new Intent(cjkptt.this, pinglun.class);
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
        public LoadDataTask(Context context) {

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                /* ===== 文本数据 ===== */
                URL textUrl = new URL(
                        "http://47.93.57.125:521/WebAPI/public/scenInfo/tops.txt"
                );
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(textUrl.openStream())
                );

                headTextData = "场景库";
                headText2Data = "PTT排行";

                for (int j = 0; j < currentPage * 20; j++) {
                    reader.readLine();
                }

                for (int i = 0; i < 20; i++) {
                    String line = reader.readLine();
                    if (line == null) break;

                    String[] data = line.split(",");
                    ppaimingData[i] = data[0];
                    sscoreData[i]   = data[3];
                    ppttData[i]    = data[1];
                    nnameData[i]   = data[2];
                }
                reader.close();

                publishProgress(-1);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == -1) {

                headText.setText(headTextData);
                headText2.setText(headText2Data);

                // 头图（如果你之后有 URL，也可以直接换成 load(url)）
                Glide.with(cjkptt.this)
                        .load(R.drawable.rating_gold_)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(headImg);

                for (int i = 0; i < 20; i++) {
                    if (ppaimingData[i] == null) break;

                    ppaiming[i].setText("#" + ppaimingData[i]);
                    sscore[i].setText(sscoreData[i]);
                    pptt[i].setText("qq: " + ppttData[i]);
                    nname[i].setText(nnameData[i]);

                    String qq = ppttData[i];
                    String avatarUrl =
                            "https://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=640";

                    Glide.with(cjkptt.this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.rating_off)
                            .error(R.drawable.rating_off)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(iimg[i]);
                }
            }
        }

    }

}