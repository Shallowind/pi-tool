package com.zmmxkid.pitool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static com.zmmxkid.pitool.MainActivity.randomNumber;
import static com.zmmxkid.pitool.personscore.PREF_QQ;
import static com.zmmxkid.pitool.personscore.PREFS_NAME;

public class RecommendActivity extends AppCompatActivity {

    private static final String SCEN_LIST_URL = "http://47.93.57.125:521/WebAPI/public/scenInfo/scenList.txt";

    private LinearLayout listContainer;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        ConstraintLayout back = findViewById(R.id.backlayout);
        switch (randomNumber) {
            case 1:
                back.setBackgroundResource(R.drawable.img11);
                break;
            case 2:
                back.setBackgroundResource(R.drawable.img22);
                break;
            case 3:
                back.setBackgroundResource(R.drawable.img33);
                break;
            case 4:
                back.setBackgroundResource(R.drawable.img44);
                break;
            case 5:
                back.setBackgroundResource(R.drawable.img55);
                break;
            default:
                back.setBackgroundResource(R.drawable.img11);
                break;
        }

        listContainer = findViewById(R.id.listRecommend);
        progressBar = findViewById(R.id.progressRecommend);
        scrollView = findViewById(R.id.scrollRecommend);

        findViewById(R.id.btnRefreshRecommend).setOnClickListener(this::onRefreshClick);

