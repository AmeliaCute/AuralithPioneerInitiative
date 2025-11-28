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
            
            vec3 fresnelSchlick(float cosTheta, vec3 F0) {
                return F0 + (1.0 - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
            }
            
            float distributionGGX(vec3 N, vec3 H, float roughness) {
                float a = roughness * roughness;
                float a2 = a * a;
                float NdotH = max(dot(N, H), 0.0);
                float NdotH2 = NdotH * NdotH;
                float nom = a2;
                float denom = (NdotH2 * (a2 - 1.0) + 1.0);
                denom = PI * denom * denom;
                return nom / max(denom, 0.0000001);
            }
            
            float geometrySchlickGGX(float NdotV, float roughness) {
                float r = (roughness + 1.0);
                float k = (r * r) / 8.0;
                float nom = NdotV;
                float denom = NdotV * (1.0 - k) + k;
                return nom / max(denom, 0.0000001);
            }
            
            float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
                float NdotV = max(dot(N, V), 0.0);
                float NdotL = max(dot(N, L), 0.0);
                float ggx2 = geometrySchlickGGX(NdotV, roughness);
                float ggx1 = geometrySchlickGGX(NdotL, roughness);
                return ggx1 * ggx2;
            }
            
            void main() {
                vec3 N = normalize(FragNormal);
                vec3 V = normalize(uCameraPos - FragPos);
                vec3 F0 = vec3(0.04);
                F0 = mix(F0, uAlbedo, uMetallic);
                vec3 L = normalize(uLightPos - FragPos);
                vec3 H = normalize(V + L);
                float distance = length(uLightPos - FragPos);
                float attenuation = 1.0 / (distance * distance * 0.00001 + 1.0);
                vec3 radiance = uLightColor * attenuation * 50.0;
                float NDF = distributionGGX(N, H, uRoughness);
                float G = geometrySmith(N, V, L, uRoughness);
                vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);
                vec3 numerator = NDF * G * F;
                float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.0001;
                vec3 specular = numerator / denominator;
                vec3 kS = F;
                vec3 kD = vec3(1.0) - kS;
                kD *= 1.0 - uMetallic;
                float NdotL = max(dot(N, L), 0.0);
                float shadow = 1.0;
                vec3 lightDir = normalize(uLightPos - uPlanetCenter);
                vec3 posFromCenter = normalize(FragPos - uPlanetCenter);
                float lightSide = dot(posFromCenter, lightDir);
                shadow = smoothstep(-0.2, 0.2, lightSide);
                vec3 Lo = (kD * uAlbedo / PI + specular) * radiance * NdotL * shadow;
                vec3 ambient = vec3(0.02) * uAlbedo * uAO;
                vec3 color = ambient + Lo;
                color = color / (color + vec3(1.0));
                color = pow(color, vec3(1.0/2.2));
                FragColor = vec4(color, 1.0);
            }
            """;
}