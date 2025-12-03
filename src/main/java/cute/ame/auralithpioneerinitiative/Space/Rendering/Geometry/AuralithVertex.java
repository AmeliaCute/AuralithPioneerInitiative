package cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry;

import org.joml.Vector2f;
import org.joml.Vector3f;

public record AuralithVertex(
        Vector3f position,
        Vector3f normal,
        Vector3f color,
        Vector2f uv,
        int faceIndex,
        Vector3f localPos
)
{
    public AuralithVertex(Vector3f position, Vector3f normal, Vector3f color) {
        this(position, normal, color, new Vector2f(0, 0), 0, new Vector3f(position));
    }

    public AuralithVertex(Vector3f position, Vector3f normal, Vector3f color, Vector2f uv, int faceIndex) {
        this(position, normal, color, uv, faceIndex, new Vector3f(position));
    }
}