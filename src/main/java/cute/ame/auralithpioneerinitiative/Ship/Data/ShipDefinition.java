package cute.ame.auralithpioneerinitiative.Ship.Data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gg.amecute.auralithutilities.Multiblock.Data.BlockDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public record ShipDefinition(
    ResourceLocation id,
    String displayName,
    String shipClass,
    float mass,
    float maxHull,
    float maxShield,
    float maxFuel,
    long  maxEu,
    int   hardpoints,
    Map<Character, BlockDefinition> palette,
    List<List<String>> layers,
    BlockOffset cockpitOffset,
    List<PanelConfig> panels,
    Optional<ShipAnimationConfig> animationConfig
)
{
    private static final Codec<Map<Character, BlockDefinition>> PALETTE_CODEC =
    Codec.unboundedMap(Codec.STRING, BlockDefinition.CODEC)
    .xmap(
    stringMap -> stringMap.entrySet().stream()
        .filter(e -> !e.getKey().isEmpty())
        .collect(Collectors.toMap(
            e -> e.getKey().charAt(0),
            Map.Entry::getValue,
            (a, b) -> a,
            LinkedHashMap::new
        )),
    charMap -> charMap.entrySet().stream()
        .collect(Collectors.toMap(
            e -> String.valueOf(e.getKey()),
            Map.Entry::getValue,
            (a, b) -> a,
            LinkedHashMap::new
        ))
    );

    private static final Codec<List<List<String>>> LAYERS_CODEC = Codec.STRING.listOf().listOf();

    public static final Codec<ShipDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(ShipDefinition::id),
            Codec.STRING.optionalFieldOf("display_name", "Unknown Ship").forGetter(ShipDefinition::displayName),
            Codec.STRING.optionalFieldOf("class", "unknown").forGetter(ShipDefinition::shipClass),
            Codec.FLOAT.optionalFieldOf("mass", 10_000f).forGetter(ShipDefinition::mass),
            Codec.FLOAT.optionalFieldOf("max_hull", 200f).forGetter(ShipDefinition::maxHull),
            Codec.FLOAT.optionalFieldOf("max_shield", 50f).forGetter(ShipDefinition::maxShield),
            Codec.FLOAT.optionalFieldOf("max_fuel", 100f).forGetter(ShipDefinition::maxFuel),
            Codec.LONG.optionalFieldOf("max_eu", 10_000L).forGetter(ShipDefinition::maxEu),
            Codec.INT.optionalFieldOf("hardpoints", 0).forGetter(ShipDefinition::hardpoints),
            PALETTE_CODEC.fieldOf("palette").forGetter(ShipDefinition::palette),
            LAYERS_CODEC.fieldOf("layers").forGetter(ShipDefinition::layers),
            BlockOffset.CODEC.optionalFieldOf("cockpit_offset", BlockOffset.ZERO).forGetter(ShipDefinition::cockpitOffset),
            PanelConfig.CODEC.listOf().optionalFieldOf("panels", List.of()).forGetter(ShipDefinition::panels),
            ShipAnimationConfig.CODEC.optionalFieldOf("animation_config").forGetter(ShipDefinition::animationConfig)
        ).apply(instance, ShipDefinition::new)
    );

    public Optional<ResourceLocation> findControllerBlock()
    {
        return palette.values().stream()
            .filter(BlockDefinition::isController)
            .map(BlockDefinition::blockId)
            .findFirst();
    }

    public int[] computeSize()
    {
        int y = layers.size();
        int z = layers.stream().mapToInt(List::size).max().orElse(0);
        int x = layers.stream().flatMap(List::stream).mapToInt(String::length).max().orElse(0);
        return new int[]{x, y, z};
    }
}
