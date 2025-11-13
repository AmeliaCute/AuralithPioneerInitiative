package cute.ame.auralithpioneerinitiative.Registries;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class ShaderRegistries
{
    public static ShaderInstance FRESNEL_ATMOSPHERE_SHADER;

    public static void register()
    {
        Minecraft.getInstance().execute(() ->
            {
                try {
                    FRESNEL_ATMOSPHERE_SHADER = new ShaderInstance(
                            Minecraft.getInstance().getResourceManager(),
                            ResourceLocation.fromNamespaceAndPath(Auralithpioneerinitiative.MODID, "fresnel_atmosphere"),
                            DefaultVertexFormat.POSITION_COLOR_NORMAL);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        );
    }
}
