package com.example.michi.amido;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;

/**
 * Created by michi on 09.11.15.
 */
public class CharacterDatabase {

    static class AnswerItem {
        public Character c;
        public float score;
        public AnswerItem(Character c, float score) {
            this.c = c;
            this.score = score;
        }
        public String getScore() {
            return scoreToString(score);
        }
    }

    public static String scoreToString(float score) {
        if (score > 0.8f)
            return "★★★★★";
        if (score > 0.6f)
            return "★★★★☆";
        if (score > 0.4f)
            return "★★★☆☆";
        if (score > 0.2f)
            return "★★☆☆☆";
        return "★☆☆☆☆";
    }


    static class Answer extends ArrayList<CharacterDatabase.AnswerItem> {
        AnswerItem best;
        public Answer(AnswerItem best) {
            this.best = best;
        }
        public void append(AnswerItem it) {
            if (it.score > best.score)
                best = it;

            for (int i=0; i<size(); i++)
                if (it.score > get(i).score) {
                    add(i, it);
                    return;
                }
            add(it);
        }
    }

    private ArrayList<Character> characters = new ArrayList<>();
    private Character dummy_no_character;

    private Context context;

    public enum State {
        NOT_LOADED,
        LOADING,
        LOADED
    }

    private State state = State.NOT_LOADED;

