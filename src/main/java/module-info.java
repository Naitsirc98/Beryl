module Beryl {

    //  ===> LWJGL

    requires org.lwjgl;
    requires org.lwjgl.natives;

    requires org.lwjgl.jemalloc;
    requires org.lwjgl.jemalloc.natives;

    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.glfw;

    requires org.lwjgl.stb;
    requires org.lwjgl.stb.natives;

    requires org.lwjgl.vulkan;

    requires org.joml;

    // <===

    requires java.logging;

    requires jdk.unsupported;

}