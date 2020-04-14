package naitsirc98.beryl.meshes.models;

import naitsirc98.beryl.util.types.IBuilder;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public final class StaticVertexHandler {

    private final Function<Vector3f, Vector3f> positionFunction;
    private final Function<Vector3f, Vector3f> normalFunction;
    private final Function<Vector2f, Vector2f> texCoordsFunction;

    public StaticVertexHandler() {
        positionFunction = Function.identity();
        normalFunction = Function.identity();
        texCoordsFunction = Function.identity();
    }

    public StaticVertexHandler(Function<Vector3f, Vector3f> positionFunction,
                               Function<Vector3f, Vector3f> normalFunction,
                               Function<Vector2f, Vector2f> texCoordsFunction) {

        this.positionFunction = requireNonNull(positionFunction);
        this.normalFunction = requireNonNull(normalFunction);
        this.texCoordsFunction = requireNonNull(texCoordsFunction);
    }

    public Vector3f mapPosition(Vector3f position) {
        return positionFunction.apply(position);
    }

    public Vector3f mapNormal(Vector3f normal) {
        return normalFunction.apply(normal);
    }

    public Vector2f mapTextureCoords(Vector2f texCoord) {
        return texCoordsFunction.apply(texCoord);
    }


    public static final class Builder implements IBuilder<StaticVertexHandler> {

        private Function<Vector3f, Vector3f> positionFunction = Function.identity();
        private Function<Vector3f, Vector3f> normalFunction = Function.identity();
        private Function<Vector2f, Vector2f> texCoordsFunction = Function.identity();

        public Builder() {
        }

        public Builder positionFunction(Function<Vector3f, Vector3f> positionFunction) {
            this.positionFunction = requireNonNull(positionFunction);
            return this;
        }

        public Builder normalFunction(Function<Vector3f, Vector3f> normalFunction) {
            this.normalFunction = requireNonNull(normalFunction);
            return this;
        }

        public Builder texCoordsFunction(Function<Vector2f, Vector2f> texCoordsFunction) {
            this.texCoordsFunction = requireNonNull(texCoordsFunction);
            return this;
        }

        @Override
        public StaticVertexHandler build() {
            return new StaticVertexHandler(positionFunction, normalFunction, texCoordsFunction);
        }
    }
}
