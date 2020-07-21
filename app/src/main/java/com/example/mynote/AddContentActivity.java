package com.example.mynote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddContentActivity extends AppCompatActivity {

    public static String CONTENT = "content";
    public static String TIME = "time";
    public static String editImagePath = "";
    private static final String TAG = "AddContentActivity";
    private String time;
    private EditText content;
    private TextView showTime;
    private ImageView imageView;
    public static final int CHOOSE_ARTICLE_IMAGE = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);
        content = findViewById(R.id.add_content);
        showTime = findViewById(R.id.time_show);
        imageView = findViewById(R.id.edit_img);
        Toolbar toolbar = findViewById(R.id.toolbar_1);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {//显示系统返回按钮
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //取得内容和时间
        Intent intent = getIntent();
        time = intent.getStringExtra(TIME);
        String showContent = intent.getStringExtra(CONTENT);
        String imagepath = intent.getStringExtra(editImagePath);
        Log.i(TAG, ">>>,get Extra:" + imagepath);
        displayImage(imagepath);
        showTime.setText(time);
        content.setText(showContent);
        if (showContent != null) {
            content.setSelection(showContent.length());//将光标移动到文本最后
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AddContentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddContentActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_ARTICLE_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKiKat(data);
                    } else {
                        handleImageBeforeKiKat(data);
                    }
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKiKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如或是content类型的URI就使用普通方法处理
            imagePath = getImagePath(uri, null);

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的直接获取图片路径就行
            imagePath = uri.getPath();
        }
        editImagePath = imagePath;
        // 根据图片路径显示图片
        displayImage(imagePath);
    }

    private void handleImageBeforeKiKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private void openAlbum() {
        Intent mIntent = new Intent("android.intent.action.GET_CONTENT");
        mIntent.setType("image/*");
        startActivityForResult(mIntent, CHOOSE_ARTICLE_IMAGE);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    // 通过路径显示图像
    private void displayImage(String imagePath) {
        if (!TextUtils.isEmpty(imagePath)) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            Log.i(TAG, "show>>>>" + imagePath);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.demo_img);
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
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
                    Date date = new Date(System.currentTimeMillis());
                    String inputText = content.getText().toString();
                    // 当输入内容不为空时进行保存
                    if (!inputText.equals("")) {
                        Tickler tickler = new Tickler();
                        tickler.setContent(inputText);
                        tickler.setTime(simpleDateFormat.format(date));
                        tickler.setArticleImagePath(editImagePath);
                        Log.i(TAG, "save>>" + simpleDateFormat.format(date) + "##" + inputText + "##" + editImagePath);
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
