package com.zmmxkid.pitool;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import static com.zmmxkid.pitool.MainActivity.randomNumber;

public class caculate extends AppCompatActivity {

    // true = 简单计算页，false = 高级计算页
    private boolean isSimpleTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caculate);

        // ===== 随机背景 =====
        ConstraintLayout backLayout = findViewById(R.id.background);
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
                backLayout.setBackgroundResource(R.drawable.img11);
                break;
        }

        // ===== Spinner 初始化 =====
        Spinner spinner1 = findViewById(R.id.spinner2);
        Spinner spinner2 = findViewById(R.id.spinner3);

        String[] options1 = {
                "细菌", "病毒", "真菌", "寄生虫", "朊病毒",
                "纳米病毒", "生化武器", "Neurax蠕虫",
                "Necora病毒", "猩猩流感", "暗影瘟疫"
        };
        String[] options2 = {"简单", "普通", "困难", "天灾/终极困难"};

        ArrayAdapter<String> adapter1 =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> adapter2 =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options2);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);

        // ===== 初始化 =====
        initRealtimeCalculate();
        switchToSimpleTab(); // 默认显示简单计算
    }

    /* =======================
       Tab 切换
       ======================= */
    public void onTabClick(View view) {
        if (view.getId() == R.id.qiehuan && !isSimpleTab) {
            switchToSimpleTab();
        } else if (view.getId() == R.id.textView2 && isSimpleTab) {
            switchToAdvancedTab();
        }
    }

    private void switchToSimpleTab() {
        isSimpleTab = true;

        findViewById(R.id.linearLayout8).setVisibility(View.GONE);
        findViewById(R.id.linearLayoutsc).setVisibility(View.VISIBLE);

        TextView tabLeft = findViewById(R.id.qiehuan);
        TextView tabRight = findViewById(R.id.textView2);

        tabLeft.setBackgroundResource(R.drawable.bg_tab_active);
        tabLeft.setTextColor(getColor(R.color.tab_text_active));

        tabRight.setBackgroundResource(0);
        tabRight.setTextColor(getColor(R.color.tab_text_inactive));

        calculateSimple();
    }

    private void switchToAdvancedTab() {
        isSimpleTab = false;

        findViewById(R.id.linearLayout8).setVisibility(View.VISIBLE);
        findViewById(R.id.linearLayoutsc).setVisibility(View.GONE);

        TextView tabLeft = findViewById(R.id.qiehuan);
        TextView tabRight = findViewById(R.id.textView2);

        tabLeft.setBackgroundResource(0);
        tabLeft.setTextColor(getColor(R.color.tab_text_inactive));

        tabRight.setBackgroundResource(R.drawable.bg_tab_active);
        tabRight.setTextColor(getColor(R.color.tab_text_active));

        calculateAdvanced();
    }


    /* =======================
       实时监听
       ======================= */

    private void initRealtimeCalculate() {

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSimpleTab) calculateSimple();
                else calculateAdvanced();
            }
        };

        int[] edits = {
                R.id.editTextText, R.id.editTextText2, R.id.editTextText3,
                R.id.editdaytext, R.id.editcuretext, R.id.editdnatext,
                R.id.edityellowtext, R.id.editdietext, R.id.editdnahua
        };

        for (int id : edits) {
            EditText et = findViewById(id);
            if (et != null) et.addTextChangedListener(watcher);
        }

        AdapterView.OnItemSelectedListener spinnerListener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        calculateAdvanced();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                };

        ((Spinner) findViewById(R.id.spinner2)).setOnItemSelectedListener(spinnerListener);
        ((Spinner) findViewById(R.id.spinner3)).setOnItemSelectedListener(spinnerListener);
    }

    /* =======================
       简单计算
       ======================= */

    private void calculateSimple() {
        try {
            float day = Float.parseFloat(((EditText) findViewById(R.id.editTextText)).getText().toString());
            float cure = Float.parseFloat(((EditText) findViewById(R.id.editTextText2)).getText().toString());
            float score = Float.parseFloat(((EditText) findViewById(R.id.editTextText3)).getText().toString());

            float result = 20000000 * (1 + (1 - cure) / 3) * (1 + score) / day;
            ((TextView) findViewById(R.id.textView10)).setText(String.valueOf(result));
        } catch (Exception ignored) {
        }
    }

    /* =======================
       高级计算
       ======================= */

    private void calculateAdvanced() {
        try {
            Spinner spinner1 = findViewById(R.id.spinner2);
            Spinner spinner2 = findViewById(R.id.spinner3);

            float day = Float.parseFloat(((EditText) findViewById(R.id.editdaytext)).getText().toString());
            float cure = Float.parseFloat(((EditText) findViewById(R.id.editcuretext)).getText().toString());
            float dna = Float.parseFloat(((EditText) findViewById(R.id.editdnatext)).getText().toString());
            float yellow = Float.parseFloat(((EditText) findViewById(R.id.edityellowtext)).getText().toString());
            float die = Float.parseFloat(((EditText) findViewById(R.id.editdietext)).getText().toString());
            float dnahua = Float.parseFloat(((EditText) findViewById(R.id.editdnahua)).getText().toString());

            String bacteria = spinner1.getSelectedItem().toString();
            String diff = spinner2.getSelectedItem().toString();

            float s1 = 1f;
            if (bacteria.equals("Neurax蠕虫")) s1 = 1.1f;
            else if (bacteria.equals("Necora病毒")) s1 = 1.4f;
            else if (!bacteria.equals("细菌") && !bacteria.equals("猩猩流感") && !bacteria.equals("暗影瘟疫"))
                s1 = 0.9f;

            float s2 = 1f;
            if (diff.equals("普通")) s2 = 4f;
            else if (diff.equals("困难")) s2 = bacteria.equals("Necora病毒") ? 10f : 8f;
            else if (diff.equals("天灾/终极困难")) s2 = bacteria.equals("Necora病毒") ? 10f : 12f;

            float dianshu = dna * 2 + dnahua;
            float jiyin = yellow * dianshu;

            float jieyao = 100 * cure;
            if (jieyao < 1) jieyao = 1;
            if (jieyao > 100) jieyao = bacteria.equals("Necora病毒") ? 400 : 100;
            if (bacteria.equals("Necora病毒")) jieyao /= 5;

            day = (float) Math.floor(Math.log(day));
            if (day <= 0) day = 1;

            float result = (float) (10 * Math.floor(((jiyin * s1 * s2) / (jieyao * day) + die * 100)));
            ((TextView) findViewById(R.id.textView10)).setText(String.valueOf(result));

        } catch (Exception ignored) {
        }
    }
}
