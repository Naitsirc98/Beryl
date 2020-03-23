module Beryl {

    //  ===> LWJGL

    requires org.lwjgl;
    requires org.lwjgl.natives;

    requires org.lwjgl.jemalloc;
    requires org.lwjgl.jemalloc.natives;

    requires org.lwjgl.glfw;
    requires org.lwjgl.glfw.natives;

    requires org.lwjgl.stb;
    requires org.lwjgl.stb.natives;

    requires org.lwjgl.vulkan;

    requires org.lwjgl.vma;
    requires org.lwjgl.vma.natives;

    requires org.lwjgl.opengl;
    requires org.lwjgl.opengl.natives;

    requires org.lwjgl.shaderc;
    requires org.lwjgl.shaderc.natives;

    requires org.lwjgl.assimp;
    requires org.lwjgl.assimp.natives;

    requires org.joml;

    // <===

    requires java.logging;

    requires jdk.unsupported;
}