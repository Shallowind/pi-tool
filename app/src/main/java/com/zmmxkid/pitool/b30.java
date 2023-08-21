package com.zmmxkid.pitool;

import static com.zmmxkid.pitool.MainActivity.currentTask;
import static com.zmmxkid.pitool.MainActivity.currentTask1;

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
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import android.graphics.Color;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class b30 extends AppCompatActivity {
    private String qq;
    private ImageView imageView7;
    private ImageView[] imgViews = new ImageView[30];
    private TextView[] bydTextViews = new TextView[30];
    private TextView[] nameTextViews = new TextView[30];
    private TextView[] scoreTextViews = new TextView[30];
    private TextView[] levelTextViews = new TextView[30];
    private TextView[] pttTextViews = new TextView[30];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b30);
        LinearLayout linearLayout = findViewById(R.id.screenlay);
        if (currentTask1 != null) {
            currentTask1.cancel(true);
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

// 设置 LinearLayout 的布局参数
        ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
        layoutParams.width = targetWidth;
        layoutParams.height = targetHeight;
        linearLayout.setLayoutParams(layoutParams);

        qq = getIntent().getStringExtra("qq");

        imageView7 = findViewById(R.id.imageView7);

        for (int i = 0; i < 30; i++) {
            imgViews[i] = findViewById(getResources().getIdentifier("img" + (i + 1), "id", getPackageName()));
            bydTextViews[i] = findViewById(getResources().getIdentifier("byd" + (i + 1), "id", getPackageName()));
            nameTextViews[i] = findViewById(getResources().getIdentifier("name" + (i + 1), "id", getPackageName()));
            scoreTextViews[i] = findViewById(getResources().getIdentifier("score" + (i + 1), "id", getPackageName()));
            levelTextViews[i] = findViewById(getResources().getIdentifier("level" + (i + 1), "id", getPackageName()));
            pttTextViews[i] = findViewById(getResources().getIdentifier("ptt" + (i + 1), "id", getPackageName()));
        }

        new DownloadImageTask().execute("https://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=100");
        new DownloadFileTask().execute("http://47.241.0.156:34567/WebAPI/public/Bests/" + qq + ".txt");
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
                imageView7.setImageBitmap(result);
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
                Toast.makeText(b30.this, "该QQ不存在，请到相应QQ群输入相关指令", Toast.LENGTH_LONG).show();
                return;
            }
            String[] lines = content.split("\n");

            String[] firstRowData = lines[0].split(",");
            TextView player = findViewById(R.id.player);
            TextView potential = findViewById(R.id.potential);
            TextView rank = findViewById(R.id.rank);
            if (true) {
                player.setText(firstRowData[0]);
                potential.setText(firstRowData[1]);
                double potentialValue=Double.parseDouble(firstRowData[1]);
                if (potentialValue < 0.1) {
                    potential.setBackgroundResource(R.drawable.rating_off);
                } else if (potentialValue >= 0.1 && potentialValue < 3.5) {
                    potential.setBackgroundResource(R.drawable.rating_1);
                } else if (potentialValue >= 3.5 && potentialValue < 7) {
                    potential.setBackgroundResource(R.drawable.rating_2);
                } else if (potentialValue >= 7 && potentialValue < 10) {
                    potential.setBackgroundResource(R.drawable.rating_3);
                } else if (potentialValue >= 10 && potentialValue < 11) {
                    potential.setBackgroundResource(R.drawable.rating_4);
                } else if (potentialValue >= 11 && potentialValue < 12) {
                    potential.setBackgroundResource(R.drawable.rating_5);
                } else if (potentialValue >= 12 && potentialValue < 12.5) {
                    potential.setBackgroundResource(R.drawable.rating_6);
                } else if (potentialValue >= 12.5 && potentialValue < 13) {
                    potential.setBackgroundResource(R.drawable.rating_7);
                } else if (potentialValue >= 13) {
                    potential.setBackgroundResource(R.drawable.rating_9);
                }
//                TextView potentialTextView = findViewById(R.id.potential);
//
//                potentialTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        // 移除监听，避免重复调用
//                        potentialTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//
//                        // 获取宽度
//                        int width = potentialTextView.getWidth();
//
//                        // 设置高度为与宽度相同的值
//                        potentialTextView.getLayoutParams().height = width;
//
//                        // 重新布局以应用更改
//                        potentialTextView.requestLayout();
//                    }
//                });
                rank.setText("#"+firstRowData[4]);
            }

            for (int i = 1; i < lines.length && i <= 30; i++) {
                if (isCancelled()) {
                    break;
                }
                String[] rowData = lines[i].split(",");
                if (rowData.length >= 7) {
                    String text = rowData[4];
                    if (text.equals("BYD")) {
                        bydTextViews[i - 1].setTextColor(Color.RED);  // 设置字体颜色为红色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.WHITE);  // 设置白色描边
                    } else if (text.equals("FTR")) {
                        bydTextViews[i - 1].setTextColor(Color.MAGENTA);  // 设置字体颜色为紫色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.WHITE);  // 设置白色描边
                    }else if (text.equals("MXM")) {
                        bydTextViews[i - 1].setTextColor(Color.YELLOW);  // 设置字体颜色为紫色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.WHITE);  // 设置白色描边
                    }else if (text.equals("PST")) {
                        bydTextViews[i - 1].setTextColor(Color.BLUE);  // 设置字体颜色为紫色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.WHITE);  // 设置白色描边
                    }else if (text.equals("PRS")) {
                        bydTextViews[i - 1].setTextColor(Color.GREEN);  // 设置字体颜色为紫色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.WHITE);  // 设置白色描边
                    } else {
                        // 其他情况，可以根据需要进行处理，比如设置默认的字体颜色和描边
                        bydTextViews[i - 1].setTextColor(Color.BLACK);  // 设置字体颜色为黑色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.TRANSPARENT);  // 取消描边
                    }
                    bydTextViews[i - 1].setText(rowData[4]);
                    nameTextViews[i - 1].setText(rowData[2]);
                    scoreTextViews[i - 1].setText(rowData[3]);
                    levelTextViews[i - 1].setText(rowData[5]);
                    pttTextViews[i - 1].setText("PTT:"+rowData[6]);
                    String ii = rowData[1]; // Assuming it's the index of the image
                    if (!ii.isEmpty()) {
                        currentTask1 = new DownloadImageTaskForImageView(imgViews[i - 1]);
                        currentTask1.execute("http://47.241.0.156:34567/WebAPI/public/icons/" + ii + ".png");
                    }
                }
            }
        }
    }

    public class DownloadImageTaskForImageView extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public DownloadImageTaskForImageView(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            Bitmap bitmap = null;
            if (!isCancelled()) {
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
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }
}
