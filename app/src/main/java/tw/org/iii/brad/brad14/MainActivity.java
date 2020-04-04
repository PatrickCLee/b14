package tw.org.iii.brad.brad14;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private SimpleAdapter adapter;                  //送資料給ListView
    private String[] from = {"field1","field2"};
    private int[] to = {R.id.item_title,R.id.item_type};
    private LinkedList<HashMap<String,String>> data = new LinkedList<>();   //*1 準備空的資料

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        initListView();                                         //*0 畫好版面也畫好item.xml
        fetchRemoteData();                                      //*2
    }

    private void initListView(){
        adapter = new SimpleAdapter(this,data,R.layout.item,from,to);
        listView.setAdapter(adapter);                                               //*1 做好空的資料
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {     //*9
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoDetail(position);                                               //*10
            }
        });
    }

    private void gotoDetail(int which){                                             //*10
        Intent intent = new Intent(this,ContentActivity.class);
        intent.putExtra("pic",data.get(which).get("pic"));
        intent.putExtra("content",data.get(which).get("content"));
        startActivity(intent);
    }

    private void fetchRemoteData(){         //*2 抓頁面原始碼,偷13的code(test3),稍微修改
        new Thread(){
            @Override
            public void run() {
                try {
                    URL url = new URL("https://data.coa.gov.tw/Service/OpenData/RuralTravelData.aspx");//open data農委會-農村小旅行
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();

                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()));
                    String line; StringBuffer sb = new StringBuffer();
                    while ( (line = reader.readLine()) != null){
                        sb.append(line);
                    }
                    reader.close();
                    parseJSON(sb.toString());           //*3
                }catch (Exception e){
                    Log.v("brad", e.toString());
                }
            }
        }.start();
    }

    private void parseJSON(String json){                //*3
        try {
            JSONArray root = new JSONArray(json);       //從網頁中可看到最外層是陣列,裡面是物件,故巡訪陣列後再來物件
            for (int i = 0; i<root.length(); i++){
                JSONObject row = root.getJSONObject(i);

                HashMap<String,String> temp = new HashMap<>();      //準備一個HM塞我們要的資料
                temp.put(from[0], row.getString("Title"));
                temp.put(from[1], row.getString("TravelType")   //網頁中此項目有跳脫字元,故於此處置換
                .replace('\n',' ')
                .replace('\r',' ')
                .replace("  ",""));
                temp.put("pic", row.getString("PhotoUrl"));         //此兩行不會顯示在螢幕上,只是在資料結構上增加內容
                temp.put("content", row.getString("Contents"));
                data.add(temp);
            }
            uiHandler.sendEmptyMessage(0);          //*6 解析JSON不費時,故全部解完再發出
        } catch (Exception e) {
            Log.v("brad",e.toString());
        }
    }

    private  UIHandler uiHandler = new UIHandler();         //*5 要記得有物件
    private class UIHandler extends Handler {               //*4 由於解JSON時必須在執行序,執行序要影響畫面需要Handler
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            adapter.notifyDataSetChanged();                 //*7
        }
    }
}
