package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.PBR;

public class PBRShaders
{

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
            uniform vec3 uShadowCasterPositions[10];
            uniform float uShadowCasterRadii[10];
            
            const float PI = 3.14159265359;
            
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
            
            float calculateSubsurfaceScattering(vec3 normal, vec3 lightDir, vec3 viewDir)
            {
                float NdotL = dot(normal, lightDir);
                vec3 H = normalize(lightDir + normal * 0.5);
                float scatter = pow(clamp(dot(viewDir, -H), 0.0, 1.0), 4.0);
            
                float darkSide = clamp(-NdotL, 0.0, 1.0);
                return scatter * darkSide * 0.15;
            }
            
            float calculateInterPlanetShadow(vec3 fragPos, vec3 lightPos)
            {
                vec3 toLight = lightPos - fragPos;
                float distToLight = length(toLight);
                vec3 lightDir = toLight / distToLight;
                float shadow = 1.0;
            
                for (int i = 0; i < uNumShadowCasters; i++)
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
            
                            float shadowAmount = smoothstep(casterRadius - penumbraSize,\s
                                                           casterRadius + penumbraSize,\s
                                                           boxDist);
                            float coreShadow = smoothstep(casterRadius * 0.5, casterRadius, boxDist);
                            shadowAmount = mix(coreShadow * 0.05, shadowAmount, 0.6);
                            shadow *= mix(0.01, 1.0, shadowAmount);
                        }  \s
                    }
                }
            
                return shadow;
            }
            
            float calculateAtmosphericRim(vec3 normal, vec3 viewDir, float NdotL)
            {
                float rim = 1.0 - max(dot(normal, viewDir), 0.0);
                rim = pow(rim, 3.0);
            
                float litRim = rim * max(NdotL, 0.0);
                return litRim * 0.3;
            }
            
            void main()
            {
                vec3 N = normalize(FragNormal);
                vec3 V = normalize(uCameraPos - FragPos);
                vec3 L = normalize(uLightPos - FragPos);
            
                float terminator = calculateSmoothTerminator(N, L, LocalPos);
                float interPlanetShadow = calculateInterPlanetShadow(FragPos, uLightPos);
                float shadow = terminator * interPlanetShadow;
                float distance = length(uLightPos - FragPos);
                float referenceDistance = 5000.0;
                float minDistance = 1000.0;
                float effectiveDistance = max(distance, minDistance);
            
                float attenuation = (referenceDistance * referenceDistance) / (effectiveDistance * effectiveDistance);
                attenuation = clamp(attenuation, 0.05, 2.0);
            
                vec3 diffuse = uAlbedo * uLightColor * shadow * attenuation;
            
                vec3 absLocalPos = abs(LocalPos);
                float maxAxis = max(max(absLocalPos.x, absLocalPos.y), absLocalPos.z);
                float cubeEdgeDistance = maxAxis / length(LocalPos);
                float cubicAmbient = pow(cubeEdgeDistance, 2.0);
            
                float ambientStrength = 0.0000000025;
                vec3 ambient = vec3(ambientStrength, ambientStrength, ambientStrength) * uAlbedo * uAO * cubicAmbient;
            
                float sss = calculateSubsurfaceScattering(N, L, V) * interPlanetShadow;
                vec3 subsurface = sss * uAlbedo * uLightColor * attenuation;
                vec3 H = normalize(L + V);
                float spec = pow(max(dot(N, H), 0.0), 64.0);
                vec3 specular = spec * uLightColor * shadow * attenuation * 0.3;
            
                float NdotL = dot(N, L);
                float atmosphericRim = calculateAtmosphericRim(N, V, NdotL);
                vec3 rimLight = atmosphericRim * uLightColor * interPlanetShadow * attenuation;
            
                vec3 color = ambient + diffuse + subsurface + specular + rimLight;
                color = color / (color + vec3(0.8));
                color = pow(color, vec3(1.0/2.2));
            
                FragColor = vec4(color, 1.0);
            }
            """;
}