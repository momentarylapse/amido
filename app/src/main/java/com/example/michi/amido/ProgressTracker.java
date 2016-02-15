package com.example.michi.amido;

import android.content.Context;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by michi on 02.01.16.
 */
public class ProgressTracker {
    class Success {
        String type;
        String method;
        String key;
        float score;
        Date date;
        public Success(String type, String method, String key, float score) {
            this.type = type;
            this.method = method;
            this.key = key;
            this.score = score;
            this.date = new Date();
        }
        public String toString() {
            return type + "/" + method + "/" + key + "/" + score + "/" + date.toString();
        }
    }

    ArrayList<Success> successes;

    static Context context;

    static ProgressTracker instance;
    public static ProgressTracker getInstance(Context context) {
        if (instance == null)
            instance = new ProgressTracker(context);
        return instance;
    }

    private ProgressTracker(Context context) {
        this.context = context;
        load();
    }

    public void add(String type, String method, String key, float score) {
        successes.add(new Success(type, method, key, score));
        save();
    }

    public Date getLast(String type, String method, String key) {
        Date date = null;
        for (Success s : successes) {
            if (s.type.equals(type) && s.method.equals(method) && s.key.equals(key)) {
                if (date == null)
                    date = s.date;
                else if (date.before(s.date))
                    date = s.date;
            }
        }
        return date;
    }

    public float getBest(String type, String method, String key) {
        float best = 0;
        for (Success s : successes) {
            if (s.type.equals(type) && s.method.equals(method) && s.key.equals(key)) {
                if (s.score > best)
                    best = s.score;
            }
        }
        return best;
    }

    public void save() {
        File file = new File(context.getFilesDir(), "progress.txt");
        if (file.exists())
            file.delete();

        try {
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(stream, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            for (Success s : successes) {
                serializer.startTag(null, "success");
                serializer.attribute("", "type", s.type);
                serializer.attribute("", "method", s.method);
                serializer.attribute("", "key", s.key);
                serializer.attribute("", "date", String.valueOf(s.date.getTime()));
                serializer.attribute("", "score", String.valueOf(s.score));
                serializer.endTag(null, "success");
            }
            serializer.endDocument();

            serializer.flush();
            stream.close();
        } catch (IOException e) {
            Toast.makeText(context, "progress save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void load() {
        successes = new ArrayList<>();

        File file = new File(context.getFilesDir(), "progress.txt");
        if (!file.exists())
            return;
        try {
            FileInputStream stream = new FileInputStream(file);

            XmlPullParserFactory xf = XmlPullParserFactory.newInstance();

            XmlPullParser x = xf.newPullParser();
            x.setFeature(x.FEATURE_PROCESS_NAMESPACES, false);
            x.setInput(stream, null);

            int event = x.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = x.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (name.equals("success")) {
                            Success s = new Success("", "", "", 0);
                            s.type = x.getAttributeValue(null, "type");
                            s.method = x.getAttributeValue(null, "method");
                            s.key = x.getAttributeValue(null, "key");
                            s.score = Float.valueOf(x.getAttributeValue(null, "score"));
                            s.date = new Date(Long.valueOf(x.getAttributeValue(null, "date")));
                            successes.add(s);
                            //Toast.makeText(context, s.toString(), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = x.next();
            }
        } catch (Exception e) {
            Toast.makeText(context, "progress load error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    static String niceDate(Date date) {
        Date now = new Date();
        int hours = (int)((now.getTime() - date.getTime()) / (60*60*1000));
        if (hours < 24)
            return String.format(context.getResources().getString(R.string.date_hours_ago), hours);
        return String.format(context.getResources().getString(R.string.date_days_ago), hours/24);
    }

    public float getScore(ListManager.List l, String method) {

        Date now = new Date();
        int count = 0;
        Date last = null;
        for (Success s : successes) {
            if ((!s.type.equals(l.type)) || (!s.key.equals(l.key)) || (!s.method.equals(method)))
                continue;
            count ++;
            if (last != null) {
                if (last.before(s.date))
                    last = s.date;
            } else
                last = s.date;
        }

        if (count >= 1) {
            float days = (now.getTime() - last.getTime()) / (24.0f*60*60*1000);

            return count / days * 5.0f;
        }
        return 0;
    }

    public ArrayList<String> getKeys(String type, String method) {
        ArrayList<String> keys = new ArrayList<>();

        for (Success s : successes) {
            if (!s.type.equals(type))
                continue;
            if (!s.method.equals(method))
                continue;
            boolean found = false;
            for (String k : keys)
                if (k.equals(s.key)) {
                    found = true;
                    break;
                }
            if (!found)
                keys.add(s.key);
        }
        return keys;
    }

    public int getLearnedCount(String type, String method) {
        float[] scores = new float[2000];
        ListManager lm = ListManager.getInstance(context);

        ArrayList<String> keys = getKeys(type, method);

        for (String key : keys) {
            ListManager.List l = lm.getList(type, key);
            float score = getScore(l, method);
            for (int i : l.ids)
                scores[i] = score;
        }

        int learned_count = 0;
        for (float s : scores)
            if (s >= 1.0)
                learned_count++;
        return learned_count;
    }
}
