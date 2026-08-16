package com.zmmxkid.pitool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 100;
    private static final String ANNOUNCEMENT_URL = "http://47.93.57.125:521/Webapi/public/comment/gg/source/announcement_default.txt";

    public static double version = 4.5;
    public static scnperson.LoadDataTask currentTask;
    public static mainperson.LoadDataTask currentTask2;
    public static b30.DownloadImageTaskForImageView currentTask1;
    public static int randomNumber = 1;
    private static String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAllFilesPermission(); // ⬅️ 添加权限检查

        new CheckVersionTask().execute();

        Random random = new Random();
        randomNumber = random.nextInt(5 - 1 + 1) + 1;
        LinearLayout backLayout = findViewById(R.id.backlayout);
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
                backLayout.setBackgroundResource(R.drawable.img11);
                break;
        }
        // 启动时自动检查公告是否需要弹出
        fetchAnnouncement(data -> {
            String[] parts = data.split("\\|");
            if (parts.length >= 4) {
                String qq = parts[0];
                String timestamp = parts[1];
                String nickname = parts[2];
                String content = parts[3];

                if (!hasShown(timestamp)) {
                    showAnnouncement(qq, timestamp, nickname, content);
                    setShown(timestamp);
                }
            }
        });

        // 动态添加悬浮按钮
        addFloatingButton();
    }

    private void addFloatingButton() {
        TextView fab = new TextView(this);
        fab.setText("📢");
        fab.setTextSize(24);
        fab.setTextColor(Color.WHITE);
        fab.setBackgroundResource(R.drawable.select_button);
        fab.setPadding(20, 20, 20, 20);
        fab.setGravity(Gravity.CENTER);

// 设置布局在右下角
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMargins(30, 30, 30, 60);

        FrameLayout rootLayout = findViewById(android.R.id.content);
        rootLayout.addView(fab, params);

// 点击事件
        fab.setOnClickListener(v -> {
            fetchAnnouncement(data -> {
                String[] parts = data.split("\\|");
                if (parts.length >= 4) {
                    String qq = parts[0];
                    String timestamp = parts[1];
                    String nickname = parts[2];
                    String content = parts[3];
                    showAnnouncement(qq, timestamp, nickname, content);
                }
            });
        });

    }

    // 拉取公告文本
    private void fetchAnnouncement(Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(ANNOUNCEMENT_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = reader.readLine();
                reader.close();
                if (line != null) {
                    callback.onResult(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 判断是否已弹出过这个公告
    private boolean hasShown(String timestamp) {
        SharedPreferences prefs = getSharedPreferences("announcement", MODE_PRIVATE);
        return timestamp.equals(prefs.getString("last_shown", ""));
    }

    // 记录已经弹出过的公告时间戳
    private void setShown(String timestamp) {
        getSharedPreferences("announcement", MODE_PRIVATE)
                .edit()
                .putString("last_shown", timestamp)
                .apply();
    }

    @SuppressLint("SetTextI18n")
    private void showAnnouncement(String qq, String timestamp, String nickname, String content) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.RoundedDialog);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_announcement, null);

            // 初始化视图（保持之前的代码）...
            ShapeableImageView avatar = view.findViewById(R.id.avatar);
            TextView nameView = view.findViewById(R.id.name);
            TextView qqView = view.findViewById(R.id.qq_number);
            TextView timeView = view.findViewById(R.id.time);
            TextView contentView = view.findViewById(R.id.content);

            // 头像加载（注意去掉了 nk 后面的空格）...
            String avatarUrl = "https://q1.qlogo.cn/g?b=qq&nk=" + qq.trim() + "&s=640";
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .circleCrop()
                    .into(avatar);

            // 设置文本...
            nameView.setText(nickname);
            qqView.setText("QQ: " + qq);
            String formattedTime = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
                    .format(new Date(Long.parseLong(timestamp) * 1000L));
            timeView.setText("发布于 " + formattedTime);
            String processedContent = content.replace("\\r\\n", "\n").replace("\\n", "\n");
            contentView.setText(processedContent);

            // 创建对话框（不带按钮）
            AlertDialog dialog = builder
                    .setView(view)
                    .create();

            // 设置按钮点击事件（关键修改）
            view.findViewById(R.id.btn_confirm).setOnClickListener(v -> dialog.dismiss());

            view.findViewById(R.id.btn_post).setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(this, charts.class);
                intent.putExtra("url", "http://47.93.57.125:521/WebAPI/public/comment/gg/?bg="+randomNumber);
                startActivity(intent);
            });

            // 显示动画和透明背景...
            dialog.setOnShowListener(d -> {
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.getWindow().getDecorView().setAlpha(0f);
                    dialog.getWindow().getDecorView().animate()
                            .alpha(1f)
                            .setDuration(250)
                            .start();
                }
            });

            dialog.show();
        });
    }

    private void showPermissionHintDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("权限提示")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 再次检测权限，如果没权限则提示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showPermissionHintDialog("请授予“所有文件访问权限”以正常使用文件写入功能。");
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showPermissionHintDialog("请授予“存储权限”以正常使用文件写入功能。");
            }
        }
    }

    private void checkAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    // fallback for HarmonyOS or其他不支持的设备
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CODE_READ_EXTERNAL_STORAGE);
            }
        }
    }

    public void qiehuan(View view) {
        // 获取当前的主题模式
        int currentMode = AppCompatDelegate.getDefaultNightMode();

        // 判断当前模式并进行切换
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // 当前是深色模式，切换为浅色模式
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            // 当前是浅色模式，切换为深色模式
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    public void openRecommend(View view) {
        startActivity(new Intent(this, RecommendActivity.class));
    }

    public void chaxun(View view) {
        Intent intent = new Intent(this,scnlist.class);
        startActivity(intent);
    }

    public void jisuanqi(View view) {
        Intent intent = new Intent(this, worldscore.class);
        startActivity(intent);
    }

    public void personscore(View view) {
        Intent intent = new Intent(this, personscore.class);
        startActivity(intent);
    }
    public void thecharts(View view) {
        Intent intent = new Intent(this, thecharts.class);
        startActivity(intent);
    }
    public void scn(View view) {
        Intent intent = new Intent(this, scnlist.class);
        startActivity(intent);
    }

    public void author(View view) {
        Intent intent = new Intent(this, authors.class);
        startActivity(intent);
    }

    // 简单的回调接口定义
    interface Callback {
        void onResult(String data);
    }

    private class DownloadJsonTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
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
                    int scenarioId = Integer.parseInt(id);

                    for (int i = 0; i < scenariosArray.length(); i++) {
                        JSONObject scenarioObject = scenariosArray.getJSONObject(i);
                        int id = scenarioObject.getInt("id");

                        // 找到具有匹配id的场景
                        if (id == scenarioId) {
                            String fileName = scenarioObject.getString("fileName");
                            String downloadUrl = "http://47.93.57.125:521/AutoScenarios/" + fileName + ".zip";

                            // 开始下载场景ZIP文件
                            new DownloadScenarioTask1().execute(downloadUrl);
                            return;
                        }
                    }

                    // 如果找不到具有给定id的场景
                    Toast.makeText(MainActivity.this, "未找到场景", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(MainActivity.this, "下载场景数据失败", Toast.LENGTH_SHORT).show();
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

    private class DownloadScenarioTask1 extends AsyncTask<String, Integer, File> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
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
                // 将下载的文件重命名为.plagueinc
                String renamedFilePath = downloadedFile.getAbsolutePath().replace(".zip", ".plagueinc");
                File renamedFile = new File(renamedFilePath);

                if (downloadedFile.renameTo(renamedFile)) {
                    // 创建一个Intent来打开文件
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri fileUri = FileProvider.getUriForFile(MainActivity.this, "com.zmmxkid.pitool.fileprovider", renamedFile);
                    intent.setDataAndType(fileUri, "*/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.miniclip.plagueinc");
                    // 检查是否有可以处理该Intent的应用程序
                    if (true) {
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("注意：\n1.不是每个场景都能用手机游玩的，如遇问题请游玩其他场景\n2.请确保你把瘟疫公司从最近任务里划走(彻底处于未打开状态)才能正常游玩").setPositiveButton("开始游戏", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // 用户点击了确定按钮
                                    try {
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        if (ActivityNotFoundException.class.isInstance(e)) {
                                            Toast.makeText(MainActivity.this, "瘟疫公司未安装", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage(e.getMessage()).setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                    } else {
                        Toast.makeText(MainActivity.this, "瘟疫公司未安装", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "文件重命名失败", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(MainActivity.this, "下载场景文件失败", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }
    }

    private class CheckVersionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL("http://47.93.57.125:8888/down/10oLCfjinRJR");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                StringBuilder content = new StringBuilder();
                int bytesRead;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    content.append(new String(buffer, 0, bytesRead));
                }
                inputStream.close();

                double txtVersion = Double.parseDouble(content.toString());
                return txtVersion != version;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean shouldUpdate) {
            if (shouldUpdate) {
                Toast.makeText(MainActivity.this, "有可用更新", Toast.LENGTH_LONG).show();
                // Open the default browser with the update URL
                Uri updateUri = Uri.parse("http://47.93.57.125:8888/down/MOuOLRaJI0rE");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, updateUri);
                startActivity(browserIntent);
            }
        }
    }

}
