package com.example.michi.amido;

import android.content.Context;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michi on 10.01.16.
 */
public class Settings {

    public static Settings getInstance(Context context) {
        if (instance == null)
            instance = new Settings(context);
        return instance;
    }
    private static Settings instance;

    private Context context;

    private boolean adminAllowed;
    private boolean adminEnabled;
    private boolean showKana;
    private int learnShowCount;
    private int learnDrawCount;


    List<String> learnCountList = new ArrayList<>();

    public List<String> getLearnCountList() {
        return learnCountList;
    }

    public int getLearnCountListIndex(int count) {
        for (String l : learnCountList)
            if (Integer.valueOf(l) == count)
                return learnCountList.indexOf(l);
        return 0;
    }

    private Settings(Context context) {
        this.context = context;
        learnCountList.add("5");
        learnCountList.add("10");
        learnCountList.add("20");
        learnCountList.add("50");

        reset();

        load();
    }

    private void reset() {
        adminAllowed = false;
        adminEnabled = false;
        showKana = false;
        learnShowCount = 10;
        learnDrawCount = 5;
    }

    public int getLearnDrawCount() {
        return learnDrawCount;
    }

    public void setLearnDrawCount(int learnDrawCount) {
        this.learnDrawCount = learnDrawCount;
        save();
    }

    public int getLearnShowCount() {
        return learnShowCount;
    }

    public void setLearnShowCount(int learnShowCount) {
        this.learnShowCount = learnShowCount;
        save();
    }

    public int getLearnCount(String method) {
        if (method.equals("show"))
            return learnShowCount;
        if ((method.equals("draw")) || (method.equals("flash")))
            return learnDrawCount;
        return 10;
    }

    public void load() {

        File file = new File(context.getFilesDir(), "settings.xml");
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
                        if (name.equals("settings")) {
                            if (x.getAttributeValue("", "admin-allowed") != null)
                                adminAllowed = x.getAttributeValue("", "admin-allowed").equals("true");
                            if (x.getAttributeValue("", "admin-enabled") != null)
                                adminEnabled = x.getAttributeValue("", "admin-enabled").equals("true");
                            if (x.getAttributeValue("", "show-kana") != null)
                                showKana = x.getAttributeValue("", "show-kana").equals("true");
                            if (x.getAttributeValue("", "learn-draw-count") != null)
                                learnDrawCount = Integer.valueOf(x.getAttributeValue("", "learn-draw-count"));
                            if (x.getAttributeValue("", "learn-show-count") != null)
                                learnShowCount = Integer.valueOf(x.getAttributeValue("", "learn-show-count"));
                        }
                        break;
                }
                event = x.next();
            }
        } catch (Exception e) {
            Toast.makeText(context, "settings load error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void save() {
        File file = new File(context.getFilesDir(), "settings.xml");
        if (file.exists())
            file.delete();

        try {
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(stream, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "settings");
            serializer.attribute("", "admin-allowed", adminAllowed ? "true" : "false");
            serializer.attribute("", "admin-enabled", adminEnabled ? "true" : "false");
            serializer.attribute("", "show-kana", showKana ? "true" : "false");
            serializer.attribute("", "learn-show-count", "" + learnShowCount);
            serializer.attribute("", "learn-draw-count", "" + learnDrawCount);
            serializer.endTag(null, "settings");
            serializer.endDocument();
            serializer.flush();
            stream.close();
        } catch (IOException e) {
            Toast.makeText(context, "settings save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isShowKana() {
        return showKana;
    }

    public void setShowKana(boolean showKana) {
        this.showKana = showKana;
        save();
    }

    public void setAdminEnabled(boolean adminEnabled) {
        this.adminEnabled = adminEnabled;
        save();
    }

    public void setAdminAllowed(boolean adminAllowed) {
        this.adminAllowed = adminAllowed;
        save();
    }

    public boolean isAdmin() {
        return adminAllowed && adminEnabled;
    }

    public boolean isAdminAllowed() {
        return adminAllowed;
    }

    public boolean isAdminEnabled() {
        return adminEnabled;
    }


}
