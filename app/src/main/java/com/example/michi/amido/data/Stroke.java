package com.example.michi.amido.data;

import java.util.ArrayList;

/**
 * Created by michi on 09.01.16.
 */
public class Stroke {
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

    public float getLength() {
        float length = 0;
        for (int i=0; i<points.size()-1; i++)
            length += points.get(i+1).distance(points.get(i));
        return length;
    }
}
