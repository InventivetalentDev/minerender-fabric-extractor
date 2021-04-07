package org.minerender.extractor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.registry.Registry;

public class Extractor {

    public static void extractDefaultBlockStates() {
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
                        json.addProperty("default", ((Enum<?>) entry.getValue()).name());
                        json.addProperty("type", "enum");
                        json.addProperty("valueType", property.getType().getSimpleName());
                        property.getValues().forEach(v -> valuesArray.add(((Enum<?>) v).name()));
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

    //TODO: entity models?
    //TODO: tint colors

}