        String qq = resolveQq();
        if (qq.isEmpty()) {
            showBindDialog();
        } else {
            loadRecommendations(qq);
        }
    }

    private String resolveQq() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String qq = sp.getString(PREF_QQ, "");
        if (qq != null && !qq.trim().isEmpty()) {
            return qq.trim();
        }
        File f = new File(getExternalFilesDir(null), "QQ.txt");
        if (f.exists()) {
            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                String line = r.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    return line.trim();
                }
            } catch (IOException ignored) {
            }
        }
        return "";
    }

    private void showBindDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_bind_qq_recommend, null);
        EditText et = v.findViewById(R.id.edit_qq_bind);

        String hint = resolveQq();
        if (!hint.isEmpty()) et.setText(hint);

        AlertDialog dialog = new AlertDialog.Builder(this) // ✅ 去掉自定义style
                .setTitle("绑定 QQ")
                .setMessage("场景推荐需要读取您在场景库中的成绩，请先绑定 QQ 号（与场景库一致）。")
                .setView(v)
                .setPositiveButton("绑定并加载", null)
                .setNegativeButton("取消", (d, w) -> finish())
                .create();

        dialog.setOnShowListener(d -> {

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_bg);
            }

            Button pos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button neg = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (pos != null) {
                pos.setTextColor(getResources().getColor(R.color.ptt_blue, null));
                pos.setOnClickListener(v1 -> {
                    String qq = et.getText().toString().trim();
                    if (qq.isEmpty()) {
                        Toast.makeText(this, "请输入 QQ 号", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pos.setEnabled(false);
                    new BindQqTask(qq, () -> {
                        dialog.dismiss();
                        loadRecommendations(qq);
                    }, () -> pos.setEnabled(true)).execute();
                });
            }

            if (neg != null) {
                neg.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
        });

        dialog.show();
    }

    /** 与 personscore 中下载 Bests 逻辑一致 */
    private class BindQqTask extends AsyncTask<Void, Void, Boolean> {
        private final String qq;
        private final Runnable onSuccess;
        private final Runnable onFinally;

        BindQqTask(String qq, Runnable onSuccess, Runnable onFinally) {
            this.qq = qq;
            this.onSuccess = onSuccess;
            this.onFinally = onFinally;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL("http://47.93.57.125:521/WebAPI/public/Bests/" + qq + ".txt");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                reader.close();
                c.disconnect();
                String response = sb.toString().trim();
                if (response.isEmpty()) return false;

                File out = new File(getExternalFilesDir(null), qq + ".txt");
                FileOutputStream fos = new FileOutputStream(out);
                fos.write(response.getBytes());
                fos.close();

                File qqFile = new File(getExternalFilesDir(null), "QQ.txt");
                FileOutputStream qfos = new FileOutputStream(qqFile);
                qfos.write(qq.getBytes());
                qfos.close();

                SharedPreferences.Editor ed = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                ed.putString(PREF_QQ, qq);
                ed.apply();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            if (onFinally != null) onFinally.run();
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(RecommendActivity.this, "账号绑定成功", Toast.LENGTH_SHORT).show();
                if (onSuccess != null) onSuccess.run();
            } else {
                Toast.makeText(RecommendActivity.this, "该QQ不存在或暂无数据，请到相应QQ群输入相关指令", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onRefreshClick(View v) {
        String qq = resolveQq();
        if (qq.isEmpty()) {
            showBindDialog();
            return;
        }
        loadRecommendations(qq);
    }

    private void loadRecommendations(String qq) {
        listContainer.removeAllViews();
        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
        new LoadRecommendTask(qq).execute();
    }

    private static int resolveTypeColor(String type) {
        if (type == null) return Color.WHITE;
        switch (type.toUpperCase(Locale.ROOT)) {
            case "PST":
                return Color.parseColor("#6BCB77");
            case "PRS":
                return Color.parseColor("#4D96FF");
            case "FTR":
                return Color.parseColor("#A66CFF");
            case "ETR":
                return Color.parseColor("#FF8A3D");
            case "BYD":
            case "MXM":
                return Color.parseColor("#FF5C8A");
            default:
                return Color.parseColor("#D7E3FF");
        }
    }

private CharSequence buildMetaText(RecommendationEngine.RecommendationItem rec) {
    String type = rec.scenario.type == null ? "" : rec.scenario.type.trim().toUpperCase(Locale.ROOT);
    String lv = String.format(Locale.getDefault(), "%.1f", rec.scenario.constantDisplay);

    String base = type.isEmpty() ? lv : (type + " " + lv);
    String by = rec.scenario.author != null && !rec.scenario.author.trim().isEmpty()
            ? "  By " + rec.scenario.author.trim() : "";
    String text = base + by;

    SpannableString sp = new SpannableString(text);
    if (!type.isEmpty()) {
        // 类型 + 定数整段染色，不包含作者
        sp.setSpan(new ForegroundColorSpan(resolveTypeColor(type)),
                0, base.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    return sp;
 }

    private class LoadRecommendTask extends AsyncTask<Void, Void, List<RecommendationEngine.RecommendationItem>> {
        private final String qq;

        LoadRecommendTask(String qq) {
            this.qq = qq;
        }

        @Override
        protected List<RecommendationEngine.RecommendationItem> doInBackground(Void... voids) {
            try {
                String scenText = httpGet(SCEN_LIST_URL);
                String bestText = httpGet("http://47.93.57.125:521/WebAPI/public/Bests/" + qq + ".txt");
                if (bestText == null || bestText.trim().isEmpty()) {
                    return Collections.emptyList();
                }

                List<RecommendationEngine.ScenarioInfo> all = RecommendationEngine.parseScenarioList(scenText);
                Map<String, RecommendationEngine.ScenarioInfo> byId = new HashMap<>();
                for (RecommendationEngine.ScenarioInfo s : all) {
                    byId.put(s.id, s);
                }

                List<RecommendationEngine.PlayerScoreRow> scores = RecommendationEngine.parseBestsContent(bestText);
                return RecommendationEngine.generate(all, scores, byId, random);
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        @Override
        protected void onPostExecute(List<RecommendationEngine.RecommendationItem> items) {
            progressBar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);

            if (items == null || items.isEmpty()) {
                TextView empty = new TextView(RecommendActivity.this);
                empty.setText("暂无推荐。请确认已绑定 QQ 且在场景库中有成绩；或稍后再试。");
                empty.setTextColor(getResources().getColor(R.color.text_secondary, null));
                empty.setPadding(8, 16, 8, 16);
                listContainer.addView(empty);
                return;
            }

            LayoutInflater inf = LayoutInflater.from(RecommendActivity.this);
            for (RecommendationEngine.RecommendationItem rec : items) {
                View row = inf.inflate(R.layout.item_recommend, listContainer, false);
                TextView cat = row.findViewById(R.id.recCategory);
                TextView title = row.findViewById(R.id.recTitle);
                TextView meta = row.findViewById(R.id.recMeta);
                TextView reason = row.findViewById(R.id.recReason);
                TextView predict = row.findViewById(R.id.recPredict);
                DiagonalCutImageView icon = row.findViewById(R.id.recIcon);

                cat.setText(rec.category);
                title.setText(rec.scenario.name);
                meta.setText(buildMetaText(rec));
                reason.setText(rec.reason);
                predict.setText(rec.predictedLine);

                String iconUrl = "http://47.93.57.125:521/WebAPI/public/icons/" + rec.scenario.idRaw + ".png";
                Glide.with(RecommendActivity.this)
                        .load(iconUrl)
                        .placeholder(R.drawable.rating_off)
                        .error(R.drawable.rating_off)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(icon);

                row.setOnClickListener(v -> {
                    Intent intent = new Intent(RecommendActivity.this, scnperson.class);
                    intent.putExtra("key", rec.scenario.idRaw);
                    if (rec.scenario.beilv != null && !rec.scenario.beilv.isEmpty()) {
                        intent.putExtra("fenbeilv", rec.scenario.beilv);
                    }
                    startActivity(intent);
                });

                listContainer.addView(row);
            }
        }
    }

    private static String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setConnectTimeout(12000);
        c.setReadTimeout(12000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        reader.close();
        c.disconnect();
        return sb.toString();
    }
}
