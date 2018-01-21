package com.example.michi.amido.data;

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
 * Created by michi on 21.01.18.
 */

public class Digest {
    ArrayList<StrokeDigest> strokes;

    public Digest() {
        strokes = new ArrayList<>();
    }

    public int num_strokes() {
        return strokes.size();
    }

    public void setByString(String s) {
        strokes = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(s);
            for (int i=0; i<a.length(); i++) {
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
                strokes.add(d);
            }
        } catch (JSONException e) {
            Log.e("xxx", "digest: " + e.getMessage(), e);
        }
    }

    public String toString() {
        String r = "[";
        Locale l = null;
        boolean first = true;
        for (StrokeDigest s : strokes){
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

    public float distance(Digest b) {
        if (num_strokes() != b.num_strokes())
            return 1000000;
        float d = 0;
        int i = 0;
        for (StrokeDigest as : strokes) {
            StrokeDigest bs = b.strokes.get(i);
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
        return d / num_strokes() * 1000;
    }

    public float score(Digest b) {
        float d = distance(b);
        return max(0, (float)(1.0 - pow(d / 200.0, 1.5)));
    }

    public static Digest digest(ArrayList<Stroke> strokes) {
        BoundingBox box = new BoundingBox();
        for (Stroke s : strokes) {
            box.add(s.getBoundingBox());
        }
        box = box.square();

        Digest digest = new Digest();
        for (Stroke s : strokes) {
            digest.strokes.add(s.digest(box));
        }
        return digest;
    }
}
