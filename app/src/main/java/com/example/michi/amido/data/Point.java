package com.example.michi.amido.data;

/**
 * Created by michi on 09.01.16.
 */
public class Point {
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
