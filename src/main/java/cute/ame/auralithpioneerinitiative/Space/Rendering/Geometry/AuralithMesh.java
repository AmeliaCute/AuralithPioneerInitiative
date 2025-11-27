package cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry;

import java.util.ArrayList;
import java.util.List;

public class AuralithMesh
{
    public List<AuralithTriangle> triangles = new ArrayList<>();

    public void AddTriangle(AuralithTriangle tri)
    {
        triangles.add(tri);
    }
}
