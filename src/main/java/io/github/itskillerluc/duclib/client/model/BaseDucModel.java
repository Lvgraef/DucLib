package io.github.itskillerluc.duclib.client.model;

import io.github.itskillerluc.duclib.client.model.definitions.*;
import io.github.itskillerluc.duclib.data.model.DucLibModelLoader;
import io.github.itskillerluc.duclib.data.model.serializers.Bone;
import io.github.itskillerluc.duclib.data.model.serializers.Cube;
import io.github.itskillerluc.duclib.data.model.serializers.Geometry;
import io.github.itskillerluc.duclib.data.model.serializers.GeometryHolder;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * this is here in case you don't want any of the other stuff and just want a raw model.
 */
public abstract class BaseDucModel extends Model {
    public BaseDucModel(Function<ResourceLocation, RenderType> pRenderType) {
        super(pRenderType);
    }

    /**
     * internal method to generate the lake definition
     * @param entity entity for the model.
     * @return a new LakeDefinition generated from the json file
     */
    static LakeDefinition generateLakeDefinition(ResourceLocation entity){
        GeometryHolder holder = DucLibModelLoader.getModel(entity);
        if (holder == null) {
            LogManager.getLogger().error(entity.toString() + " Has a null holder.");
        }
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

        generateLakeDefinitionRecursively("dl_top_root", count, bones, topRoot, new float[]{0, 24, 0});

        return LakeDefinition.create(pondDefinition, geometry.description().textureWidth(), geometry.description().textureHeight());
    }

    /**
     * helper method for generating a DuclingDefinition from a json file
     */
    private static DuclingDefinition generateLakeDefinitionRecursively(String parent, int count, List<Bone> bones, DuclingDefinition parentDucling, float[] offset){
        for (Bone bone : bones) {
            if (bone.parent().equals(parent)){
                DuclingDefinition parentDef = parentDucling.addOrReplaceChild(bone.name(), createWings(bone, new float[]{bone.pivot()[0], bone.pivot()[1], bone.pivot()[2]}), PartPose.offsetAndRotation(bone.pivot()[0] - offset[0], -(bone.pivot()[1] - offset[1]), bone.pivot()[2] - offset[2], Mth.DEG_TO_RAD * bone.rotation()[0], Mth.DEG_TO_RAD * bone.rotation()[1], Mth.DEG_TO_RAD * bone.rotation()[2]));
                if (bone.cubes() != null) {
                    for (Cube cube : bone.cubes()) {
                        if (cube.rotation() != null) {
                            parentDef.addOrReplaceChild("cube_" + count, createWings(new Bone("cube_" + count, bone.name(), new float[]{}, null, new Cube[]{new Cube(cube.origin(), cube.size(), null, null, cube.inflate(), cube.uv(), cube.mirror())}), new float[]{cube.pivot()[0], cube.pivot()[1], cube.pivot()[2]}), PartPose.offsetAndRotation(cube.pivot()[0] - bone.pivot()[0], bone.pivot()[1] - cube.pivot()[1], cube.pivot()[2] - bone.pivot()[2], Mth.DEG_TO_RAD * cube.rotation()[0], Mth.DEG_TO_RAD * cube.rotation()[1], Mth.DEG_TO_RAD * cube.rotation()[2]));
                            count++;
                        }
                    }
                }
                generateLakeDefinitionRecursively(bone.name(), count, bones, parentDef, new float[]{bone.pivot()[0], bone.pivot()[1], bone.pivot()[2]});
            }
        }
        return parentDucling;
    }

    /**
     * Deserialize all the wings from the json file to java code.
     */
    private static WingListBuilder createWings(Bone bone, float[] offset){
        WingListBuilder builder = WingListBuilder.create();
        if (bone.cubes() != null) {
            for (Cube cube : bone.cubes()) {
                if (cube.rotation() == null) {
                    if (cube.uv().left().isPresent()) {
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

    /**
     * Use this to create a LakeDefinition generated from a model file.
     * @param location The entity Model location
     * @return a LakeDefinition generated from the model json file.
     */
    public static LakeDefinition getLakeDefinition(ResourceLocation location){
        return BaseDucModel.generateLakeDefinition(location);
    }
}
