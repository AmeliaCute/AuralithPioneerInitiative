package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PBR;

import cute.ame.auralithpioneerinitiative.Space.Rendering.RenderingConstants;

public class PBRShaders {

    public static final String VERTEX_SHADER = """
            #version 150 core
            
            in vec3 Position;
            in vec3 Normal;
            
            out vec3 FragPos;
            out vec3 FragNormal;
            out vec3 LocalPos;
            
            uniform mat4 uModel;
            uniform mat4 uView;
            uniform mat4 uProjection;
            uniform mat4 uNormalMatrix;
            
            void main() {
                vec4 worldPos = uModel * vec4(Position, 1.0);
                FragPos = worldPos.xyz;
                LocalPos = Position;
                FragNormal = mat3(uNormalMatrix) * Normal;
                gl_Position = uProjection * uView * worldPos;
            }
            """;

    public static final String FRAGMENT_SHADER = """
            #version 150 core
            
            in vec3 FragPos;
            in vec3 FragNormal;
            in vec3 LocalPos;
            
            out vec4 FragColor;
            
            uniform vec3 uCameraPos;
            uniform vec3 uLightPos;
            uniform vec3 uLightColor;
            uniform vec3 uAlbedo;
            uniform float uMetallic;
            uniform float uRoughness;
            uniform float uAO;
            uniform vec3 uPlanetCenter;
            
            uniform int uNumShadowCasters;
            uniform vec3 uShadowCasterPositions[%d];
            uniform float uShadowCasterRadii[%d];
            
            uniform int uNumDynamicPointLights;
            uniform vec3 uDynamicPointLightPositions[%d];
            uniform vec3 uDynamicPointLightColors[%d];
            uniform vec4 uDynamicPointLightParams[%d]; // brightness, range, linear, quadratic
            
            uniform int uNumDynamicSpotlights;
            uniform vec3 uDynamicSpotlightPositions[%d];
            uniform vec3 uDynamicSpotlightDirections[%d];
            uniform vec3 uDynamicSpotlightColors[%d];
            uniform vec4 uDynamicSpotlightParams[%d]; // brightness, range, innerCone, outerCone
            
            const float PI = 3.14159265359;
            
            float DistributionGGX(vec3 N, vec3 H, float roughness)
            {
                float a = roughness * roughness;
                float a2 = a * a;
                float NdotH = max(dot(N, H), 0.0);
                float NdotH2 = NdotH * NdotH;
            
                float nom = a2;
                float denom = (NdotH2 * (a2 - 1.0) + 1.0);
                denom = PI * denom * denom;
            
                return nom / max(denom, 0.0001);
            }
            
            float GeometrySchlickGGX(float NdotV, float roughness) 
            {
                float r = (roughness + 1.0);
                float k = (r * r) / 8.0;
                
                float nom = NdotV;
                float denom = NdotV * (1.0 - k) + k;
                
                return nom / max(denom, 0.0001);
            }
            
            float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness) 
            {
                float NdotV = max(dot(N, V), 0.0);
                float NdotL = max(dot(N, L), 0.0);
                float ggx2 = GeometrySchlickGGX(NdotV, roughness);
                float ggx1 = GeometrySchlickGGX(NdotL, roughness);
                
                return ggx1 * ggx2;
            }
            
            vec3 fresnelSchlick(float cosTheta, vec3 F0) 
            {
                return F0 + (1.0 - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
            }
            
            float calculateSmoothTerminator(vec3 normal, vec3 lightDir, vec3 localPos) 
            {
                vec3 sphericalNormal = normalize(localPos);
                float NdotL = dot(sphericalNormal, lightDir);
                
                vec3 centerToFragment = normalize(localPos);
                vec3 absLightDir = abs(lightDir);
                float maxComponent = max(max(absLightDir.x, absLightDir.y), absLightDir.z);
                float minComponent = min(min(absLightDir.x, absLightDir.y), absLightDir.z);
                float straightness = (maxComponent - minComponent);
                float terminatorThreshold = mix(-0.4, -0.9, straightness);
                float adjustedNdotL = NdotL - terminatorThreshold;
                
                float terminatorSoftness = 0.6;
                float wrapped = (adjustedNdotL + terminatorSoftness) / (1.0 + terminatorSoftness);
                wrapped = clamp(wrapped, 0.0, 1.0);
                
                float darkness = smoothstep(0.0, 1.0, wrapped);
                darkness = pow(darkness, 12.0);
                
                return darkness;
            }
            
            float calculateInterPlanetShadow(vec3 fragPos, vec3 lightPos) 
            {
                vec3 toLight = lightPos - fragPos;
                float distToLight = length(toLight);
                vec3 lightDir = toLight / distToLight;
                float shadow = 1.0;
                
                for (int i = 0; i < uNumShadowCasters; ++i) 
                {
                    vec3 casterCenter = uShadowCasterPositions[i];
                    float casterRadius = uShadowCasterRadii[i];
                    
                    vec3 toCaster = casterCenter - fragPos;
                    float distToCaster = length(toCaster);
                    float projectionOnRay = dot(toCaster, lightDir);
                    
                    if (projectionOnRay > 0.0 && projectionOnRay < distToLight) 
                    {
                        vec3 closestPoint = fragPos + lightDir * projectionOnRay;
                        vec3 offsetFromCenter = closestPoint - casterCenter;
                        vec3 absOffset = abs(offsetFromCenter);
                        float boxDist = max(max(absOffset.x, absOffset.y), absOffset.z);
                        
                        if (boxDist < casterRadius * 2.0) 
                        {
                            float distanceRatio = projectionOnRay / distToLight;
                            float penumbraSize = casterRadius * (0.5 + distanceRatio * 0.8);
                            
                            float shadowAmount = smoothstep(casterRadius - penumbraSize,  casterRadius + penumbraSize,  boxDist);
                            float coreShadow = smoothstep(casterRadius * 0.5, casterRadius, boxDist);
                            shadowAmount = mix(coreShadow * 0.05, shadowAmount, 0.6);
                            shadow *= mix(0.01, 1.0, shadowAmount);
                        }
                    }
                }
                
                return shadow;
            }
            
            vec3 calculatePBRLight(vec3 N, vec3 V, vec3 L, vec3 lightColor, float lightIntensity, vec3 F0) 
            {
                vec3 H = normalize(V + L);
                
                float NDF = DistributionGGX(N, H, uRoughness);
                float G = GeometrySmith(N, V, L, uRoughness);
                vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);
                
                vec3 numerator = NDF * G * F;
                float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.0001;
                vec3 specular = numerator / denominator;
                
                vec3 kS = F;
                vec3 kD = vec3(1.0) - kS;
                kD *= 1.0 - uMetallic;
                
                float NdotL = max(dot(N, L), 0.0);
                return (kD * uAlbedo / PI + specular) * lightColor * lightIntensity * NdotL;
            }
            
            vec3 calculateDynamicPointLight(int index, vec3 N, vec3 V, vec3 F0) 
            {
                vec3 lightPos = uDynamicPointLightPositions[index];
                vec3 lightColor = uDynamicPointLightColors[index];
                float brightness = uDynamicPointLightParams[index].x;
                float range = uDynamicPointLightParams[index].y;
                float linear = uDynamicPointLightParams[index].z;
                float quadratic = uDynamicPointLightParams[index].w;
                
                vec3 L = lightPos - FragPos;
                float distance = length(L);
                if (distance > range) return vec3(0.0);
                
                L = normalize(L);
                float attenuation = 1.0 / (1.0 + linear * distance + quadratic * (distance * distance));
                float terminator = calculateSmoothTerminator(N, L, LocalPos);
                vec3 radiance = calculatePBRLight(N, V, L, lightColor, brightness * attenuation * terminator, F0);
                
                return radiance;
            }
            
            vec3 calculateDynamicSpotlight(int index, vec3 N, vec3 V, vec3 F0) 
            {
                vec3 lightPos = uDynamicSpotlightPositions[index];
                vec3 lightDir = normalize(uDynamicSpotlightDirections[index]);
                vec3 lightColor = uDynamicSpotlightColors[index];
                float brightness = uDynamicSpotlightParams[index].x;
                float range = uDynamicSpotlightParams[index].y;
                float innerCone = uDynamicSpotlightParams[index].z;
                float outerCone = uDynamicSpotlightParams[index].w;
                
                vec3 L = lightPos - FragPos;
                float distance = length(L);
                if (distance > range) return vec3(0.0);
                
                L = normalize(L);
                float theta = dot(L, normalize(-lightDir));
                if (theta <= outerCone) return vec3(0.0);
                
                float epsilon = innerCone - outerCone;
                float intensity = clamp(pow((theta - outerCone) / epsilon, 3.0), 0.0, 1.0);
                float distanceFactor = 1.0 - clamp(distance / range, 0.0, 1.0);
                
                float terminator = calculateSmoothTerminator(N, L, LocalPos);
                vec3 radiance = calculatePBRLight(N, V, L, lightColor,  brightness * intensity * distanceFactor * terminator, F0);
                return radiance;
            }
            
            void main() {
                vec3 N = normalize(FragNormal);
                vec3 V = normalize(uCameraPos - FragPos);
                
                vec3 F0 = vec3(0.04);
                F0 = mix(F0, uAlbedo, uMetallic);
                
                vec3 L = normalize(uLightPos - FragPos);
                
                float terminator = calculateSmoothTerminator(N, L, LocalPos);
                float interPlanetShadow = calculateInterPlanetShadow(FragPos, uLightPos);
                float shadow = terminator * interPlanetShadow;
                
                vec3 sunRadiance = calculatePBRLight(N, V, L, uLightColor, shadow, F0);
                vec3 dynamicRadiance = vec3(0.0);
                
                for (int i = 0; i < uNumDynamicPointLights && i < 16; ++i) dynamicRadiance += calculateDynamicPointLight(i, N, V, F0);
                
                for (int i = 0; i < uNumDynamicSpotlights && i < 16; ++i) dynamicRadiance += calculateDynamicSpotlight(i, N, V, F0);

                vec3 absLocalPos = abs(LocalPos);
                float maxAxis = max(max(absLocalPos.x, absLocalPos.y), absLocalPos.z);
                float cubeEdgeDistance = maxAxis / length(LocalPos);
                float cubicAmbient = pow(cubeEdgeDistance, 2.0);
                
                float ambientStrength = 0.005;
                vec3 ambient = vec3(ambientStrength) * uAlbedo * uAO * cubicAmbient;
                
                vec3 color = ambient + sunRadiance + dynamicRadiance;
                
                color = color / (color + vec3(0.8));
                color = pow(color, vec3(1.0/2.2));
                
                FragColor = vec4(color, 1.0);
            }
            """.formatted(
                RenderingConstants.MAX_SHADOW_CASTERS,
                RenderingConstants.MAX_SHADOW_CASTERS,
                RenderingConstants.MAX_POINT_LIGHTS,
                RenderingConstants.MAX_POINT_LIGHTS,
                RenderingConstants.MAX_POINT_LIGHTS,
                RenderingConstants.MAX_SPOTLIGHTS,
                RenderingConstants.MAX_SPOTLIGHTS,
                RenderingConstants.MAX_SPOTLIGHTS,
                RenderingConstants.MAX_SPOTLIGHTS
            );
}