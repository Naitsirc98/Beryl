package naitsirc98.beryl.util;

public final class XORProperty<A, B> {

    private static final Object NULL = new Object();

    private Object a;
    private Object b;

    public XORProperty() {
        a = b = NULL;
    }

    public <T> T selected() {
        if(a == NULL && b == NULL) {
            throw new IllegalStateException("No value is selected");
        }
        return (T) (aSelected() ? a : b);
    }

    public boolean aSelected() {
        return a != NULL;
    }

    public A a() {
        return (A) a;
    }

    public XORProperty<A, B> selectA(A a) {
        this.a = a;
        b = NULL;
        return this;
    }

    public boolean bSelected() {
        return b != NULL;
    }

    public B b() {
        return (B) b;
    }

    public XORProperty<A, B> selectB(B b) {
        this.b = b;
        a = NULL;
        return this;
    }
}
