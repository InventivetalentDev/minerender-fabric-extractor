package org.minerender.extractor.extractors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;
import org.minerender.extractor.Output;

import java.awt.*;
import java.util.function.BiConsumer;

public class BlockExtractor {

    public static void extract() {
        System.out.println("[BlockExtractor] Extracting...");

        extractDefaultBlockStates();
        extractBlockColors();
    }

    static void extractDefaultBlockStates() {
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

    static void extractBlockColors() {
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
                    } else if (property instanceof EnumProperty) {
                        array.add(extractBlockColorForBlockState(state.with(property, ((Enum) value))));
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
        json.addProperty("raw", color);
        json.addProperty("r", clr.getRed());
        json.addProperty("g", clr.getGreen());
        json.addProperty("b", clr.getBlue());
        json.addProperty("a", clr.getAlpha());
        return json;
    }


}
