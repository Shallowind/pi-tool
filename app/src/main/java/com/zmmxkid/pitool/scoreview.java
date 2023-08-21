package com.zmmxkid.pitool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class scoreview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreview);
    }
    public void chaxun(View view) {
        String url = "http://47.241.0.156:34567/WebAPI/public/%E4%B8%BB%E6%B8%B8%E6%88%8F/%E5%AF%84%E7%94%9F%E8%99%AB.txt";
        String headerRow = "位,分数,QQ号,昵称\n"; // 这里是第一行的内容
        new DownloadFileTask().execute(url, headerRow);
    }

    public void jisuanqi(View view) {
        Intent intent = new Intent(this,caculate.class);
        startActivity(intent);
    }

    private class DownloadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String headerRow = params[1];
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
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Insert headerRow at the beginning of the content
            content.insert(0, headerRow);
            return content.toString();
        }

        @Override
        protected void onPostExecute(String content) {
            parseAndDisplayContent(content);
        }
    }

    private void parseAndDisplayContent(String content) {
        try {
            LinearLayout container = findViewById(R.id.container);
            container.removeAllViews();

            String[] lines = content.split("\n");
            int numColumns = -1;
            List<int[]> maxColumnWidthsList = new ArrayList<>();

            for (String line : lines) {
                String[] data = line.split(",");
                if (numColumns == -1) {
                    numColumns = data.length;
                    for (int i = 0; i < numColumns; i++) {
                        maxColumnWidthsList.add(new int[]{});
                    }
                }

                for (int i = 0; i < numColumns; i++) {
                    if (i < data.length) {
                        int[] maxColumnWidths = maxColumnWidthsList.get(i);
                        int currentWidth = data[i].length();
                        if (maxColumnWidths.length == 0 || currentWidth > maxColumnWidths[0]) {
                            maxColumnWidthsList.set(i, new int[]{currentWidth});
                        }
                    }
                }
            }

            int additionalWidthForNumber = getResources().getDimensionPixelSize(R.dimen.additional_width_for_number);

            for (String line : lines) {
                TableLayout tableLayout = new TableLayout(this);
                container.addView(tableLayout);

                TableRow tableRow = new TableRow(this);
                tableLayout.addView(tableRow);

                HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
                tableRow.addView(horizontalScrollView);

                LinearLayout innerLayout = new LinearLayout(this);
                innerLayout.setOrientation(LinearLayout.HORIZONTAL);
                horizontalScrollView.addView(innerLayout);

                String[] data = line.split(",");
                for (int i = 0; i < data.length; i++) {
                    TextView textView = new TextView(this);
                    textView.setText(data[i]);
                    textView.setBackgroundColor(Color.WHITE);
                    textView.setPadding(5, 0, 5, 0);
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

                    textView.setBackgroundResource(R.drawable.border);

                    int[] maxColumnWidths = maxColumnWidthsList.get(i);
                    int columnWidth = getResources().getDimensionPixelSize(R.dimen.min_column_width); // Minimum width
                    if (maxColumnWidths.length > 0) {
                        int charWidth = Math.max(maxColumnWidths[0], String.valueOf(i).length());
                        columnWidth = Math.max(columnWidth, (charWidth + 1) * 20 + additionalWidthForNumber);
                    }

                    // Additional adjustment to ensure enough width
                    if (textView.getPaint() != null) {
                        float textWidth = textView.getPaint().measureText(data[i]);
                        if (textWidth > columnWidth) {
                            columnWidth = (int) Math.ceil(textWidth) + 20 + additionalWidthForNumber;
                        }

                        // Check if text will wrap to next line
                        int textLines = (int) Math.ceil(textWidth / columnWidth);
                        if (textLines > 1) {
                            columnWidth += (20 + additionalWidthForNumber) * (textLines - 1);
                        }
                    }

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            columnWidth,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    textView.setLayoutParams(params);

                    innerLayout.addView(textView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}