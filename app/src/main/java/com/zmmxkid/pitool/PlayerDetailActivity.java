package com.zmmxkid.pitool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class PlayerDetailActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvQQ, tvUserName, tvTotalScore, tvTotalRank;
    private CardView rankBadge;
    private RecyclerView recyclerView;
    private SceneScoreAdapter adapter;
    private ConstraintLayout rootLayout;

    public static void start(Context context, String qq) {
        Intent intent = new Intent(context, PlayerDetailActivity.class);
        intent.putExtra("qq", qq);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_detail);

        String qq = getIntent().getStringExtra("qq");
        if (qq == null) {
            Toast.makeText(this, "QQ号不能为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ConstraintLayout backLayout = findViewById(R.id.root_layout); // 假设你有一个 LinearLayout 用作背景
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
        loadData(qq);
    }

    private void initViews() {
        rootLayout = findViewById(R.id.root_layout);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvQQ = findViewById(R.id.tv_qq);
        tvUserName = findViewById(R.id.tv_user_name);
        tvTotalScore = findViewById(R.id.tv_total_score);
        tvTotalRank = findViewById(R.id.tv_total_rank);
        rankBadge = findViewById(R.id.rank_badge);
        recyclerView = findViewById(R.id.recycler_scenes);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SceneScoreAdapter();
        recyclerView.setAdapter(adapter);

    }

    private void loadData(String qq) {
        new LoadPlayerDataTask().execute(qq);
    }

    private String formatNumber(long num) {
        return String.format("%,d", num);
    }

    private class LoadPlayerDataTask extends AsyncTask<String, Void, PlayerDataResult> {
        @Override
        protected PlayerDataResult doInBackground(String... params) {
            String qq = params[0];
            PlayerDataResult result = new PlayerDataResult();
            result.qq = qq;
            result.items = new ArrayList<>();

            Map<String, Integer> categoryCounters = new HashMap<>();
            categoryCounters.put("MainGame", 0);
            categoryCounters.put("Scenario", 0);
            categoryCounters.put("SpeedRun", 0);
            categoryCounters.put("Cure", 0);

            try {
                // 获取 vanillaQuery
                URL vanillaUrl = new URL("http://47.93.57.125:4321/vanillaQuery?qq=" + qq);
                HttpURLConnection vanillaConn = (HttpURLConnection) vanillaUrl.openConnection();
                vanillaConn.setRequestMethod("GET");
                vanillaConn.setConnectTimeout(10000);
                vanillaConn.setReadTimeout(10000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(vanillaConn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject vanillaJson = new JSONObject(sb.toString());

                result.userName = vanillaJson.optString("UserName", "未知玩家");
                result.totalRankScore = vanillaJson.optLong("TotalRankScore", 0);
                result.totalRank = vanillaJson.optInt("TotalRank", 0);
                result.totalPlayers = vanillaJson.optInt("TotalPlayers", 0);

                if (vanillaJson.has("MainGame")) {
                    JSONObject mainGame = vanillaJson.getJSONObject("MainGame");
                    parseCategory(mainGame, result.items, "MainGame", categoryCounters);
                }

                if (vanillaJson.has("Scenario")) {
                    JSONObject scenario = vanillaJson.getJSONObject("Scenario");
                    parseCategory(scenario, result.items, "Scenario", categoryCounters);
                }

                if (vanillaJson.has("SpeedRun")) {
                    JSONObject speedRun = vanillaJson.getJSONObject("SpeedRun");
                    parseCategory(speedRun, result.items, "SpeedRun", categoryCounters);
                }

                // 获取 Cure
                URL cureUrl = new URL("http://47.93.57.125:4321/cureQuery?qq=" + qq);
                HttpURLConnection cureConn = (HttpURLConnection) cureUrl.openConnection();
                cureConn.setRequestMethod("GET");
                cureConn.setConnectTimeout(10000);
                cureConn.setReadTimeout(10000);

                BufferedReader cureReader = new BufferedReader(
                        new InputStreamReader(cureConn.getInputStream()));
                StringBuilder cureSb = new StringBuilder();
                while ((line = cureReader.readLine()) != null) {
                    cureSb.append(line);
                }
                cureReader.close();

                JSONObject cureJson = new JSONObject(cureSb.toString());

                if (cureJson.has("Cure")) {
                    JSONObject cure = cureJson.getJSONObject("Cure");
                    parseCategory(cure, result.items, "Cure", categoryCounters);
                }

            } catch (Exception e) {
                e.printStackTrace();
                result.error = e.getMessage();
            }
            return result;
        }

        private void parseCategory(JSONObject categoryObj, List<SceneScoreItem> list,
                                   String categoryName, Map<String, Integer> counters) {
            Iterator<String> keys = categoryObj.keys();
            while (keys.hasNext()) {
                String sceneName = keys.next();
                try {
                    JSONObject sceneData = categoryObj.getJSONObject(sceneName);
                    long score = sceneData.optLong("Score", 0);
                    int rank = sceneData.optInt("Rank", 0);
                    int total = sceneData.optInt("TotalPlayers", 0);

                    int currentIndex = counters.get(categoryName) + 1;
                    counters.put(categoryName, currentIndex);

                    list.add(new SceneScoreItem(sceneName, score, rank, total, categoryName, currentIndex));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(PlayerDataResult result) {
            if (result.error != null) {
                Toast.makeText(PlayerDetailActivity.this, "加载失败: " + result.error, Toast.LENGTH_SHORT).show();
                return;
            }

            tvQQ.setText("QQ: " + result.qq);
            tvUserName.setText(result.userName);
            tvTotalScore.setText("总积分: " + formatNumber(result.totalRankScore));
            tvTotalRank.setText("总排名: " + result.totalRank + " / " + result.totalPlayers);

            Glide.with(PlayerDetailActivity.this)
                    .load("https://q.qlogo.cn/g?b=qq&nk=" + result.qq + "&s=640")
                    .circleCrop()
                    .placeholder(R.mipmap.logo_round)
                    .into(ivAvatar);

            setupRankBadge(result.totalRank);
            adapter.setData(result.items);
        }

        private void setupRankBadge(int rank) {
            int borderColor;
            if (rank == 1) borderColor = Color.parseColor("#FFD700");
            else if (rank == 2) borderColor = Color.parseColor("#C0C0C0");
            else if (rank == 3) borderColor = Color.parseColor("#C57E47");
            else if (rank <= 20) borderColor = Color.parseColor("#FF0000");
            else if (rank <= 100) borderColor = Color.parseColor("#00FFFF");
            else if (rank <= 300) borderColor = Color.parseColor("#FFA500");
            else borderColor = Color.parseColor("#434343");

            TextView badgeText = findViewById(R.id.tv_rank_badge_text);
            if (badgeText != null) {
                badgeText.setText("#" + rank);
                badgeText.setTextColor(borderColor);
            }
        }
    }

    private static class PlayerDataResult {
        String qq;
        String userName;
        long totalRankScore;
        int totalRank;
        int totalPlayers;
        List<SceneScoreItem> items;
        String error;
    }

    public class SceneScoreAdapter extends RecyclerView.Adapter<SceneScoreAdapter.ViewHolder> {
        private List<SceneScoreItem> data = new ArrayList<>();

        public void setData(List<SceneScoreItem> list) {
            this.data = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_scene_score, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SceneScoreItem item = data.get(position);

            holder.tvSequence.setText("#" + item.categoryIndex);

            String label = item.getCategoryLabel();
            holder.tvCategoryLabel.setText(label);
            holder.tvCategoryLabel.setTextColor(item.getCategoryColor());

            holder.tvSceneName.setText(item.sceneName);
            holder.tvDetail.setText("排名: " + item.rank + " / " + item.totalPlayers);
            holder.tvScore.setText(formatNumber(item.score));

            String percentage = item.getPercentage();
            holder.tvPercentage.setText(percentage + "%");
            holder.tvPercentage.setTextColor(item.getColorByPercentage());

            if (item.rank == 1) {
                holder.tvPercentage.setText("👑 " + percentage + "%");
            }

            holder.categoryIndicator.setBackgroundColor(item.getCategoryColor());

            Glide.with(PlayerDetailActivity.this)
                    .load(item.iconUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.mipmap.logo_round)
                    .error(R.mipmap.logo_round)
                    .centerCrop()
                    .into(holder.ivScene);

            // ===== 点击跳转逻辑（参考 worldscore.java）=====
            holder.itemView.setOnClickListener(v -> {
                String sceneName = item.sceneName;
                String category = item.category;

                Intent intent = new Intent(PlayerDetailActivity.this, mainperson.class);

                // 根据分类处理场景名映射（特别是剧情模式）
                if ("Scenario".equals(category)) {
                    // 剧情模式字符串映射（参考 worldscore.java 的 juqing 方法）
                    if (sceneName.equals("虚假消息")) sceneName = "虚假信息";
                    else if (sceneName.equals("地旷人稀")) sceneName = "地广人稀";
                    else if (sceneName.equals("遥传")) sceneName = "谣传";
                    else if (sceneName.equals("立百病毒")) sceneName = "尼帕病毒";
                    else if (sceneName.equals("全球暖化")) sceneName = "全球变暖";
                    else if (sceneName.equals("火山喷发")) sceneName = "火山爆发";
                    else if (sceneName.equals("颠倒乾坤")) sceneName = "乾坤颠倒";
                    else if (sceneName.equals("圣诞老人的小助手")) sceneName = "圣诞蠕虫";

                    intent.putExtra("key", sceneName);
                    intent.putExtra("key1", "剧情");
                } else if ("MainGame".equals(category)) {
                    intent.putExtra("key", sceneName);
                    intent.putExtra("key1", "主游戏");
                } else if ("SpeedRun".equals(category)) {
                    intent.putExtra("key", sceneName);
                    intent.putExtra("key1", "急速");
                } else if ("Cure".equals(category)) {
                    intent.putExtra("key", sceneName);
                    intent.putExtra("key1", "解药");
                }

                startActivity(intent);
            });

            // 图标点击也可跳转（与 worldscore 逻辑一致）
            holder.ivScene.setOnClickListener(v -> {
                holder.itemView.performClick(); // 触发上面的点击事件
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSequence, tvCategoryLabel, tvSceneName, tvDetail, tvScore, tvPercentage;
            DiagonalCutImageView ivScene;
            View categoryIndicator;

            ViewHolder(View itemView) {
                super(itemView);
                tvSequence = itemView.findViewById(R.id.tv_sequence);
                tvCategoryLabel = itemView.findViewById(R.id.tv_category_label);
                tvSceneName = itemView.findViewById(R.id.tv_scene_name);
                tvDetail = itemView.findViewById(R.id.tv_detail);
                tvScore = itemView.findViewById(R.id.tv_score);
                tvPercentage = itemView.findViewById(R.id.tv_percentage);
                ivScene = itemView.findViewById(R.id.iv_scene);
                categoryIndicator = itemView.findViewById(R.id.category_indicator);
            }
        }
    }
}