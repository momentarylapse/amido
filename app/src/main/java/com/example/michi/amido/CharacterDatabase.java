package com.example.michi.amido;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.opengl.Matrix;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

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
    }

    static class BoundingBox {
        public float x0, y0, x1, y1;
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
        public float mx(){ return (x0 + x1) / 2; }
        public float my(){ return (y0 + y1) / 2; }
        public float dx(){ return (x1 - x0); }
        public float dy(){ return (y1 - y0); }
    }

    static class Stroke {
        public ArrayList<Point> points = new ArrayList<>();
        public void add(Point p) { points.add(p); }
        public void clear() {
            points.clear();
        }
        public BoundingBox getBoundingBox() {
            BoundingBox b = new BoundingBox(10000, 10000, -10000, -10000);
            //for ($strokes as $s)
            for (Point p : points)
                b.add(p);

            return b;
        }

        public Stroke scaleByBoundingBox(BoundingBox b) {
            Stroke r = new Stroke();
            float dx = b.dx();
            float dy = b.dy();
            for (Point p : points)
                r.add(new Point((p.x - b.x0) / dx, (p.y - b.y0) / dy));
            return r;
        }
    }


    static class StrokeDigest {
        public static final int COUNT = 32;
        public float mx, my, dx, dy, length;
        public float fmx, fmy, fdx, fdy, fl;
        public float[] w = new float[COUNT];
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
                        d.w[j] = ia.getInt(j);
                    }
                    strokes_digest.add(d);
                }
            } catch (JSONException e) {
                Log.e("xxx", "digest: " + e.getMessage(), e);
            }
        }

        public float distance(Character o) {
            if (num_strokes != o.num_strokes)
                return 1000000;
            float d = 0;
            int i = 0;
            for (StrokeDigest s : strokes_digest){
                StrokeDigest so = o.strokes_digest.get(i);
                d += s.fl * pow(s.length - so.length, 2);
                d += s.fmx * pow(s.mx - so.mx, 2);
                d += s.fmy * pow(s.my - so.my, 2);
                d += s.fdx * pow(s.dx - so.dx, 2);
                d += s.fdy * pow(s.dy - so.dy, 2);
                float dw = 0;
                for (int j=0; j<256; j+=4){
                    float wdist = abs(s.w[j] - so.w[j]);
                    if (wdist < 0.5)
                        dw += wdist;
                    else
                        dw += 1 - wdist;
                    //$dw += /*$s["fw"][$j] * */ self::w_dist($s["w"][$j], $si["w"][$j]);
                }
                d += dw / /*256*/ 64 * s.length;
                i ++;
            }
            return d / num_strokes * 1000;
        }
    }

    static class AnswerItem {
        public Character c;
        public float score;
    }
    static class Answer extends ArrayList<CharacterDatabase.AnswerItem> {
    }

    private ArrayList<Character> characters = new ArrayList<>();

    public CharacterDatabase(Context context) {
        load(context);
    }


    public void load(Context context) {

        Log.i("xxx", "load...");
        Character c = new Character();
        XmlResourceParser _xml = context.getResources().getXml(R.xml.characters);
        try
        {
            //Check for end of document
            int eventType = _xml.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                //Search for record tags
                if ((eventType == XmlPullParser.START_TAG) && (_xml.getName().equals("character"))){
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
                if ((eventType == XmlPullParser.END_TAG) && (_xml.getName().equals("character"))){
                    characters.add(c);
                    c = new Character();
                }
                eventType = _xml.next();
            }
        }
        //Catch errors
        catch (XmlPullParserException e)
        {
            Log.e("xxx", e.getMessage(), e);
        }
        catch (IOException e)
        {
            Log.e("xxx", e.getMessage(), e);
        } finally
        {
            //Close the xml file
            _xml.close();
        }
        Log.i("xxx", "fertig");
    }

    public Answer find(Character digest) {
        Answer al = new Answer();

        Character cc;
        float dmin = 999999f;
        for (Character c : characters) {
            if (c.num_strokes != digest.num_strokes)
                continue;
            float d = c.distance(digest);
            if (d < dmin) {
                dmin = d;
                cc = c;
            }
        }
        AnswerItem ai = new AnswerItem();
        ai.score = (float)(1.0 - pow(dmin / 200.0, 1.2));
        al.add(ai);

        return al;
    }

    public static Character digest(ArrayList<Stroke> strokes) {
        Character c = new Character();
        return c;
    }
}
