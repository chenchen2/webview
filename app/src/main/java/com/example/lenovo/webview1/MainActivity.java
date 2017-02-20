package com.example.lenovo.webview1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import static android.webkit.WebView.enableSlowWholeDocumentDraw;


public class MainActivity extends AppCompatActivity {

    private WebView webview;
    private Button cut;
    private String url = "http://123.57.213.217:81/html/login.html";
    private static final String APP_SCHEME = "example-app:";
    private TextView title;
    private Button back;
    private View.OnClickListener myListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.cut:
                    cutScreen();
                    break;
                case R.id.back:
                    goBack();
                    break;
                default:
                    break;
            }
        }
    };
    private Bitmap bitmap;

    /**
     * 回退到初始状态
     */
    private void goBack() {

        cut.setVisibility(View.VISIBLE);
        back.setVisibility(View.GONE);
    }

    /**
     * 截图 并保存
     */
    private void cutScreen() {
        //剪切图片
        float scale = webview.getScale();
        int webViewHeight = (int) (webview.getContentHeight()*scale);
        bitmap = Bitmap.createBitmap(webview.getWidth(),webViewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        webview.draw(canvas);
        cut.setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);

        //判断是否图片保存
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("温馨提示");
        adb.setMessage("是否保存图片");
        adb.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //将图片保存
                saveImagee(MainActivity.this,bitmap);
            }
        });
        adb.setNegativeButton("取消",null);
        adb.create();
        adb.show();

        

    }

    private void saveImagee(Context context, Bitmap bitmap) {
        //将图片保存到固定文件夹
        File appDir = new File(Environment.getExternalStorageDirectory(),"webview");
        if(!appDir.exists()){
            appDir.mkdir();
        }
        String filename = System.currentTimeMillis()+".jpg";
        File file = new File(appDir,filename);

        try {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            Log.e("===","6");
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),filename,null);
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(file));
            sendBroadcast(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
        Toast.makeText(MainActivity.this,"保存成功",Toast.LENGTH_LONG).show();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableSlowWholeDocumentDraw();
        setContentView(R.layout.activity_main);
        findView();
        init();
        setOnClick();
    }

    /**
     * 查找
     */
    private void findView() {

        title = (TextView)findViewById(R.id.title);
        cut = (Button)findViewById(R.id.cut);
        back = (Button)findViewById(R.id.back);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()){
            webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 初始化
     */
    private void init() {

        webview = (WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);

        //webView中java和js交互
        //获取当前ua
        String ua = webview.getSettings().getUserAgentString();
        //在当前ua字符串的末端增加app的标识和版本信息好
        webview.getSettings().setUserAgentString(ua+"APP_TAG/5.0.1");


        //打开网页时不掉用系统浏览器，而在本webview中展示
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);
                return true;
            }
        });
        //动态获取标题
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title1) {
                title.setText(title1);
                super.onReceivedTitle(view, title1);
            }
        });
        //弹出提示框
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return true;
            }
        });

        webview.loadUrl(url);

     }

     /**
     * 设置点击事件
     */
    private void setOnClick() {
        cut.setOnClickListener(myListener);
        back.setOnClickListener(myListener);

    }
}
