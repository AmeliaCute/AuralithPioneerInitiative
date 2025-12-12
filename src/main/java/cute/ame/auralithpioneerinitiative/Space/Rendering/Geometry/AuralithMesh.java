package cute.ame.auralithpioneerinitiative.Space.Rendering.Geometry;

import org.lwjgl.opengl.GL33;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class AuralithMesh implements AutoCloseable
{

    public final List<AuralithTriangle> triangles = new ArrayList<>();
    private int vao = 0;
    private int vbo = 0;
    private boolean gpuResourcesInitialized = false;
    private int vertexCount = 0;

    public void addTriangle(AuralithTriangle tri)
    {
        triangles.add(tri);
        invalidateGPUResources();
    }

    public void prepareGPUResources()
    {
        if (gpuResourcesInitialized)
            return;

        if (triangles.isEmpty())
            return;

        List<Float> vertices = new ArrayList<>(triangles.size() * 3 * 6);
        for (AuralithTriangle tri : triangles)
        {
            addVertexData(vertices, tri.v1());
            addVertexData(vertices, tri.v2());
            addVertexData(vertices, tri.v3());
        }

        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++)
            vertexArray[i] = vertices.get(i);

        FloatBuffer vertexBuffer = org.lwjgl.BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(vao);
        vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertexBuffer, GL33.GL_STATIC_DRAW);

        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 6 * Float.BYTES, 0);
        GL33.glEnableVertexAttribArray(0);

        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        GL33.glEnableVertexAttribArray(1);

        GL33.glBindVertexArray(0);

        vertexCount = triangles.size() * 3;
        gpuResourcesInitialized = true;
    }

    private void addVertexData(List<Float> vertices, AuralithVertex vertex)
    {
        vertices.add(vertex.position().x);
        vertices.add(vertex.position().y);
        vertices.add(vertex.position().z);
        vertices.add(vertex.normal().x);
        vertices.add(vertex.normal().y);
        vertices.add(vertex.normal().z);
    }

    public void render()
    {
        if (!gpuResourcesInitialized)
            throw new IllegalStateException("GPU resources not initialized. Call prepareGPUResources() first.");


        GL33.glBindVertexArray(vao);
        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, vertexCount);
        GL33.glBindVertexArray(0);
    }

    public boolean isReadyToRender()
    {
        return gpuResourcesInitialized;
    }

    public void invalidateGPUResources()
    {
        if (gpuResourcesInitialized)
            cleanup();
    }

    public void cleanup()
    {
        if (vbo != 0)
        {
            GL33.glDeleteBuffers(vbo);
            vbo = 0;
        }
        if (vao != 0)
        {
            GL33.glDeleteVertexArrays(vao);
            vao = 0;
        }
        gpuResourcesInitialized = false;
    }

    @Override
    public void close()
    {
        cleanup();
    }

    public int getTriangleCount()
    {
        return triangles.size();
    }

    public int getVertexCount()
    {
        return vertexCount;
    }
}