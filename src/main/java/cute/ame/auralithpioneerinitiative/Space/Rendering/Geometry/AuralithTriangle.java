package cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry;

import org.joml.Vector3f;

public class AuralithTriangle {
    public AuralithVertex v1, v2, v3;
    public Vector3f faceNormal;

    public AuralithTriangle(AuralithVertex v1, AuralithVertex v2, AuralithVertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        calculateFaceNormal();
    }

    private void calculateFaceNormal()
    {
        Vector3f edge1 = new Vector3f(v2.position()).sub(v1.position());
        Vector3f edge2 = new Vector3f(v3.position()).sub(v1.position());
        faceNormal = new Vector3f(edge1).cross(edge2).normalize();
    }
}