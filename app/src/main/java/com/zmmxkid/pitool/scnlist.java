package com.zmmxkid.pitool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Collator;  // 新增：拼音排序
import java.util.*;
import java.util.Locale;    // 新增

import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class scnlist extends AppCompatActivity {

    private ListView listView;
    private SearchView searchView;
    private CustomAdapter adapter;
    private List<String[]> dataList;
    private List<String[]> filteredDataList;
    private boolean isIdAscending = false;
    private boolean isDingshuAscending = true;
    private boolean isZuozheAscending = true;
    private boolean isBaoAscending = true;
    private static final int SORT_BY_ID = 0;
    private static final int SORT_BY_DINGSHU = 1;
    private static final int SORT_BY_ZUOZHE = 2;
    private static final int SORT_BY_BAO = 3;
    private int currentSortMode = SORT_BY_ID;
    private Map<String, List<String>> additionalData = new HashMap<>();
    private Map<String, Integer> scoreData = new HashMap<>();

    // 新增：中文拼音排序器
    private final Collator chineseCollator = Collator.getInstance(Locale.CHINA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scnlist);

        listView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);

        dataList = new ArrayList<>();
        filteredDataList = new ArrayList<>();
        adapter = new CustomAdapter();

        listView.setAdapter(adapter);
        File qqFile = new File(getExternalFilesDir(null), "QQ.txt");
        String qq;
        try {
            FileInputStream inputStream = new FileInputStream(qqFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            qq = reader.readLine();
            reader.close();
            new DownloadScoreDataTask().execute("http://47.93.57.125:521/WebAPI/public/scores/" + qq + ".txt");
        } catch (IOException e) {
            Toast.makeText(scnlist.this, "账号未绑定或没有成绩，请先去主页绑定账号或提交一个成绩。", Toast.LENGTH_LONG).show();
        }
        new DownloadTask().execute("http://47.93.57.125:521/WebAPI/public/scenInfo/scenList.txt");
        new DownloadAdditionalDataTask().execute("http://47.93.57.125:8888/down/CmbnTcKeNoqT");

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String[] data = filteredDataList.get(position);
            String firstData = data[0];
            Intent intent = new Intent(scnlist.this, scnperson.class);
            intent.putExtra("key", firstData);
            startActivity(intent);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });

        ConstraintLayout backLayout = findViewById(R.id.backlayout);
        switch (randomNumber) {
            case 1: backLayout.setBackgroundResource(R.drawable.img11); break;
            case 2: backLayout.setBackgroundResource(R.drawable.img22); break;
            case 3: backLayout.setBackgroundResource(R.drawable.img33); break;
            case 4: backLayout.setBackgroundResource(R.drawable.img44); break;
            case 5: backLayout.setBackgroundResource(R.drawable.img55); break;
            default: backLayout.setBackgroundResource(R.drawable.img11); break;
        }
    }

    private void filterData(String query) {
        filteredDataList.clear();
        adapter.clear();
        String lowerCaseQuery = query.toLowerCase();

        for (String[] data : dataList) {
            boolean matches = data[1].toLowerCase().contains(lowerCaseQuery) ||
                    data[6].toLowerCase().contains(lowerCaseQuery) ||
                    (data.length > 8 && data[8].toLowerCase().contains(lowerCaseQuery));

            if (matches) {
                filteredDataList.add(data);
                adapter.add(data);
                continue;
            }

            String sequenceNumber = data[0];
            if (additionalData.containsKey(sequenceNumber)) {
                for (String item : additionalData.get(sequenceNumber)) {
                    if (item.toLowerCase().contains(lowerCaseQuery)) {
                        filteredDataList.add(data);
                        adapter.add(data);
                        break;
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        // 关键修复：搜索过滤后更新索引，防止数量变化导致错位
        adapter.updateSections(filteredDataList);
    }

    public void sortbyid(View view) {
        currentSortMode = SORT_BY_ID;
        Collections.sort(filteredDataList, (o1, o2) -> isIdAscending ?
                Integer.compare(Integer.parseInt(o1[0]), Integer.parseInt(o2[0])) :
                Integer.compare(Integer.parseInt(o2[0]), Integer.parseInt(o1[0])));
        isIdAscending = !isIdAscending;
        updateListView();
        adapter.updateSections(filteredDataList);
    }

    public void sortbydingshu(View view) {
        currentSortMode = SORT_BY_DINGSHU;
        Collections.sort(filteredDataList, (o1, o2) -> {
            float v1 = Float.parseFloat(o1[5]);
            float v2 = Float.parseFloat(o2[5]);
            return isDingshuAscending ? Float.compare(v1, v2) : Float.compare(v2, v1);
        });
        isDingshuAscending = !isDingshuAscending;
        updateListView();
        adapter.updateSections(filteredDataList);
    }

    // 关键修复：中文按拼音首字母排序
    public void sortbyzuozhe(View view) {
        currentSortMode = SORT_BY_ZUOZHE;
        Collections.sort(filteredDataList, (o1, o2) -> {
            // 使用 Collator 按拼音排序，解决中文乱序问题
            int result = chineseCollator.compare(o1[6], o2[6]);
            return isZuozheAscending ? result : -result;
        });
        isZuozheAscending = !isZuozheAscending;
        updateListView();
        adapter.updateSections(filteredDataList);
    }

    // 关键修复：中文按拼音首字母排序
    public void sortbybao(View view) {
        currentSortMode = SORT_BY_BAO;
        Collections.sort(filteredDataList, (o1, o2) -> {
            String bao1 = (o1.length > 8) ? o1[8] : "NULL";
            String bao2 = (o2.length > 8) ? o2[8] : "NULL";
            // 使用 Collator 按拼音排序
            int result = chineseCollator.compare(bao1, bao2);
            return isBaoAscending ? result : -result;
        });
        isBaoAscending = !isBaoAscending;
        updateListView();
        adapter.updateSections(filteredDataList);
    }

    private void updateListView() {
        adapter.clear();
        for (String[] data : filteredDataList) {
            adapter.add(data);
        }
        adapter.notifyDataSetChanged();
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                String[] lines = result.split("\n");
                for (String line : lines) {
                    String[] data = line.split(",");
                    dataList.add(data);
                    filteredDataList.add(data);
                    adapter.add(data);
                }
                adapter.notifyDataSetChanged();
                // 关键修复：初始数据加载完成后立即生成右侧索引，确保刚进入页面就能显示气泡
                adapter.updateSections(filteredDataList);
            }
        }
    }

    // Adapter 保持原样（省略重复代码，与之前提供的 SectionIndexer 实现一致）
    private class CustomAdapter extends ArrayAdapter<String[]> implements SectionIndexer {
        private String[] sections;
        private int[] sectionPositions;
        private int[] sectionForPosition;

        CustomAdapter() { super(scnlist.this, R.layout.list_item_with_icon); }

        public void updateSections(List<String[]> data) {
            if (data == null || data.isEmpty()) {
                sections = new String[0];
                sectionPositions = new int[0];
                sectionForPosition = new int[0];
                return;
            }

            LinkedHashMap<String, Integer> sectionMap = new LinkedHashMap<>();
            for (int i = 0; i < data.size(); i++) {
                String[] item = data.get(i);
                String sectionLabel = getSectionLabel(item);
                if (!sectionMap.containsKey(sectionLabel)) {
                    sectionMap.put(sectionLabel, i);
                }
            }

            sections = sectionMap.keySet().toArray(new String[0]);
            sectionPositions = new int[sections.length];
            for (int i = 0; i < sections.length; i++) {
                sectionPositions[i] = sectionMap.get(sections[i]);
            }

            sectionForPosition = new int[data.size()];
            int currentSection = 0;
            for (int i = 0; i < data.size(); i++) {
                if (currentSection < sectionPositions.length - 1 && i >= sectionPositions[currentSection + 1]) {
                    currentSection++;
                }
                sectionForPosition[i] = currentSection;
            }
        }

        private String truncate(String s, int maxLen) {
            if (s == null || s.isEmpty()) return "#";
            return s.length() <= maxLen ? s : s.substring(0, maxLen);
        }

        private String getSectionLabel(String[] data) {
            switch (currentSortMode) {
                case SORT_BY_ID:
                    int id = Integer.parseInt(data[0]);
                    int start = (id / 50) * 50 + 1;
                    int end = start + 49;
                    return String.format("%03d-%03d", start, end);
                case SORT_BY_DINGSHU:
                    float dingshu = Float.parseFloat(data[5]);
                    return (int) dingshu + ".x";
                case SORT_BY_ZUOZHE:
                    return truncate(data[6], 16);
                case SORT_BY_BAO:
                    String bao = data.length > 8 ? data[8] : "NULL";
                    return bao.equals("NULL") ? "#" : truncate(bao, 16);
                default:
                    return "*";
            }
        }

        @Override public Object[] getSections() { return sections; }
        @Override public int getPositionForSection(int sectionIndex) {
            return (sectionPositions == null || sectionIndex >= sectionPositions.length) ? 0 : sectionPositions[sectionIndex];
        }
        @Override public int getSectionForPosition(int position) {
            return (sectionForPosition == null || position >= sectionForPosition.length) ? 0 : sectionForPosition[position];
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_with_icon, parent, false);
            }

            DiagonalCutImageView iconView = convertView.findViewById(R.id.icon);
            Spinner spinnerView = convertView.findViewById(R.id.spinner);
            TextView fenbeilv = convertView.findViewById(R.id.text2);
            TextView zuozhe = convertView.findViewById(R.id.text3);
            TextView text = convertView.findViewById(R.id.text4);
            TextView bao = convertView.findViewById(R.id.text5);
            TextView id = convertView.findViewById(R.id.text6);
            TextView textView = convertView.findViewById(R.id.text);
            TextView text7 = convertView.findViewById(R.id.text7);
            ConstraintLayout linearLayout = convertView.findViewById(R.id.container);

            String[] data = getItem(position);
            if (data == null) return convertView;

            /* ===== 分数状态 ===== */
            String idValue = data[0] + "分数";
            Integer score = scoreData.get(idValue);

            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int shadowColor = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)
                    ? Color.WHITE : Color.BLACK;
            text7.setShadowLayer(2, 0, 0, shadowColor);

            if (score != null) {
                if (score >= 75000) {
                    text7.setText("理论");
                    linearLayout.setBackgroundColor(Color.argb(40, 255, 221, 0));
                    text7.setTextColor(Color.parseColor("#FFDD00"));
                } else if (score >= 65000) {
                    text7.setText("三星");
                    linearLayout.setBackgroundColor(Color.argb(40, 255, 128, 0));
                    text7.setTextColor(Color.parseColor("#FF8000"));
                } else if (score >= 55000) {
                    text7.setText("合格");
                    linearLayout.setBackgroundColor(Color.argb(40, 255, 168, 255));
                    text7.setTextColor(Color.parseColor("#FFA8FF"));
                } else if (score > 1) {
                    text7.setText("通关");
                    linearLayout.setBackgroundColor(Color.argb(40, 20, 200, 20));
                    text7.setTextColor(Color.parseColor("#14C814"));
                } else {
                    text7.setText("无");
                    linearLayout.setBackgroundColor(Color.TRANSPARENT);
                    text7.setTextColor(Color.parseColor("#B0B0B0"));
                }
            } else {
                text7.setText("无");
                linearLayout.setBackgroundColor(Color.TRANSPARENT);
                text7.setTextColor(Color.parseColor("#B0B0B0"));
            }

            /* ===== 难度颜色 ===== */
            String colorCode = data[3];
            int color = Color.BLACK;
            if ("FTR".equals(colorCode)) color = Color.parseColor("#800080");
            else if ("BYD".equals(colorCode)) color = Color.RED;
            else if ("MXM".equals(colorCode)) color = Color.parseColor("#EEEE11");
            else if ("PST".equals(colorCode)) color = Color.BLUE;
            else if ("PRS".equals(colorCode)) color = Color.GREEN;
            else if ("ETR".equals(colorCode)) color = Color.parseColor("#ffaaff");

            String coloredText = "【" + data[3] + " " + data[5] + "】";
            Spannable spannable = new SpannableString(coloredText);
            spannable.setSpan(new ForegroundColorSpan(color), 0, coloredText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannable);

            /* ===== 文字 ===== */
            zuozhe.setText("By " + data[6]);
            fenbeilv.setText("分倍率：" + data[2]);
            text.setText(data[7]);
            bao.setText(data.length > 8 ? "包：" + data[8] : "包：NULL");

            String str = data[0];
            while (str.length() < 3) str = "0" + str;
            id.setText(str);

            /* ===== ★ Glide 高级缓存图标（核心） ===== */
            String iconUrl = "http://47.93.57.125:521/WebAPI/public/icons/" + data[0] + ".png";
            iconView.setImageDrawable(null);
            iconView.setTag(iconUrl);

            Glide.with(iconView)
                    .load(iconUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 内存 + 磁盘
                    .dontAnimate()                            // 防闪
                    .placeholder(null)
                    .error(Optional.ofNullable(null))
                    .into(iconView);

            /* ===== Spinner ===== */
            String sequenceNumber = data[0];
            List<String> spinnerItems = new ArrayList<>();
            spinnerItems.add(data[1]);

            if (additionalData != null && additionalData.containsKey(sequenceNumber)) {
                for (String item : additionalData.get(sequenceNumber)) {
                    if (!spinnerItems.contains(item)) {
                        spinnerItems.add(item);
                    }
                }
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    scnlist.this,
                    android.R.layout.simple_spinner_item,
                    spinnerItems
            );
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerView.setAdapter(spinnerAdapter);

            return convertView;
        }
    }

    private class DownloadAdditionalDataTask extends AsyncTask<String, Void, Map<String, List<String>>> {
        @Override
        protected Map<String, List<String>> doInBackground(String... urls) {
            Map<String, List<String>> additionalDataMap = new HashMap<>();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "GB2312"));

                String line;
                String currentLabel = null;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("[")) {
                        // 这是一个标签行
                        currentLabel = line.replace("[", "").replace("]", "").trim();
                    } else if (line.startsWith("序号=") && currentLabel != null) {
                        // 这是一个序号行
                        String sequenceNumber = line.split("=")[1].trim();
                        if (!additionalDataMap.containsKey(sequenceNumber)) {
                            additionalDataMap.put(sequenceNumber, new ArrayList<>());
                        }
                        additionalDataMap.get(sequenceNumber).add(currentLabel);
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return additionalDataMap;
        }

        @Override
        protected void onPostExecute(Map<String, List<String>> result) {
            // 将结果存储在字段中，并在需要时更新UI
            additionalData = result;
            adapter.notifyDataSetChanged(); // 获取附加数据后更新ListView
        }
    }

    private class DownloadScoreDataTask extends AsyncTask<String, Void, Map<String, Integer>> {
        @Override
        protected Map<String, Integer> doInBackground(String... urls) {
            Map<String, Integer> scoreMap = new HashMap<>();
            try {
                // 打开连接并读取分数文件
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    // 解析每一行
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String id = parts[0].trim();
                        int score = Integer.parseInt(parts[1].trim());
                        scoreMap.put(id, score); // 将ID和分数保存到map中
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return scoreMap;
        }

        @Override
        protected void onPostExecute(Map<String, Integer> result) {
            // 下载完成后，保存分数数据，并更新UI
            scoreData = result;
            adapter.notifyDataSetChanged(); // 更新适配器，刷新界面
        }
    }

}