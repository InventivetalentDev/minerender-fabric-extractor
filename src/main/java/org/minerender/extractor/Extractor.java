package org.minerender.extractor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;
import org.minerender.extractor.mixin.model.geom.ModelPartMixin;
import org.minerender.extractor.mixin.particle.ParticleManagerMixin;
import org.minerender.extractor.mixin.renderer.blockentity.BlockEntityRenderDispatcherMixin;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;

public class Extractor {

    protected static List<Field> collectAllInheritedFields(Class clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        while ((clazz = clazz.getSuperclass()) != null) {
            if (Object.class.equals(clazz)) { break; }
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        return fields;
    }

    protected static JsonElement blockStateValueToJson(Comparable<?> comparable) {
        if (comparable instanceof Boolean) {
            return new JsonPrimitive((Boolean) comparable);
        }
        if (comparable instanceof Number) {
            return new JsonPrimitive((Number) comparable);
        }
        return new JsonPrimitive(comparable.toString());
    }

    protected static JsonObject minecraftColorToJson(int color) {
        JsonObject json = new JsonObject();
        Color clr = new Color(color, true);
        json.addProperty("r", clr.getRed());
        json.addProperty("g", clr.getGreen());
        json.addProperty("b", clr.getBlue());
        json.addProperty("a", clr.getAlpha());
        return json;
    }

    public static void extractDefaultBlockStates() {
        System.out.println("Extracting default block states...");

        for (Block block : Registry.BLOCK) {
            JsonObject state = new JsonObject();
            BlockState blockState = block.getDefaultState();
            blockState.getEntries().entrySet().forEach(entry -> {
                if (entry != null) {
                    JsonObject json = new JsonObject();
                    Property<?> property = entry.getKey();

                    JsonArray valuesArray = new JsonArray();
                    if (property instanceof BooleanProperty) {
                        json.addProperty("default", (Boolean) entry.getValue());
                        json.addProperty("type", "boolean");
                        json.addProperty("valueType", "boolean");
                        property.getValues().forEach(v -> valuesArray.add((Boolean) v));
                    } else if (property instanceof IntProperty) {
                        json.addProperty("default", (Integer) entry.getValue());
                        json.addProperty("type", "int");
                        json.addProperty("valueType", "int");
                        property.getValues().forEach(v -> valuesArray.add((Integer) v));
                    } else if (property instanceof EnumProperty) {
                        json.addProperty("default", ((Enum<?>) entry.getValue()).toString());
                        json.addProperty("type", "enum");
                        json.addProperty("valueType", property.getType().getSimpleName());
                        property.getValues().forEach(v -> valuesArray.add(((Enum<?>) v).toString()));
                    }
                    json.add("values", valuesArray);

                    state.add(property.getName(), json);
                }
            });
            if (state.size() > 0) {
                Output.append("defaultBlockStates", Registry.BLOCK.getId(block).toString(), state);
            }
        }
        Output.write("defaultBlockStates");
    }

    public static void extractBlockEntityModels() {
        System.out.println("Extracting block entity models...");

        Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ((BlockEntityRenderDispatcherMixin) BlockEntityRenderDispatcher.INSTANCE).getRenderers();
        System.out.println(renderers);
        renderers.forEach((type, renderer) -> {
            JsonObject parts = new JsonObject();

            extractAllModelsFromClass(renderer, parts);

            Output.append("blockModels", Objects.requireNonNull(Registry.BLOCK_ENTITY_TYPE.getId(type)).toString(), parts);
        });

        Output.write("blockModels");
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

    public static void extractEntityModels() {
        System.out.println("Extracting entity models...");

        //TODO
        Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ((BlockEntityRenderDispatcherMixin) BlockEntityRenderDispatcher.INSTANCE).getRenderers();
        System.out.println(renderers);
        renderers.forEach((type, renderer) -> {
            JsonObject parts = new JsonObject();

            Field[] fields = renderer.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (ModelPart.class.equals(field.getType())) {
                    try {
                        field.setAccessible(true);
                        parts.add(field.getName(), extractModelPart((ModelPart) field.get(renderer)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            Output.append("blockModels", type.toString(), parts);
        });

        Output.write("blockModels");
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

    public static void extractParticles() {
        System.out.println("Extracting particles...");

        JsonArray particles = new JsonArray();

        ParticleManagerMixin particleEngine = ((ParticleManagerMixin) MinecraftClient.getInstance().particleManager);
        particleEngine.spriteAwareFactories().forEach((identifier, spriteProvider) -> {
            try {
                particles.add(identifier.toString());
                //                    ((ParticleManagerMixin.SimpleSpriteProviderMixin) spriteProvider).sprites().forEach(sprite -> {
                //                        System.out.println(sprite);
                //                    });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Output.write("particles", particles);
    }

    public static void extractBlockColors() {
        System.out.println("Extracting block colors...");

        JsonObject json = new JsonObject();
        for (Block block : Registry.BLOCK) {
            try {
                extractBlockColorForBlock(block, json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Output.write("blockColors", json);
    }

    protected static void extractBlockColorForBlock(Block block, JsonObject target) {
        JsonArray array = new JsonArray();

        //TODO: other states

        BlockState state = block.getDefaultState();
//        array.add(extractBlockColorForBlockState(state));

        for (Property property : state.getProperties()) {
            for (Object value : property.getValues()) {
                try {
                    if (property instanceof IntProperty) {
                        array.add(extractBlockColorForBlockState(state.with(property, (Integer) value)));
                    } else if (property instanceof BooleanProperty) {
                        array.add(extractBlockColorForBlockState(state.with(property, (Boolean) value)));
                    } else if(property instanceof EnumProperty) {
                        array.add(extractBlockColorForBlockState(state.with( property, ((Enum) value))));
                    }
//                    else if(value instanceof Enum) {
//                        array.add(extractBlockColorForBlockState(state.with((Property<Enum>) property, (Enum) value)));
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        target.add(Registry.BLOCK.getId(block).toString(), array);
    }

    protected static JsonObject extractBlockColorForBlockState(BlockState state) {
        JsonObject obj = new JsonObject();

        int color = MinecraftClient.getInstance().getBlockColors().getColor(state, null, null);

        JsonObject stateJson = new JsonObject();
        state.getEntries().forEach(new BiConsumer<Property<?>, Comparable<?>>() {
            @Override
            public void accept(Property<?> property, Comparable<?> comparable) {
                stateJson.add(property.getName(), blockStateValueToJson(comparable));
            }
        });
        obj.add("state", stateJson);
        //        obj.addProperty("color", color);
        obj.add("color", minecraftColorToJson(color));

        return obj;
    }

    //TODO: entity models?
    //TODO: tint colors
    //TODO: particles

}
