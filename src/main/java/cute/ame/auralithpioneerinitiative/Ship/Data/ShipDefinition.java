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
    long maxEu,
    int hardpoints,
    Map<Character, BlockDefinition> palette,
    List<List<String>> layers,
    BlockOffset cockpitOffset,
    char seatChar,
    String pilotDirection,
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
                    LinkedHashMap::new)),
            charMap -> charMap.entrySet().stream()
                .collect(Collectors.toMap(
                    e -> String.valueOf(e.getKey()),
                    Map.Entry::getValue,
                    (a, b) -> a,
                    LinkedHashMap::new))
            );

    private static final Codec<List<List<String>>> LAYERS_CODEC =
        Codec.STRING.listOf().listOf();

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
            Codec.STRING.optionalFieldOf("seat_char", "@").xmap(s -> s.isEmpty() ? '@' : s.charAt(0), String::valueOf).forGetter(ShipDefinition::seatChar),
            Codec.STRING.optionalFieldOf("pilot_direction", "north").forGetter(ShipDefinition::pilotDirection),
            PanelConfig.CODEC.listOf().optionalFieldOf("panels", List.of()).forGetter(ShipDefinition::panels),
            ShipAnimationConfig.CODEC.optionalFieldOf("animation_config").forGetter(ShipDefinition::animationConfig)
        ).apply(instance, ShipDefinition::new)
    );

    public Optional<ResourceLocation> findControllerBlock()
    {
        return palette.values().stream().filter(BlockDefinition::isController).map(BlockDefinition::blockId).findFirst();
    }

    public int[] computeSize()
    {
        int y = layers.size();
        int z = layers.stream().mapToInt(List::size).max().orElse(0);
        int x = layers.stream().flatMap(List::stream).mapToInt(String::length).max().orElse(0);
        return new int[]{x, y, z};
    }

    public BlockOffset findCockpitOffset()
    {
        if (cockpitOffset.x() != 0 || cockpitOffset.y() != 0 || cockpitOffset.z() != 0) return cockpitOffset;

        int ctrlX = 0, ctrlY = 0, ctrlZ = 0;
        outer:
        for (int ly = 0; ly < layers.size(); ly++)
        {
            List<String> rows = layers.get(ly);
            for (int lz = 0; lz < rows.size(); lz++)
            {
                String row = rows.get(lz);
                for (int lx = 0; lx < row.length(); lx++)
                {
                    BlockDefinition bd = palette.get(row.charAt(lx));
                    if (bd != null && bd.isController())
                    {
                        ctrlX = lx; ctrlY = ly; ctrlZ = lz;
                        break outer;
                    }
                }
            }
        }

        for (int ly = 0; ly < layers.size(); ly++)
        {
            List<String> rows = layers.get(ly);
            for (int lz = 0; lz < rows.size(); lz++)
            {
                String row = rows.get(lz);
                for (int lx = 0; lx < row.length(); lx++)
                {
                    if (row.charAt(lx) == seatChar) return new BlockOffset(lx - ctrlX, ly - ctrlY, lz - ctrlZ);
                }
            }
        }

        return BlockOffset.ZERO;
    }

    public org.joml.Quaternionf computeInitialRotation()
    {
        org.joml.Quaternionf q = new org.joml.Quaternionf();
        return switch (pilotDirection.toLowerCase(java.util.Locale.ROOT))
        {
            case "south" -> q.rotationY((float) Math.PI);
            case "west" -> q.rotationY((float) (Math.PI / 2.0));
            case "east" -> q.rotationY((float) (-Math.PI / 2.0));
            default -> q;
        };
    }
}