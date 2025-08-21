package io.github.itskillerluc.duclib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.itskillerluc.duclib.client.model.definitions.*;
import io.github.itskillerluc.duclib.data.model.DucLibModelLoader;
import io.github.itskillerluc.duclib.data.model.serializers.Bone;
import io.github.itskillerluc.duclib.data.model.serializers.Cube;
import io.github.itskillerluc.duclib.data.model.serializers.Geometry;
import io.github.itskillerluc.duclib.data.model.serializers.GeometryHolder;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mixin for handling the replacing of models.
 */
@Mixin(LayerDefinitions.class)
public class LayerDefinitionsMixin {
    @ModifyReturnValue(method = "createRoots()Ljava/util/Map;",at = @At("TAIL"))
    private static Map<ModelLayerLocation, LayerDefinition> injected(Map<ModelLayerLocation, LayerDefinition> original){
        Map<ModelLayerLocation, LayerDefinition> map = new HashMap<>(original);
        for (Map.Entry<ResourceLocation, GeometryHolder> resourceLocationGeometryHolderEntry : DucLibModelLoader.getOverrides().entrySet()) {
            var key = original.keySet().stream().filter(i -> i.getModel().equals(resourceLocationGeometryHolderEntry.getKey())).findFirst();
            key.ifPresent(modelLayerLocation -> map.replace(modelLayerLocation, generateLakeDefinition(resourceLocationGeometryHolderEntry.getKey())));
        }
        return map;
    }

    private static LakeDefinition generateLakeDefinition(ResourceLocation entity){
        GeometryHolder holder = DucLibModelLoader.getModel(entity);
        Geometry geometry = holder.geometry()[0];
        List<Bone> bones = Arrays.stream(geometry.bones()).collect(Collectors.toCollection(ArrayList::new));
        PondDefinition pondDefinition = new PondDefinition();
        DuclingDefinition topRoot = pondDefinition.getRoot();

        for (int i = 0; i < bones.size(); i++) {
            Bone bone = bones.get(i);
            if (bone.parent() == null) {
                bones.set(i, new Bone(bone.name(), "dl_top_root", new float[]{bone.pivot()[0], bone.pivot()[1], bone.pivot()[2]}, bone.rotation(), bone.cubes()));
            }
        }
        int count = 0;

        generateLakeDefinitionRecursively("dl_top_root", count, bones, topRoot, new float[]{0, 24, 0}, bones.stream().noneMatch(bone -> bone.cubes() != null && bone.cubes()[0].uv().left().isPresent()));

        return LakeDefinition.create(pondDefinition, geometry.description().textureWidth(), geometry.description().textureHeight());
    }

    private static DuclingDefinition generateLakeDefinitionRecursively(String parent, int count, List<Bone> bones, DuclingDefinition parentDucling, float[] offset, boolean boxUV){
        for (Bone bone : bones) {
            if (bone.parent().equals(parent)){
                DuclingDefinition parentDef = parentDucling.addOrReplaceChild(bone.name(), createWings(bone, new float[]{bone.pivot()[0], bone.pivot()[1], bone.pivot()[2]}, boxUV), PartPose.offsetAndRotation(bone.pivot()[0] - offset[0], -(bone.pivot()[1] - offset[1]), bone.pivot()[2] - offset[2], Mth.DEG_TO_RAD * bone.rotation()[0], Mth.DEG_TO_RAD * bone.rotation()[1], Mth.DEG_TO_RAD * bone.rotation()[2]));
                if (bone.cubes() != null) {
                    for (Cube cube : bone.cubes()) {
                        if (cube.rotation() != null) {
                            parentDef.addOrReplaceChild("cube_" + count, createWings(new Bone("cube_" + count, bone.name(), new float[]{}, null, new Cube[]{new Cube(cube.origin(), cube.size(), null, null, cube.inflate(), cube.uv(), cube.mirror())}), new float[]{cube.pivot()[0], cube.pivot()[1], cube.pivot()[2]}, boxUV), PartPose.offsetAndRotation(cube.pivot()[0] - bone.pivot()[0], bone.pivot()[1] - cube.pivot()[1], cube.pivot()[2] - bone.pivot()[2], Mth.DEG_TO_RAD * cube.rotation()[0], Mth.DEG_TO_RAD * cube.rotation()[1], Mth.DEG_TO_RAD * cube.rotation()[2]));
                        }
                    }
                }
                generateLakeDefinitionRecursively(bone.name(), count, bones, parentDef, new float[]{bone.pivot()[0], bone.pivot()[1], bone.pivot()[2]}, boxUV);
            }
        }
        return parentDucling;
    }

    private static WingListBuilder createWings(Bone bone, float[] offset, boolean boxUV){
        WingListBuilder builder = WingListBuilder.create();
        if (bone.cubes() != null) {
            for (Cube cube : bone.cubes()) {
                if (cube.rotation() == null) {
                    if (!boxUV) {
                        builder.mirror(cube.mirror()).addBox(null, cube.origin()[0] - offset[0], -(cube.origin()[1] - offset[1] + cube.size()[1]), cube.origin()[2] - offset[2],
                                cube.size()[0], cube.size()[1], cube.size()[2],
                                new CubeDeformation(cube.inflate()), new AdvancedUV[]{
                                        new AdvancedUV(Direction.NORTH, new UVPair(cube.uv().left().get().north().uv()[0], cube.uv().left().get().north().uv()[1]), new UVPair(cube.uv().left().get().north().uvSize()[0], cube.uv().left().get().north().uvSize()[1])),
                                        new AdvancedUV(Direction.EAST, new UVPair(cube.uv().left().get().east().uv()[0], cube.uv().left().get().east().uv()[1]), new UVPair(cube.uv().left().get().east().uvSize()[0], cube.uv().left().get().east().uvSize()[1])),
                                        new AdvancedUV(Direction.SOUTH, new UVPair(cube.uv().left().get().south().uv()[0], cube.uv().left().get().south().uv()[1]), new UVPair(cube.uv().left().get().south().uvSize()[0], cube.uv().left().get().south().uvSize()[1])),
                                        new AdvancedUV(Direction.WEST, new UVPair(cube.uv().left().get().west().uv()[0], cube.uv().left().get().west().uv()[1]), new UVPair(cube.uv().left().get().west().uvSize()[0], cube.uv().left().get().west().uvSize()[1])),
                                        new AdvancedUV(Direction.UP, new UVPair(cube.uv().left().get().up().uv()[0], cube.uv().left().get().up().uv()[1]), new UVPair(cube.uv().left().get().up().uvSize()[0], cube.uv().left().get().up().uvSize()[1])),
                                        new AdvancedUV(Direction.DOWN, new UVPair(cube.uv().left().get().down().uv()[0], cube.uv().left().get().down().uv()[1]), new UVPair(cube.uv().left().get().down().uvSize()[0], cube.uv().left().get().down().uvSize()[1]))
                                }, cube.mirror());
                    } else {
                        builder.mirror(cube.mirror()).addBox(null, cube.origin()[0] - offset[0], -(cube.origin()[1] - offset[1] + cube.size()[1]), cube.origin()[2] - offset[2],
                                cube.size()[0], cube.size()[1], cube.size()[2],
                                new CubeDeformation(cube.inflate()), cube.uv().right().get()[0], cube.uv().right().get()[1], cube.mirror());
                    }
                }
            }
        }
        return builder;
    }
}
