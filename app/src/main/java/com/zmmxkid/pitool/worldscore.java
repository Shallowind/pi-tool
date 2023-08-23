package com.zmmxkid.pitool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class worldscore extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worldscore);
        Spinner spinnerzhu = findViewById(R.id.spinnerzhu);
        Spinner spinnerju = findViewById(R.id.spinnerju);
        Spinner spinnerji = findViewById(R.id.spinnerji);
        Spinner spinnerjie = findViewById(R.id.spinnerjie);

        // 定义选项列表数据
        String[] optionszhu = {"细菌", "病毒", "真菌", "寄生虫", "朊病毒", "纳米病毒", "生化武器", "Neurax蠕虫", "Necroa病毒"};
        String[] optionsju = {"虚假消息", "科学否定", "狂牛怪疫", "地旷人稀", "航空时代", "圣诞老人的小助手", "遥传", "人造器官", "冰冻病毒", "来历不明", "立百病毒", "不闻不问", "人人平等", "债务危机", "全球暖化", "关闭一切", "冰河时代", "天花", "恐外症", "海盗瘟疫", "火山喷发", "猪流感", "颠倒乾坤", "黄金时代", "黑死病"};
        String[] optionsji = {"细菌", "病毒", "真菌", "寄生虫", "朊病毒", "纳米病毒", "生化武器", "Neurax蠕虫", "Necroa病毒", "猩猩流感", "暗影瘟疫"};
        String[] optionsjie = {"细菌", "病毒", "真菌", "寄生虫", "朊病毒", "纳米病毒", "生化武器","冰冻病毒"};
        // 创建适配器
        ArrayAdapter<String> adapterzhu = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionszhu);
        adapterzhu.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapterju = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionsju);
        adapterju.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapterji = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionsji);
        adapterji.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapterjie = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, optionsjie);
        adapterjie.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 设置适配器到 Spinner
        spinnerzhu.setAdapter(adapterzhu);
        spinnerju.setAdapter(adapterju);
        spinnerji.setAdapter(adapterji);
        spinnerjie.setAdapter(adapterjie);
    }

    public void cjk(View view) {
        Intent intent = new Intent(this,scnlist.class);
        startActivity(intent);
    }

    public void zhuyouxi(View view) {
        Spinner spinnerzhu = findViewById(R.id.spinnerzhu);
        //Toast.makeText(this, "功能在开发中", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(worldscore.this, mainperson.class);
        intent.putExtra("key",spinnerzhu.getSelectedItem().toString());
        intent.putExtra("key1","主游戏");
        startActivity(intent);
    }

    public void juqing(View view) {
        Spinner spinnerju = findViewById(R.id.spinnerju);
        //Toast.makeText(this, "功能在开发中", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(worldscore.this, mainperson.class);
        String str=spinnerju.getSelectedItem().toString();
        if(str=="虚假消息")str="虚假信息";
        if(str=="地旷人稀")str="地广人稀";
        if(str=="遥传")str="谣传";
        if(str=="立百病毒")str="尼帕病毒";
        if(str=="全球暖化")str="全球变暖";
        if(str=="火山喷发")str="火山爆发";
        if(str=="颠倒乾坤")str="乾坤颠倒";
        if(str=="圣诞老人的小助手")str="圣诞蠕虫";

        intent.putExtra("key",str);
        intent.putExtra("key1","剧情");
        startActivity(intent);
    }

    public void jisu(View view) {
        Spinner spinnerji = findViewById(R.id.spinnerji);
        //Toast.makeText(this, "功能在开发中", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(worldscore.this, mainperson.class);
        intent.putExtra("key",spinnerji.getSelectedItem().toString());
        intent.putExtra("key1","急速");
        startActivity(intent);
    }

    public void jieyao(View view) {
        Spinner spinnerjie = findViewById(R.id.spinnerjie);
        //Toast.makeText(this, "功能在开发中", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(worldscore.this, mainperson.class);
        intent.putExtra("key",spinnerjie.getSelectedItem().toString());
        intent.putExtra("key1","解药");
        startActivity(intent);
    }
}