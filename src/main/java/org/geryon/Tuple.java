package org.geryon;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class Tuple<F, S> {
    private F first;
    private S second;

    private Tuple(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Tuple<F, S> tuple(F first, S second) {
        return new Tuple<>(first, second);
    }

    public F first() {
        return first;
    }

    public S second() {
        return second;
    }
}
