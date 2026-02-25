package com.zmmxkid.pitool;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FloatingViewService extends Service {
    private static final String TAG = "FloatingService";
    private final String watchPath = "/storage/emulated/0/PlagueIncSaves/original/quicksave.k3a";
    private final String saveDirPath = "/storage/emulated/0/PlagueIncSaves";

    private WindowManager windowManager;
    private View collapsedView;
    private View expandedView;
    private boolean isExpanded = false;
    private FileObserver fileObserver;
    private long lastHandledTime = 0;
    private String thestart;

    private boolean isRestoring = false;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        collapsedView = LayoutInflater.from(this).inflate(R.layout.floating_collapsed, null);
        expandedView = LayoutInflater.from(this).inflate(R.layout.floating, null);
//        expandedView.setLayoutParams(new FrameLayout.LayoutParams(dpToPx(30), dpToPx(40)));

// 收回按钮，改为销毁悬浮窗
        Button button25 = expandedView.findViewById(R.id.button25);
        button25.setOnClickListener(v -> {
            try {
                // 完全移除 expandedView 或 collapsedView
                if (isExpanded) {
                    windowManager.removeView(expandedView);
                } else {
                    windowManager.removeView(collapsedView);
                }

                // 停止服务，销毁悬浮窗
                stopSelf();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        LinearLayout outerLayout = expandedView.findViewById(R.id.backlayout2);
        outerLayout.setOnClickListener(v -> {
            if (isExpanded) {
                params.width = WindowManager.LayoutParams.WRAP_CONTENT;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                windowManager.removeView(expandedView);
                windowManager.addView(collapsedView, params);
                isExpanded = false;
            }
        });
        // 恢复按钮
        Button button26 = expandedView.findViewById(R.id.button26);
        button26.setOnClickListener(v -> {
            ListView listView = expandedView.findViewById(R.id.listView);
            SaveFileAdapter adapter = (SaveFileAdapter) listView.getAdapter();
            String selected = adapter.getSelectedFilename();
            if (selected == null) {
                showCustomToast("请先选择一个存档文件");
                return;
            }

            // 移除扩展名
            String filenameWithoutExt = selected.replaceFirst("\\.k3a$", "");

            // 分割文件名
            String[] parts = filenameWithoutExt.split("-");

            if (parts.length >= 2) {
                // 有两个及以上的部分，取前两段组合
                thestart = parts[0] + "-" + parts[1];
            } else {
                // 否则取整个文件名（无扩展名）
                thestart = filenameWithoutExt;
            }

            // 可选：打印日志查看
            Log.d(TAG, "提取到的thestart: " + thestart);

            File source = new File(saveDirPath, selected);
            File target = new File(saveDirPath + "/original", "quicksave.k3a");

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    isRestoring = true;  // 设置为正在恢复
                    Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    showCustomToast("成功！游戏设置中可回溯到该存档点\n存档系统已视为您在游玩该存档场景");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        isRestoring = false;
                        Log.d(TAG, "恢复文件完成，重新允许监听");
                    }, 1500);
                }
            } catch (Exception e) {
                Log.e(TAG, "恢复存档失败", e);
                showCustomToast("恢复失败:");
            }
        });

        // 删除按钮
        Button button28 = expandedView.findViewById(R.id.button28);
        button28.setOnClickListener(v -> {
            ListView listView = expandedView.findViewById(R.id.listView);
            SaveFileAdapter adapter = (SaveFileAdapter) listView.getAdapter();
            String selected = adapter.getSelectedFilename();
            if (selected == null) {
                showCustomToast("请先选择一个存档文件");
                return;
            }
            File file = new File(saveDirPath, selected);
            if (file.exists() && file.delete()) {
                showCustomToast("删除成功");

                updateSaveList();
            } else {
                showCustomToast("删除失败");

            }
        });

