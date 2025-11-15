package cute.ame.auralithpioneerinitiative.Registries;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class ShaderRegistry
{
    public static ShaderInstance FRESNEL_ATMOSPHERE_SHADER;
    public static ShaderInstance PLANET_SURFACE_SHADER;
    public static ShaderInstance PLANET_CLOUDS_SHADER;

    public static void register()
    {
        Minecraft.getInstance().execute(() ->
            {
                try {
                    FRESNEL_ATMOSPHERE_SHADER = new ShaderInstance(
                            Minecraft.getInstance().getResourceManager(),
                            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "fresnel_atmosphere"),
                            DefaultVertexFormat.POSITION_COLOR_NORMAL);

                    PLANET_SURFACE_SHADER = new ShaderInstance(
                            Minecraft.getInstance().getResourceManager(),
                            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "planet_surface"),
                            DefaultVertexFormat.POSITION_COLOR_NORMAL);

                    PLANET_CLOUDS_SHADER = new ShaderInstance(
                            Minecraft.getInstance().getResourceManager(),
                            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "planet_clouds"),
                            DefaultVertexFormat.POSITION_COLOR_NORMAL);

                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        );
    }
}
