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

    static class Point {
        public float x, y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
        public float distance(Point p) {
            float dx = x - p.x;
            float dy = y - p.y;
            return (float)Math.sqrt(dx*dx + dy*dy);
        }
        public float direction(Point p) {
            float dx = x - p.x;
            float dy = y - p.y;
            return (float)Math.atan2(dx, dy);
        }
        public Point interpolateTo(Point p, float t) {
            return new Point((1-t)*x + t*p.x, (1-t)*y + t*p.y);
        }
        public String toString() {
            return "(" + x + " " + y + ")";
        }
    }

    static class BoundingBox {
        public float x0, y0, x1, y1;
        public BoundingBox() {
            this.x0 = 1000000;
            this.y0 = 1000000;
            this.x1 = -1000000;
            this.y1 = -1000000;
        }
        public BoundingBox(float x0, float y0, float x1, float y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }
        public void add(Point p) {
            x0 = min(x0, p.x);
            x1 = max(x1, p.x);
            y0 = min(y0, p.y);
            y1 = max(y1, p.y);
        }
        public void add(BoundingBox b) {
            x0 = min(x0, b.x0);
            x1 = max(x1, b.x1);
            y0 = min(y0, b.y0);
            y1 = max(y1, b.y1);
        }
        public float mx(){ return (x0 + x1) / 2; }
        public float my(){ return (y0 + y1) / 2; }
        public float dx(){ return (x1 - x0); }
        public float dy(){ return (y1 - y0); }
        public BoundingBox square() {
            float r = max(dx(), dy()) / 2.0f;
            return new BoundingBox(mx() - r, my() - r, mx() + r, my() + r);
        }
        public String toString() {
            return "(" + x0 + " " + y0 + " " + x1 + " " + y1 + ")";
        }
    }

    static class Stroke {
        public ArrayList<Point> points = new ArrayList<>();
        public void add(Point p) { points.add(p); }
        public void clear() {
            points.clear();
        }
        public BoundingBox getBoundingBox() {
            BoundingBox b = new BoundingBox();
            for (Point p : points)
                b.add(p);
            return b;
        }
        public String toString() {
            String s = "";
            for (Point p : points)
                s += p.toString() + "  ";
            return s;
        }

        public Stroke scaleByBoundingBox(BoundingBox b) {
            Stroke r = new Stroke();
            float dx = b.dx();
            float dy = b.dy();
            for (Point p : points)
                r.add(new Point((p.x - b.x0) / dx, (p.y - b.y0) / dy));
            return r;
        }

        public StrokeDigest digest(BoundingBox box) {
            Stroke s = scaleByBoundingBox(box);
            StrokeDigest sd = new StrokeDigest();
            sd.length = 0;

            int N = s.points.size();

            float[] w = new float[N-1];
            float[] dl = new float[N-1];

            // length and directions
            for (int i=0; i<N-1; i++){
                w[i] = s.points.get(i+1).direction(s.points.get(i)) / ((float)Math.PI * 2.0f) + 0.5f;
                dl[i] = s.points.get(i+1).distance(s.points.get(i));
                sd.length += dl[i];
            }
            for (int i=0; i<N-1; i++)
                dl[i] /= sd.length;

            // interpolate w
            for (int i=0; i<StrokeDigest.COUNT-1; i++){
                float li = (float)i / (float)(StrokeDigest.COUNT-1);
                float lj = 0;
                for (int j=0; j<N-1; j++) {
                    if ((li >= lj) && (li <= lj + dl[j])) {
                        sd.w[i] = w[j];
                        break;
                    }
                    lj += dl[j];
                }
            }
            sd.w[StrokeDigest.COUNT - 1] = w[N - 2];

            // bounding box
            BoundingBox s_box = s.getBoundingBox();
            sd.mx = s_box.mx();
            sd.my = s_box.my();
            sd.dx = s_box.dx();
            sd.dy = s_box.dy();

            return sd;
        }

        float getLength() {
            float length = 0;
            for (int i=0; i<points.size()-1; i++)
                length += points.get(i+1).distance(points.get(i));
            return length;
        }
    }


    static class StrokeDigest {
        public static final int COUNT = 32;
        public float mx, my, dx, dy, length;
        public float fmx, fmy, fdx, fdy, fl;
        public float[] w = new float[COUNT];

        public Stroke undigest() {
            Stroke s = new Stroke();
            Point p = new Point(0, 0);
            s.add(p);
            for (int i=0; i<COUNT; i++) {
                float ww = (w[i] - 0.5f) * (float)Math.PI * 2.0f;
                Point p2 = new Point(p.x + (float)Math.sin(ww), p.y + (float)Math.cos(ww));
                s.add(p2);
                p = p2;
            }
            BoundingBox bb = s.getBoundingBox();
            for (Point pp : s.points) {
                pp.x = ((pp.x - bb.mx()) / bb.dx() * dx + mx) * 0.9f + 0.05f;
                pp.y = ((pp.y - bb.my()) / bb.dy() * dy + my) * 0.9f + 0.05f;
            }
            return s;
        }
    }

    static class Character {
        public int id;
        public String type;
        public String glyph;
        public String pronunciation;
        public String english;
        public String german;
        public int num_strokes;
        public String strokes;
        public ArrayList<StrokeDigest> strokes_digest;
        //public JSONArray strokes_digest;
        public void setDigest(String s) {
            strokes_digest = new ArrayList<>();
            try {
                JSONArray a = new JSONArray(s);
                for (int i=0; i<num_strokes; i++) {
                    JSONObject o = a.getJSONObject(i);
                    StrokeDigest d = new StrokeDigest();
                    d.length = (float)o.getDouble("l");
                    d.mx = (float)o.getDouble("mx");
                    d.my = (float)o.getDouble("my");
                    d.dx = (float)o.getDouble("dx");
                    d.dy = (float)o.getDouble("dy");
                    d.fl = d.fdx = d.fdy = d.fmx = d.fmy = 1;
                    JSONArray ia = o.getJSONArray("w");
                    for (int j=0; j<StrokeDigest.COUNT; j++) {
                        d.w[j] = (float)ia.getDouble(j);
                    }
                    strokes_digest.add(d);
                }
            } catch (JSONException e) {
                Log.e("xxx", "digest: " + e.getMessage(), e);
            }
        }

        public float distance(Character b) {
            if (num_strokes != b.num_strokes)
                return 1000000;
            float d = 0;
            int i = 0;
            for (StrokeDigest as : strokes_digest) {
                StrokeDigest bs = b.strokes_digest.get(i);
                d += as.fl * pow(as.length - bs.length, 2);
                d += as.fmx * pow(as.mx - bs.mx, 2);
                d += as.fmy * pow(as.my - bs.my, 2);
                d += as.fdx * pow(as.dx - bs.dx, 2);
                d += as.fdy * pow(as.dy - bs.dy, 2);
                float dw = 0;
                for (int j=0; j<StrokeDigest.COUNT; j++) {
                    float wdist = abs(as.w[j] - bs.w[j]);
                    if (wdist < 0.5)
                        dw += wdist;
                    else
                        dw += 1 - wdist;
                    //$dw += /*$s["fw"][$j] * */ self::w_dist($s["w"][$j], $si["w"][$j]);
                }
                d += dw / /*256*/ StrokeDigest.COUNT * as.length;
                i ++;
            }
            return d / num_strokes * 1000;
        }

        public float score(Character c) {
            float d = distance(c);
            return max(0, (float)(1.0 - pow(d / 200.0, 1.5)));
        }

        public static String simplify(String lang) {
            if (lang.contains("|"))
                return lang.split("\\|")[0].replace(",", ", ");
            return lang.replace(",", ", ");

        }

        public static String niceList(String lang) {
            return lang.replace(",", ", ").replace("|", ", ");
            /*String[] sl = lang.split(",|\\|");
            String r = "";
            for (String s : sl)
                if (r.length() > 0)
                    r = r + ", " + s;
                else
                    r = s;
            return r;*/
        }

        public String getSimpleTranslation() {
            return simplify(getTranslation());
        }
        public String getTranslation() {
            if (context.getResources().getString(R.string.language).equals("de"))
                return german;
            return english;
        }


        /*public BoundingBox getBoundingBox() {
            BoundingBox box = new BoundingBox();
            for (Stroke s : strokes) {
                box.add(s.getBoundingBox());
            }
            return box;
        }*/
    }

    static class AnswerItem {
        public Character c;
        public float score;
        public AnswerItem(Character c, float score) {
            this.c = c;
            this.score = score;
        }
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

    static Context context;

    public enum State {
        NOT_LOADED,
        LOADING,
        LOADED
    }

    State state = State.NOT_LOADED;

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
        Character c = new Character();
        XmlResourceParser _xml = context.getResources().getXml(R.xml.characters);
        try {
            //Check for end of document
            int eventType = _xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //Search for record tags
                if ((eventType == XmlPullParser.START_TAG) && (_xml.getName().equals("character"))) {
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
                    c = new Character();
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
            if ((c.glyph == query) || (c.english.toLowerCase().contains(query)) || (c.german.toLowerCase().contains(query)) || (c.pronunciation.toLowerCase().contains(query)))
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
            CharacterDatabase.Character c = get(type, i);
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

