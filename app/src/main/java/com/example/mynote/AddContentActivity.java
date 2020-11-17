package com.example.mynote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddContentActivity extends AppCompatActivity {

    private static final String TAG = "AddContentActivity";
    private String show_time;
    private String show_image;
    private String show_content;
    // 当前修改后的图像路径
    private String tmp_img;

    private EditText editText_content;
    private ImageView imageView;
    public static final int CHOOSE_IMAGE = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);
        // 绑定控件
        editText_content = findViewById(R.id.add_content);
        ToggleButton toggleButton = findViewById(R.id.add_togglebutton);
        TextView textView_time = findViewById(R.id.time_show);
        Toolbar toolbar = findViewById(R.id.toolbar_1);
        imageView = findViewById(R.id.edit_img);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        // 显示返回按钮
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 取得内容和时间
        Intent intent = getIntent();
        show_time = intent.getStringExtra("EXT_time");
        show_content = intent.getStringExtra("EXT_content");
        show_image = intent.getStringExtra("EXT_image");
        // 判断新建还是修改
        Log.i(TAG, ">>>获得的图像：" + show_image);
        textView_time.setText(show_time);
        editText_content.setText(show_content);

        // 移动光标到末尾
        if (show_content != null && !show_content.equals("")) {
            editText_content.setSelection(show_content.length());
        }
        // 判读是否显示图像
        if (show_image != null && !show_image.equals("")) {
            toggleButton.setChecked(true);
            displayImage(show_image);
        }
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    imageView.setVisibility(View.VISIBLE);
                    Log.i(TAG, ">>>选择使用图像");
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
                } else {
                    imageView.setVisibility(View.INVISIBLE);
                    tmp_img = "";
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE) {
            if (resultCode == RESULT_OK) {
                handleImageOnKiKat(Objects.requireNonNull(data));
            }
        }
    }

    private void handleImageOnKiKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.i(TAG, ">>>uri: " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String docId = DocumentsContract.getDocumentId(uri);
            Log.i(TAG, ">>>docId: " + docId);
            if ("com.android.providers.media.documents".equals(Objects.requireNonNull(uri).getAuthority())) {
                String id = docId.split(":")[1];
                Log.i(TAG, ">>>id: " + id);
                String selection = MediaStore.Images.Media._ID + "=" + id;
                Log.i(TAG, ">>>selection: " + selection);
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                Log.i(TAG, ">>>imagePath: " + imagePath);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }
        tmp_img = imagePath;
        // 根据图片路径显示图片
        displayImage(imagePath);
    }


    private void openAlbum() {
        Intent mIntent = new Intent("android.intent.action.GET_CONTENT");
        // 指定显示图像
        mIntent.setType("image/*");
        startActivityForResult(mIntent, CHOOSE_IMAGE);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        Log.i(TAG, ">>>cursor: " + cursor);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                Log.i(TAG, ">>>path: " + path);
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
                if (show_time != null) {
                    //noinspection unused
                    Intent intent = getIntent();

                    String inputText = editText_content.getText().toString();

                    Log.i(TAG, ">>>show_image：" + show_image);
                    Log.i(TAG, ">>>tmp_img：" + tmp_img);
                    Note note = new Note();
                    // 当内容与之前不相同是进行修改操作
                    if (!inputText.equals(show_content) || (tmp_img != null && !tmp_img.equals(show_image))) {
                        note.setContent(inputText);
                        note.setArticleImagePath(tmp_img);
                        note.updateAll("time=?", show_time);
                        Toast.makeText(this, "修改成功.", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Save(修改)>>>" + show_image);
                    }
                    finish();
                } else {
                    //取得新增记录时的系统时间
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.getDefault());
                    Date date = new Date(System.currentTimeMillis());
                    String inputText = editText_content.getText().toString();
                    // 当输入内容不为空时进行保存
                    if (!inputText.equals("")) {
                        Note note = new Note();
                        note.setContent(inputText);
                        note.setTime(simpleDateFormat.format(date));
                        note.setArticleImagePath(tmp_img);
                        Log.i(TAG, "Save(新增)>>>" + simpleDateFormat.format(date) + "##" + inputText + "##" + show_image);
                        note.save();
                        Toast.makeText(this, "保存成功.", Toast.LENGTH_SHORT).show();
                        finish();//操作完成结束当前活动
                    } else {
                        Toast.makeText(this, "内容为空.", Toast.LENGTH_SHORT).show();
//                        finish();
                    }
                }
                break;
            //一定添加android还有下面这行代码，我当时为这搞了半天
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
        }
        return true;
    }
}
