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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by michi on 17.09.16.
 */

public abstract class MSyncDatabase<ItemClass extends MSync.Item> {



    static Context context;

    String app, user;
    String filename;

    boolean dirty;

    List<ItemClass> list;
    List<OnChangeListener> onChangeListeners = new ArrayList<>();

    class SyncThread extends Thread {

        public void run() {
            while(true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
                if (dirty)
                    sync();
            }

        }
    };
    SyncThread syncThread;

    public interface OnChangeListener {
        public void onChange();
    }

    public interface OnCategoriesChangeListener {
        public void onChange();
    }

    protected MSyncDatabase(String app, String user, Context context) {
        list = new ArrayList<>();
        this.context = context;
        this.app = app;
        this.user = user;
        this.filename = app + ".xml";
        dirty = true;
        load();
        markForSync();

        syncThread = new SyncThread();
        syncThread.start();
    }

    public void markForSync() {
        dirty = true;
        onChange();
    }

    public void sync() {
        Log.w("xxxx", "sync");
        if (user.isEmpty())
            return;

        // upload new/changed
        for (MSync.Item item: list){
            if (item.status == MSync.Status.NEW) {
                if (MSync.add(user, app, item)) {
                }
            } else if (item.status == MSync.Status.CHANGED) {
                if (MSync.set(item)) {
                }
            } else if (item.status == MSync.Status.DELETED){
                if (MSync.delete(item)){
                    list.remove(item);
                }
            }
        }

        List<MSync.IndexItem> indexItems = MSync.index(user, app);

        for (MSync.IndexItem indexItem: indexItems){
            ItemClass item = find(indexItem);

            if ((item == null) && (indexItem.status == MSync.Status.OK)) {
                // added remotely
                item = createItem(indexItem);
                if (MSync.get(item))
                    list.add(item);
            } else if ((item != null) && (indexItem.status == MSync.Status.DELETED)) {
                // deleted remotely
                list.remove(item);
            } else if ((item != null) && (indexItem.status == MSync.Status.OK)) {
                // changed remotely
                if (item.time < indexItem.time)
                    MSync.get(item);
            }
        }

        dirty = false;
        onChange();
    }

    void onChange() {
        save();

        for (OnChangeListener listener: onChangeListeners)
            listener.onChange();
    }

    ItemClass find(MSync.IndexItem indexItem) {
        for (ItemClass item: list) {
            if (item.id == indexItem.id)
                return item;
        }
        return null;
    }

    ItemClass getByUid(int uid) {
        for (ItemClass item: list) {
            if (item.uid == uid)
                return item;
        }
        return null;
    }

    public void add(ItemClass item) {
        list.add(item);

        markForSync();
    }

    public void set(ItemClass item) {
        item.markChanged();

        markForSync();
    }

    public void delete(ItemClass item) {
        item.markDeleted();

        markForSync();
    }


    public void save() {
        File file = new File(context.getFilesDir(), filename);
        if (file.exists())
            file.delete();

        try {
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(stream, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            for (ItemClass item: list) {
                serializer.startTag(null, "item");
                serializer.attribute("", "id", String.valueOf(item.id));
                serializer.attribute("", "time", String.valueOf(item.time));
                serializer.attribute("", "status", String.valueOf(item.status));
                serializer.attribute("", "data", item.getData());
                serializer.endTag(null, "item");
            }
            serializer.endDocument();

            serializer.flush();
            stream.close();
        } catch (IOException e) {
            Toast.makeText(context, "todo save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void load() {
        list = new ArrayList<>();

        File file = new File(context.getFilesDir(), filename);
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
                        if (name.equals("item")) {
                            ItemClass item = createItem(new MSync.IndexItem());
                            item.id = Integer.valueOf(x.getAttributeValue(null, "id"));
                            item.time = Integer.valueOf(x.getAttributeValue(null, "time"));
                            item.status = MSync.Status.valueOf(x.getAttributeValue(null, "status"));
                            item.setData(x.getAttributeValue(null, "data"));
                            list.add(item);
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
            Log.e("xxx", "msync load error: " + e.getMessage());
            Toast.makeText(context, "msync load error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public abstract ItemClass createItem(MSync.IndexItem item);

    public void addOnChangeListener(OnChangeListener listener) {
        onChangeListeners.add(listener);
    }
}
