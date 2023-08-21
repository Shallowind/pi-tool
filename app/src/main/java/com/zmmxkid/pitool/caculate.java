package com.zmmxkid.pitool;

import androidx.appcompat.app.AppCompatActivity;
import java.lang.Math;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class caculate extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caculate);

        Spinner spinner = findViewById(R.id.spinner2);
        Spinner spinner2 = findViewById(R.id.spinner3);
        // 定义选项列表数据
        String[] options = {"细菌", "病毒", "真菌", "寄生虫", "朊病毒", "纳米病毒", "生化武器", "Neurax蠕虫", "Necora病毒", "猩猩流感", "暗影瘟疫"};
        String[] options2 = {"简单", "普通", "困难", "天灾/终极困难"};
        // 创建适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options2);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 设置适配器到 Spinner
        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter2);
    }
    public void jisuan(View view) {
        EditText day1=findViewById(R.id.editTextText);
        float day=Float.parseFloat(day1.getText().toString());
        EditText cure1=findViewById(R.id.editTextText2);
        float cure=Float.parseFloat(cure1.getText().toString());
        EditText score1=findViewById(R.id.editTextText3);
        float score=Float.parseFloat(score1.getText().toString());
        if(cure<0.01)cure= 0.01F;
        float scoremax=20000000*(1+(1-cure)/3)*(1+score)/day;
        TextView textView=findViewById(R.id.textView7);
        textView.setText(Float.toString(scoremax));

    }
    public void jisuan1(View view) {
        Spinner spinner1 = findViewById(R.id.spinner2);
        Spinner spinner2 = findViewById(R.id.spinner3);
        EditText day1 = findViewById(R.id.editdaytext);
        EditText cure1 = findViewById(R.id.editcuretext);
        EditText dna1 = findViewById(R.id.editdnatext);
        EditText yellow1 = findViewById(R.id.edityellowtext);
        EditText die1 = findViewById(R.id.editdietext);
        EditText dna2 = findViewById(R.id.editdnahua);
        TextView textView = findViewById(R.id.textView10);

        float day = Float.parseFloat(day1.getText().toString());
        float cure = Float.parseFloat(cure1.getText().toString());
        float dna = Float.parseFloat(dna1.getText().toString());
        float yellow = Float.parseFloat(yellow1.getText().toString());
        float die = Float.parseFloat(die1.getText().toString());
        float dnahua = Float.parseFloat(dna2.getText().toString());

        float s1 = 0.0f; // 初始值
        String selectedBacteria = spinner1.getSelectedItem().toString();
        if (selectedBacteria.equals("细菌") || selectedBacteria.equals("猩猩流感") || selectedBacteria.equals("暗影瘟疫")) {
            s1 = 1.0f;
        } else if (selectedBacteria.equals("病毒") || selectedBacteria.equals("真菌") || selectedBacteria.equals("寄生虫") ||
                selectedBacteria.equals("朊病毒") || selectedBacteria.equals("纳米病毒") || selectedBacteria.equals("生化武器")) {
            s1 = 0.9f;
        } else if (selectedBacteria.equals("Neurax蠕虫")) {
            s1 = 1.1f;
        } else if (selectedBacteria.equals("Necora病毒")) {
            s1 = 1.4f;
        }

        float s2 = 0.0f; // 初始值
        String selectedDifficulty = spinner2.getSelectedItem().toString();
        if (selectedDifficulty.equals("简单")) {
            s2 = 1.0f;
        } else if (selectedDifficulty.equals("普通")) {
            s2 = 4.0f;
        } else if (selectedDifficulty.equals("困难") || selectedDifficulty.equals("天灾/终极困难")) {
            if (selectedBacteria.equals("Necora病毒")) {
                s2 = 10.0f;
            } else {
                if (selectedDifficulty.equals("困难")) {
                    s2 = 8.0f;
                }
                else s2 = 12.0f;
            }
        }
        // 进行相关的计算和逻辑操作
        float dianshu=dna*2+dnahua;
        float jiyin=yellow*dianshu;
        float jieyao=100*cure;if(jieyao<1)jieyao=1;
        if(jieyao>100) {
            if (selectedBacteria.equals("Necora病毒"))jieyao = 400;
            else jieyao = 100;
        }
        if(selectedBacteria.equals("Necora病毒"))jieyao/=5;
        double naturalLog = Math.log(day); // 计算自然对数
        day= (float) Math.floor(naturalLog); // 向下取整
        float result = (float) (10*Math.floor(((jiyin*s2*s1)/(jieyao*day)+die*100)));
        textView.setText(Float.toString(result));
    }

}