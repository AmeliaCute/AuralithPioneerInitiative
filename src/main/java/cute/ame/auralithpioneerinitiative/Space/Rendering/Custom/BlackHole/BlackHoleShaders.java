package cute.ame.auralithpioneerinitiative.Space.Rendering.Custom.BlackHole;

public class BlackHoleShaders {

    public static final String VERTEX_SHADER = """
            #version 150 core
            
            in vec2 Position;
            in vec2 TexCoord;
            
            out vec2 texCoord;
            
            void main() {
                texCoord = TexCoord;
                gl_Position = vec4(Position, 0.0, 1.0);
            }
            """;

    public static final String FRAGMENT_SHADER = """
            #version 150
            
            #define PI 3.141592653589792
            #define minBend 0.15
            
            uniform float Fov;
            uniform float _Size;
            uniform float _Speed;
            uniform float Scale;
            uniform float Intensity;
            uniform float _Steps;
            
            uniform mat4 InvProjMatrix;
            uniform mat4 InvViewModleMatrix;
            uniform mat4 RotationMatrix;
            uniform mat4 FullMatN;
            uniform mat4 FullMatP;
            
            uniform vec3 CameraPosition;
            uniform vec3 BlackholePosition;
            uniform vec3 Color;
            vec3 BLACK_HOLE_COLOR;
            
            uniform vec2 Resolution;
            uniform float GameTime;
            uniform float RenderDistance;
            
            uniform sampler2D DiffuseSampler;
            uniform sampler2D depth;
            
            uniform vec2 OutSize;
            
            in vec2 texCoord;
            out vec4 fragColor;
            
            vec3 projectAndDivide(mat4 projectionMatrix, vec3 position) 
            {
                vec4 homogeneousPos = projectionMatrix * vec4(position, 1);
                return homogeneousPos.xyz / homogeneousPos.w;
            }
            
            vec3 screenToView(vec3 Position) 
            {
                vec3 ndcPos = Position * 2.0 - 1.0;
                return projectAndDivide(InvProjMatrix, ndcPos);
            }
            
            float viewDepth = texture(depth, texCoord).r;
            vec3 viewPos = screenToView(vec3(texCoord, viewDepth));
            vec3 viewPosScreen = screenToView(vec3(texCoord, 1.0));
            vec3 PlayerViewPos = (InvViewModleMatrix * vec4(viewPosScreen, 1.0)).xyz;
            float viewDistance = length(viewPos);
            
            float inverseCubeDist(vec3 p) 
            {
                return 1. / max(abs(p.x / dot(p, p)), max(abs(p.y / dot(p, p)), abs(p.z / dot(p, p))));
            }
            
            float cubeDist(vec3 p) 
            {
                return max(abs(p.x), max(abs(p.y), abs(p.z)));
            }
            
            float inverseCubeDist(vec2 p) 
            {
                return 1. / max(abs(p.x / dot(p, p)), abs(p.y / dot(p, p)));
            }
            
            float cubeDist(vec2 p) 
            {
                return max(abs(p.x), abs(p.y));
            }
            
            float hash(float x)
            {
                return fract(sin(x) * 152754.742);
            }
            
            float hash(vec2 x) 
            {
                return hash(x.x + hash(x.y));
            }
            
            float value(vec2 p, float f) 
            {
                float bl = hash(floor(p * f + vec2(0.0, 0.0)));
                float br = hash(floor(p * f + vec2(1.0, 0.0)));
                float tl = hash(floor(p * f + vec2(0.0, 1.0)));
                float tr = hash(floor(p * f + vec2(1.0, 1.0)));
                
                vec2 fr = fract(p * f);
                fr = (3. - 2. * fr) * fr * fr;
                float b = mix(bl, br, fr.x);
                float t = mix(tl, tr, fr.x);
                return mix(b, t, fr.y);
            }
            
            vec4 background(vec3 ray) 
            {
                vec3 calcRay = mat3(FullMatN) * ray;
                calcRay = calcRay * mat3(RotationMatrix);
                calcRay = calcRay / calcRay.z;
                vec2 uv = calcRay.xy;
                float fovslope = tan(Fov / 360. * PI);
                uv = vec2((uv.x / fovslope) / (OutSize.x / OutSize.y), uv.y / fovslope);
                float uvcubedist = cubeDist(uv);
                
                if (uvcubedist > 1.0) uv = uv / (uvcubedist * uvcubedist * uvcubedist);
                uv = fract(uv * 0.5 + vec2(0.5));
                
                vec4 nebulae = texture(DiffuseSampler, uv);
                return nebulae;
            }
            
            vec4 raymarchDisk(vec3 ray, vec3 zeroPos) 
            {
                vec3 position = zeroPos;
                float lengthPos = cubeDist(position.xz);
                float dist = min(1.0, lengthPos * (1. / _Size) * 0.5) * _Size * 0.4 * (1. / _Steps) / abs(ray.y);
                
                position += dist * _Steps * ray * 0.5;
                
                vec2 deltaPos;
                deltaPos.x = -zeroPos.z * 0.01 + zeroPos.x;
                deltaPos.y = zeroPos.x * 0.01 + zeroPos.z;
                deltaPos = normalize(deltaPos - zeroPos.xz);
                
                float parallel = dot(ray.xz, deltaPos);
                parallel /= sqrt(lengthPos);
                parallel *= 0.5;
                float redShift = parallel + 0.3;
                redShift *= redShift;
                redShift = clamp(redShift, 0.0, 1.0);
                
                float disMix = clamp((lengthPos - _Size * 2.0) * (1. / _Size) * 0.24, 0.0, 1.0);
                vec3 insideCol;
                
                if (Color == vec3(-1.0, -1.0, -1.0)) 
                {
                    insideCol = mix(vec3(1.0, 0.8, 0.0), vec3(0.5, 0.13, 0.02) * 0.2, disMix);
                    insideCol *= mix(vec3(0.4, 0.2, 0.1), vec3(1.6, 2.4, 4.0), redShift);
                } else
                {
                    insideCol = mix(BLACK_HOLE_COLOR, BLACK_HOLE_COLOR * 0.2, disMix);
                    insideCol *= mix(BLACK_HOLE_COLOR * 0.8, BLACK_HOLE_COLOR * 2.0, redShift);
                }
                
                insideCol *= 1.25;
                redShift += 0.12;
                redShift *= redShift;
                
                vec4 o = vec4(0.0);
                
                for (float i = 0.0; i < _Steps; i++) 
                {
                    position -= dist * ray;
                    
                    float intensity = clamp(1. - abs((i - 0.8) * (1. / _Steps) * 2.0), 0.0, Intensity / 255.0);
                    float lengthPos = cubeDist(position.xz);
                    float distMult = 1.0;
                    
                    distMult *= clamp((lengthPos - _Size * 0.75) * (1. / _Size) * 1.5, 0.0, 1.0);
                    distMult *= clamp((_Size * 10. - lengthPos) * (1. / _Size) * 0.20, 0.0, 1.0);
                    distMult *= distMult;
                    
                    float u = lengthPos + GameTime * _Size * 0.3 + intensity * _Size * 0.2;
                    
                    vec2 xy;
                    float rot = mod(GameTime * _Speed, 8192.0);
                    xy.x = -position.z * sin(rot) + position.x * cos(rot);
                    xy.y = position.x * sin(rot) + position.z * cos(rot);
                    
                    float x = abs(xy.x / xy.y);
                    float angle = 0.02 * atan(x);
                    
                    const float f = 70.0;
                    float noise = value(vec2(angle, u * (1. / _Size) * 0.05), f);
                    noise = noise * 0.66 + 0.33 * value(vec2(angle, u * (1. / _Size) * 0.05), f * 2.0);
                    
                    float extraWidth = noise * (1. - clamp(i * (1. / _Steps) * 2. - 1.0, 0.0, 1.0));
                    float alpha = clamp(noise * (intensity + extraWidth) * ((1. / _Size) * 10. + 0.01) * dist * distMult, 0.0, 1.0);
                    
                    vec3 col = 2. * mix(vec3(0.3, 0.2, 0.15) * insideCol, insideCol, min(1.0, intensity * 2.0));
                    o = clamp(vec4(col * alpha + o.rgb * (1. - alpha), o.a * (1. - alpha) + alpha), vec4(0.0), vec4(1.0));
                    
                    lengthPos *= (1. / _Size);
                    o.rgb += redShift * (intensity + 0.5) * (1. / _Steps) * 100. * distMult / (lengthPos * lengthPos);
                }
                
                o.rgb = clamp(o.rgb - 0.005, 0.0, 1.0);
                return o;
            }
            
            void main() 
            {
                BLACK_HOLE_COLOR = Color / 255.0;
                
                float fovslope = tan(Fov / 360. * PI);
                float aspectratio = OutSize.x / OutSize.y;
                vec3 dircord = vec3(texCoord.x * aspectratio * fovslope, texCoord.y * fovslope, 1.0);
                vec3 blhcord = normalize(BlackholePosition - CameraPosition) * mat3(RotationMatrix);
                float bhangle = (1. - dot(dircord, blhcord)) * 1.5;
                float nocalcang = length((BlackholePosition - CameraPosition) / Scale);
                nocalcang = 7. / nocalcang;
                
                if (bhangle < nocalcang) 
                {
                    fragColor = texture(DiffuseSampler, texCoord);
                    return;
                }
                
                vec3 ray = mat3(FullMatP) * normalize(PlayerViewPos);
                vec3 pos = mat3(FullMatP) * ((BlackholePosition - CameraPosition) / Scale);
                vec3 initalRay = ray;
                float totalBend = 0.0;
                
                float dist = length(pos);
                
                vec4 col = vec4(0.0);
                vec4 glow = vec4(0.0);
                vec4 outCol = vec4(100.0);
                
                for (int disks = 0; disks < 20; ++disks) 
                {
                    for (int h = 0; h < 6; ++h) {
                        float centDist = cubeDist(pos);
                        float invDist = 1. / centDist;
                        float stepDist = 0.92 * abs(pos.y / ray.y);
                        float farLimit = centDist * 0.5;
                        float closeLimit = centDist * 0.1 + 0.05 * centDist * centDist * (1. / _Size);
                        stepDist = min(stepDist, min(farLimit, closeLimit));
                        
                        float invDistSqr = invDist * invDist;
                        float bendForce = stepDist * invDistSqr * _Size * 0.625;
                        vec3 bender = (bendForce * invDist) * pos;
                        ray = (ray - bender) / cubeDist(ray - bender);
                        totalBend = totalBend + cubeDist(bender);
                        
                        pos += stepDist * (ray / sqrt(3.0));
                        
                        if (Color == vec3(-1.0, -1.0, -1.0)) 
                            glow += vec4(1.2, 1.1, 1, 1.0) * (0.01 * stepDist * invDistSqr * invDistSqr * clamp(centDist * 2. - 1.2, 0.0, 1.0));
                        else 
                        {
                            float redShift = clamp(dot(ray.xz, normalize(vec2(-pos.z, pos.x))) / max(1.0, length(pos.xz)), 0.0, 1.0);
                            redShift *= redShift;
                            vec3 glowColor = mix(BLACK_HOLE_COLOR, BLACK_HOLE_COLOR * 1.5, redShift);
                            glow += vec4(glowColor, 1.0) * (0.01 * stepDist * invDistSqr * invDistSqr * clamp(centDist * 2. - 1.2, 0.0, 1.0));
                        }
                    }
                    
                    float dist2 = length(pos);
                    
                    if (dist2 < _Size) 
                    {
                        outCol = vec4(col.rgb * col.a + glow.rgb * (1. - col.a), 1.0);
                        break;
                    } else if (dist2 > _Size * 1000.0) 
                    {
                        float thresholdsmooth = totalBend;
                        thresholdsmooth = thresholdsmooth - minBend;
                        thresholdsmooth = max(min(thresholdsmooth * 7.0, 1.0), 0.0);
                        thresholdsmooth = (3. - 2. * thresholdsmooth) * thresholdsmooth * thresholdsmooth;
                        vec3 smoothedRay = thresholdsmooth * ray + (1. - thresholdsmooth) * initalRay;
                        vec4 bg = background(smoothedRay / length(smoothedRay));
                        
                        if (totalBend > minBend) outCol = vec4(col.rgb * col.a + bg.rgb * (1. - col.a) + glow.rgb * (1. - col.a), 1.0);
                        else outCol = texture(DiffuseSampler, texCoord);
                            
                        break;
                    } else if (abs(pos.y) <= _Size * 0.007) 
                    {
                        vec4 diskCol = raymarchDisk(ray, pos);
                        pos.y = 0.0;
                        pos += abs(_Size * 0.001 / ray.y) * ray;
                        col = vec4(diskCol.rgb * (1. - col.a) + col.rgb, col.a + diskCol.a * (1. - col.a));
                    }
                }
                
                if (outCol.r == 100.0) outCol = vec4(col.rgb + glow.rgb * (col.a + glow.a), 1.0);
                col = outCol;
                
                float blackholeDistance = distance(BlackholePosition, CameraPosition);
                vec3 finalColor = vec3(0.0);
                
                if (blackholeDistance < viewDistance) finalColor = col.rgb;
                else finalColor = viewDistance < RenderDistance * 16.0 ? texture(DiffuseSampler, texCoord).rgb : col.rgb;
                
                fragColor = vec4(finalColor, 1.0);
            }
            """;
}