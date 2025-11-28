package cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry;

import org.joml.Vector2f;
import org.joml.Vector3f;

public record AuralithVertex(
        Vector3f position,
        Vector3f normal,
        Vector3f color,
        Vector2f uv,
        int faceIndex
)
{
    public AuralithVertex(Vector3f position, Vector3f normal, Vector3f color) {
        this(position, normal, color, new Vector2f(0, 0), 0);
    }
}