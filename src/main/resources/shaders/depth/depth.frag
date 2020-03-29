@beryl

layout(location = 0) out float frag_FragDepth;

void main() {
    frag_FragDepth = gl_FragCoord.z;
}
