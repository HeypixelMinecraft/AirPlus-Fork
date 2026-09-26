#version 120

uniform vec2 InSize;

varying vec2 texCoord;
varying vec2 oneTexel;

void main() {
    gl_Position = ftransform();
    texCoord = gl_MultiTexCoord0.st;
    oneTexel = 1.0 / InSize;
}
