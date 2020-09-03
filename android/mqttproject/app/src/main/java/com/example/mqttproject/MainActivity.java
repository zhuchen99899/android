package com.example.mqttproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/*MainActivity继承AppCompatActivity*/
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //这里是界面打开后 最先运行的地方
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//对应界面UI
        //一般先用来进行界面初始化 控件初始化 初始化一些参数和变量。。。。。
    }
}