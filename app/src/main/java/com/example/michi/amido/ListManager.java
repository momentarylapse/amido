package com.example.michi.amido;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by michi on 02.01.16.
 */
public class ListManager {
    static ListManager instance;
    static Context context;

    class List {
        String type;
        String key;
        ArrayList<Integer> ids;
        public List(String type, String key, ArrayList<Integer> ids) {
            this.type = type;
            this.key = key;
            this.ids = ids;
        }
        public List(String type, String key) {
            this.type = type;
            this.key = key;
            this.ids = new ArrayList<>();
        }
        public int[] getIds() {
            int[] ids = new int[this.ids.size()];
            for (int i=0; i<ids.length; i++)
                ids[i] = this.ids.get(i);
            return ids;
        }
    }

    ArrayList<List> userLists;

    public static ListManager getInstance(Context context) {
        if (instance == null)
            instance = new ListManager(context);
        return instance;
    }

    private ListManager(Context context) {
        this.context = context;

        load();
    }

    private void load() {
        userLists = new ArrayList<>();

        File file = new File(context.getFilesDir(), "lists.xml");
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
                        if (name.equals("list")) {
                            List l = new List("", "");
                            l.type = x.getAttributeValue(null, "type");
                            l.key = x.getAttributeValue(null, "key");
                            String sids = x.getAttributeValue(null, "ids");
                            String[] strings = sids.replace("[", "").replace("]", "").split(", ");
                            for (int i = 0; i < strings.length; i++)
                                l.ids.add(Integer.parseInt(strings[i]));
                            userLists.add(l);
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
            Toast.makeText(context, "lists load error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void save() {

        File file = new File(context.getFilesDir(), "lists.xml");
        if (file.exists())
            file.delete();

        try {
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(stream, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            for (List l : userLists) {
                serializer.startTag(null, "list");
                serializer.attribute("", "type", l.type);
                serializer.attribute("", "key", l.key);
                serializer.attribute("", "ids", Arrays.toString(l.ids.toArray()));
                serializer.endTag(null, "list");
            }
            serializer.endDocument();

            serializer.flush();
            stream.close();
        } catch (IOException e) {
            Toast.makeText(context, "lists save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void addToUserList(String type, String key, int id) {
        List list = null;

        // list already exists?
        for (List l : userLists)
            if (l.type.equals(type) && l.key.equals(key))
                list = l;

        // new list
        if (list == null) {
            list = new List(type, key);
            userLists.add(list);
        }

        // already in list?
        for (int _id : list.ids)
            if (_id == id)
                return;

        list.ids.add(id);
        save();
    }

    public List makeList(String type, int first, int count) {
        int last = first + count - 1;
        String key = String.format("%d-%d", first, last);
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i=first; i<=last; i++)
            ids.add(i);
        return new List(type, key, ids);
    }

    public ArrayList<List> getUserLists(String type) {
        ArrayList<List> lists = new ArrayList<>();
        for (List l : userLists) {
            if (l.type.equals(type))
                lists.add(l);
        }
        return lists;
    }

    public ArrayList<List> getLists(String type, int step, int year) {
        ArrayList<List> lists = getUserLists(type);//new ArrayList<>();
        for (int i=1; i<=1000; i+= step)
            lists.add(makeList(type, i, step));
        /*if (year == 1) {
            for (int i=1; i<=80; i+= STEP)
                lists.add(makeList(type, i, STEP));
        } else if (year == 2) {
            for (int i=81; i<=240; i+= STEP)
                lists.add(makeList(type, i, STEP));
        } else if (year == 3) {
            for (int i=241; i<=440; i+= STEP)
                lists.add(makeList(type, i, STEP));
        } else if (year == 4) {
            for (int i=441; i<=1000; i+= STEP)
                lists.add(makeList(type, i, STEP));
        }*/
        return lists;

    }

    public List getList(String type, String key) {
        for (List l : userLists) {
            if ((l.type.equals(type)) && (l.key.equals(key)))
                return l;
        }
        String[] r = key.split("-");
        if (r.length == 2) {
            int a = Integer.parseInt(r[0]);
            int b = Integer.parseInt(r[1]);
            return makeList(type, a, b-a+1);
        }

        return new List(type, key);
    }
}
