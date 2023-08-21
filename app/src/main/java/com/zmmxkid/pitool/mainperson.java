package com.zmmxkid.pitool;

import static com.zmmxkid.pitool.MainActivity.currentTask2;

import android.content.Context;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class mainperson extends AppCompatActivity {

    private TextView headText, headText2;
    private TextView[] ppaiming = new TextView[20];
    private TextView[] sscore = new TextView[20];
    private TextView[] pptt = new TextView[20];
    private TextView[] nname = new TextView[20];
    private ImageView[] iimg = new ImageView[20];
    private ImageView headImg;
    private String num,num1,qq;
    private int currentPage = 0;

    private boolean buttondef=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainperson);
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
                    Toast.makeText(mainperson.this, "最后一页了", Toast.LENGTH_LONG).show();
                }
            }
        });
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
        private String[] sscoreData = new String[20];
        private String[] ppttData = new String[20];
        private String[] nnameData = new String[20];
        private String[] iimgUrls = new String[20];
        private Bitmap[] iimgBitmaps = new Bitmap[20];
        private Bitmap iimgBitmap;

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                // Load text data from URL
                URL textUrl = new URL("http://47.241.0.156:34567/WebAPI/public/"+num1+"/" + num + ".txt");
                HttpURLConnection textConnection = (HttpURLConnection) textUrl.openConnection();
                textConnection.connect();
                InputStream textStream = textConnection.getInputStream();
                BufferedReader textReader = new BufferedReader(new InputStreamReader(textStream));
                headTextData = num1;
                headText2Data = num;

                for (int j = 0; j < currentPage * 20; j++) {
                    textReader.readLine();
                }
                for (int i = 0; i < 20; i++) {
                    String line = textReader.readLine();
                    if (line == null) {
                        break;
                    }
                    String[] lineData = line.split(",");
                    ppaimingData[i] = lineData[0];
                    sscoreData[i] = lineData[1];
                    ppttData[i] = lineData[2];
                    nnameData[i] = lineData[3];
                    if (lineData[3].equalsIgnoreCase("NULL")) {
                        buttondef = false;
                    }

                    if (lineData.length > 2 && !lineData[2].isEmpty()) {
                        qq = lineData[2];
                        iimgUrls[i] = "https://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=100";
                    }
                }

                textStream.close();

                // Load image data
                for (int i = 0; i < 20; i++) {
                    if (iimgUrls[i] == null) {
                        break;
                    }
                    if (isCancelled()) {
                        break;
                    }
                    URL imgUrl = new URL(iimgUrls[i]);
                    HttpURLConnection imgConnection = (HttpURLConnection) imgUrl.openConnection();
                    imgConnection.connect();
                    InputStream imgStream = imgConnection.getInputStream();
                    iimgBitmaps[i] = BitmapFactory.decodeStream(imgStream);
                    imgStream.close();
                    publishProgress(i);
                    URL imgUrl2 = new URL("http://47.241.0.156:34567/WebAPI/icons/" + num + ".png");
                    HttpURLConnection imgConnection2 = (HttpURLConnection) imgUrl2.openConnection();
                    imgConnection2.connect();
                    InputStream imgStream2 = imgConnection2.getInputStream();
                    iimgBitmap = BitmapFactory.decodeStream(imgStream2);
                    imgStream2.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            headText.setText(headTextData);
            headText2.setText(headText2Data);
            headImg.setImageBitmap(iimgBitmap);

            for (int i = 0; i < 20; i++) {
                if (ppaimingData[i] == null) {
                    break;
                }
                ppaiming[i].setText("#" + ppaimingData[i]);
                sscore[i].setText(sscoreData[i]);
                pptt[i].setText("qq: "+ppttData[i]);
                nname[i].setText(nnameData[i]);
            }
            int index = values[0];
            iimg[index].setImageBitmap(iimgBitmaps[index]);
        }
    }
}