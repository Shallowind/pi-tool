package com.zmmxkid.pitool;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class scnlist extends AppCompatActivity {

    private ListView listView;
    private SearchView searchView;
    private CustomAdapter adapter;
    private List<String[]> dataList;
    private List<String[]> filteredDataList;

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

        new DownloadTask().execute("http://47.241.0.156:34567/WebAPI/public/scenInfo/scenList.txt");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] data = filteredDataList.get(position);
                String firstData = data[0];

                Intent intent = new Intent(scnlist.this, scnperson.class);
                intent.putExtra("key", firstData);
                startActivity(intent);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });
    }

    private void filterData(String query) {
        filteredDataList.clear();
        adapter.clear();
        for (String[] data : dataList) {
            if (data[1].toLowerCase().contains(query.toLowerCase())) {
                filteredDataList.add(data);
                adapter.add(data);
            }
        }
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
                    sb.append(line);
                    sb.append("\n");
                }

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
            }
        }
    }

    private class CustomAdapter extends ArrayAdapter<String[]> {
        CustomAdapter() {
            super(scnlist.this, android.R.layout.simple_list_item_1);
        }

        public void add(String[] data) {
            super.add(data);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String[] data = getItem(position);
            String colorCode = data[3];

            String coloredText = "【" + data[3] + " " + data[4] + "】";
            String plainText = data[1];

            int color = Color.BLACK; // Default color

            if ("FTR".equals(colorCode)) {
                color = Color.parseColor("#800080"); // Purple
            } else if ("BYD".equals(colorCode)) {
                color = Color.RED;
            } else if ("MXM".equals(colorCode)) {
                color = Color.parseColor("#EEEE11");
            } else if ("PST".equals(colorCode)) {
                color = Color.BLUE;
            } else if ("PRS".equals(colorCode)) {
                color = Color.GREEN;
            }

            Spannable spannable = new SpannableString(coloredText + plainText);
            spannable.setSpan(new ForegroundColorSpan(color), 0, coloredText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            convertView = super.getView(position, convertView, parent);
            ((TextView) convertView).setText(spannable);

            return convertView;
        }
    }

}
