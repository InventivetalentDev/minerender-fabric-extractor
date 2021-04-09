package org.minerender.extractor.mixin.particle;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(ParticleManager.class)
public interface ParticleManagerMixin {

    @Accessor("spriteAwareFactories")
    Map<Identifier, SpriteProvider> spriteAwareFactories();

    @Mixin( targets = "net.minecraft.client.particle.ParticleManager$SimpleSpriteProvider")
    public interface SimpleSpriteProviderMixin {

        @Accessor("sprites")
        List<Sprite> sprites();

    }


}
