package com.example.michi.amido;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by michi on 09.01.16.
 */
public class BoundingBox {
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