// 重命名按钮
        Button button27 = expandedView.findViewById(R.id.button27);
        button27.setOnClickListener(v -> {
            ListView listView = expandedView.findViewById(R.id.listView);
            SaveFileAdapter adapter = (SaveFileAdapter) listView.getAdapter();
            String selected = adapter.getSelectedFilename();
            if (selected == null) {
                showCustomToast("请先选择一个存档文件");

                return;
            }

            if (!selected.endsWith(".k3a")) {
                showCustomToast("文件扩展名必须为.k3a");

                return;
            }

            String baseName = selected.substring(0, selected.length() - 4); // 去掉 .k3a
            String[] parts = baseName.split("-", 2); // 注意：只分割前两个部分

            boolean hasPrefix = false;
            String prefixPart = "";
            String editablePart;

            if (parts.length == 2 && parts[0].matches("\\d+")) {
                // 有序号，剩余部分是可编辑的
                hasPrefix = true;
                prefixPart = parts[0];
                editablePart = parts[1]; // 剩下的全交给用户编辑（无论是否包含“-”）
            } else {
                // 无序号，整个都可以编辑
                editablePart = baseName;
            }

            File oldFile = new File(saveDirPath, selected);
            showRenameDialog(editablePart, prefixPart, hasPrefix, oldFile);
        });


        // 拖动展开
        collapsedView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private long touchStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchStartTime = System.currentTimeMillis();
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(collapsedView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - touchStartTime < 200) {
                            if (!isExpanded) {
                                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                                params.height = WindowManager.LayoutParams.MATCH_PARENT;
                                windowManager.removeView(collapsedView);
                                windowManager.addView(expandedView, params);

                                isExpanded = true;
                                updateSaveList();
                            }
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(collapsedView, params);

        startMonitoring();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            thestart = intent.getStringExtra("key");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (collapsedView != null) windowManager.removeView(collapsedView);
            if (expandedView != null) windowManager.removeView(expandedView);
        } catch (Exception ignored) {
        }

        if (fileObserver != null) {
            fileObserver.stopWatching();
            fileObserver = null;
        }
    }

    private void showRenameDialog(String editablePart, String prefixPart, boolean hasPrefix, File oldFile) {
        // 获取 WindowManager
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 创建全屏透明外层容器
        FrameLayout outerLayout = new FrameLayout(this);
        outerLayout.setBackgroundColor(0x00000000); // 完全透明
        outerLayout.setClickable(true); // 能够接收点击事件

        // 设置点击空白区域关闭弹窗
        outerLayout.setOnClickListener(v -> {
            try {
                wm.removeView(outerLayout);
            } catch (Exception ignored) {
            }
        });

        // 内部弹窗布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(0xA0FFFFFF);
        layout.setClickable(true); // 阻止点击事件继续冒泡到 outerLayout

        // 编辑框
        EditText input = new EditText(this);
        input.setText(editablePart);
        input.setTextColor(Color.BLACK);
        input.setSingleLine();
        layout.addView(input);

        // 确定按钮
        Button confirm = new Button(this);
        confirm.setText("确定");
        layout.addView(confirm);

        // 取消按钮
        Button cancel = new Button(this);
        cancel.setText("取消");
        layout.addView(cancel);

        // 将 layout 添加到 outerLayout 中居中显示
        FrameLayout.LayoutParams innerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        innerParams.gravity = Gravity.CENTER;
        outerLayout.addView(layout, innerParams);

        // 设置 WindowManager 参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
        params.gravity = Gravity.CENTER;

        // 添加到窗口
        wm.addView(outerLayout, params);

        // 显示输入法
        input.requestFocus();
        input.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
            }
        }, 100);

        // 确定按钮处理
        confirm.setOnClickListener(v -> {
            String newNamePart = input.getText().toString().trim();
            if (newNamePart.isEmpty()) {
                showCustomToast("名称不能为空");

                return;
            }

            String newFilename = hasPrefix
                    ? prefixPart + "-" + newNamePart + ".k3a"
                    : newNamePart + ".k3a";

            File newFile = new File(saveDirPath, newFilename);
            if (newFile.exists()) {
                showCustomToast("新文件名已存在");

            } else if (oldFile.renameTo(newFile)) {
                showCustomToast("重命名成功");

                updateSaveList();
            } else {
                showCustomToast("重命名失败");
            }

            wm.removeView(outerLayout);
        });

        cancel.setOnClickListener(v -> {
            wm.removeView(outerLayout);
        });
    }


    private void updateSaveList() {
        File folder = new File(saveDirPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".k3a"));
        List<File> fileList = new ArrayList<>();
        if (files != null) {
            fileList.addAll(Arrays.asList(files));
            fileList.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        }

        List<String> fileNames = new ArrayList<>();
        for (File file : fileList) {
            fileNames.add(file.getName());
        }

        ListView listView = expandedView.findViewById(R.id.listView);
        SaveFileAdapter adapter = new SaveFileAdapter(this, fileNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            adapter.setSelectedPosition(position);
        });
    }

    private void startMonitoring() {
        File watchFile = new File(watchPath);
        File parentDir = watchFile.getParentFile();

        if (parentDir == null || !parentDir.exists()) {
            Log.e(TAG, "监控目录不存在: " + watchPath);
            return;
        }

        File saveDir = new File(saveDirPath);
        if (!saveDir.exists()) saveDir.mkdirs();

        fileObserver = new FileObserver(parentDir.getAbsolutePath(), FileObserver.CLOSE_WRITE | FileObserver.MODIFY) {
            @Override
            public void onEvent(int event, String path) {
                if (path == null || !path.equals("quicksave.k3a")) return;

                if (isRestoring) {
                    Log.d(TAG, "正在恢复，不处理这次文件变动");
                    return;
                }


                long now = System.currentTimeMillis();
                if (now - lastHandledTime < 2000) return;
                lastHandledTime = now;

                File srcFile = new File(watchPath);
                @SuppressLint("SimpleDateFormat") String formattedTime = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date(now));
                String safeStart = thestart.replaceAll("[:\\\\/*?\"<>|]", "_");
                File destFile = new File(saveDirPath, safeStart + "-" + formattedTime + ".k3a");

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        new Handler(getMainLooper()).post(() -> updateSaveList());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "复制失败", e);
                }
            }
        };
        fileObserver.startWatching();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void showCustomToast(String message) {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 创建 TextView
        TextView toastView = new TextView(this);
        toastView.setText(message);
        toastView.setTextColor(Color.WHITE);
        toastView.setTextSize(16);
        toastView.setPadding(40, 20, 40, 20);
        toastView.setGravity(Gravity.CENTER);

        // 圆角背景
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(Color.parseColor("#CC333333")); // 半透明黑
        bgDrawable.setCornerRadius(30);
        toastView.setBackground(bgDrawable);
        toastView.setAlpha(0f); // 初始透明

        // 添加到窗口的参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.y = 200;

        // 添加视图
        wm.addView(toastView, params);

        // 淡入动画
        toastView.animate()
                .alpha(1f)
                .setDuration(300)
                .start();

        // 延迟后淡出并移除
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            toastView.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        // 设置不可见，避免一瞬间重绘或动画冲突造成闪烁
                        toastView.setVisibility(View.GONE);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            try {
                                wm.removeView(toastView);
                            } catch (Exception e) {
                                Log.e("CustomToast", "移除失败", e);
                            }
                        }, 50); // 多等一点点再移除视图
                    })
                    .start();
        }, 2000);
    }


    public static class SaveFileAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final List<String> filenames;
        private int selectedPosition = -1;

        public SaveFileAdapter(Context context, List<String> filenames) {
            super(context, R.layout.list_item_save, filenames);
            this.context = context;
            this.filenames = filenames;
        }

        public void setSelectedPosition(int position) {
            this.selectedPosition = position;
            notifyDataSetChanged();
        }

        public String getSelectedFilename() {
            return selectedPosition >= 0 && selectedPosition < filenames.size() ?
                    filenames.get(selectedPosition) : null;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_save, parent, false);
            }

            String fileName = filenames.get(position);
            TextView textView = convertView.findViewById(R.id.textView27);
            ImageView imageView = convertView.findViewById(R.id.iconlogo);

            textView.setText(fileName);
            textView.setTextColor(Color.BLACK);

            // 设置背景或高亮选中状态
            if (position == selectedPosition) {
                convertView.setBackgroundColor(0xA0FFFFFF);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

            // 提取前缀数字（例如“363-xxx.k3a”）
            String prefix = null;
            if (fileName.contains("-")) {
                String candidate = fileName.split("-")[0];
                if (candidate.matches("\\d+")) {
                    prefix = candidate;
                }
            }

            // 设置图标（如果有合法前缀）
            if (prefix != null) {
                File iconFile = new File(context.getFilesDir(), "icons/" + prefix + ".png");
                if (iconFile.exists()) {
                    imageView.setImageURI(Uri.fromFile(iconFile));
                } else {
                    imageView.setImageResource(R.mipmap.logo); // 默认图标
                }
            } else {
                imageView.setImageResource(R.mipmap.logo); // 无合法前缀时使用默认图标
            }

            return convertView;
        }

    }
}
