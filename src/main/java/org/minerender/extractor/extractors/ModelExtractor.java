package org.minerender.extractor.extractors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;
import org.minerender.extractor.Output;
import org.minerender.extractor.mixin.model.geom.ModelPartMixin;
import org.minerender.extractor.mixin.renderer.blockentity.BlockEntityRenderDispatcherMixin;
import org.minerender.extractor.mixin.renderer.entity.EntityRenderDispatcherMixin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.minerender.extractor.extractors.Extractor.collectAllInheritedFields;

public class ModelExtractor {

    public static void extract() {
        System.out.println("[ModelExtractor] Extracting...");

        extractBlockEntityModels();
        extractEntityModels();
        extractPlayerModels();
    }

    static void extractBlockEntityModels() {
        System.out.println("Extracting block entity models...");

        Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ((BlockEntityRenderDispatcherMixin) BlockEntityRenderDispatcher.INSTANCE).getRenderers();
        System.out.println(renderers);
        renderers.forEach((type, renderer) -> {
            JsonObject parts = new JsonObject();

            extractAllModelsFromClass(renderer, parts);

            Output.append("blockEntityModels", Objects.requireNonNull(Registry.BLOCK_ENTITY_TYPE.getId(type)).toString(), parts);
        });

        Output.write("blockEntityModels");
    }


    static void extractEntityModels() {
        System.out.println("Extracting entity models...");

        Map<EntityType<?>, EntityRenderer<?>> renderers = ((EntityRenderDispatcherMixin) MinecraftClient.getInstance().getEntityRenderDispatcher()).getRenderers();
        System.out.println(renderers);
        renderers.forEach((type, renderer) -> {
            JsonObject parts = new JsonObject();

            extractAllModelsFromClass(renderer, parts);

            Output.append("entityModels", Objects.requireNonNull(Registry.ENTITY_TYPE.getId(type)).toString(), parts);
        });

        Output.write("entityModels");
    }

    static void extractPlayerModels() {
        System.out.println("Extracting models...");

        Map<String, PlayerEntityRenderer> renderers = ((EntityRenderDispatcherMixin) MinecraftClient.getInstance().getEntityRenderDispatcher()).getModelRenderers();
        System.out.println(renderers);
        renderers.forEach((type, renderer) -> {
            JsonObject parts = new JsonObject();

            extractAllModelsFromClass(renderer, parts);

            Output.append("playerModels", type, parts);
        });

        // This is the same as the "default" entry in the above map
//        //TODO: doesn't exist in 1.18+
//        PlayerEntityRenderer renderer = ((EntityRenderDispatcherMixin) MinecraftClient.getInstance().getEntityRenderDispatcher()).getPlayerRenderer();
//        JsonObject parts = new JsonObject();
//        extractAllModelsFromClass(renderer, parts);
//        Output.append("playerModels", "player", parts);

        Output.write("playerModels");
    }

    protected static JsonObject extractModelPart(ModelPart modelPart) {
        ModelPartMixin part = (ModelPartMixin) modelPart;

        JsonObject root = new JsonObject();

        root.addProperty("textureWidth", part.textureWidth());
        root.addProperty("textureHeight", part.textureHeight());
        root.addProperty("textureOffsetU", part.textureOffsetU());
        root.addProperty("textureOffsetV", part.textureOffsetV());
        root.addProperty("pivotX", part.pivotX());
        root.addProperty("pivotY", part.pivotY());
        root.addProperty("pivotZ", part.pivotZ());
        root.addProperty("pitch", part.pitch());
        root.addProperty("yaw", part.yaw());
        root.addProperty("roll", part.roll());
        root.addProperty("mirror", part.mirror());

        JsonArray cubeArray = new JsonArray();
        for (ModelPart.Cuboid cube : part.cuboids()) {
            JsonObject json = new JsonObject();
            json.addProperty("minX", cube.minX);
            json.addProperty("minY", cube.minY);
            json.addProperty("minZ", cube.minZ);
            json.addProperty("maxX", cube.maxX);
            json.addProperty("maxY", cube.maxY);
            json.addProperty("maxZ", cube.maxZ);
            cubeArray.add(json);
        }
        root.add("cubes", cubeArray);

        JsonArray childArray = new JsonArray();
        for (ModelPart child : part.children()) {
            childArray.add(extractModelPart(child));
        }
        root.add("children", childArray);

        return root;
    }

    protected static void extractAllModelsFromClass(Object container, JsonObject parts) {
        List<Field> fields = collectAllInheritedFields(container.getClass());
        fields.forEach(field -> {
            if (ModelPart.class.equals(field.getType())) {
                try {
                    field.setAccessible(true);
                    parts.add(field.getName(), extractModelPart((ModelPart) field.get(container)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (Model.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Model model = (Model) field.get(container);
                    extractAllModelsFromClass(model, parts);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
