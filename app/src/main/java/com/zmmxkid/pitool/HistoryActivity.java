package com.zmmxkid.pitool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private TextView tvQQ, tvRank;
    private ImageView ivAvatar;
    private CardView pttBadge;
    private TextView tvPtt;

    // 启动入口
    public static void start(Context context, String qq) {
        Intent intent = new Intent(context, HistoryActivity.class);
        intent.putExtra("qq", qq);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        String qq = getIntent().getStringExtra("qq");
        if (qq == null) qq = "123456";
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
        initViews();
        loadPlayerHeader(qq);
        loadHistory(qq);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_history);
        tvQQ = findViewById(R.id.tv_qq);
        tvRank = findViewById(R.id.tv_rank);
        ivAvatar = findViewById(R.id.iv_avatar);
        pttBadge = findViewById(R.id.ptt_badge);
        tvPtt = findViewById(R.id.tv_ptt);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    // 加载玩家头部信息（头像、PTT等）
    private void loadPlayerHeader(String qq) {
        tvQQ.setText("QQ: " + qq);

        // QQ头像
        Glide.with(this)
                .load("https://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=640")
                .circleCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(ivAvatar);

        // PTT和排名通常从另一个接口获取(/Bests/qq.txt)，这里简化处理
        // 如需完整实现，可在此开线程加载Bests文件解析
        tvRank.setText("#--");
        pttBadge.setCardBackgroundColor(Color.parseColor("#333333"));
        tvPtt.setText("-");
        new LoadPlayerInfoTask().execute(qq);
    }

    // 加载历史记录
    private void loadHistory(String qq) {
        new Thread(() -> {
            try {
                URL url = new URL("http://47.93.57.125:521/WebAPI/public/history/" + qq + ".txt");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));

                List<HistoryItem> list = new ArrayList<>();
                String line;
                int seq = 1;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    HistoryItem item = parseLine(line, seq++);
                    if (item != null) list.add(item);
                }
                reader.close();

                // 倒序：最新的在上面
                Collections.reverse(list);

                runOnUiThread(() -> adapter.setData(list));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "加载历史记录失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // 解析单行历史记录
    private HistoryItem parseLine(String line, int seq) {
        try {
            line = line.trim();

            // 分割日期和主体
            int firstSpace = line.indexOf(' ');
            if (firstSpace == -1) return null;

            String dateStr = line.substring(0, firstSpace).trim();
            String dataPart = line.substring(firstSpace + 1).trim();
            // 分割日期和时间（找到"日"后面的内容）
            String datePart = dateStr;
            String timePart = "";
            int dayIdx = dateStr.indexOf('日');
            if (dayIdx != -1 && dayIdx < dateStr.length() - 1) {
                datePart = dateStr.substring(0, dayIdx + 1);  // 2023年6月26日
                timePart = dateStr.substring(dayIdx + 1);     // 10时22分 或 10时22分7秒
            }
            // 分割场景ID名称和后面的数据
            int firstComma = dataPart.indexOf(',');
            if (firstComma == -1) return null;

            String scenePart = dataPart.substring(0, firstComma).trim();
            String valuesPart = dataPart.substring(firstComma + 1).trim();

            // 解析场景ID和名称
            int underIdx = scenePart.indexOf('_');
            if (underIdx == -1) return null;

            int sceneId = Integer.parseInt(scenePart.substring(0, underIdx).trim());
            String sceneName = scenePart.substring(underIdx + 1).trim();

            // 创建对象
            HistoryItem item = new HistoryItem();
            item.sequence = seq;
            item.date = datePart;
            item.scenarioId = sceneId;
            item.scenarioName = sceneName;
            item.time = timePart;
// 使用正则提取所有 "数字→数字" 对（支持小数）
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)[→\\->](\\d+\\.?\\d*)");
            java.util.regex.Matcher matcher = pattern.matcher(valuesPart);

            int pairCount = 0;
            while (matcher.find()) {
                switch (pairCount) {
                    case 0: // 分数
                        item.scoreFrom = Long.parseLong(matcher.group(1));
                        item.scoreTo = Long.parseLong(matcher.group(2));
                        break;
                    case 1: // 单项排名 - 忽略
                        break;
                    case 2: // PTT（现在能正确匹配 0.48 了）
                        item.pttFrom = Float.parseFloat(matcher.group(1));
                        item.pttTo = Float.parseFloat(matcher.group(2));
                        break;
                    case 3: // 总分
                        item.totalFrom = Long.parseLong(matcher.group(1));
                        item.totalTo = Long.parseLong(matcher.group(2));
                        break;
                    case 4: // 排名
                        item.rankFrom = Integer.parseInt(matcher.group(1));
                        item.rankTo = Integer.parseInt(matcher.group(2));
                        break;
                    // DX Rating 忽略 (case 5)
                }
                pairCount++;
            }

            return item;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 数据模型
    public static class HistoryItem {
        int sequence;
        String date;   // 只保留日期部分：2023年6月26日
        String time;   // 只保留时间部分：10时22分（可能为空）
        int scenarioId;
        String scenarioName;
        long scoreFrom, scoreTo;
        float pttFrom, pttTo;
        long totalFrom, totalTo;
        int rankFrom, rankTo;
    }

    // Adapter
    public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        private List<HistoryItem> data = new ArrayList<>();
        private final int COLOR_GREEN;
        private final int COLOR_RED;
        private final int COLOR_BLUE;
        private final int COLOR_GRAY;

        public HistoryAdapter(Context context) {
            // 使用 Context 获取颜色，会自动选择 values 或 values-night
            COLOR_GREEN = context.getColor(R.color.positive_green);
            COLOR_RED = context.getColor(R.color.negative_red);
            COLOR_BLUE = context.getColor(R.color.ptt_blue);
            COLOR_GRAY = context.getColor(R.color.text_secondary);
        }

        public void setData(List<HistoryItem> list) {
            this.data = list;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = getLayoutInflater()
                    .inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            HistoryItem item = data.get(position);

            // 序号
            holder.tvSequence.setText("#" + item.sequence);

            // 分数（修正后）
            long scoreDiff = item.scoreTo - item.scoreFrom;
            holder.tvScore.setTextColor(scoreDiff > 0 ? COLOR_GREEN : (scoreDiff < 0 ? COLOR_RED : COLOR_GRAY));
            holder.tvScore.setText(formatNumber(item.scoreTo) +
                    (scoreDiff != 0 ? " (" + (scoreDiff > 0 ? "+" : "") + formatNumber(scoreDiff) + ")" : ""));

            // PTT（修正后）
            float pttDiff = item.pttTo - item.pttFrom;
            if (Math.abs(pttDiff) < 0.001) {
                holder.tvPtt.setText("PTT: KEEP");
                holder.tvPtt.setTextColor(COLOR_GRAY);
            } else {
                holder.tvPtt.setTextColor(COLOR_BLUE);
                holder.tvPtt.setText(String.format(Locale.US, "PTT: %.2f(%+.2f)", item.pttTo, pttDiff));
            }
            // 场景图标 (从服务器加载)
            Glide.with(HistoryActivity.this)
                    .load("http://47.93.57.125:521/WebAPI/public/icons/" + item.scenarioId + ".png")
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.ivScene);

            // 场景名
            holder.tvSceneName.setText(item.scenarioName);

            // 日期
            holder.tvDate.setText(item.date);
            if (item.time != null && !item.time.isEmpty()) {
                holder.tvTime.setText(item.time);
                holder.tvTime.setVisibility(android.view.View.VISIBLE);
            } else {
                holder.tvTime.setVisibility(android.view.View.GONE);
            }


            // 总分变化
            long totalDiff = item.totalTo - item.totalFrom;
            holder.tvTotal.setText("总分: " + formatNumber(item.totalTo) +
                    (totalDiff != 0 ? " (" + (totalDiff > 0 ? "+" : "") + formatNumber(totalDiff) + ")" : ""));
            holder.tvTotal.setTextColor(totalDiff > 0 ? COLOR_GREEN : COLOR_GRAY);
            // 点击跳转场景详情（对应 scnlist 中的 listView.setOnItemClickListener）
            holder.itemView.setOnClickListener(v -> {
                // 获取场景ID（对应 scnlist 中的 String firstData = data[0]）
                String scenarioId = String.valueOf(item.scenarioId);

                // 创建 Intent 跳转到场景详情页（假设为 scnperson）
                Intent intent = new Intent(HistoryActivity.this, scnperson.class);
                intent.putExtra("key", scenarioId);  // 对应 scnlist 中的 putExtra("key", firstData)

                startActivity(intent);
            });

            // 场景图标单独点击也可跳转（同 web 版逻辑）
            holder.ivScene.setOnClickListener(v -> {
                Intent intent = new Intent(HistoryActivity.this, scnperson.class);
                intent.putExtra("key", String.valueOf(item.scenarioId));
                startActivity(intent);
            });
            // 难度类别标签（如果有数据可启用）
            // holder.tvCategory.setText("FTR 9");
            // holder.tvCategory.setBackgroundColor(Color.parseColor("#bb11bb"));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private String formatNumber(long num) {
            return String.format("%,d", num);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSequence, tvSceneName, tvDate, tvTime, tvScore, tvPtt, tvTotal, tvCategory;
            DiagonalCutImageView ivScene;

            ViewHolder(android.view.View itemView) {
                super(itemView);
                tvSequence = itemView.findViewById(R.id.tv_sequence);
                ivScene = itemView.findViewById(R.id.iv_scene);
                tvSceneName = itemView.findViewById(R.id.tv_scene_name);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvScore = itemView.findViewById(R.id.tv_score);
                tvPtt = itemView.findViewById(R.id.tv_ptt);
                tvTime = itemView.findViewById(R.id.tv_time);
                tvTotal = itemView.findViewById(R.id.tv_total);
                tvCategory = itemView.findViewById(R.id.tv_category);

                // 点击跳转场景详情（可选）
                itemView.setOnClickListener(v -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        HistoryItem item = data.get(pos);
                        // 可在此处启动场景详情页
                        // ScenarioActivity.start(HistoryActivity.this, item.scenarioId);
                    }
                });
            }
        }
    }

    // 在 HistoryActivity 中添加这个内部类和数据类
    private static class PlayerInfo {
        String name;
        float ptt;
        long totalScore;
        int rating;
        int rank;
    }

    private class LoadPlayerInfoTask extends AsyncTask<String, Void, PlayerInfo> {
        @Override
        protected PlayerInfo doInBackground(String... params) {
            String qq = params[0];
            PlayerInfo info = new PlayerInfo();

            try {
                // 修正 URL（移除空格）
                URL url = new URL("http://47.93.57.125:521/WebAPI/public/Bests/" + qq + ".txt");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setConnectTimeout(10000);
                conn.connect();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String line = reader.readLine();

                if (line != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        info.name = parts[0];
                        info.ptt = Float.parseFloat(parts[1]);
                        info.totalScore = Long.parseLong(parts[2]);
                        info.rating = Integer.parseInt(parts[3]);
                        info.rank = Integer.parseInt(parts[4]);
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return info;
        }

        @Override
        protected void onPostExecute(PlayerInfo info) {
            if (info.name == null) return;

            // 设置 PTT 数值
            tvPtt.setText(String.format(Locale.US, "%.2f", info.ptt));
            tvRank.setText("#" + info.rank);

            // 设置 PTT 徽章背景和特效（从你的 personview 移植）
            int bgRes;
            int shadowColor = 0;
            boolean needShadow = false;

            if (info.ptt < 0.1) {
                bgRes = R.drawable.rating_off;
            } else if (info.ptt < 3.5) {
                bgRes = R.drawable.rating_1;
            } else if (info.ptt < 7) {
                bgRes = R.drawable.rating_2;
            } else if (info.ptt < 10) {
                bgRes = R.drawable.rating_3;
            } else if (info.ptt < 11) {
                bgRes = R.drawable.rating_4;
            } else if (info.ptt < 12) {
                bgRes = R.drawable.rating_5;
            } else if (info.ptt < 12.5) {
                bgRes = R.drawable.rating_6;
            } else if (info.ptt < 13) {
                bgRes = R.drawable.rating_7;
            } else if (info.ptt < 13.5) {
                bgRes = R.drawable.rating_9;
                shadowColor = Color.rgb(255, 215, 0); // 金色
                needShadow = true;
            } else {
                bgRes = R.drawable.rating_10;
                shadowColor = Color.rgb(138, 43, 226); // 紫色
                needShadow = true;
            }

            // 应用背景到 PTT 徽章 CardView
            pttBadge.setBackgroundResource(bgRes);

            // 设置发光效果（13.00+）
            if (needShadow) {
                tvPtt.setShadowLayer(3, 0, 0, shadowColor);
            }

        }
    }
}