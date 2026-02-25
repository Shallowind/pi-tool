package com.zmmxkid.pitool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.zmmxkid.pitool.MainActivity.currentTask1;
import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class b30 extends AppCompatActivity {
    private String qq;
    private DiagonalCutImageView imageView7;
    private DiagonalCutImageView[] imgViews = new DiagonalCutImageView[30];
    private TextView[] bydTextViews = new TextView[30];
    private TextView[] nameTextViews = new TextView[30];
    private TextView[] scoreTextViews = new TextView[30];
    private TextView[] levelTextViews = new TextView[30];
    private TextView[] pttTextViews = new TextView[30];
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b30);
        LinearLayout linearLayout = findViewById(R.id.screenlay);
        if (currentTask1 != null) {
            currentTask1.cancel(true);
        }
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

        new DownloadImageTask().execute("https://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=640");
        new DownloadFileTask().execute("http://47.93.57.125:521/WebAPI/public/Bests/" + qq + ".txt");
    }

    public void seeall(View view) {
        // 创建 Handler 以便从后台线程更新 UI
        handler = new Handler(Looper.getMainLooper());

        // 自动下载并读取内容
        new Thread(new b30.DownloadTextRunnable()).start();
    }



    private class DownloadTextRunnable implements Runnable {
        @Override
        public void run() {
            try {
                URL url = new URL("http://47.93.57.125:521/WebAPI/public/scores/" + qq + ".txt");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder otherLines = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    otherLines.append(line).append("\n");
                }

                inputStream.close();

                // 使用 Handler 在主线程更新 UI
                final String finalOtherLines = otherLines.toString();
                handler.post(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(b30.this);
                    builder.setMessage(finalOtherLines)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    // 设置圆角背景（关键代码）
                    if (alertDialog.getWindow() != null) {
                        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
                    }

                    // 可选：美化按钮颜色（使用你已有的蓝色）
                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    if (positiveButton != null) {
                        positiveButton.setTextColor(getResources().getColor(R.color.ptt_blue, null));
                    }
                    if (negativeButton != null) {
                        negativeButton.setTextColor(getResources().getColor(R.color.text_secondary, null));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(b30.this, "下载失败", Toast.LENGTH_LONG).show();
                });
            }
        }
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
                Toast.makeText(b30.this, "该QQ未开通场景库，请到相应QQ群输入相关指令", Toast.LENGTH_LONG).show();
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
                double potentialValue = Double.parseDouble(firstRowData[1]);
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
                } else if (potentialValue >= 13 && potentialValue < 13.5) {
                    potential.setBackgroundResource(R.drawable.rating_9);
                    potential.setShadowLayer(2, 0, 0, Color.rgb(255, 215, 0));
                } else if (potentialValue >= 13.5) {
                    potential.setBackgroundResource(R.drawable.rating_10);
                    potential.setShadowLayer(2, 0, 0, Color.rgb(138, 43, 226));
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
                rank.setText("#" + firstRowData[4]);
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
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.BLACK);  // 设置白色描边
                    } else if (text.equals("ETR")) {
                        bydTextViews[i - 1].setTextColor(Color.parseColor("#ffaaff"));  // 设置字体颜色为紫色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.BLACK);  // 设置白色描边
                    } else if (text.equals("FTR")) {
                        bydTextViews[i - 1].setTextColor(Color.MAGENTA);  // 设置字体颜色为紫色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.BLACK);  // 设置白色描边
                    } else if (text.equals("MXM")) {
                        bydTextViews[i - 1].setTextColor(Color.YELLOW);  // 设置字体颜色为紫色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.BLACK);  // 设置白色描边
                    } else if (text.equals("PST")) {
                        bydTextViews[i - 1].setTextColor(Color.BLUE);  // 设置字体颜色为紫色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.BLACK);  // 设置白色描边
                    } else if (text.equals("PRS")) {
                        bydTextViews[i - 1].setTextColor(Color.GREEN);  // 设置字体颜色为紫色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.BLACK);  // 设置白色描边
                    } else {
                        // 其他情况，可以根据需要进行处理，比如设置默认的字体颜色和描边
                        bydTextViews[i - 1].setTextColor(Color.BLACK);  // 设置字体颜色为黑色
                        bydTextViews[i - 1].setShadowLayer(2, 0, 0, Color.TRANSPARENT);  // 取消描边
                    }
                    bydTextViews[i - 1].setText(rowData[4]);
                    nameTextViews[i - 1].setText(rowData[2]);
                    scoreTextViews[i - 1].setText(rowData[3]);
                    levelTextViews[i - 1].setText(rowData[5]);
                    pttTextViews[i - 1].setText("PTT:" + rowData[6]);
                    String ii = rowData[1]; // Assuming it's the index of the image
                    if (!ii.isEmpty()) {
                        File directory = new File(getFilesDir(), "icons");
                        String fileName = ii + ".png";
                        File file = new File(directory, fileName);
                        imgViews[i - 1].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(b30.this, scnperson.class);
                                intent.putExtra("key", ii);
                                startActivity(intent);
                            }
                        });
                        if (file.exists()) {
                            // 如果本地存在该图片，则直接设置ImageView的图片
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            imgViews[i - 1].setImageBitmap(bitmap);

                        } else {
                            // 如果本地不存在该图片，则开启下载任务
                            currentTask1 = new DownloadImageTaskForImageView(
                                    imgViews[i - 1],
                                    ii   // icon id，用来存文件名
                            );
                            currentTask1.execute("http://47.93.57.125:521/WebAPI/public/icons/" + ii + ".png");
                        }
                    }

                }
            }
        }
    }

    public class DownloadImageTaskForImageView extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;
        private String iconId;

        public DownloadImageTaskForImageView(ImageView imageView, String iconId) {
            this.imageView = imageView;
            this.iconId = iconId;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                if (bitmap != null) {
                    saveIconToLocal(bitmap, iconId);
                }

                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }

        private void saveIconToLocal(Bitmap bitmap, String iconId) {
            try {
                File dir = new File(getFilesDir(), "icons");
                if (!dir.exists()) dir.mkdirs();

                File file = new File(dir, iconId + ".png");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
