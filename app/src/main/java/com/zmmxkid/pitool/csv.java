package com.zmmxkid.pitool;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class csv extends Activity {


    private static final int REQUEST_READ_STORAGE_PERMISSION = 100;
    private static final int PICK_FILE_REQUEST_CODE = 101;


    public boolean checkStoragePermission() {
        // 检查是否已经授予READ_EXTERNAL_STORAGE权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有权限，请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csv);

        Button button12 = findViewById(R.id.button12);
        button12.setOnClickListener(v -> daoru());
    }


    public void daoru() {
        if (checkStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // 可以指定文件类型，例如 "text/plain" 只选择文本文件
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            // 设置初始目录
            Uri uri = Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/瘟疫公司存档");
            intent.setData(uri);

            startActivityForResult(Intent.createChooser(intent, "选择文件"), PICK_FILE_REQUEST_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData(); // 获取用户选择的文件URI
                if (uri != null) {
                    String filePath = uri.getPath(); // 获取文件路径
                    if (filePath != null && (filePath.endsWith(".k3a"))) {
                        // 处理 CSV 或 TXT 文件
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            File destinationFile = new File(getFilesDir(), "copiedFile.txt");
                            FileOutputStream outputStream = new FileOutputStream(destinationFile);
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                            inputStream.close();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "复制文件时出错", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "请选择.k3a存档文件", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }


}
