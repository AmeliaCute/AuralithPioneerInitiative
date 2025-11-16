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
uniform vec3 uNormalizedPlanetPos;
uniform float uSizeScale;

uniform float uCloudTurbulence;
uniform float uCloudDensity;
uniform float uCloudEdgeSoftness;
uniform float uCloudShadowIntensity;
uniform int uCloudCastsShadows;
uniform float uCloudAnimationSpeed;

out vec4 fragColor;

float getCloudLayer(vec3 pixelPos, int layerIndex, float timeOffset)
{
    float speed = uCloudSpeed * (1.0 + float(layerIndex) * 0.3) * uCloudAnimationSpeed;

    vec2 cloudUV = (pixelPos.xz + vec2(uTime * speed + timeOffset, 0.0)) / uSizeScale;
    cloudUV += (pixelPos.yy * 0.5) / uSizeScale;

    float cloudNoise = fbm(cloudUV * 2.0 * uCloudTurbulence + uSeed, 3);
    float threshold = 1.0 - uCloudCoverage;
    float cloudMask = smoothstep(threshold - uCloudEdgeSoftness, threshold + uCloudEdgeSoftness, cloudNoise);

    return cloudMask;
}

vec4 getCloudColor(int biome, float density)
{
    vec4 color = uCloudColor;

    switch(biome)
    {
        case 5:
            float bandVariation = density * 0.3;
            color = mix(
                vec4(0.95, 0.8, 0.6, 1.0),
                vec4(0.9, 0.7, 0.5, 1.0),
                bandVariation
            );

            if (density > 0.85)
                color = vec4(0.95, 0.3, 0.2, 1.0);

            break;

        case 6:
            color = mix(
                vec4(0.7, 0.9, 0.4, 1.0),
                vec4(0.5, 0.8, 0.3, 1.0),
                density
            );
            break;

        case 7:
            color = mix(
                vec4(0.3, 0.25, 0.2, 1.0),
                vec4(0.4, 0.35, 0.3, 1.0),
                density
            );
            break;

        default:
            break;
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
    totalCloudDensity *= uCloudDensity;

    if (totalCloudDensity < 0.05)
        discard;

    vec4 cloudColor = getCloudColor(uBiomeType, maxDensity);

    float NdotL = max(dot(normal, lightDir), 0.0);
    float diffuse = NdotL * 0.7 + 0.3;

    if (uBiomeType == 5) diffuse = NdotL * 0.4 + 0.6;

    if (uCloudCastsShadows > 0)
        diffuse *= (1.0 - uCloudShadowIntensity * (1.0 - NdotL));

    cloudColor.rgb *= diffuse;
    cloudColor.a = totalCloudDensity;

    float edgeFade = pow(max(dot(normal, viewDir), 0.0), 0.5);
    cloudColor.a *= edgeFade;

    fragColor = cloudColor * ColorModulator;
}