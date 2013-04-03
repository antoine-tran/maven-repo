package de.l3s.gossen.burstdetection;

import java.io.PrintWriter;
import java.util.Locale;

import com.google.common.collect.Range;

public class Burst<O> {
    public enum Direction {
        HIGHER, LOWER
    }

    private final O object;
    private final int start;
    private final int end;
    private final double strength;
    private final int state;
    private final Direction direction;

    public Burst(O object, int start, int end, int state, double strength, Direction direction) {
        this.object = object;
        this.start = start;
        this.end = end;
        this.state = state;
        this.strength = strength;
        this.direction = direction;
    }

    public static <S> Burst<S> of(S object, int start, int end, int state, double strength) {
        return of(object, start, end, state, strength, Direction.HIGHER);
    }

    public static <S> Burst<S> of(S object, int start, int end, int state, double strength, Direction direction) {
        return new Burst<S>(object, start, end, state, strength, direction);
    }

    public O getObject() {
        return object;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Range<Integer> getDuration() {
        return Range.closed(start, end);
    }

    public int getState() {
        return state;
    }

    public double getStrength() {
        return strength;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isContainedIn(Burst<O> o) {
        if (o == null) {
            return false;
        }
        return object.equals(o.object) && start == o.start && end == o.end && state <= o.state;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Burst [%s, %s...%s %d@%f]", object,
                start, end, state, strength);
    }

    public void printCsv(PrintWriter out) {
        out.printf(Locale.ENGLISH, "%s;%s;%s;%d;%3.2f%n", object, start, end,
                state, strength);
    }

}
