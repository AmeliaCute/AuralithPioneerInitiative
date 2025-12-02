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
            
            float calculateSelfShadow(vec3 fragPos, vec3 planetCenter, vec3 lightPos, vec3 normal)
            {
                // For cubes with smooth shading, keep it simple
                // The main lighting is already handled by NdotL
                vec3 toLightDir = normalize(lightPos - fragPos);
                float facing = dot(normal, toLightDir);
                
                // Very subtle darkening for back faces only
                // Most of the shading comes from the standard diffuse term
                if (facing < -0.2) {
                    return smoothstep(-1.0, -0.2, facing) * 0.5 + 0.1;
                }
                
                return 1.0;
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
                    float casterSize = uShadowCasterRadii[i];
                    
                    float distToCaster = length(casterCenter - fragPos);
                    vec3 toCaster = casterCenter - fragPos;
                    float projectionOnRay = dot(toCaster, lightDir);
                    
                    if (projectionOnRay > 0.0 && projectionOnRay < distToLight)
                    {
                        vec3 closestPoint = fragPos + lightDir * projectionOnRay;
                        vec3 offsetFromCenter = closestPoint - casterCenter;
                        
                        vec3 absOffset = abs(offsetFromCenter);
                        float boxDist = max(max(absOffset.x, absOffset.y), absOffset.z);
                        
                        if (boxDist < casterSize)
                        {
                            float distanceRatio = projectionOnRay / distToLight;
                            float penumbraSize = casterSize * (0.02 + distanceRatio * 0.08);
                            
                            float shadowAmount = smoothstep(casterSize - penumbraSize, casterSize + penumbraSize * 0.5, boxDist);
                            shadow *= mix(0.05, 1.0, shadowAmount);
                        }
                    }
                }
                
                return shadow;
            }
            
            void main() 
            {
                vec3 N = normalize(FragNormal);
                vec3 V = normalize(uCameraPos - FragPos);
                vec3 L = normalize(uLightPos - FragPos);
                
                float NdotL = dot(N, L);
                float wrap = 0.75;
                float wrappedDiffuse = max(0.0, (NdotL + wrap) / (1.0 + wrap));
                
                wrappedDiffuse = pow(wrappedDiffuse, 0.8);
                
                float selfShadow = calculateSelfShadow(FragPos, uPlanetCenter, uLightPos, N);
                float interPlanetShadow = calculateInterPlanetShadow(FragPos, uLightPos);
                float shadow = selfShadow * interPlanetShadow;
                
                float distance = length(uLightPos - FragPos);
                float attenuation = 1.0 / (distance * distance * 0.00001 + 1.0);
                vec3 diffuse = uAlbedo * uLightColor * attenuation * wrappedDiffuse * shadow * 12.0;
                float ambientStrength = 0.02;
                vec3 ambient = vec3(ambientStrength) * uAlbedo * uAO;
                
                vec3 H = normalize(L + V);
                float specular = pow(max(dot(N, H), 0.0), 48.0);
                vec3 specularLight = specular * uLightColor * shadow * 6.0;
                float wideSpec = pow(max(dot(N, H), 0.0), 8.0);
                specularLight += wideSpec * uLightColor * shadow * 3.0;
                
                float rimFresnel = pow(1.0 - max(dot(N, V), 0.0), 2.5);
                vec3 rimLight = rimFresnel * uLightColor * 0.25 * max(NdotL, 0.0) * shadow;
                vec3 color = ambient + diffuse + specularLight + rimLight;
                
                color = color / (color + vec3(1.0));
                color = pow(color, vec3(1.0/2.2));
                
                FragColor = vec4(color, 1.0);
            }
            """;
}