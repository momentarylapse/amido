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

    public int getLearnedCount(String type) {
        int count = 0;
        return count;
    }
}
