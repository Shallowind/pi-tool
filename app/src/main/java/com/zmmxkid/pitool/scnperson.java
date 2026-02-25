package com.zmmxkid.pitool;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.zmmxkid.pitool.MainActivity.currentTask;
import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class scnperson extends AppCompatActivity {

    private TextView headText, headText2;
    private TextView[] ppaiming = new TextView[20];
    private TextView[] sscore = new TextView[20];
    private TextView[] pptt = new TextView[20];
    private TextView[] nname = new TextView[20];
    private DiagonalCutImageView[] iimg = new DiagonalCutImageView[20];
    private DiagonalCutImageView headImg;
    private String num, qq;
    private int currentPage = 0;

    private boolean buttondef = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scnperson);
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
        if (currentTask != null) {
            currentTask.cancel(true);
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
                    Intent intent = new Intent(scnperson.this, b30.class);
                    TextView qqt = findViewById(getResources().getIdentifier("pptt" + (finalJ + 1), "id", getPackageName()));
                    String qq = qqt.getText().toString();
                    qq = qq.substring(4);
                    intent.putExtra("qq", qq);
                    startActivity(intent);
                }
            });
        }
        num = getIntent().getStringExtra("key");
        qq = "123456789"; // Replace with the actual QQ value
        String headIconUrl =
                "http://47.93.57.125:521/WebAPI/public/icons/" + num + ".png";
        Glide.with(scnperson.this)
                .load(headIconUrl)
                .placeholder(R.drawable.rating_off) // 加载中
                .error(R.drawable.rating_off)       // 失败兜底
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(headImg);
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
                    Toast.makeText(scnperson.this, "最后一页了", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void playscn(View view) {
        try {
            // 定义JSON文件的URL
            String jsonUrl = "http://47.93.57.125:521/ScenarioConfig/scenarioList.txt";
            // 执行AsyncTask来下载和解析JSON
            new DownloadJsonTask().execute(jsonUrl);
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(scnperson.this);
            builder.setMessage(e.getMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // 用户点击了确定按钮
                            dialog.dismiss(); // 关闭对话框
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
        }
    }

    public void chakanscn(View view) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(scnperson.this);
            builder.setMessage("请选择查看方式");
            builder.setPositiveButton("表格查看", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // 定义JSON文件的URL
//                    String jsonUrl = "http://47.93.57.125:521/ScenarioConfig/scenarioList.txt";
                    // 执行AsyncTask来下载和解析JSON
//                    new DownloadJsonTask_2().execute(jsonUrl);
                    Intent intent = new Intent(scnperson.this, shuju.class);
                    intent.putExtra("xuhao", num);
                    startActivity(intent);
                    dialog.dismiss(); // 关闭对话框
                }
            });
            builder.setNegativeButton("编辑器查看", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String jsonUrl = "http://47.93.57.125:521/ScenarioConfig/scenarioList.txt";
                    // 执行AsyncTask来下载和解析JSON
                    new DownloadJsonTask_3().execute(jsonUrl);
                    dialog.dismiss(); // 关闭对话框
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

        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(scnperson.this);
            builder.setMessage(e.getMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // 用户点击了确定按钮
                            dialog.dismiss(); // 关闭对话框
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
        }
    }

    public void chakanpl(View view) {
        Intent intent = new Intent(scnperson.this, pinglun.class);
        intent.putExtra("xuhao", num);
        intent.putExtra("bg", String.valueOf(randomNumber));
        startActivity(intent);
    }

    private void updateUI() {
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        currentTask = new LoadDataTask(this); // ✅ 传 Activity 的 Context
        currentTask.execute();
    }

    private class DownloadJsonTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(scnperson.this);
            progressDialog.setMessage("正在下载场景数据...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            String jsonUrl = strings[0];
            String jsonContent = ""; // 用于存储JSON内容的变量

            try {
                URL url = new URL(jsonUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // 获取用于进度跟踪的总文件大小
                int fileLength = connection.getContentLength();

                // 设置输入流并读取JSON内容
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }

                jsonContent = stringBuilder.toString();

                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonContent;
        }

        @Override
        protected void onPostExecute(String jsonContent) {
            super.onPostExecute(jsonContent);
            progressDialog.dismiss();

            // 解析JSON内容并提取必要的信息
            if (!jsonContent.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonContent);
                    JSONArray scenariosArray = jsonObject.getJSONArray("Scenarios");

                    // 假设num在此处可用，将其替换为实际值
                    int scenarioId = Integer.parseInt(num);

                    for (int i = 0; i < scenariosArray.length(); i++) {
                        JSONObject scenarioObject = scenariosArray.getJSONObject(i);
                        int id = scenarioObject.getInt("id");

                        // 找到具有匹配id的场景
                        if (id == scenarioId) {
                            String fileName = scenarioObject.getString("fileName");
                            String downloadUrl = "http://47.93.57.125:521/AutoScenarios/" + fileName + ".zip";

                            // 开始下载场景ZIP文件
                            new DownloadScenarioTask().execute(downloadUrl);
                            return;
                        }
                    }

                    // 如果找不到具有给定id的场景
                    Toast.makeText(scnperson.this, "未找到场景", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(scnperson.this, "下载场景数据失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
    }

    private class DownloadScenarioTask extends AsyncTask<String, Integer, File> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(scnperson.this);
            progressDialog.setMessage("正在下载场景文件...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected File doInBackground(String... strings) {
            String downloadUrl = strings[0];
            File downloadedFile = null;

            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();

                InputStream inputStream = connection.getInputStream();
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
                downloadedFile = new File(getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(downloadedFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                long total = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    total += bytesRead;
                    outputStream.write(buffer, 0, bytesRead);
                    publishProgress((int) (total * 100 / fileLength));
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return downloadedFile;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File downloadedFile) {
            super.onPostExecute(downloadedFile);
            progressDialog.dismiss();

            if (downloadedFile != null) {
                String renamedFilePath = downloadedFile.getAbsolutePath().replace(".zip", ".plagueinc");
                File renamedFile = new File(renamedFilePath);

                if (downloadedFile.renameTo(renamedFile)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri fileUri = FileProvider.getUriForFile(scnperson.this, "com.zmmxkid.pitool.fileprovider", renamedFile);
                    intent.setDataAndType(fileUri, "*/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.miniclip.plagueinc");

                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(scnperson.this);
                        builder.setMessage("注意：\n1.不是每个场景都能用手机游玩的，如遇问题请游玩其他场景\n2.请确保你把瘟疫公司从最近任务里划走(彻底处于未打开状态)才能正常游玩")
                                .setPositiveButton("开始游戏（我已划走瘟疫公司）", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // 启动悬浮窗服务
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(scnperson.this)) {
                                            Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:" + getPackageName()));
                                            startActivity(overlayIntent);
                                            Toast.makeText(scnperson.this, "请授予悬浮窗权限后再尝试", Toast.LENGTH_LONG).show();
                                        } else {
                                            Intent floatIntent = new Intent(scnperson.this, FloatingViewService.class);
                                            floatIntent.putExtra("key", num + "-" + headText.getText().toString());
                                            startService(floatIntent);
                                            try {
                                                startActivity(intent);
                                            } catch (Exception e) {
                                                Toast.makeText(scnperson.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        dialog.dismiss();
                                    }
                                });
                        builder.create().show();
                    } catch (Exception e) {
                        new AlertDialog.Builder(scnperson.this)
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                                .create().show();
                    }
                } else {
                    Toast.makeText(scnperson.this, "文件重命名失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(scnperson.this, "下载场景文件失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
    }

    private class DownloadJsonTask_2 extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(scnperson.this);
            progressDialog.setMessage("正在下载场景数据...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            String jsonUrl = strings[0];
            String jsonContent = ""; // 用于存储JSON内容的变量

            try {
                URL url = new URL(jsonUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // 获取用于进度跟踪的总文件大小
                int fileLength = connection.getContentLength();

                // 设置输入流并读取JSON内容
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }

                jsonContent = stringBuilder.toString();

                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonContent;
        }

        @Override
        protected void onPostExecute(String jsonContent) {
            super.onPostExecute(jsonContent);
            progressDialog.dismiss();

            // 解析JSON内容并提取必要的信息
            if (!jsonContent.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonContent);
                    JSONArray scenariosArray = jsonObject.getJSONArray("Scenarios");

                    // 假设num在此处可用，将其替换为实际值
                    int scenarioId = Integer.parseInt(num);

                    for (int i = 0; i < scenariosArray.length(); i++) {
                        JSONObject scenarioObject = scenariosArray.getJSONObject(i);
                        int id = scenarioObject.getInt("id");

                        // 找到具有匹配id的场景
                        if (id == scenarioId) {
                            String fileName = scenarioObject.getString("fileName");
                            String downloadUrl = "http://47.93.57.125:521/AutoScenarios/" + fileName + ".zip";

                            // 开始下载场景ZIP文件
                            new DownloadScenarioTask_2().execute(downloadUrl);
                            return;
                        }
                    }

                    // 如果找不到具有给定id的场景
                    Toast.makeText(scnperson.this, "未找到场景", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(scnperson.this, "下载场景数据失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
    }

    private class DownloadScenarioTask_2 extends AsyncTask<String, Integer, File> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(scnperson.this);
            progressDialog.setMessage("正在下载场景文件...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected File doInBackground(String... strings) {
            String downloadUrl = strings[0];
            File downloadedFile = null;

            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // 获取用于进度跟踪的总文件大小
                int fileLength = connection.getContentLength();

                // 设置输入流和文件下载的输出流
                InputStream inputStream = connection.getInputStream();
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
                downloadedFile = new File(getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(downloadedFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                long total = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    total += bytesRead;
                    outputStream.write(buffer, 0, bytesRead);
                    // 发布进度
                    publishProgress((int) (total * 100 / fileLength));
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return downloadedFile;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File downloadedFile) {
            super.onPostExecute(downloadedFile);
            progressDialog.dismiss();
            if (downloadedFile != null) {
                Intent intent = new Intent(scnperson.this, csv.class);
                intent.putExtra("path", downloadedFile.getAbsolutePath());
                startActivity(intent);
            } else {
                Toast.makeText(scnperson.this, "下载场景文件失败", Toast.LENGTH_SHORT).show();
            }

        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
    }

    private class DownloadJsonTask_3 extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(scnperson.this);
            progressDialog.setMessage("正在下载场景数据...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... strings) {
            String jsonUrl = strings[0];
            String jsonContent = ""; // 用于存储JSON内容的变量

            try {
                URL url = new URL(jsonUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // 获取用于进度跟踪的总文件大小
                int fileLength = connection.getContentLength();

                // 设置输入流并读取JSON内容
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }

                jsonContent = stringBuilder.toString();

                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonContent;
        }

        @Override
        protected void onPostExecute(String jsonContent) {
            super.onPostExecute(jsonContent);
            progressDialog.dismiss();

            // 解析JSON内容并提取必要的信息
            if (!jsonContent.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonContent);
                    JSONArray scenariosArray = jsonObject.getJSONArray("Scenarios");

                    // 假设num在此处可用，将其替换为实际值
                    int scenarioId = Integer.parseInt(num);

                    for (int i = 0; i < scenariosArray.length(); i++) {
                        JSONObject scenarioObject = scenariosArray.getJSONObject(i);
                        int id = scenarioObject.getInt("id");

                        // 找到具有匹配id的场景
                        if (id == scenarioId) {
                            String fileName = scenarioObject.getString("fileName");
                            String downloadUrl = "http://47.93.57.125:521/AutoScenarios/" + fileName + ".zip";

                            // 开始下载场景ZIP文件
                            new DownloadScenarioTask_3().execute(downloadUrl);
                            return;
                        }
                    }

                    // 如果找不到具有给定id的场景
                    Toast.makeText(scnperson.this, "未找到场景", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(scnperson.this, "下载场景数据失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
    }

    private class DownloadScenarioTask_3 extends AsyncTask<String, Integer, File> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(scnperson.this);
            progressDialog.setMessage("正在下载场景文件...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected File doInBackground(String... strings) {
            String downloadUrl = strings[0];
            File downloadedFile = null;

            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // 获取用于进度跟踪的总文件大小
                int fileLength = connection.getContentLength();

                // 设置输入流和文件下载的输出流
                InputStream inputStream = connection.getInputStream();
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
                downloadedFile = new File(getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(downloadedFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                long total = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    total += bytesRead;
                    outputStream.write(buffer, 0, bytesRead);
                    // 发布进度
                    publishProgress((int) (total * 100 / fileLength));
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return downloadedFile;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File downloadedFile) {
            super.onPostExecute(downloadedFile);
            progressDialog.dismiss();

            if (downloadedFile != null) {
                // 解压下载的.zip文件
                File tempDir = new File(getCacheDir(), "tempDir");
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }

                try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(downloadedFile))) {
                    ZipEntry zipEntry;
                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        File extractedFile = new File(tempDir, zipEntry.getName());
                        if (zipEntry.isDirectory()) {
                            extractedFile.mkdirs();
                        } else {
                            try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = zipInputStream.read(buffer)) != -1) {
                                    fos.write(buffer, 0, length);
                                }
                            }
                        }
                        zipInputStream.closeEntry();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(scnperson.this, "解压文件失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 查找并处理events.txt
                File eventsFile = new File(tempDir, "events.txt");
                if (eventsFile.exists()) {
                    try {
                        // 读取events.txt内容
                        List<String> lines = new ArrayList<>();
                        try (BufferedReader reader = new BufferedReader(new FileReader(eventsFile))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                // 删除包含 "PopupType", "Achievement", 或 "NewsVisibility" 的行
                                if (line.contains("PopupType") || line.contains("Achievement") || line.contains("NewsVisibility")) {
                                    continue;
                                }

                                // 删除字符串 "PopulationFrom=" PopulationTo=""
                                line = line.replaceAll("PopulationFrom=\"\" PopulationTo=\"\"", "");

                                lines.add(line);
                            }
                        }

                        // 将处理后的内容写回文件
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(eventsFile))) {
                            for (String line : lines) {
                                writer.write(line);
                                writer.newLine();
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(scnperson.this, "处理 events.txt 文件失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(scnperson.this, "未找到 events.txt 文件", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 重新压缩文件
                File renamedFile = new File(downloadedFile.getAbsolutePath().replace(".zip", ".plagueinc"));
                try {
                    // 重新创建压缩包
                    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(renamedFile))) {
                        addFilesToZip(tempDir, zipOutputStream);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(scnperson.this, "重新压缩文件失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 继续文件重命名和Intent操作
                if (renamedFile.renameTo(renamedFile)) {
                    // 创建一个Intent来打开文件
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri fileUri = FileProvider.getUriForFile(scnperson.this, "com.zmmxkid.pitool.fileprovider", renamedFile);
                    intent.setDataAndType(fileUri, "*/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.ndemiccreations.scenariocreatos");

                    // 检查是否有可以处理该Intent的应用程序
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(scnperson.this, "修改版编辑器未安装", Toast.LENGTH_SHORT).show();
                        Uri updateUri = Uri.parse("https://ncsb.lanzoul.com/iCKAb2chpipa");
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, updateUri);
                        startActivity(browserIntent);
                    }
                } else {
                    Toast.makeText(scnperson.this, "文件重命名失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(scnperson.this, "下载场景文件失败", Toast.LENGTH_SHORT).show();
            }
        }

        // 递归将文件添加到压缩包
        private void addFilesToZip(File dir, ZipOutputStream zipOutputStream) throws IOException {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    addFilesToZip(file, zipOutputStream);
                } else {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        zipOutputStream.putNextEntry(new ZipEntry(file.getAbsolutePath().replace(dir.getAbsolutePath() + File.separator, "")));
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) != -1) {
                            zipOutputStream.write(buffer, 0, length);
                        }
                        zipOutputStream.closeEntry();
                    }
                }
            }
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
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
                /* ===== 文本 ===== */
                URL textUrl = new URL(
                        "http://47.93.57.125:521/WebAPI/public/场景库/" + num + ".txt"
                );
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(textUrl.openStream())
                );

                headTextData = reader.readLine();
                headText2Data = reader.readLine();

                for (int j = 0; j < currentPage * 20; j++) {
                    reader.readLine();
                }

                for (int i = 0; i < 20; i++) {
                    String line = reader.readLine();
                    if (line == null) break;

                    String[] data = line.split(",");
                    ppaimingData[i] = data[0];
                    sscoreData[i] = data[1];
                    ppttData[i] = data[2];
                    nnameData[i] = data[3];
                }

                reader.close();


                publishProgress(-1);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private Bitmap downloadAndSave(URL url, File file) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == -1) {
                headText.setText(headTextData);
                String text = headText2Data;
                if (text.contains("BYD")) {
                    headText2.setTextColor(Color.RED);  // 设置字体颜色为红色
                } else if (text.contains("ETR")) {
                    headText2.setTextColor(Color.parseColor("#ffaaff"));  // 设置字体颜色为紫色
                } else if (text.contains("FTR")) {
                    headText2.setTextColor(Color.MAGENTA);  // 设置字体颜色为紫色
                } else if (text.contains("MXM")) {
                    headText2.setTextColor(Color.YELLOW);  // 设置字体颜色为紫色
                } else if (text.contains("PST")) {
                    headText2.setTextColor(Color.BLUE);  // 设置字体颜色为紫色
                } else if (text.contains("PRS")) {
                    headText2.setTextColor(Color.GREEN);  // 设置字体颜色为紫色
                } else {
                    // 其他情况，可以根据需要进行处理，比如设置默认的字体颜色和描边
                    headText2.setTextColor(Color.BLACK);  // 设置字体颜色为黑色
                }if (text.contains("-1")) {
                    headText.setText(headTextData+"【已下架】");
                }
                headText2.setText(headText2Data);


                for (int i = 0; i < 20; i++) {
                    if (ppttData[i] == null) break;
                    if (ppaimingData[i] == null) break;
                    ppaiming[i].setText("#" + ppaimingData[i]);
                    sscore[i].setText(sscoreData[i]);
                    pptt[i].setText("qq: " + ppttData[i]);
                    nname[i].setText(nnameData[i]);
                    String qq = ppttData[i];
                    String qqAvatarUrl =
                            "https://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=640";

                    Glide.with(scnperson.this)
                            .load(qqAvatarUrl)
                            .placeholder(R.drawable.rating_off)
                            .error(R.drawable.rating_off)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(iimg[i]);
                }
            }
        }
    }

}