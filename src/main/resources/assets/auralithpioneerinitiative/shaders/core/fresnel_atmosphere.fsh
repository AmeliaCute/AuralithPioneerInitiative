#version 150

in vec4 vertexColor;
in vec3 vertexNormal;
in vec3 vertexPos;
in vec3 viewDirection;

uniform vec4 ColorModulator;
uniform float uFresnelPower;
uniform vec4 uAtmosphereColor;
uniform float uAtmosphereIntensity;

out vec4 fragColor;

void main() {
    vec3 normal = normalize(vertexNormal);
    vec3 viewDir = normalize(viewDirection);
    vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));

    vec4 baseColor = vertexColor * ColorModulator;

    if (uAtmosphereIntensity < 0.01)
    {
        float NdotL = max(dot(normal, lightDir), 0.0);

        float viewDotLight = dot(viewDir, lightDir);
        float backlitView = max(viewDotLight, 0.0);
        float lightingReduction = 1.0 - (backlitView * 0.5);
        float diffuse = NdotL * 0.5 * lightingReduction + 0.2;
        float rimLight = pow(1.0 - max(dot(normal, viewDir), 0.0), 3.0) * 0.2 * (1.0 - NdotL);

        float lighting = diffuse + rimLight;
        fragColor = baseColor * lighting;
        fragColor.a = 1.0;

    } else {
        float NdotV = max(dot(normal, viewDir), 0.0);
        float NdotL = max(dot(normal, lightDir), 0.0);

        float viewDotLight = dot(viewDir, lightDir);
        float backlitView = max(viewDotLight, 0.0);

        float fresnel1 = pow(1.0 - NdotV, uFresnelPower);
        float fresnel2 = pow(1.0 - NdotV, uFresnelPower * 0.5);

        float edgeFactor = 1.0 - NdotV * NdotV;
        float sharpEdge = pow(edgeFactor, 1.5);

        float fresnel = mix(fresnel1, fresnel2, 0.3);
        fresnel = mix(fresnel, sharpEdge, 0.4);
        fresnel = clamp(fresnel, 0.0, 1.0);

        float isEdge = pow(fresnel, 0.3);
        float centerModulation = 1.0 - (backlitView * 0.85 * (1.0 - isEdge));

        float litModulation = 1.0 - (NdotL * 0.3);

        float finalModulation = centerModulation * litModulation;

        float modulatedFresnel = fresnel * mix(finalModulation, 1.0, isEdge * 0.8);

        float density = pow(modulatedFresnel, 0.8);

        vec4 innerAtmosphere = uAtmosphereColor * 0.7;
        vec4 outerAtmosphere = uAtmosphereColor * 1.3;
        vec4 atmosphereColor = mix(innerAtmosphere, outerAtmosphere, fresnel);
        float glowIntensity = uAtmosphereIntensity * (1.0 + fresnel * 0.5);
        vec4 atmosphereGlow = atmosphereColor * density * glowIntensity;

        float pulse = 1.0 + sin(fresnel * 3.14159) * 0.15;
        atmosphereGlow *= pulse;

        float alphaGradient = pow(fresnel, 0.4) * 0.85;
        alphaGradient = mix(alphaGradient * finalModulation, alphaGradient, isEdge * 0.9);
        float innerGlow = pow(1.0 - fresnel, 3.0) * 0.15 * finalModulation;
        atmosphereGlow.rgb += uAtmosphereColor.rgb * innerGlow;

        fragColor = atmosphereGlow;
        fragColor.a = alphaGradient;

        fragColor.rgb = clamp(fragColor.rgb, 0.0, 1.0);
    }
}