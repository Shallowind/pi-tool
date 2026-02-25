package com.zmmxkid.pitool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zmmxkid.pitool.MainActivity.currentTask2;
import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class mainperson extends AppCompatActivity {

    private TextView headText, headText2;
    private TextView[] ppaiming = new TextView[20];
    private TextView[] sscore = new TextView[20];
    private TextView[] pptt = new TextView[20];
    private TextView[] nname = new TextView[20];
    private DiagonalCutImageView[] iimg = new DiagonalCutImageView[20];
    private DiagonalCutImageView headImg;
    private String num, num1, qq;
    private int currentPage = 0;

    private boolean buttondef = true;

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
        if (currentTask2 != null) {
            currentTask2.cancel(true);
        }

        headText = findViewById(R.id.headtext);
        headText2 = findViewById(R.id.headtext2);
        headImg = findViewById(R.id.headimg);
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
                    Intent intent = new Intent(mainperson.this, b30.class);
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
                    buttondef = true;
                    updateUI();
                }
            }
        });

        findViewById(R.id.down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttondef) {
                    currentPage++;
                    for (int j = 0; j < 20; j++) {
                        iimg[j].setImageResource(R.drawable.rating_off);
                    }
                    updateUI();
                } else {
                    Toast.makeText(mainperson.this, "最后一页了", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    public void chakanpl(View view) {
        Intent intent = new Intent(mainperson.this, pinglun.class);
        intent.putExtra("xuhao", headText.getText().toString() + "-" + headText2.getText().toString());
        intent.putExtra("bg", String.valueOf(randomNumber));
        startActivity(intent);
    }
    public void startmaingame(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.miniclip.plagueinc");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(mainperson.this)) {
            Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(overlayIntent);
            Toast.makeText(mainperson.this, "请授予悬浮窗权限后再尝试", Toast.LENGTH_LONG).show();
        } else {
            Intent floatIntent = new Intent(mainperson.this, FloatingViewService.class);
            floatIntent.putExtra("key", headText.getText().toString() + "-" + headText2.getText().toString());
            startService(floatIntent);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }
    }

    private void updateUI() {
        if (currentTask2 != null) {
            currentTask2.cancel(true);
        }
        currentTask2 = new LoadDataTask();
        currentTask2.execute();
    }

    public class LoadDataTask extends AsyncTask<Void, Integer, Void> {

        private String headTextData, headText2Data;
        private String[] ppaimingData = new String[20];
        private String[] sscoreData   = new String[20];
        private String[] ppttData     = new String[20];
        private String[] nnameData    = new String[20];
        private String[] iimgUrls     = new String[20];   // QQ头像URL
        private String headImgUrl;                         // 头图URL

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL textUrl = new URL(
                        "http://47.93.57.125:521/WebAPI/public/" + num1 + "/" + num + ".txt"
                );
                HttpURLConnection conn = (HttpURLConnection) textUrl.openConnection();
                conn.connect();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                headTextData  = num1;
                headText2Data = num;

                // 跳页
                for (int j = 0; j < currentPage * 20; j++) {
                    reader.readLine();
                }

                for (int i = 0; i < 20; i++) {
                    String line = reader.readLine();
                    if (line == null) break;

                    String[] data = line.split(",");

                    ppaimingData[i] = data[0];
                    sscoreData[i]   = data[1];
                    ppttData[i]    = data[2];
                    nnameData[i]   = data[3];

                    if ("NULL".equalsIgnoreCase(data[3])) {
                        buttondef = false;
                    }

                    if (data.length > 2 && !data[2].isEmpty()) {
                        iimgUrls[i] =
                                "https://q.qlogo.cn/g?b=qq&nk=" + data[2] + "&s=640";
                    }
                }

                reader.close();

                // 头图 URL
                headImgUrl =
                        "http://47.93.57.125:521/WebAPI/icons/" + num + ".png";

                publishProgress(-1);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            /* ===== 文本 ===== */
            headText.setText(headTextData);
            headText2.setText(headText2Data);

            for (int i = 0; i < 20; i++) {
                if (ppaimingData[i] == null) break;

                ppaiming[i].setText("#" + ppaimingData[i]);
                sscore[i].setText(sscoreData[i]);
                pptt[i].setText("qq: " + ppttData[i]);
                nname[i].setText(nnameData[i]);
            }

            /* ===== 头图：Glide ===== */
            Glide.with(mainperson.this)
                    .load(headImgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.rating_off)
                    .error(R.drawable.rating_off)
                    .into(headImg);

            /* ===== QQ 头像：Glide ===== */
            for (int i = 0; i < 20; i++) {
                if (iimgUrls[i] == null) break;

                Glide.with(mainperson.this)
                        .load(iimgUrls[i])
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.rating_off)
                        .error(R.drawable.rating_off)
                        .into(iimg[i]);
            }
        }
    }

}