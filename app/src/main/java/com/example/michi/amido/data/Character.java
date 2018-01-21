package com.example.michi.amido.data;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.example.michi.amido.KanaRenderer;
import com.example.michi.amido.R;
import com.example.michi.amido.Settings;

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
    public String strokes;
    public Digest digest;
    public boolean changed;

    public Character() {
        id = -1;
        glyph = "?";
        pronunciation = "?";
        english = "?";
        german = "?";
        pinyin = "?";
        strokes = "[]";
        changed = false;
        digest = new Digest();
    }

    //public JSONArray strokes_digest;
    public void setDigestByString(String s) {
        digest.setByString(s);
    }

    public String getDigestString() {
        return digest.toString();
    }

    public float score(Character c) {
        return digest.score(c.digest);
    }

    public float score(Digest d) {
        return digest.score(d);
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
        if (context.getResources().getString(R.string.language).equals("de") && !german.isEmpty())
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
            for (int i=0; i<a.length(); i++) {
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

    public void setStrokes(ArrayList<Stroke> _strokes, Digest _digest) {
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
        digest = _digest;
    }


        /*public BoundingBox getBoundingBox() {
            BoundingBox box = new BoundingBox();
            for (Stroke s : strokes) {
                box.add(s.getBoundingBox());
            }
            return box;
        }*/
}
