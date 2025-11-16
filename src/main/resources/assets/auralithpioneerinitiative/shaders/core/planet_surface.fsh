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
uniform vec3 uNormalizedPlanetPos;
uniform float uSizeScale;

uniform vec4 uSurfaceColor1;
uniform vec4 uSurfaceColor2;
uniform vec4 uSurfaceColor3;
uniform float uNoiseScale1;
uniform float uNoiseScale2;
uniform float uNoiseScale3;
uniform int uNoiseOctaves;
uniform float uRimLightPower;
uniform float uRimLightIntensity;

out vec4 fragColor;

vec4 getBiomeColorDirect(float n1, float n2, float n3, float combined, int biome, vec3 pixelPos)
{
    vec4 color1 = uSurfaceColor1;
    vec4 color2 = uSurfaceColor2;
    vec4 color3 = uSurfaceColor3;

    switch(biome)
    {
        case 0:
            if (combined < uLandRatio)
                return mix(color1, color2, combined / uLandRatio);
            else
                return color3;

        case 1:
            if (combined < uIceCoverage)
                return mix(color1, color2, n1);
            else
                return color3;

        case 2:
            return mix(mix(color1, color2, n1), color3, n2 * 0.3);

        case 3:
            return mix(mix(color1, color2, n1), color3, n2 * 0.5);

        case 4:
            return mix(mix(color1, color2, n1), color3, n3);

        case 5:
            float bandPos = pixelPos.y * 8.0 + n1 * 0.5;
            float band = fract(bandPos);

            vec4 bandColor = mix(color1, color2, band);
            return mix(bandColor, color3, n2 * 0.3);

        case 6:
            return mix(mix(color1, color2, n1), color3, n2 * 0.4);

        case 7:
            if (combined > 0.7)
                return mix(color2, vec4(1.0, 0.5, 0.1, 1.0), n1);
            return mix(color1, color3, n2);


        default:
            return mix(mix(color1, color2, n1), color3, n2 * 0.5);

    }
}


void main()
{
    vec3 normal = normalize(vertexNormal);
    vec3 viewDir = normalize(viewDirection);
    vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));

    vec3 relativeToCenter = worldPos - uPlanetPos;
    vec2 screenUV = gl_FragCoord.xy / float(uPixelSize);
    screenUV = floor(screenUV);

    vec3 stablePos = relativeToCenter + vec3(screenUV.x, screenUV.y, 0.0) * 0.001;
    vec3 pixelPos = pixelate(stablePos * 0.5, uPixelSize);
    vec2 baseUV = (pixelPos.xz + pixelPos.yy * 0.5) / uSizeScale;

    float n1 = fbm(baseUV * uNoiseScale1 + uSeed, uNoiseOctaves);
    float n2 = fbm(baseUV * uNoiseScale2 + uSeed * 1.5, uNoiseOctaves);
    float n3 = fbm(baseUV * uNoiseScale3 + uSeed * 2.0, uNoiseOctaves);

    float combined = (n1 + n2 + n3) / 3.0;
    vec4 baseColor = getBiomeColorDirect(n1, n2, n3, combined, uBiomeType, pixelPos);

    float NdotL = max(dot(normal, lightDir), 0.0);
    float viewDotLight = dot(viewDir, lightDir);
    float backlitView = max(viewDotLight, 0.0);
    float lightingReduction = 1.0 - (backlitView * 0.5);
    float diffuse = NdotL * 0.6 * lightingReduction + 0.3;

    float rimLight = pow(1.0 - max(dot(normal, viewDir), 0.0), uRimLightPower) * uRimLightIntensity;
    float lighting = diffuse + rimLight;

    fragColor = baseColor * ColorModulator * lighting;
    fragColor.a = 1.0;
}