package com.zmmxkid.pitool;
import android.graphics.Color;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class scn extends Activity {

    private static final String TAG = "MainActivity";
    private static final String TXT_URL = "http://47.241.0.156:34567/WebAPI/public/scenInfo/scenList.txt";

    private TableLayout tableLayout;
    private ViewGroup.LayoutParams originalTableLayoutParams;
    private ScrollView scrollView;
    private HorizontalScrollView horizontalScrollView;

    private static final float SCALE_FACTOR = 0.9f;
    private static final float SCALE_FACTOR_1 = 1.1f;
    private float currentScale = 0.9f;
    private Boolean ding=false;

    // ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scn);

        tableLayout = findViewById(R.id.tablelayout);
        scrollView = findViewById(R.id.scrollView3);
        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        originalTableLayoutParams = tableLayout.getLayoutParams();

        Button button11 = findViewById(R.id.button11);
        Button button14 = findViewById(R.id.button14);
        Button button12 = findViewById(R.id.button12);
        Button button13 = findViewById(R.id.button13);

        new LoadTxtDataTask().execute(TXT_URL);

        button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                small();
            }
        });
        button14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                big();
            }
        });
        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yuanshi();
            }
        });
        button13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dingshu();
            }
        });


    }

    private void small() {
        currentScale *= SCALE_FACTOR;
        tableLayout.setPivotX(0);
        tableLayout.setPivotY(0);
        tableLayout.setScaleX(currentScale);
        tableLayout.setScaleY(currentScale);

        // Remove empty rows
        int numRowsToRemove = Math.min(tableLayout.getChildCount(), (int) (0.1 * tableLayout.getChildCount()));
        for (int i = 0; i < numRowsToRemove; i++) {
            TableRow lastRow = (TableRow) tableLayout.getChildAt(tableLayout.getChildCount() - 1);
            boolean isRowEmpty = true;

            // Check if the row is empty
            for (int j = 0; j < lastRow.getChildCount(); j++) {
                TextView textView = (TextView) lastRow.getChildAt(j);
                if (!textView.getText().toString().isEmpty()) {
                    isRowEmpty = false;
                    break;
                }
            }

            if (isRowEmpty) {
                tableLayout.removeViewAt(tableLayout.getChildCount() - 1);
            } else {
                // If the last row is not empty, stop removing rows
                break;
            }
        }
    }

    private void big() {
        currentScale *= SCALE_FACTOR_1;
        tableLayout.setPivotX(0);
        tableLayout.setPivotY(0);
        tableLayout.setScaleX(currentScale);
        tableLayout.setScaleY(currentScale);

        // Add additional empty rows
        int numRowsToAdd = 1 + (int) (0.1 * tableLayout.getChildCount());
        for (int i = 0; i < numRowsToAdd; i++) {
            TableRow newRow = new TableRow(this);

            for (int j = 0; j < tableLayout.getChildCount(); j++) {
                TextView originalTextView = new TextView(this);
                originalTextView.setText("");  // Empty text
                originalTextView.setGravity(Gravity.CENTER);

                newRow.addView(originalTextView);
            }

            tableLayout.addView(newRow);
        }
    }
    private void dingshu() {
        ding = true;
        currentScale = 0.9f;  // Reset the currentScale3
        tableLayout.setPivotX(0);
        tableLayout.setPivotY(0);
        tableLayout.setScaleX(currentScale);
        tableLayout.setScaleY(currentScale);
        tableLayout.removeAllViews();
        new LoadTxtDataTask().execute(TXT_URL);
    }

    private void yuanshi(){
        ding = false;
        currentScale = 0.9f;  // Reset the currentScale
        tableLayout.setPivotX(0);
        tableLayout.setPivotY(0);
        tableLayout.setScaleX(currentScale);
        tableLayout.setScaleY(currentScale);
        tableLayout.removeAllViews();
        new LoadTxtDataTask().execute(TXT_URL);
    }

    private class LoadTxtDataTask extends AsyncTask<String, Void, List<String[]>> {

        @Override
        protected List<String[]> doInBackground(String... urls) {
            List<String[]> dataRows = new ArrayList<>();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    dataRows.add(line.split(","));
                }
                reader.close();

                // Sort data rows by the 6th field in descending order
                if (ding) {
                    Collections.sort(dataRows, (row1, row2) -> {
                        double value1 = Double.parseDouble(row1[5]);
                        double value2 = Double.parseDouble(row2[5]);
                        return Double.compare(value2, value1);
                    });
                }
                String headerRow = "序号,名称,倍率,类别,等级,定数,作者";
                dataRows.add(0, headerRow.split(",")); // Insert the header row at the beginning

            } catch (IOException e) {
                Log.e(TAG, "Error loading TXT data: " + e.getMessage());
            }
            return dataRows;
        }

        @Override
        protected void onPostExecute(List<String[]> dataRows) {
            String[] headers = dataRows.get(0);
            createTable(headers);

            for (int i = 1; i < dataRows.size(); i++) {
                addTableRow(dataRows.get(i));
            }
        }
    }

    private void createTable(String[] headers) {
        TableRow headerRow = new TableRow(this);
        for (String header : headers) {
            TextView textView = new TextView(this);
            textView.setText(header);

            if (header.equals("作者")) {
                textView.setGravity(Gravity.START); // Align text to the left
            } else {
                textView.setGravity(Gravity.CENTER); // Default center alignment
            }

            headerRow.addView(textView);
        }
        tableLayout.addView(headerRow);
    }


    private void addTableRow(String[] rowData) {
        TableRow tableRow = new TableRow(this);
        for (int i = 0; i < rowData.length; i++) {
            TextView textView = new TextView(this);
            textView.setText(rowData[i]);

            if (i < rowData.length - 1) {  // 对最后一列以外的单元格进行居中对齐
                textView.setGravity(Gravity.CENTER);
            }

            // 设置不同的文本颜色
            String cellContent = rowData[i];
            if ("FTR".equals(cellContent)) {
                textView.setTextColor(getResources().getColor(R.color.purple)); // 设置紫色
            } else if ("BYD".equals(cellContent)) {
                textView.setTextColor(getResources().getColor(R.color.red)); // 设置红色
            } else if ("MXM".equals(cellContent)) {
                textView.setTextColor(getResources().getColor(R.color.yellow)); // 设置黄色
            } else if ("PST".equals(cellContent)) {
                textView.setTextColor(getResources().getColor(R.color.blue)); // 设置蓝色
            } else if ("PRS".equals(cellContent)) {
                textView.setTextColor(getResources().getColor(R.color.green)); // 设置绿色
            }

            tableRow.addView(textView);
        }
        tableLayout.addView(tableRow);
        tableLayout.setPivotX(0);
        tableLayout.setPivotY(0);
        tableLayout.setScaleX(currentScale);
        tableLayout.setScaleY(currentScale);
    }
}
