package com.zmmxkid.pitool;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class personview extends Activity {

    private ImageView head;
    private String qq;
    private String name, PTT, allscore, Rating, level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personview);

        head = findViewById(R.id.imageView);
        qq = getIntent().getStringExtra("qq");

        String avatarUrl = "https://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=100";
        String txtUrl = "http://47.241.0.156:34567/WebAPI/public/Bests/" + qq + ".txt";

        new LoadImageTask().execute(avatarUrl);
        new ReadTextFileTask().execute(txtUrl);
    }

    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                head.setImageBitmap(bitmap);
            }
        }
    }

    private class ReadTextFileTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            String txtUrl = urls[0];
            try {
                URL url = new URL(txtUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        name = parts[0];
                        PTT = parts[1];
                        allscore = parts[2];
                        Rating = parts[3];
                        level = parts[4];
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ImageView img=findViewById(R.id.imageView2);
            TextView Name=findViewById(R.id.textView12);
            TextView Qq=findViewById(R.id.textView18);
            TextView ptt=findViewById(R.id.textView11);
            TextView ptt2=findViewById(R.id.textViewmiao);
            TextView Allscore=findViewById(R.id.textView16);
            TextView rating=findViewById(R.id.textView15);
            TextView Level=findViewById(R.id.textView14);
            Name.setText(name);
            Qq.setText("QQ:"+qq);
            ptt.setText(PTT);
            ptt2.setText(PTT);
            Allscore.setText("总分："+allscore);
            rating.setText("DX Rating: "+Rating);
            Level.setText("排名："+level);
            float Ptt=Float.parseFloat(PTT);
            if(Ptt < 0.1){
                img.setImageResource(R.drawable.rating_off);
            }else if(Ptt >=0.1&&Ptt <3.5){
                img.setImageResource(R.drawable.rating_1);
            }else if(Ptt >=3.5&&Ptt <7){
                img.setImageResource(R.drawable.rating_2);
            }else if(Ptt >=7&&Ptt <10){
                img.setImageResource(R.drawable.rating_3);
            }else if(Ptt >=10&&Ptt <11){
                img.setImageResource(R.drawable.rating_4);
            }else if(Ptt >=11&&Ptt <12){
                img.setImageResource(R.drawable.rating_5);
            }else if(Ptt >=12&&Ptt <12.5){
                img.setImageResource(R.drawable.rating_6);
            }else if(Ptt >=12.5&&Ptt <13){
                img.setImageResource(R.drawable.rating_7);
            }else if(Ptt >=13){
                img.setImageResource(R.drawable.rating_9);
            }
            // Use the parsed data here (name, PTT, allscore, Rating, level)
        }
    }
}
