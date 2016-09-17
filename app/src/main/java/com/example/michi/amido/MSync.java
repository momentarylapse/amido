package com.example.michi.amido;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by michi on 17.07.16.
 */
public class MSync {

    static final String SYNC_URL = "http://michi.is-a-geek.org/sync/index.php";

    enum Status{
        OK,
        DELETED,
        NEW,
        CHANGED,
        INDEX
    }

    public static class IndexItem {
        public int id;
        public int time;
        public Status status;

        public IndexItem() {
            id = -1;
            status = Status.NEW;
        }

        public IndexItem(int id, int time, Status status) {
            this.id = id;
            this.time = time;
            this.status = status;
        }

        public void setStatus(int status) {
            if (status == 0)
                this.status = Status.OK;
            else
                this.status = Status.DELETED;
        }

        public void markChanged() {
            status = Status.CHANGED;
        }

        public void markDeleted() {
            status = Status.DELETED;
        }
    }
    public static class Item extends IndexItem {
        public int uid;
        static Random rand = new Random(0);

        public Item() {
            super();
            uid = rand.nextInt();
        }

        public Item(IndexItem item) {
            id = item.id;
            time = item.time;
            status = Status.INDEX;
            uid = rand.nextInt();
        }

        public void setData(String data){};
        public String getData(){ return ""; };
    }

    static String load(String u) {
        HttpURLConnection urlConnection = null;
        String result = "";
        try {
            URL url = new URL(u);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] b = new byte[4096];
            while(true) {
                int len = in.read(b);
                if (len <= 0)
                    break;
                //readStream(in);
                String temp = new String(b);
                result = result + temp.substring(0, len);
            }
        } catch(Exception e) {
            Log.e("xxx", "MSync.load: " + e + " " + e.getMessage());
            return "";
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return result;
    }

    public static List<IndexItem> index(String user, String app) {
        List<IndexItem> list = new ArrayList<IndexItem>();

        String result = load(SYNC_URL + "?r=data/index&user=" + user + "&app=" + app);
        try {
            JSONArray a = new JSONArray(result);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                IndexItem it = new IndexItem();
                it.id = o.getInt("id");
                it.time = o.getInt("time");
                it.setStatus(o.getInt("status"));
                list.add(it);
            }
        } catch(Exception e) {
            Log.e("xxx", e.getMessage());
        }

        return list;
    }

    public static boolean get(Item it) {

        String result = load(SYNC_URL + "?r=data/show&id=" + it.id);
        try {
            JSONObject o = new JSONObject(result);
            it.id = o.getInt("id");
            it.time = o.getInt("time");
            it.setStatus(o.getInt("status"));
            it.setData(o.getString("data"));
        } catch(Exception e) {
            Log.e("xxx", e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean add(String user, String app, Item it) {
        String result = load(SYNC_URL + "?r=data/add&user=" + user + "&app=" + app + "&data=" + URLEncoder.encode(it.getData()));

        try {
            JSONObject o = new JSONObject(result);
            it.id = o.getInt("id");
            it.time = o.getInt("time");
            it.status = Status.OK;
        } catch(Exception e) {
            Log.e("xxx", e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean set(Item it) {
        String result = load(SYNC_URL + "?r=data/edit&id=" + it.id + "&data=" + URLEncoder.encode(it.getData()));

        try {
            JSONObject o = new JSONObject(result);
            it.id = o.getInt("id");
            it.time = o.getInt("time");
            it.status = Status.OK;
        } catch(Exception e) {
            Log.e("xxx", e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean delete(Item it) {
        String result = load(SYNC_URL + "?r=data/delete&id=" + it.id);

        try {
            JSONObject o = new JSONObject(result);
            it.status = Status.DELETED;
        } catch(Exception e) {
            Log.e("xxx", e.getMessage());
            return false;
        }
        return true;
    }
}
