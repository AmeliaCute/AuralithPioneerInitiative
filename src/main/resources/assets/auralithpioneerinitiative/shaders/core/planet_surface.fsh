#version 150

#moj_import <auralithpioneerinitiative:planet_common.glsl>

in vec4 vertexColor;
in vec3 vertexNormal;
in vec3 vertexPos;
in vec3 worldPos;
in vec3 viewDirection;

uniform vec4 ColorModulator;
uniform int uPixelSize;
uniform int uBiomeType;
uniform float uSeed;
uniform float uIceCoverage;
uniform float uLandRatio;
uniform vec3 uPlanetPos;

out vec4 fragColor;

vec4 getBiomeColorDirect(float n1, float n2, float n3, float combined, int biome, vec3 pixelPos)
{
    vec4 color1, color2, color3;

    if (biome == 0)
    { // Aquatic
        color1 = vec4(0.1, 0.3, 0.6, 1.0);
        color2 = vec4(0.2, 0.5, 0.8, 1.0);
        color3 = vec4(0.3, 0.6, 0.3, 1.0);
        if (combined < uLandRatio)
        {
            return mix(color1, color2, combined / uLandRatio);
        } else {
            return color3;
        }

    } else if (biome == 1) { // Arctic
        color1 = vec4(0.85, 0.9, 0.95, 1.0);
        color2 = vec4(0.7, 0.8, 0.9, 1.0);
        color3 = vec4(0.4, 0.5, 0.6, 1.0);

        if (combined < uIceCoverage) {
        return mix(color1, color2, n1);
        } else {
        return color3;
        }

    } else if (biome == 2) { // Desert
        color1 = vec4(0.9, 0.7, 0.4, 1.0);
        color2 = vec4(0.8, 0.6, 0.3, 1.0);
        color3 = vec4(0.6, 0.4, 0.2, 1.0);

        return mix(mix(color1, color2, n1), color3, n2 * 0.3);

    } else if (biome == 3) { // Rocky
        color1 = vec4(0.4, 0.35, 0.3, 1.0);
        color2 = vec4(0.5, 0.45, 0.4, 1.0);
        color3 = vec4(0.3, 0.25, 0.2, 1.0);

        return mix(mix(color1, color2, n1), color3, n2 * 0.5);

    } else if (biome == 4) { // Ice World
        color1 = vec4(0.7, 0.85, 0.95, 1.0);
        color2 = vec4(0.5, 0.7, 0.9, 1.0);
        color3 = vec4(0.9, 0.95, 1.0, 1.0);

        return mix(mix(color1, color2, n1), color3, n3);

    } else if (biome == 5) { // Gas Giant (base layer)
        float bandPos = pixelPos.y * 8.0 + n1 * 0.5;
        float band = fract(bandPos);

        color1 = vec4(0.9, 0.7, 0.5, 1.0);
        color2 = vec4(0.8, 0.6, 0.4, 1.0);
        color3 = vec4(0.95, 0.8, 0.6, 1.0);

        vec4 bandColor = mix(color1, color2, band);
        return mix(bandColor, color3, n2 * 0.3);

    } else if (biome == 6) { // Ammonia World
        color1 = vec4(0.6, 0.8, 0.3, 1.0);
        color2 = vec4(0.4, 0.7, 0.5, 1.0);
        color3 = vec4(0.8, 0.9, 0.5, 1.0);

        return mix(mix(color1, color2, n1), color3, n2 * 0.4);

    } else if (biome == 7) { // Volcanic
        color1 = vec4(0.2, 0.15, 0.1, 1.0);
        color2 = vec4(0.9, 0.3, 0.1, 1.0);
        color3 = vec4(0.3, 0.25, 0.2, 1.0);

        if (combined > 0.7) {
            return mix(color2, vec4(1.0, 0.5, 0.1, 1.0), n1);
        }

        return mix(color1, color3, n2);
    }

    return vec4(0.5, 0.5, 0.5, 1.0);
}

void main()
{
    vec3 normal = normalize(vertexNormal);
    vec3 viewDir = normalize(viewDirection);
    vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));

    // Pixelate using SCREEN SPACE position to avoid interpolation issues
    vec3 relativeToCenter = worldPos - uPlanetPos;

    // The key: pixelate based on fragment screen position derivative
    vec2 screenUV = gl_FragCoord.xy / float(uPixelSize);
    screenUV = floor(screenUV);

    // Mix screen-space stability with world-space coordinates
    vec3 stablePos = relativeToCenter + vec3(screenUV.x, screenUV.y, 0.0) * 0.001;
    vec3 pixelPos = pixelate(stablePos * 0.5, uPixelSize);

    vec2 baseUV = pixelPos.xz + pixelPos.yy * 0.5;

    float n1 = fbm(baseUV * 3.0 + uSeed, 3);
    float n2 = fbm(baseUV * 5.0 + uSeed * 1.5, 3);
    float n3 = fbm(baseUV * 4.0 + uSeed * 2.0, 3);

    float combined = (n1 + n2 + n3) / 3.0;

    vec4 baseColor = getBiomeColorDirect(n1, n2, n3, combined, uBiomeType, pixelPos);

    float NdotL = max(dot(normal, lightDir), 0.0);
    float viewDotLight = dot(viewDir, lightDir);
    float backlitView = max(viewDotLight, 0.0);
    float lightingReduction = 1.0 - (backlitView * 0.5);
    float diffuse = NdotL * 0.6 * lightingReduction + 0.3;

    float rimLight = pow(1.0 - max(dot(normal, viewDir), 0.0), 2.0) * 0.15;
    float lighting = diffuse + rimLight;

    fragColor = baseColor * ColorModulator * lighting;
    fragColor.a = 1.0;
}