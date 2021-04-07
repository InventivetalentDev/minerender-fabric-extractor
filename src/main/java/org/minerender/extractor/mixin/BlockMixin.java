package org.minerender.extractor.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Accessor(value = "defaultState")
    protected abstract void reallySetDefaultState(BlockState blockState);

    @Redirect(
            method = "setDefaultState",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/block/Block;defaultState:Lnet/minecraft/block/BlockState;",
                     opcode = Opcodes.PUTFIELD))
    private void setDefaultState(Block block, BlockState blockState) {
//        System.out.println("setDefaultState");
//        System.out.println(block);
//        System.out.println(blockState);
        reallySetDefaultState(blockState);

//        JsonObject state = new JsonObject();
//        blockState.getEntries().forEach((property, comparable) -> state.addProperty(property.getName(), String.valueOf(comparable)));
//        Output.append("defaultBlockStates", Registry.BLOCK.getId(block).toString(), state);
    }

}
