package tw.org.iii.brad.brad14;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.net.HttpURLConnection;
import java.net.URL;

public class ContentActivity extends AppCompatActivity {
    private ImageView img;
    private TextView content;
    private String strPic;
    private String strContent;                              //*11
    private UIHandler uiHandler = new UIHandler();          //*13
    private Bitmap bmp;                                     //*13

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        img = findViewById(R.id.content_img);
        content = findViewById(R.id.content_content);

        strPic = getIntent().getStringExtra("pic").replace("http","https");
        strContent = getIntent().getStringExtra("content");

        content.setText(strContent);                        //*11
//        fetchImage();
        fetchImageV2();
    }

    private void fetchImage(){                              //*12 偷13的
        new Thread(){
            @Override
            public void run() {
                try{
                    URL url = new URL(strPic);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.connect();

                    // 1. conn.getInputStream()
                    // 2. ImageView
                    bmp = BitmapFactory.decodeStream(conn.getInputStream());
                    uiHandler.sendEmptyMessage(0);



                }catch (Exception e){
                    Log.v("brad",e.toString());
                }
            }
        }.start();
    }

    private void fetchImageV2(){                            //先看brad15, 16 再回來 *14 Volley
        RequestQueue queue = Volley.newRequestQueue(this);
        ImageRequest request = new ImageRequest(
                strPic,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        img.setImageBitmap(response);
                    }
                },
                0, 0,
                Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("brad",error.toString());
                    }
                }
        );
        queue.add(request);
    }

    private class UIHandler extends Handler {           //*13
        @Override
        public void handleMessage(@NonNull Message msg) {
//            try {
//                Thread.sleep(3000);
//            } catch (Exception e) {
//                Log.v("brad",e.toString());
//            }
            super.handleMessage(msg);
            img.setImageBitmap(bmp);
        }
    }
}
