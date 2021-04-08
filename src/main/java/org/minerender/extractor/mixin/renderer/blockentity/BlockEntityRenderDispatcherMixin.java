package org.minerender.extractor.mixin.renderer.blockentity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(BlockEntityRenderDispatcher.class)
public interface BlockEntityRenderDispatcherMixin {

    @Accessor("renderers")
    Map<BlockEntityType<?>, BlockEntityRenderer<?>> getRenderers();

}
