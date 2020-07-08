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

public class MainActivity extends AppCompatActivity {

    private List<Map<String, String>> contentList = new ArrayList<Map<String, String>>();
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton addContent = (FloatingActionButton) findViewById(R.id.add_tickler);
        // 点击
        addContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddContentActivity.class);
                startActivity(intent);
            }
        });

        // over
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
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // 布局样式
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        final ContentAdapter adapter = new ContentAdapter(contentList);

        adapter.setOnItemClickListener(new ContentAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                String content = contentList.get(position).get("content");
                String time = contentList.get(position).get("time");
                Intent intent = new Intent(MainActivity.this, AddContentActivity.class);
                intent.putExtra(AddContentActivity.CONTENT, content);
                intent.putExtra(AddContentActivity.TIME, time);
                startActivity(intent);
//                Log.e(TAG, "点击！！" + position);
            }

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
            Map<String, String> map = new HashMap<String, String>();
            map.put("content", content);
            map.put("time", time);
            contentList.add(map);
        }
    }
}