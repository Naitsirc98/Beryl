package naitsirc98.beryl.meshes.vertices;

import static naitsirc98.beryl.meshes.vertices.VertexAttribute.*;

public class VertexLayouts {

    public static final VertexLayout VERTEX_LAYOUT_3D = new VertexLayout.Builder()
            .put(0, 0, POSITION3D, NORMAL, TEXCOORDS2D)
            .build();


    public static final VertexLayout VERTEX_LAYOUT_3D_INDIRECT = new VertexLayout.Builder(2)
            .put(0, 0, POSITION3D, NORMAL, TEXCOORDS2D)
            .put(1, 3, INDEX, INDEX).instanced(1, true)
            .build();

}
