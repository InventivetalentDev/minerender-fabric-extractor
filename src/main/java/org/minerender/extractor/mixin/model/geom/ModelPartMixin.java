package org.minerender.extractor.mixin.model.geom;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelPart.class)
public interface ModelPartMixin {

    @Accessor("textureWidth")
    float textureWidth();

    @Accessor("textureHeight")
    float textureHeight();

    @Accessor("textureOffsetU")
    int textureOffsetU();

    @Accessor("textureOffsetV")
    int textureOffsetV();

    @Accessor("pivotX")
    float pivotX();

    @Accessor("pivotY")
    float pivotY();

    @Accessor("pivotZ")
    float pivotZ();

    @Accessor("pitch")
    float pitch();

    @Accessor("yaw")
    float yaw();

    @Accessor("roll")
    float roll();

    @Accessor("mirror")
    boolean mirror();

    @Accessor("cuboids")
    ObjectList<ModelPart.Cuboid> cuboids();

    @Accessor("children")
    ObjectList<ModelPart> children();


}
