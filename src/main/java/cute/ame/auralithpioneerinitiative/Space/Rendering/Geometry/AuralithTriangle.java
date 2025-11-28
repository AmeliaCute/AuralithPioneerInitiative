package cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry;

import org.joml.Vector3f;

public record AuralithTriangle
(
        AuralithVertex v1,
        AuralithVertex v2,
        AuralithVertex v3,
        Vector3f faceNormal
)
{
    public AuralithTriangle(AuralithVertex v1,AuralithVertex v2,AuralithVertex v3)
    {
        this(v1,v2,v3,new Vector3f( new Vector3f(v2.position()).sub(v1.position())).cross(new Vector3f(v3.position()).sub(v1.position())).normalize());
    }
}