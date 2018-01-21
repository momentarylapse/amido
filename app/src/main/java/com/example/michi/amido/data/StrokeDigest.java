package com.example.michi.amido.data;

/**
 * Created by michi on 09.01.16.
 */
public class StrokeDigest {
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
