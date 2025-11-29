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
            
            const float PI = 3.14159265359;
            
            float calculateShadow(vec3 fragPos, vec3 planetCenter, vec3 lightPos) 
            {
                vec3 toPlanet = fragPos - planetCenter;
                float distFromCenter = length(toPlanet);
                vec3 planetDir = toPlanet / distFromCenter;
                
                vec3 toLightFromCenter = lightPos - planetCenter;
                float lightDist = length(toLightFromCenter);
                vec3 lightDir = toLightFromCenter / lightDist;
                
                float alignment = dot(planetDir, lightDir);
                
                float terminator = smoothstep(-0.15, 0.15, alignment);
                
                float occlusion = 1.0;
                if (alignment < 0.0) 
                {
                    vec3 toLight = lightPos - fragPos;
                    float distToLight = length(toLight);
                    vec3 rayDir = toLight / distToLight;
                    
                    vec3 rayToPlanetCenter = planetCenter - fragPos;
                    float projectionOnRay = dot(rayToPlanetCenter, rayDir);
                    
                    if (projectionOnRay > 0.0 && projectionOnRay < distToLight) 
                    {
                        vec3 closestPoint = fragPos + rayDir * projectionOnRay;
                        float distToCenter = length(closestPoint - planetCenter);
                        
                        float shadowRadius = distFromCenter * 0.85;
                        occlusion = smoothstep(shadowRadius * 0.7, shadowRadius * 1.2, distToCenter);
                    }
                }
                
                return terminator * occlusion;
            }
            
            void main() 
            {
                vec3 N = normalize(FragNormal);
                vec3 V = normalize(uCameraPos - FragPos);
                vec3 L = normalize(uLightPos - FragPos);
                float NdotL = max(dot(N, L), 0.0);
                
                NdotL = clamp(NdotL, 0.3, 1.0);
                NdotL = 0.5 + (NdotL - 0.3) * 0.714;
                float distance = length(uLightPos - FragPos);
                float attenuation = 1.0 / (distance * distance * 0.00001 + 1.0);
                
                float shadow = calculateShadow(FragPos, uPlanetCenter, uLightPos);
                vec3 diffuse = uAlbedo * uLightColor * attenuation * NdotL * shadow * 15.0;
                float ambientStrength = 0.08 + (1.0 - shadow) * 0.03;
                vec3 ambient = vec3(ambientStrength) * uAlbedo * uAO;
                vec3 absLocal = abs(normalize(LocalPos));
                
                float maxAxis = max(max(absLocal.x, absLocal.y), absLocal.z);
                vec3 edgeDist = vec3(1.0) - absLocal / maxAxis;
                float minEdgeDist = min(min(edgeDist.x, edgeDist.y), edgeDist.z);
                
                float rimStrength = 0.18;
                float rimFalloff = 0.3;
                float rim = smoothstep(rimFalloff, 0.0, minEdgeDist);
                
                vec3 rimLight = rim * uAlbedo * rimStrength * smoothstep(0.1, 0.7, shadow);
                vec3 color = ambient + diffuse + rimLight;
                color = color / (color + vec3(1.0));
                color = pow(color, vec3(1.0/2.2));
                
                FragColor = vec4(color, 1.0);
            }
            """;
}