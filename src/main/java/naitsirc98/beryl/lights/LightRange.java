package naitsirc98.beryl.lights;

public enum LightRange {

    VERY_SMALL(1, 0.08f, 0.024f),
    SMALL(1, 0.022f, 0.0019f),
    MEDIUM(1, 0.007f, 0.0002f),
    LARGE(1, 0.0032f, 0.00008f),
    VERY_LARGE(1, 0.0014f, 0.000007f);

    private final float constant;
    private final float linear;
    private final float quadratic;

    LightRange(float constant, float linear, float quadratic) {
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    public float constant() {
        return constant;
    }

    public float linear() {
        return linear;
    }

    public float quadratic() {
        return quadratic;
    }
}
