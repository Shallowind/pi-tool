package com.zmmxkid.pitool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Overall extends AppCompatActivity {
    private String qq;
    private ImageView imageView5;
    private TextView textview24, textview13, textview29, textview32, textview25;
    private TextView all1, lilun1, sanxing1, hege1, tongguan1, zongji1;
    private TextView[] allTextViews = new TextView[18];
    private TextView[] lilunTextViews = new TextView[18];
    private TextView[] sanxingTextViews = new TextView[18];
    private TextView[] hegeTextViews = new TextView[18];
    private TextView[] tongguanTextViews = new TextView[18];
    private TextView[] zongjiTextViews = new TextView[18];
    private double width;
    private double height;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overall);
        qq = getIntent().getStringExtra("qq");
        LinearLayout linearLayout = findViewById(R.id.screenlay2);

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



// 设置 LinearLayout 的布局参数
        ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
        layoutParams.width = targetWidth;
        layoutParams.height = targetHeight;
        linearLayout.setLayoutParams(layoutParams);

        imageView5 = findViewById(R.id.imageView5);
        textview24 = findViewById(R.id.textView24);
        textview13 = findViewById(R.id.textView13);
        textview29 = findViewById(R.id.textView29);
        textview32 = findViewById(R.id.textView32);
        textview25 = findViewById(R.id.textView25);

        all1 = findViewById(R.id.all1);
        lilun1 = findViewById(R.id.lilun1);
        sanxing1 = findViewById(R.id.sanxing1);
        hege1 = findViewById(R.id.hege1);
        tongguan1 = findViewById(R.id.tongguan1);
        zongji1 = findViewById(R.id.zongji1);

        for (int i = 0; i < 18; i++) {
            allTextViews[i] = findViewById(getResources().getIdentifier("all" + (i + 2), "id", getPackageName()));
            lilunTextViews[i] = findViewById(getResources().getIdentifier("lilun" + (i + 2), "id", getPackageName()));
            sanxingTextViews[i] = findViewById(getResources().getIdentifier("sanxing" + (i + 2), "id", getPackageName()));
            hegeTextViews[i] = findViewById(getResources().getIdentifier("hege" + (i + 2), "id", getPackageName()));
            tongguanTextViews[i] = findViewById(getResources().getIdentifier("tongguan" + (i + 2), "id", getPackageName()));
            zongjiTextViews[i] = findViewById(getResources().getIdentifier("zongji" + (i + 2), "id", getPackageName()));
        }

        new DownloadImageTask().execute("https://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=100");
        new DownloadFileTask().execute("http://47.241.0.156:34567/WebAPI/public/Overalls/" + qq + ".txt");
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView5.setImageBitmap(result);
            }
        }
    }

    private class DownloadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            StringBuilder content = new StringBuilder();
            try {
                URL fileUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                inputStream.close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return content.toString();
        }

        @Override
        protected void onPostExecute(String content) {
            if (content.isEmpty()) {
                Toast.makeText(Overall.this, "该QQ不存在，请到相应QQ群输入相关指令", Toast.LENGTH_LONG).show();
                return;
            }
            String[] lines = content.split("\n");

            String[] firstRowData = lines[0].split(",");
            textview24.setText(firstRowData[0]);
            textview13.setText(firstRowData[1]);
            double potentialValue=Double.parseDouble(firstRowData[1]);
            if (potentialValue < 0.1) {
                textview13.setBackgroundResource(R.drawable.rating_off);
            } else if (potentialValue >= 0.1 && potentialValue < 3.5) {
                textview13.setBackgroundResource(R.drawable.rating_1);
            } else if (potentialValue >= 3.5 && potentialValue < 7) {
                textview13.setBackgroundResource(R.drawable.rating_2);
            } else if (potentialValue >= 7 && potentialValue < 10) {
                textview13.setBackgroundResource(R.drawable.rating_3);
            } else if (potentialValue >= 10 && potentialValue < 11) {
                textview13.setBackgroundResource(R.drawable.rating_4);
            } else if (potentialValue >= 11 && potentialValue < 12) {
                textview13.setBackgroundResource(R.drawable.rating_5);
            } else if (potentialValue >= 12 && potentialValue < 12.5) {
                textview13.setBackgroundResource(R.drawable.rating_6);
            } else if (potentialValue >= 12.5 && potentialValue < 13) {
                textview13.setBackgroundResource(R.drawable.rating_7);
            } else if (potentialValue >= 13) {
                textview13.setBackgroundResource(R.drawable.rating_9);
            }
            textview29.setText("总分："+firstRowData[2]);
            textview32.setText(firstRowData[4]);
            textview25.setText(qq);

            if (lines.length >= 19) {
                String[] rowData = lines[18].split(",");
                if (rowData.length >= 6) {
                    all1.setText(rowData[0]);
                    lilun1.setText(rowData[1]);
                    sanxing1.setText(rowData[2]);
                    hege1.setText(rowData[3]);
                    tongguan1.setText(rowData[4]);
                    zongji1.setText(rowData[5]);
                }
            }

            for (int i = 1; i < 18 && i < lines.length; i++) {
                String[] rowData = lines[i].split(",");
                if (rowData.length >= 6) {
                    allTextViews[i - 1].setText("【"+rowData[0]+"】");
                    if(i==1||i==3||i==5||i==7||i==8)allTextViews[i - 1].setText("【 "+rowData[0]+" 】");
                    if(i>8&&i<18)allTextViews[i - 1].setText("【  "+rowData[0]+"  】");
                    lilunTextViews[i - 1].setText(rowData[1]);
                    sanxingTextViews[i - 1].setText(rowData[2]);
                    hegeTextViews[i - 1].setText(rowData[3]);
                    tongguanTextViews[i - 1].setText(rowData[4]);
                    zongjiTextViews[i - 1].setText(rowData[5]);
                }
            }

// 假设你有一个存储后缀数字的数组，例如 [1, 2, 3, ..., 18]
            int[] suffixNumbers = new int[18];
            for (int i = 0; i < 18; i++) {
                suffixNumbers[i] = i + 1;
            }

// 针对每个后缀数字，执行相同的操作
            for (int suffix : suffixNumbers) {
                TextView yellow = findViewById(getResources().getIdentifier("yellow" + suffix, "id", getPackageName()));
                TextView orange = findViewById(getResources().getIdentifier("orange" + suffix, "id", getPackageName()));
                TextView pink = findViewById(getResources().getIdentifier("pink" + suffix, "id", getPackageName()));
                TextView green = findViewById(getResources().getIdentifier("green" + suffix, "id", getPackageName()));
                TextView gray = findViewById(getResources().getIdentifier("gray" + suffix, "id", getPackageName()));

                TextView lilun = findViewById(getResources().getIdentifier("lilun" + suffix, "id", getPackageName()));
                TextView sanxing = findViewById(getResources().getIdentifier("sanxing" + suffix, "id", getPackageName()));
                TextView hege = findViewById(getResources().getIdentifier("hege" + suffix, "id", getPackageName()));
                TextView zongji = findViewById(getResources().getIdentifier("zongji" + suffix, "id", getPackageName()));
                TextView tongguan = findViewById(getResources().getIdentifier("tongguan" + suffix, "id", getPackageName()));

                int lilunValue = Integer.parseInt((String) lilun.getText());
                int zongjiValue = Integer.parseInt((String) zongji.getText());
                if(zongjiValue==0){
                    zongjiValue=1;
                }

                LinearLayout.LayoutParams yellowLayoutParams = (LinearLayout.LayoutParams) yellow.getLayoutParams();
                yellowLayoutParams.weight = (float) lilunValue / zongjiValue;
                yellow.setLayoutParams(yellowLayoutParams);

                int sanxingValue = Integer.parseInt((String) sanxing.getText()) - lilunValue;
                LinearLayout.LayoutParams orangeLayoutParams = (LinearLayout.LayoutParams) orange.getLayoutParams();
                orangeLayoutParams.weight = (float) sanxingValue / zongjiValue;
                orange.setLayoutParams(orangeLayoutParams);

                int pinkValue = Integer.parseInt((String) hege.getText()) - Integer.parseInt((String) sanxing.getText());
                LinearLayout.LayoutParams pinkLayoutParams = (LinearLayout.LayoutParams) pink.getLayoutParams();
                pinkLayoutParams.weight = (float) pinkValue / zongjiValue;
                pink.setLayoutParams(pinkLayoutParams);

                int greenValue = Integer.parseInt((String) tongguan.getText()) - Integer.parseInt((String) hege.getText());
                LinearLayout.LayoutParams greenLayoutParams = (LinearLayout.LayoutParams) green.getLayoutParams();
                greenLayoutParams.weight = (float) greenValue / zongjiValue;
                green.setLayoutParams(greenLayoutParams);

                int grayValue = zongjiValue - Integer.parseInt((String) tongguan.getText());
                LinearLayout.LayoutParams grayLayoutParams = (LinearLayout.LayoutParams) gray.getLayoutParams();
                if(zongjiValue==0){
                    grayValue=1;
                    zongjiValue=1;
                }
                grayLayoutParams.weight = (float) grayValue / zongjiValue;
                gray.setLayoutParams(grayLayoutParams);
            }

        }
    }
}
