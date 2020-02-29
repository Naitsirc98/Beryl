package naitsirc98.beryl.graphics.opengl.vertex;

import naitsirc98.beryl.graphics.opengl.GLBuffer;
import naitsirc98.beryl.graphics.opengl.GLObject;
import naitsirc98.beryl.meshes.vertices.VertexAttribute;
import naitsirc98.beryl.meshes.vertices.VertexAttributeList;
import naitsirc98.beryl.meshes.vertices.VertexAttributeList.VertexAttributeListIterator;

import static naitsirc98.beryl.graphics.opengl.GLUtils.toGL;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL45.*;

public final class GLVertexArray implements GLObject {

    private final int handle;

    public GLVertexArray() {
        handle = glCreateVertexArrays();
    }

    @Override
    public int handle() {
        return handle;
    }

    public void setIndexBuffer(GLBuffer indexBuffer) {
        glVertexArrayElementBuffer(handle, indexBuffer.handle());
    }

    public void removeIndexBuffer() {
        glVertexArrayElementBuffer(handle, NULL);
    }

    public void addVertexBuffer(int binding, VertexAttributeList attributes, GLBuffer vertexBuffer) {
        glVertexArrayVertexBuffer(handle, binding, vertexBuffer.handle(), 0, attributes.stride());
        System.out.println(attributes.stride());
        VertexAttributeListIterator it = attributes.iterator();
        while(it.hasNext()) {
            setVertexAttribute(binding, it.next(), it.location(), it.offset(), attributes.stride());
        }
    }

    public void bind() {
        glBindVertexArray(handle);
    }

    private void setVertexAttribute(int binding, VertexAttribute attribute, int location, int offset, int stride) {

        glEnableVertexArrayAttrib(handle, location);

        if(attribute.dataType().decimal()) {
            setAttributef(binding, location, attribute, offset);
        } else {
            setAttributei(binding, location, attribute, offset);
        }
    }

    private void setAttributef(int binding, int location, VertexAttribute attribute, int offset) {
        glVertexArrayAttribBinding(handle, location, binding);
        glVertexArrayAttribFormat(handle, location, attribute.size(), toGL(attribute.dataType()), false, offset);
    }

    private void setAttributei(int binding, int location, VertexAttribute attribute, int offset) {
        glVertexArrayAttribBinding(handle, location, binding);
        glVertexArrayAttribIFormat(handle, location, attribute.size(), toGL(attribute.dataType()), offset);
    }

    @Override
    public void free() {
        glDeleteVertexArrays(handle);
    }
}
