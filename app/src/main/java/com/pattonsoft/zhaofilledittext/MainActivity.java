package com.pattonsoft.zhaofilledittext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FillEditView fillEditView = findViewById(R.id.fev);
        //文字组合
        ArrayList<String> strings = new ArrayList<>();
        strings.add("哈哈哈哈");
        strings.add("666");
        strings.add("qwe4556uiui");
        //空格间隔
        int spaceCount=6;
        fillEditView.init(strings, spaceCount);
    }
}
