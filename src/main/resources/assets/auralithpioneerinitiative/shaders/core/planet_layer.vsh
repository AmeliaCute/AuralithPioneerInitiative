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
out vec3 worldPos;
out vec3 viewDirection;

void main()
{
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vec3 translationFromView = vec3(ModelViewMat[3][0], ModelViewMat[3][1], ModelViewMat[3][2]);

    worldPos = uCameraPos + translationFromView + Position;

    vertexPos = Position;
    vertexColor = Color;
    vertexNormal = Normal;

    viewDirection = normalize(uCameraPos - worldPos);
}