package com.example.mynote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.mynote.R.drawable.back_img1;
import static com.example.mynote.R.drawable.back_img2;

public class MainActivity extends AppCompatActivity {

    private final List<Map<String, String>> contentList = new ArrayList<>();
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton addContent = findViewById(R.id.add_note);
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
        int randomNumber = (int) (Math.random() * 2);
        switch (randomNumber) {
            case 0:
                recyclerView.setBackgroundResource(back_img1);
                break;
            case 1:
                recyclerView.setBackgroundResource(back_img2);
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
    protected void onStart() {
        super.onStart();
        contentList.clear();
        // 初始化数据
        initContent();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        // 瀑布流布局样式
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
                intent.putExtra("EXT_content", content);
                intent.putExtra("EXT_time", time);
                intent.putExtra("EXT_image", image);
                // Log.i(TAG, ">>>传递的图像：" + image);
                startActivity(intent);
            }

            // 长按
            @Override
            public void onLongClick(final int position) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示框")
                        .setMessage("是否删除此事项?")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String content = contentList.get(position).get("content");
                                        LitePal.deleteAll(Note.class, "content=?", content);
                                        Toast.makeText(MainActivity.this, "删除成功.", Toast.LENGTH_SHORT).show();
                                        contentList.remove(position);
                                        Log.i(TAG, ">>>" + contentList);
                                        adapter.notifyItemRemoved(position);
                                    }
                                })
                        .setNegativeButton("no", null).show();
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void initContent() {
        List<Note> notes = LitePal.order("id desc").find(Note.class);
        for (Note note : notes) {
            String content = note.getContent();
            String time = note.getTime();
            String imgpath = note.getArticleImagePath();
            Map<String, String> map = new HashMap<>();
            map.put("content", content);
            map.put("time", time);
            map.put("imagepath", imgpath);
            contentList.add(map);
        }
    }
}