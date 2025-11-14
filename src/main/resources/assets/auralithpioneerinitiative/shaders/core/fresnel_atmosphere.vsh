#version 150

in vec3 Position;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 uCameraPos;
uniform vec3 uPlanetPos;

out vec4 vertexColor;
out vec3 vertexNormal;
out vec3 vertexPos;
out vec3 viewDirection;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexColor = Color;
    vertexNormal = Normal;
    vec4 worldPos = ModelViewMat * vec4(Position, 1.0);
    vertexPos = worldPos.xyz;

    viewDirection = normalize(uCameraPos - vertexPos);
}