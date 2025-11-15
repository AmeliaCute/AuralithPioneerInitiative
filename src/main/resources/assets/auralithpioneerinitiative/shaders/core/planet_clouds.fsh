#version 150

#moj_import <auralithpioneerinitiative:planet_common.glsl>

in vec4 vertexColor;
in vec3 vertexNormal;
in vec3 vertexPos;
in vec3 worldPos;
in vec3 viewDirection;

uniform vec4 ColorModulator;
uniform float uTime;

uniform int uPixelSize;
uniform int uBiomeType;
uniform float uSeed;
uniform float uCloudCoverage;
uniform int uCloudLayers;
uniform float uCloudSpeed;
uniform vec4 uCloudColor;

out vec4 fragColor;

float getCloudLayer(vec3 pixelPos, int layerIndex, float timeOffset)
{
    float speed = uCloudSpeed * (1.0 + float(layerIndex) * 0.3);
    vec2 cloudUV = pixelPos.xz + vec2(uTime * speed + timeOffset, 0.0);
    cloudUV += pixelPos.yy * 0.5;

    float cloudNoise = fbm(cloudUV * 2.0 + uSeed, 3);
    float threshold = 1.0 - uCloudCoverage;
    float cloudMask = smoothstep(threshold - 0.1, threshold + 0.1, cloudNoise);

    return cloudMask;
}

vec4 getCloudColor(int biome, float density)
{
    vec4 color = uCloudColor;

    if (biome == 5) { // Gas Giant
        float bandVariation = density * 0.3;
        color = mix(
            vec4(0.95, 0.8, 0.6, 1.0),
            vec4(0.9, 0.7, 0.5, 1.0),
            bandVariation
        );

    if (density > 0.85) {
        color = vec4(0.95, 0.3, 0.2, 1.0);
    }
    } else if (biome == 6) { // Ammonia
        color = mix(
            vec4(0.7, 0.9, 0.4, 1.0),
            vec4(0.5, 0.8, 0.3, 1.0),
            density
        );
    } else if (biome == 7) { // Volcanic
        color = mix(
            vec4(0.3, 0.25, 0.2, 1.0),
            vec4(0.4, 0.35, 0.3, 1.0),
            density
        );
    }

    return color;
}

void main()
{
    vec3 normal = normalize(vertexNormal);
    vec3 viewDir = normalize(viewDirection);
    vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));
    vec3 pixelPos = pixelate(worldPos * 0.5, uPixelSize);

    float totalCloudDensity = 0.0;
    float maxDensity = 0.0;

    for(int i = 0; i < uCloudLayers && i < 3; i++)
    {
        float layerOffset = float(i) * 100.0;
        float layerDensity = getCloudLayer(pixelPos, i, layerOffset);

        float layerWeight = 1.0 / (float(i) + 1.0);
        totalCloudDensity += layerDensity * layerWeight;
        maxDensity = max(maxDensity, layerDensity);
    }

    totalCloudDensity /= float(uCloudLayers);

    if (totalCloudDensity < 0.05)
    discard;

    vec4 cloudColor = getCloudColor(uBiomeType, maxDensity);

    float NdotL = max(dot(normal, lightDir), 0.0);
    float diffuse = NdotL * 0.7 + 0.3;

    if (uBiomeType == 5) diffuse = NdotL * 0.4 + 0.6;

    cloudColor.rgb *= diffuse;
    cloudColor.a = totalCloudDensity * 0.8;

    float edgeFade = pow(max(dot(normal, viewDir), 0.0), 0.5);
    cloudColor.a *= edgeFade;

    fragColor = cloudColor * ColorModulator;
}