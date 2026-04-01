package cute.ame.auralithpioneerinitiative.Registrie;

import cute.ame.auralithpioneerinitiative.Auralithpioneerinitiative;
import cute.ame.auralithpioneerinitiative.SpaceSuit.Data.SuitData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
  private ModAttachments() {}

  public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
      DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Auralithpioneerinitiative.MODID);

  public static final DeferredHolder<AttachmentType<?>, AttachmentType<SuitData>> SUIT_DATA =
      ATTACHMENT_TYPES.register("suit_data", () ->
          AttachmentType.builder(SuitData::new).serialize(SuitData.CODEC).build()
      );
}