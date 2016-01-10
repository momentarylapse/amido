package com.example.michi.amido;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
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
        dummy_no_character.glyph = "?";
        dummy_no_character.english = "?";
        dummy_no_character.pronunciation = "?";
        dummy_no_character.german = "?";

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
                    c.id = _xml.getAttributeIntValue(null, "id", 0);
                    c.glyph = _xml.getAttributeValue(null, "glyph");
                    c.pronunciation = _xml.getAttributeValue(null, "pronunciation");
                    c.english = _xml.getAttributeValue(null, "english");
                    c.german = _xml.getAttributeValue(null, "german");
                    c.num_strokes = _xml.getAttributeIntValue(null, "stroke_count", 0);
                    c.strokes = _xml.getAttributeValue(null, "strokes");
                }
                if (eventType == XmlPullParser.TEXT) {
                    c.setDigest(_xml.getText());
                }
                if ((eventType == XmlPullParser.END_TAG) && (_xml.getName().equals("character"))) {
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
        Log.i("xxx", "done");
        state = State.LOADED;
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

    public Character get(String type, int id) {
        makeUsable();
        for (Character c : characters) {
            if ((c.type.equals(type)) && (c.id == id))
                return c;
        }
        return dummy_no_character;
    }

    public ArrayList<Character> get(String type, int ids[]) {
        ArrayList<Character> list = new ArrayList<>();
        for (int i : ids) {
            Character c = get(type, i);
            if (c != null)
                list.add(c);
        }
        return list;
    }

    public static Character digest(ArrayList<Stroke> strokes) {
        Character c = new Character();
        c.num_strokes = strokes.size();

        BoundingBox box = new BoundingBox();
        for (Stroke s : strokes) {
            box.add(s.getBoundingBox());
        }
        box = box.square();

        c.strokes_digest = new ArrayList<>();
        for (Stroke s : strokes) {
            c.strokes_digest.add(s.digest(box));
        }
        return c;
    }
}

