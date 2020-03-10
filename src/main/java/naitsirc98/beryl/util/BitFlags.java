package naitsirc98.beryl.util;

import java.util.Objects;

public class BitFlags {

    public static int setFlag(int n, int flag) {
        return n | flag;
    }

    public static int removeFlag(int n, int flag) {
        return n & ~flag;
    }

    public static boolean testFlag(int n, int flag) {
        return (n & flag) == flag;
    }

    private int value;

    public BitFlags() {
        value = 0;
    }

    public BitFlags(int value) {
        this.value = value;
    }

    public boolean test(int mask) {
        return testFlag(value, mask);
    }

    public int get() {
        return value;
    }

    public BitFlags set(int value) {
        this.value = value;
        return this;
    }

    public BitFlags enable(int flag) {
        value = setFlag(value, flag);
        return this;
    }

    public BitFlags disable(int flag) {
        value = removeFlag(value, flag);
        return this;
    }

    public BitFlags and(int value) {
        this.value &= value;
        return this;
    }

    public BitFlags or(int value) {
        this.value |= value;
        return this;
    }

    public int count() {
        return Integer.bitCount(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitFlags bitFlags = (BitFlags) o;
        return value == bitFlags.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return Integer.toBinaryString(value);
    }

}
