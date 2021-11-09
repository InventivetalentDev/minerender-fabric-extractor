package org.minerender.extractor.extractors;

import com.google.gson.JsonArray;
import net.minecraft.client.MinecraftClient;
import org.minerender.extractor.Output;
import org.minerender.extractor.mixin.particle.ParticleManagerMixin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Extractor {

    protected static List<Field> collectAllInheritedFields(Class clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        while ((clazz = clazz.getSuperclass()) != null) {
            if (Object.class.equals(clazz)) { break; }
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        return fields;
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



    //TODO: entity models?
    //TODO: tint colors
    //TODO: particles

}
