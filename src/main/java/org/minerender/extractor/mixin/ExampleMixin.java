package org.minerender.extractor.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import org.minerender.extractor.extractors.BlockExtractor;
import org.minerender.extractor.extractors.Extractor;
import org.minerender.extractor.extractors.ModelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class ExampleMixin {

	@Inject(at = @At("HEAD"), method = "init()V")
	private void init(CallbackInfo info) {
		System.out.println("This line is printed by an example mod mixin!");


		BlockExtractor.extract();
		ModelExtractor.extract();

		Extractor.extractParticles();
	}



}
