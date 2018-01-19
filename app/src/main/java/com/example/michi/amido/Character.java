package com.example.michi.amido;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.pow;

/**
 * Created by michi on 09.01.16.
 */
public class Character {
    public int id;
    public String type;
    public String glyph;
    public String pronunciation;
    public String pinyin;
    public String english;
    public String german;
    public int num_strokes;
    public String strokes;
    public ArrayList<StrokeDigest> strokes_digest;
    public boolean changed;

    public Character() {
        id = -1;
        glyph = "?";
        pronunciation = "?";
        english = "?";
        german = "?";
        pinyin = "?";
        num_strokes = 0;
        strokes = "[]";
        changed = false;
        strokes_digest = new ArrayList<>();
    }

    //public JSONArray strokes_digest;
    public void setDigestByString(String s) {
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

    public String getDigestString() {
        String r = "[";
        Locale l = null;
        boolean first = true;
        for (StrokeDigest s : strokes_digest){
            if (!first)
                r += ",";
            r += String.format(l, "{\"l\":%.3f,\"w\":[", s.length);
            for (int i=0; i<s.w.length; i++) {
                if (i > 0)
                    r += ",";
                r += String.format(l, "%.3f", s.w[i]);
            }
            r += String.format(l, "],\"dx\":%.3f,\"mx\":%.3f,\"dy\":%.3f,\"my\":%.3f}", s.dx, s.mx, s.dy, s.my);
            first = false;
        }
        r += "]";
        return r;
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

    public String getSimpleTranslation(Context context) {
        return simplify(getTranslation(context));
    }
    public String getTranslation(Context context) {
        if (context.getResources().getString(R.string.language).equals("de"))
            return german;
        return english;
    }
    public String getPronunciation(Context context) {
        if (Settings.getInstance(context).isShowKana())
            return KanaRenderer.render(pronunciation);
        return pronunciation;
    }
    public Spanned getNicePronunciation(Context context) {
        String p = getPronunciation(context);
        String[] sl = p.split(",|\\|");
        String r = "";
        for (String s : sl) {
            if (s.contains(".")) {
                String[] ss = s.split("\\.");
                if (ss.length == 2)
                    s = "<strong>" + ss[0] + "</strong><small>" + ss[1] + "</small>";
                else
                    s = "<small>" + ss[0] + "</small><strong>" + ss[1] + "</strong><small>" + ss[2] + "</small>";
            }
            if (r.length() > 0)
                r = r + ", " + s;
            else
                r = s;
        }
        return Html.fromHtml(r);
    }

    public ArrayList<Stroke> getStrokes() {
        ArrayList<Stroke> strokes = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(this.strokes);
            for (int i=0; i<num_strokes; i++) {
                JSONArray aa = a.getJSONArray(i);
                Stroke s = new Stroke();
                for (int j=0; j<aa.length() / 2; j++)
                    s.add(new Point((float)aa.getInt(j*2) / 1000.0f, (float)aa.getInt(j*2+1) / 1000.0f));
                strokes.add(s);
            }
        } catch (JSONException e) {
            Log.e("xxx", "getStrokes: " + e.getMessage(), e);
        }
        return strokes;
    }

    public void setStrokes(ArrayList<Stroke> _strokes, ArrayList<StrokeDigest> _digest) {
        num_strokes = _strokes.size();
        strokes = "[";
        boolean firstStroke = true;
        for (Stroke s: _strokes) {
            if (!firstStroke)
                strokes += ",";
            strokes += "[";
            boolean first = true;
            for (Point p: s.points) {
                if (!first)
                    strokes += ",";
                strokes += String.format("%d,%d", (int)(p.x * 1000), (int)(p.y * 1000));
                first = false;
            }
            strokes += "]";
            firstStroke = false;
        }
        strokes += "]";
        strokes_digest = _digest;
    }


        /*public BoundingBox getBoundingBox() {
            BoundingBox box = new BoundingBox();
            for (Stroke s : strokes) {
                box.add(s.getBoundingBox());
            }
            return box;
        }*/
}