    static CharacterDatabase instance = null;
    public static CharacterDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new CharacterDatabase(context);
            instance.loadBackground();
        }
        return instance;
    }

    private CharacterDatabase(Context context) {
        dummy_no_character = new Character();

        //load(context);
        this.context = context;
    }

    public void loadBackground() {
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                CharacterDatabase.this.load();
            }
        };
        t.schedule(tt, 0);

    }

    public void makeUsable() {
        if (state == State.NOT_LOADED)
            load();
        while (state != State.LOADED) {}
    }

    public void loadExtra() {

        Log.i("xxx", "extra?");
        File file = new File(context.getFilesDir(), "characters2.xml");
        if (!file.exists())
            return;
        Log.i("xxx", "extra!!!!!!!!!!!!!!!");

        try {
            FileInputStream stream = new FileInputStream(file);
            byte[] encoded = new byte[10000];
            int l = stream.read(encoded);
            Log.i("yyy", new String(encoded, 0, l));
        }catch(Exception e){}

        try {
            FileInputStream stream = new FileInputStream(file);

            XmlPullParserFactory xf = XmlPullParserFactory.newInstance();

            XmlPullParser _xml = xf.newPullParser();
            _xml.setFeature(_xml.FEATURE_PROCESS_NAMESPACES, false);
            _xml.setInput(stream, null);

            Character c = null;

            int event = _xml.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = _xml.getName();
                if ((event == XmlPullParser.START_TAG) && (_xml.getName().equals("character"))) {
                    c = new Character();
                    //c.type = "kanji";
                    c.id = Integer.valueOf(_xml.getAttributeValue(null, "tuttle"));
                    c.glyph = _xml.getAttributeValue(null, "glyph");
                    c.type = _xml.getAttributeValue(null, "type");
                    c.pronunciation = _xml.getAttributeValue(null, "pronunciation");
                    if (c.pronunciation == null)
                        c.pronunciation = "";
                    c.pinyin = _xml.getAttributeValue(null, "pinyin");
                    if (c.pinyin == null)
                        c.pinyin = "";
                    c.english = _xml.getAttributeValue(null, "english");
                    if (c.english == null)
                        c.english = "";
                    c.german = _xml.getAttributeValue(null, "german");
                    if (c.german == null)
                        c.german = "";
                    String ss = _xml.getAttributeValue(null, "stroke_count");
                    if (ss != null)
                        c.num_strokes = Integer.valueOf(ss);
                    c.strokes = _xml.getAttributeValue(null, "strokes");
                    if (c.strokes == null)
                        c.strokes = "[]";
                }
                if ((event == XmlPullParser.TEXT) && (c != null)) {
                    Log.i("xxx text...", _xml.getText());
                    c.setDigestByString(_xml.getText());
                }
                if ((event == XmlPullParser.END_TAG) && (_xml.getName().equals("character"))) {
                    Log.i("xxxx", c.glyph);
                    for (Character cc: characters)
                        if (cc.glyph.equals(c.glyph)) {
                            Log.i("xxx", "extra! " + c.glyph);
                            cc.pronunciation = c.pronunciation;
                            cc.english = c.english;
                            cc.german = c.german;
                            cc.pinyin = c.pinyin;
                            cc.num_strokes = c.num_strokes;
                            cc.strokes = c.strokes;
                            cc.strokes_digest = c.strokes_digest;
                            Log.i("xxx", cc.strokes);
                            Log.i("xxx", cc.getDigestString());
                        }
//                    characters.add(c);
                    c = null;
                }
                event = _xml.next();
            }
        } catch (Exception e) {
            Log.e("xxx", e.getMessage(), e);
        }

    }

    public void load() {

        state = State.LOADING;

        //Toast.makeText(context, "loading character database...", Toast.LENGTH_SHORT).show();
        Log.i("xxx", "load...");
        Character c = null;
        XmlResourceParser _xml = context.getResources().getXml(R.xml.characters);
        try {
            //Check for end of document
            int eventType = _xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //Search for record tags
                if ((eventType == XmlPullParser.START_TAG) && (_xml.getName().equals("character"))) {
                    c = new Character();
                    c.type = "kanji";
                    c.id = _xml.getAttributeIntValue(null, "tuttle", 0);
                    c.glyph = _xml.getAttributeValue(null, "glyph");
                    c.pronunciation = _xml.getAttributeValue(null, "pronunciation");
                    if (c.pronunciation == null)
                        c.pronunciation = "";
                    c.pinyin = _xml.getAttributeValue(null, "pinyin");
                    if (c.pinyin == null)
                        c.pinyin = "";
                    c.english = _xml.getAttributeValue(null, "english");
                    if (c.english == null)
                        c.english = "";
                    c.german = _xml.getAttributeValue(null, "german");
                    if (c.german == null)
                        c.german = "";
                    c.num_strokes = _xml.getAttributeIntValue(null, "stroke_count", 0);
                    c.strokes = _xml.getAttributeValue(null, "strokes");
                }
                if (eventType == XmlPullParser.TEXT) {
                    c.setDigestByString(_xml.getText());
                }
                if ((eventType == XmlPullParser.END_TAG) && (_xml.getName().equals("character"))) {
                    //Log.i("xxxx", c.glyph);
                    characters.add(c);
                }
                eventType = _xml.next();
            }
        } catch (XmlPullParserException e) {
            Log.e("xxx", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("xxx", e.getMessage(), e);
        } finally {
            //Close the xml file
            _xml.close();
        }
        loadExtra();
        Log.i("xxx", "done");
        state = State.LOADED;
    }

    public void save() {

        File file = new File(context.getFilesDir(), "characters2.xml");
        if (file.exists())
            file.delete();

        try {
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(stream, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "characters");

            for (Character c : characters) {
                if (c.changed){
                    serializer.startTag(null, "character");
                    serializer.attribute("", "type", c.type);
                    serializer.attribute("", "glyph", c.glyph);
                    serializer.attribute("", "tuttle", String.valueOf(c.id));
                    serializer.attribute("", "pronunciation", c.pronunciation);
                    serializer.attribute("", "english", c.english);
                    serializer.attribute("", "german", c.german);
                    serializer.attribute("", "pinyin", c.pinyin);
                    serializer.attribute("", "stroke_count", String.valueOf(c.num_strokes));
                    serializer.attribute("", "strokes", c.strokes);
                    serializer.text(c.getDigestString());
                    Log.i("xxx", c.strokes);
                    Log.i("xxx", c.getDigestString());

                    serializer.endTag(null, "character");
                }
            }
            serializer.endTag(null, "characters");
            serializer.endDocument();
            serializer.flush();
            stream.close();
        } catch (IOException e) {
            Toast.makeText(context, "characterdb save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public Answer find(Character digest) {
        makeUsable();
        Answer al = new Answer(new AnswerItem(dummy_no_character, 0));

        for (Character c : characters) {
            float score = c.score(digest);
            if (score > 0)
                al.append(new AnswerItem(c, score));
        }

        return al;
    }

    public Answer find(String query) {
        makeUsable();
        Answer al = new Answer(new AnswerItem(dummy_no_character, 0));
        query = query.toLowerCase();

        for (Character c : characters) {
            if ((c.glyph == query) || (c.english.toLowerCase().contains(query)) || (c.german.toLowerCase().contains(query)) || (c.pronunciation.toLowerCase().contains(query)) || (query.equals("*")))
                al.append(new AnswerItem(c, 1));
        }

        return al;
    }

    public Character getSafe(String type, int id) {
        Character c = getUnsafe(type, id);
        if (c == null)
            return dummy_no_character;
        return c;
    }

    public Character getUnsafe(String type, int id) {
        makeUsable();
        for (Character c : characters) {
            if ((c.type.equals(type)) && (c.id == id))
                return c;
        }
        return null;
    }

    public ArrayList<Character> get(String type, int ids[]) {
        ArrayList<Character> list = new ArrayList<>();
        for (int i : ids) {
            Character c = getUnsafe(type, i);
            if (c != null)
                list.add(c);
        }
        return list;
    }

    public static ArrayList<StrokeDigest> digestStrokes(ArrayList<Stroke> strokes) {
        BoundingBox box = new BoundingBox();
        for (Stroke s : strokes) {
            box.add(s.getBoundingBox());
        }
        box = box.square();

        ArrayList<StrokeDigest> strokes_digest = new ArrayList<>();
        for (Stroke s : strokes) {
            strokes_digest.add(s.digest(box));
        }
        return strokes_digest;
    }

    public static Character digest(ArrayList<Stroke> strokes) {
        Character c = new Character();
        c.num_strokes = strokes.size();
        c.strokes_digest = digestStrokes(strokes);
        return c;
    }
}

