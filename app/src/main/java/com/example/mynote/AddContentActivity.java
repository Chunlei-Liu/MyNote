package com.example.mynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddContentActivity extends AppCompatActivity {

    public static final String CONTENT = "content";
    public static final String TIME = "time";
    private static final String TAG = "AddContentActivity";
    private String time;
    private EditText content;
    private TextView showTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);
        content = (EditText) findViewById(R.id.add_content);
        showTime = (TextView) findViewById(R.id.time_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_1);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {//显示系统返回按钮
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //取得内容和时间
        Intent intent = getIntent();
        time = intent.getStringExtra(TIME);
        String showContent = intent.getStringExtra(CONTENT);
        showTime.setText(time);
        content.setText(showContent);
        if (showContent != null) {
            content.setSelection(showContent.length());//将光标移动到文本最后
        }
    }

    //配置菜单项设置点击事件
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_content:
                //判断当前操作是新增还是修改
                if (time != null) {
                    Intent intent = getIntent();
                    String showContent = intent.getStringExtra(CONTENT);

                    String inputText = content.getText().toString();
                    Tickler tickler = new Tickler();
//                    Log.e(TAG, "" + inputText);
//                    Log.e(TAG, "" + showContent.toString());
                    // 当内容与之前不相同是进行修改操作
                    if (!inputText.equals(showContent)) {
                        tickler.setContent(inputText);
                        tickler.updateAll("time=?", time);
                        Toast.makeText(this, "修改成功.", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    break;


                } else {
                    //取得新增记录时的系统时间
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                    Date date = new Date(System.currentTimeMillis());
                    String inputText = content.getText().toString();
                    // 当输入内容正常时进行保存
                    if (!inputText.equals("") && inputText != null) {
                        Tickler tickler = new Tickler();
                        tickler.setContent(inputText);
                        tickler.setTime(simpleDateFormat.format(date));
                        tickler.save();
                        Toast.makeText(this, "保存成功.", Toast.LENGTH_SHORT).show();
                        finish();//操作完成结束当前活动
                        break;
                    } else {
                        Toast.makeText(this, "输入内容为空.", Toast.LENGTH_LONG).show();
//                        finish();
                        break;
                    }
                }

            case android.R.id.home://一定添加android还有下面这行代码，我当时为这搞了半天
                onBackPressed();
                return true;
            default:
        }
        return true;
    }
}
