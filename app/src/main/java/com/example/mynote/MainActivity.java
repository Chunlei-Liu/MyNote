package com.example.mynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.graphics.Color.*;
import static com.example.mynote.R.drawable.back_img1;
import static com.example.mynote.R.drawable.back_img2;
import static com.example.mynote.R.drawable.back_img3;
import static com.example.mynote.R.drawable.back_img4;
import static com.example.mynote.R.drawable.back_img5;
import static com.example.mynote.R.drawable.back_img6;
import static com.example.mynote.R.drawable.back_img7;

public class MainActivity extends AppCompatActivity {

    private List<Map<String, String>> contentList = new ArrayList<>();
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton addContent = findViewById(R.id.add_tickler);
        // 点击添加按钮
        addContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddContentActivity.class);
                startActivity(intent);
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        // 随机更改背景
        int randomNumber = (int) (Math.random() * 7);
        switch (randomNumber) {
            case 0:
                recyclerView.setBackgroundResource(back_img1);
                break;
            case 1:
                recyclerView.setBackgroundResource(back_img2);
                break;
            case 2:
                recyclerView.setBackgroundResource(back_img3);
                break;
            case 3:
                recyclerView.setBackgroundResource(back_img4);
                break;
            case 4:
                recyclerView.setBackgroundResource(back_img5);
                break;
            case 5:
                recyclerView.setBackgroundResource(back_img6);
                break;
            case 6:
                recyclerView.setBackgroundResource(back_img7);
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    protected void onStart() {//每次活动有不可见变可见时调用
        super.onStart();
        contentList.clear();//清空list子项数据，实现刷新list
        initContent();//初始化
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        // 布局样式
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // 瀑布流
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        final ContentAdapter adapter = new ContentAdapter(contentList);
        // item点击和长按事件
        adapter.setOnItemClickListener(new ContentAdapter.OnItemClickListener() {
            // 点击
            @Override
            public void onClick(int position) {
                String content = contentList.get(position).get("content");
                String time = contentList.get(position).get("time");
                String image = contentList.get(position).get("imagepath");
                Intent intent = new Intent(MainActivity.this, AddContentActivity.class);
                intent.putExtra(AddContentActivity.CONTENT, content);
                intent.putExtra(AddContentActivity.TIME, time);
                intent.putExtra(AddContentActivity.editImagePath, image);
                Log.i(TAG, ">>>,put," + image);
                startActivity(intent);
//                Log.e(TAG, "点击！！" + position);
            }

            // 长按
            @Override
            public void onLongClick(final int position) {
//                Log.e(TAG, "长按！！" + position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示框")
                        .setMessage("是否删除?")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                                        int position = holder.getAdapterPosition();
                                        String content = contentList.get(position).get("content");
                                        LitePal.deleteAll(Tickler.class, "content=?", content);
                                        Toast.makeText(MainActivity.this, "删除成功.", Toast.LENGTH_SHORT).show();
                                        contentList.remove(position);
                                        Log.e(TAG, "!!!" + contentList);
                                        adapter.notifyItemRemoved(position);
//                                        adapter.notifyItemRangeChanged(position, contentList.size());
                                    }
                                })
                        .setNegativeButton("no", null).show();
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void initContent() {
        List<Tickler> ticklers = LitePal.order("id desc").find(Tickler.class);
        for (Tickler tickler : ticklers) {
            String content = tickler.getContent();
            String time = tickler.getTime();
            String imgpath = tickler.getArticleImagePath();
            Map<String, String> map = new HashMap<>();
            map.put("content", content);
            map.put("time", time);
            map.put("imagepath", imgpath);
            contentList.add(map);
        }
    }


}