package cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class GeometryGenerator
{
    public static AuralithMesh generateCubeWithUVs(float size, int subdivisions) 
    {
        AuralithMesh mesh = new AuralithMesh();

        Vector3f[][] faceVertices = {
                { new Vector3f(-1, -1, -1), new Vector3f( 1, -1, -1), new Vector3f( 1,  1, -1), new Vector3f(-1,  1, -1) },
                { new Vector3f( 1, -1,  1), new Vector3f(-1, -1,  1), new Vector3f(-1,  1,  1), new Vector3f( 1,  1,  1) },
                { new Vector3f(-1,  1, -1), new Vector3f( 1,  1, -1), new Vector3f( 1,  1,  1), new Vector3f(-1,  1,  1) },
                { new Vector3f(-1, -1,  1), new Vector3f( 1, -1,  1), new Vector3f( 1, -1, -1), new Vector3f(-1, -1, -1) },
                { new Vector3f( 1, -1, -1), new Vector3f( 1, -1,  1), new Vector3f( 1,  1,  1), new Vector3f( 1,  1, -1) },
                { new Vector3f(-1, -1,  1), new Vector3f(-1, -1, -1), new Vector3f(-1,  1, -1), new Vector3f(-1,  1,  1) }
        };

        Vector3f[] faceNormals = {
                new Vector3f( 0,  0, -1), // Front
                new Vector3f( 0,  0,  1), // Back
                new Vector3f( 0,  1,  0), // Top
                new Vector3f( 0, -1,  0), // Bottom
                new Vector3f( 1,  0,  0), // Right
                new Vector3f(-1,  0,  0)  // Left
        };

        for (int faceIdx = 0; faceIdx < 6; ++faceIdx)
            subdivideQuadWithUVs(mesh, faceVertices[faceIdx], faceNormals[faceIdx], size, subdivisions, faceIdx);

        return mesh;
    }

    private static void subdivideQuadWithUVs(AuralithMesh mesh, Vector3f[] corners, Vector3f normal, float size, int subdivisions, int faceIndex) 
    {
        float step = 1.0f / subdivisions;
        for (int i = 0; i < subdivisions; ++i) 
        {
            for (int j = 0; j < subdivisions; ++j) 
            {
                float u1 = i * step;
                float v1 = j * step;
                float u2 = (i + 1) * step;
                float v2 = (j + 1) * step;

                Vector3f p1 = biLerp(corners, u1, v1).mul(size);
                Vector3f p2 = biLerp(corners, u2, v1).mul(size);
                Vector3f p3 = biLerp(corners, u2, v2).mul(size);
                Vector3f p4 = biLerp(corners, u1, v2).mul(size);

                Vector2f uv1 = new Vector2f(u1, v1);
                Vector2f uv2 = new Vector2f(u2, v1);
                Vector2f uv3 = new Vector2f(u2, v2);
                Vector2f uv4 = new Vector2f(u1, v2);

                Vector3f white = new Vector3f(1, 1, 1);

                mesh.AddTriangle(new AuralithTriangle(
                        new AuralithVertex(p1, new Vector3f(normal), white, uv1, faceIndex),
                        new AuralithVertex(p2, new Vector3f(normal), white, uv2, faceIndex),
                        new AuralithVertex(p3, new Vector3f(normal), white, uv3, faceIndex)
                ));

                mesh.AddTriangle(new AuralithTriangle(
                        new AuralithVertex(p1, new Vector3f(normal), white, uv1, faceIndex),
                        new AuralithVertex(p3, new Vector3f(normal), white, uv3, faceIndex),
                        new AuralithVertex(p4, new Vector3f(normal), white, uv4, faceIndex)
                ));
            }
        }
    }

    public static AuralithMesh generateCube(float size, int subdivisions, boolean flatShading)
    {
        AuralithMesh mesh = new AuralithMesh();
        Vector3f[][] faceVertices = {
                {new Vector3f(-1, -1, -1), new Vector3f(1, -1, -1), new Vector3f(1, 1, -1), new Vector3f(-1, 1, -1)},
                {new Vector3f(1, -1, 1), new Vector3f(-1, -1, 1), new Vector3f(-1, 1, 1), new Vector3f(1, 1, 1)},
                {new Vector3f(-1, 1, -1), new Vector3f(1, 1, -1), new Vector3f(1, 1, 1), new Vector3f(-1, 1, 1)},
                {new Vector3f(-1, -1, -1), new Vector3f(-1, -1, 1), new Vector3f(1, -1, 1), new Vector3f(1, -1, -1)},
                {new Vector3f(1, -1, -1), new Vector3f(1, -1, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, -1)},
                {new Vector3f(-1, -1, 1), new Vector3f(-1, -1, -1), new Vector3f(-1, 1, -1), new Vector3f(-1, 1, 1)}
        };

        Vector3f[] faceNormals = {
                new Vector3f(0, 0, -1), // Front
                new Vector3f(0, 0, 1),  // Back
                new Vector3f(0, 1, 0),  // Top
                new Vector3f(0, -1, 0), // Bottom
                new Vector3f(1, 0, 0),  // Right
                new Vector3f(-1, 0, 0)  // Left
        };

        for (int faceIdx = 0; faceIdx < 6; ++faceIdx)
            subdivideQuad(mesh, faceVertices[faceIdx], faceNormals[faceIdx], size, subdivisions, flatShading);

        return mesh;
    }

    private static void subdivideQuad(AuralithMesh mesh, Vector3f[] corners, Vector3f normal, float size, int subdivisions, boolean flatShading)
    {
        float step = 1.0f / subdivisions;

        for (int i = 0; i < subdivisions; ++i)
        {
            for (int j = 0; j < subdivisions; ++j)
            {
                float u1 = i * step;
                float v1 = j * step;
                float u2 = (i + 1) * step;
                float v2 = (j + 1) * step;

                Vector3f p1 = biLerp(corners, u1, v1).mul(size);
                Vector3f p2 = biLerp(corners, u2, v1).mul(size);
                Vector3f p3 = biLerp(corners, u2, v2).mul(size);
                Vector3f p4 = biLerp(corners, u1, v2).mul(size);

                Vector3f white = new Vector3f(1, 1, 1);

                if (flatShading)
                {
                    mesh.AddTriangle(new AuralithTriangle(
                            new AuralithVertex(p1, new Vector3f(normal), white),
                            new AuralithVertex(p2, new Vector3f(normal), white),
                            new AuralithVertex(p3, new Vector3f(normal), white)
                    ));
                    mesh.AddTriangle(new AuralithTriangle(
                            new AuralithVertex(p1, new Vector3f(normal), white),
                            new AuralithVertex(p3, new Vector3f(normal), white),
                            new AuralithVertex(p4, new Vector3f(normal), white)
                    ));
                } else
                {
                    mesh.AddTriangle(new AuralithTriangle(
                            new AuralithVertex(p1, new Vector3f(p1).normalize(), white),
                            new AuralithVertex(p2, new Vector3f(p2).normalize(), white),
                            new AuralithVertex(p3, new Vector3f(p3).normalize(), white)
                    ));
                    mesh.AddTriangle(new AuralithTriangle(
                            new AuralithVertex(p1, new Vector3f(p1).normalize(), white),
                            new AuralithVertex(p3, new Vector3f(p3).normalize(), white),
                            new AuralithVertex(p4, new Vector3f(p4).normalize(), white)
                    ));
                }
            }
        }
    }

    public static AuralithMesh generateSphere(float radius, int subdivisions)
    {
        AuralithMesh mesh = new AuralithMesh();
        float t = (1.0f + (float)Math.sqrt(5.0f)) / 2.0f;
        Vector3f[] vertices = {
                new Vector3f(-1, t, 0).normalize(), new Vector3f(1, t, 0).normalize(),
                new Vector3f(-1, -t, 0).normalize(), new Vector3f(1, -t, 0).normalize(),
                new Vector3f(0, -1, t).normalize(), new Vector3f(0, 1, t).normalize(),
                new Vector3f(0, -1, -t).normalize(), new Vector3f(0, 1, -t).normalize(),
                new Vector3f(t, 0, -1).normalize(), new Vector3f(t, 0, 1).normalize(),
                new Vector3f(-t, 0, -1).normalize(), new Vector3f(-t, 0, 1).normalize()
        };
        int[][] faces = {
                {0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11},
                {1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6}, {7, 1, 8},
                {3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9},
                {4, 9, 5}, {2, 4, 11}, {6, 2, 10}, {8, 6, 7}, {9, 8, 1}
        };
        for (int[] face : faces)
            subdivideTriangle(mesh, new Vector3f(vertices[face[0]]).mul(radius), new Vector3f(vertices[face[1]]).mul(radius), new Vector3f(vertices[face[2]]).mul(radius),radius, subdivisions);

        return mesh;
    }

    private static void subdivideTriangle(AuralithMesh mesh, Vector3f v1, Vector3f v2, Vector3f v3, float radius, int depth)
    {
        if (depth == 0)
        {
            Vector3f white = new Vector3f(1, 1, 1);
            mesh.AddTriangle(new AuralithTriangle(
                    new AuralithVertex(new Vector3f(v1), new Vector3f(v1).normalize(), white),
                    new AuralithVertex(new Vector3f(v2), new Vector3f(v2).normalize(), white),
                    new AuralithVertex(new Vector3f(v3), new Vector3f(v3).normalize(), white)
            ));
            return;
        }

        Vector3f v12 = new Vector3f(v1).add(v2).mul(0.5f).normalize().mul(radius);
        Vector3f v23 = new Vector3f(v2).add(v3).mul(0.5f).normalize().mul(radius);
        Vector3f v31 = new Vector3f(v3).add(v1).mul(0.5f).normalize().mul(radius);
        subdivideTriangle(mesh, v1, v12, v31, radius, depth - 1);
        subdivideTriangle(mesh, v2, v23, v12, radius, depth - 1);
        subdivideTriangle(mesh, v3, v31, v23, radius, depth - 1);
        subdivideTriangle(mesh, v12, v23, v31, radius, depth - 1);
    }

    private static Vector3f biLerp(Vector3f[] corners, float u, float v)
    {
        Vector3f bottom = new Vector3f(corners[0]).lerp(corners[1], u);
        Vector3f top    = new Vector3f(corners[3]).lerp(corners[2], u);
        return bottom.lerp(top, v);
    }
}