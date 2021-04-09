package org.minerender.extractor.mixin;

import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    int tick=0;

    @Inject(at = @At("HEAD"), method = "tickTime()V")
    private void tickTime(CallbackInfo info) {
//        if (tick++ == 40) {
//            Extractor.extractBlockColors();
//        }
    }

}
