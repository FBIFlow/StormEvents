package me.fbiflow.stormevents.model.event.events.stormevent.util;

import me.fbiflow.stormevents.model.event.events.stormevent.model.Triangle;
import me.fbiflow.stormevents.model.event.events.stormevent.model.Vertex2f;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class TrigonometryUtil {

    public static void main(String[] args) {
        Triangle triangle = new Triangle(
                new Vertex2f(0, 6),
                new Vertex2f(4, -1),
                new Vertex2f(-5, 2)
        );
        Vertex2f point = new Vertex2f(0, 6);
        System.out.println(isPointInTriangle(triangle, point));

        Vertex2f A = new Vertex2f(4, 2);
        Vertex2f B = new Vertex2f(-4, -8);
        Vertex2f[] path = getInterpolatedWay(A, B, 10);
        System.out.println(path.length);
        System.out.println(Arrays.toString(path));
    }

    public static Vertex2f[] getInterpolatedWay(Vertex2f A, Vertex2f B, int interpolation_rate) {
        Vertex2f vector = getVector(A, B);
        double vectorLength = getVectorLength(vector);
        int pointCount = (int) (vectorLength * interpolation_rate);
        Vertex2f[] interpolatedData = new Vertex2f[pointCount + 1];
        Vertex2f offsetDelta = new Vertex2f(vector.x / pointCount, vector.y / pointCount);
        for (int i = 0; i < interpolatedData.length; i++) {
            interpolatedData[i] = new Vertex2f(offsetDelta.x * i + A.x, offsetDelta.y * i + A.y);
        }
        return interpolatedData;
    }

    public static boolean isPointInTriangle(Triangle triangle, Vertex2f point) {
        float triangleArea = abs(getArea(triangle));
        float PBCArea = abs(getArea(new Triangle(point, triangle.B, triangle.C)));
        float PABArea = abs(getArea(new Triangle(point, triangle.A, triangle.B)));
        float PACArea = abs(getArea(new Triangle(point, triangle.A, triangle.C)));
        float allArea = PBCArea + PABArea + PACArea;

        System.out.println("TRIANGLE: " + triangleArea);
        System.out.println("POINT AREA " + allArea);

        return abs(triangleArea - allArea) < 0.0001;
    }

    public static Triangle scaleTriangle(Triangle triangle, float addition) {
        return new Triangle(
                new Vertex2f(
                        triangle.A.x > 0 ? triangle.A.x + addition : triangle.A.x - addition,
                        triangle.A.y > 0 ? triangle.A.y + addition : triangle.A.y - addition
                ),
                new Vertex2f(
                        triangle.B.x > 0 ? triangle.B.x + addition : triangle.B.x - addition,
                        triangle.B.y > 0 ? triangle.B.y + addition : triangle.B.y - addition
                ),
                new Vertex2f(
                        triangle.C.x > 0 ? triangle.C.x + addition : triangle.C.x - addition,
                        triangle.C.y > 0 ? triangle.C.y + addition : triangle.C.y - addition
                )
        );
    }

    private static float getArea(Triangle triangle) {
        Vertex2f A = triangle.A;
        Vertex2f B = triangle.B;
        Vertex2f C = triangle.C;
        Vertex2f AB = getVector(A, B);
        Vertex2f AC = getVector(A, C);

        return multiplyVector2f(AB, AC) / 2;
    }

    private static float multiplyVector2f(Vertex2f... v) {
        if (v.length != 2) {
            throw new IllegalArgumentException("only 2 values allowed");
        }
        Vertex2f A = v[0];
        Vertex2f B = v[1];
        return A.x * B.y - A.y * B.x;
    }

    private static Vertex2f getVector(Vertex2f... v) {
        if (v.length != 2) {
            throw new IllegalArgumentException("only 2 values allowed");
        }
        return new Vertex2f(v[1].x - v[0].x, v[1].y - v[0].y);
    }

    private static Vertex2f getVectorDelta(Vertex2f A, Vertex2f B) {
        float x = (float) sqrt((A.x - B.x) * (A.x - B.x));
        float y = (float) sqrt((A.y - B.y) * (A.y - B.y));
        return new Vertex2f(x, y);
    }

    private static double getVectorLength(Vertex2f v) {
        return sqrt((v.x * v.x) + (v.y * v.y));
    }
}
