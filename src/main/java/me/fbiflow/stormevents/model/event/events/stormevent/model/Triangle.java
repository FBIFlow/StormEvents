package me.fbiflow.stormevents.model.event.events.stormevent.model;

public class Triangle {

    public final Vertex2f A;
    public final Vertex2f B;
    public final Vertex2f C;

    public Triangle(Vertex2f a, Vertex2f b, Vertex2f c) {
        A = a;
        B = b;
        C = c;
    }

    @Override
    public String toString() {
        return String.format("{%s, %s, %s}", A, B, C);
    }
}
