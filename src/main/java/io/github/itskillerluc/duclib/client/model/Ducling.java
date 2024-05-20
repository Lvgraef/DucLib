package io.github.itskillerluc.duclib.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cpw.mods.util.Lazy;
import io.github.itskillerluc.duclib.client.model.definitions.AdvancedUV;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Duclings are the DucLib equivalent of the vanilla ModelParts.
 */
@SuppressWarnings("unused")
public final class Ducling extends ModelPart{
    public static final float DEFAULT_SCALE = 1.0F;
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public float xScale = 1.0F;
    public float yScale = 1.0F;
    public float zScale = 1.0F;
    public boolean visible = true;
    public boolean skipDraw;
    private final List<Wing> wings;
    private final Map<String, Ducling> children;
    private PartPose initialPose = PartPose.ZERO;

    private final Lazy<Map<String, Ducling>> duclings = Lazy.of(this::getAllDuclings);

    public Ducling(List<Wing> pWings, Map<String, Ducling> pChildren) {
        super((List<Cube>)(List<?>) pWings, (Map<String, ModelPart>) (Map<String, ?>) pChildren);
        this.wings = pWings;
        this.children = pChildren;
    }

    /**
     * save the partPose
     * @return the new partPose
     */
    @Override
    public @NotNull PartPose storePose() {
        return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
    }

    /**
     * @return the default partPose
     */
    @Override
    public @NotNull PartPose getInitialPose() {
        return this.initialPose;
    }

    /**
     * set the default partPose
     * @param pInitialPose the default partPose
     */
    @Override
    public void setInitialPose(@NotNull PartPose pInitialPose) {
        this.initialPose = pInitialPose;
    }

    /**
     * reset the partPose
     */
    @Override
    public void resetPose() {
        this.loadPose(this.initialPose);
    }

    /**
     * @param pPartPose load a partPose into the ducling
     */
    @Override
    public void loadPose(PartPose pPartPose) {
        this.x = pPartPose.x;
        this.y = pPartPose.y;
        this.z = pPartPose.z;
        this.xRot = pPartPose.xRot;
        this.yRot = pPartPose.yRot;
        this.zRot = pPartPose.zRot;
        this.xScale = 1.0F;
        this.yScale = 1.0F;
        this.zScale = 1.0F;
    }

    /**
     * @param ducling copy a partPose from another ducling
     */
    @Override
    public void copyFrom(ModelPart ducling) {
        this.xScale = ducling.xScale;
        this.yScale = ducling.yScale;
        this.zScale = ducling.zScale;
        this.xRot = ducling.xRot;
        this.yRot = ducling.yRot;
        this.zRot = ducling.zRot;
        this.x = ducling.x;
        this.y = ducling.y;
        this.z = ducling.z;
    }

    @Override
    public boolean hasChild(@NotNull String pName) {
        return this.children.containsKey(pName);
    }

    @Override
    public @NotNull Ducling getChild(@NotNull String pName) {
        Ducling ducling = this.children.get(pName);
        if (ducling == null) {
            throw new NoSuchElementException("Can't find part " + pName);
        } else {
            return ducling;
        }
    }

    /**
     * set the position of this ducling
     */
    @Override
    public void setPos(float pX, float pY, float pZ) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
    }

    /**
     * set the rotation of this ducling
     */
    @Override
    public void setRotation(float pXRot, float pYRot, float pZRot) {
        this.xRot = pXRot;
        this.yRot = pYRot;
        this.zRot = pZRot;
    }

    /**
     * render the ducling
     */
    @Override
    public void render(@NotNull PoseStack pPoseStack, @NotNull VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay) {
        this.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * render the ducling
     */
    @Override
    public void render(@NotNull PoseStack pPoseStack, @NotNull VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        if (this.visible) {
            if (!this.wings.isEmpty() || !this.children.isEmpty()) {
                pPoseStack.pushPose();
                this.translateAndRotate(pPoseStack);
                if (!this.skipDraw) {
                    this.compile(pPoseStack.last(), pVertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
                }

                for(Ducling ducling : this.children.values()) {
                    ducling.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
                }

                pPoseStack.popPose();
            }
        }
    }

    @Override
    public void visit(@NotNull PoseStack pPoseStack, ModelPart.@NotNull Visitor pVisitor) {
        this.visit(pPoseStack, pVisitor, "");
    }
    private void visit(PoseStack pPoseStack, ModelPart.Visitor pVisitor, String path) {
        if (!this.wings.isEmpty() || !this.children.isEmpty()) {
            pPoseStack.pushPose();
            this.translateAndRotate(pPoseStack);
            PoseStack.Pose poseStack$pose = pPoseStack.last();

            for(int i = 0; i < this.wings.size(); ++i) {
                pVisitor.visit(poseStack$pose, path, i, this.wings.get(i));
            }

            String s = path + "/";
            this.children.forEach((pth, child) -> child.visit(pPoseStack, pVisitor, s + pth));
            pPoseStack.popPose();
        }
    }

    @Override
    public void translateAndRotate(PoseStack pPoseStack) {
        pPoseStack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
        if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
            pPoseStack.mulPose((new Quaternionf()).rotationZYX(this.zRot, this.yRot, this.xRot));
        }

        if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
            pPoseStack.scale(this.xScale, this.yScale, this.zScale);
        }

    }

    /**
     * render all the wings of this ducling
     */
    private void compile(PoseStack.Pose pPose, VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        for(Wing ducling$wing : this.wings) {
            ducling$wing.compile(pPose, pVertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
        }

    }

    @Override
    public @NotNull Wing getRandomCube(RandomSource pRandom) {
        return this.wings.get(pRandom.nextInt(this.wings.size()));
    }

    @Override
    public boolean isEmpty() {
        return this.wings.isEmpty();
    }

    @Override
    public void offsetPos(Vector3f offset) {
        this.x += offset.x();
        this.y += offset.y();
        this.z += offset.z();
    }

    @Override
    public void offsetRotation(Vector3f offset) {
        this.xRot += offset.x();
        this.yRot += offset.y();
        this.zRot += offset.z();
    }

    @Override
    public void offsetScale(Vector3f offset) {
        this.xScale += offset.x();
        this.yScale += offset.y();
        this.zScale += offset.z();
    }

    private Map<String, Ducling> getAllDuclings() {
        return getDuclingsRecursively(new HashMap<>(), this);
    }

    private Map<String, Ducling> getDuclingsRecursively(Map<String, Ducling> parent, Ducling ducling){
        for (Map.Entry<String, Ducling> stringDuclingEntry : ducling.children.entrySet()) {
            parent.put(stringDuclingEntry.getKey(), stringDuclingEntry.getValue());
            stringDuclingEntry.getValue().getDuclingsRecursively(parent, stringDuclingEntry.getValue());
        }
        return parent;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Wing extends ModelPart.Cube{
        private final Feather[] feathers;
        public final float xMin;
        public final float yMin;
        public final float zMin;
        public final float xMax;
        public final float yMax;
        public final float zMax;

        /**
         * this is the DucLib equivalent of ModelPart$Cube
         */
        public Wing(AdvancedUV[] featherUVs, float originX, float originY, float originZ, float pDimensionX, float pDimensionY, float pDimensionZ, float pGrowX, float pGrowY, float pGrowZ, boolean pMirror, float pTexWidthScaled, float pTexHeightScaled, Set<Direction> visibleFaces) {
            super((int) featherUVs[0].uv().u(), (int) featherUVs[0].uv().v(), originX, originY, originZ, pDimensionX, pDimensionY, pDimensionZ, pGrowX, pGrowY, pGrowZ, pMirror, pTexWidthScaled, pTexHeightScaled,visibleFaces);
            this.xMin = originX;
            this.yMin = originY;
            this.zMin = originZ;

            this.xMax = originX + pDimensionX;
            this.yMax = originY + pDimensionY;
            this.zMax = originZ + pDimensionZ;

            this.feathers = new Feather[visibleFaces.size()];

            float x = originX + pDimensionX;
            float y = originY + pDimensionY;
            float z = originZ + pDimensionZ;

            originX -= pGrowX;
            originY -= pGrowY;
            originZ -= pGrowZ;

            x += pGrowX;
            y += pGrowY;
            z += pGrowZ;

            if (pMirror) {
                float f3 = x;
                x = originX;
                originX = f3;
            }

            //(0, 0, 0)
            Barb ducling$barb7 = new Barb(originX, originY, originZ, 0.0F, 0.0F);
            //(1, 0, 0)
            Barb ducling$barb = new Barb(x, originY, originZ, 0.0F, 8.0F);
            //(1, 1, 0)
            Barb ducling$barb1 = new Barb(x, y, originZ, 8.0F, 8.0F);
            //(0, 1, 0)
            Barb ducling$barb2 = new Barb(originX, y, originZ, 8.0F, 0.0F);
            //(0, 0, 1)
            Barb ducling$barb3 = new Barb(originX, originY, z, 0.0F, 0.0F);
            //(1, 0, 1)
            Barb ducling$barb4 = new Barb(x, originY, z, 0.0F, 8.0F);
            //(1, 1, 1)
            Barb ducling$barb5 = new Barb(x, y, z, 8.0F, 8.0F);
            //(0, 1, 1)
            Barb ducling$barb6 = new Barb(originX, y, z, 8.0F, 0.0F);
            int i = 0;
            Map<Direction, AdvancedUV> featherUVMap = Arrays.stream(featherUVs).collect(Collectors.toMap(AdvancedUV::direction, value -> value));
            if (visibleFaces.contains(Direction.DOWN)) {
                this.feathers[i++] = new Feather(new Barb[]{ducling$barb4, ducling$barb3, ducling$barb7, ducling$barb}, featherUVMap.get(Direction.UP).uv().u(), featherUVMap.get(Direction.UP).uv().v(), featherUVMap.get(Direction.UP).uv().u() + featherUVMap.get(Direction.UP).uvSize().u(), featherUVMap.get(Direction.UP).uv().v() + featherUVMap.get(Direction.UP).uvSize().v(), pTexWidthScaled, pTexHeightScaled, pMirror, Direction.DOWN);
            }
            if (visibleFaces.contains(Direction.UP)) {
                this.feathers[i++] = new Feather(new Barb[]{ducling$barb1, ducling$barb2, ducling$barb6, ducling$barb5}, featherUVMap.get(Direction.DOWN).uv().u(), featherUVMap.get(Direction.DOWN).uv().v(), featherUVMap.get(Direction.DOWN).uv().u() + featherUVMap.get(Direction.DOWN).uvSize().u(), featherUVMap.get(Direction.DOWN).uv().v() + featherUVMap.get(Direction.DOWN).uvSize().v(), pTexWidthScaled, pTexHeightScaled, pMirror, Direction.UP);
            }
            if (visibleFaces.contains(Direction.WEST)) {
                this.feathers[i++] = new Feather(new Barb[]{ducling$barb7, ducling$barb3, ducling$barb6, ducling$barb2}, featherUVMap.get(Direction.EAST).uv().u(), featherUVMap.get(Direction.EAST).uv().v(), featherUVMap.get(Direction.EAST).uv().u() + featherUVMap.get(Direction.EAST).uvSize().u(), featherUVMap.get(Direction.EAST).uv().v() + featherUVMap.get(Direction.EAST).uvSize().v(), pTexWidthScaled, pTexHeightScaled, pMirror, Direction.EAST);
            }
            if (visibleFaces.contains(Direction.NORTH)) {
                this.feathers[i++] = new Feather(new Barb[]{ducling$barb, ducling$barb7, ducling$barb2, ducling$barb1}, featherUVMap.get(Direction.NORTH).uv().u(), featherUVMap.get(Direction.NORTH).uv().v(), featherUVMap.get(Direction.NORTH).uv().u() + featherUVMap.get(Direction.NORTH).uvSize().u(), featherUVMap.get(Direction.NORTH).uv().v() + featherUVMap.get(Direction.NORTH).uvSize().v(), pTexWidthScaled, pTexHeightScaled, pMirror, Direction.NORTH);
            }
            if (visibleFaces.contains(Direction.EAST)) {
                this.feathers[i++] = new Feather(new Barb[]{ducling$barb4, ducling$barb, ducling$barb1, ducling$barb5}, featherUVMap.get(Direction.WEST).uv().u(), featherUVMap.get(Direction.WEST).uv().v(), featherUVMap.get(Direction.WEST).uv().u() + featherUVMap.get(Direction.WEST).uvSize().u(), featherUVMap.get(Direction.WEST).uv().v() + featherUVMap.get(Direction.WEST).uvSize().v(), pTexWidthScaled, pTexHeightScaled, pMirror, Direction.WEST);
            }
            if (visibleFaces.contains(Direction.SOUTH)) {
                this.feathers[i] = new Feather(new Barb[]{ducling$barb3, ducling$barb4, ducling$barb5, ducling$barb6}, featherUVMap.get(Direction.SOUTH).uv().u(), featherUVMap.get(Direction.SOUTH).uv().v(), featherUVMap.get(Direction.SOUTH).uv().u() + featherUVMap.get(Direction.SOUTH).uvSize().u(), featherUVMap.get(Direction.SOUTH).uv().v() + featherUVMap.get(Direction.SOUTH).uvSize().v(), pTexWidthScaled, pTexHeightScaled, pMirror, Direction.SOUTH);
            }
        }

        public Wing(int pTexCoordU, int pTexCoordV, float pMinX, float pMinY, float pMinZ, float pDimensionX, float pDimensionY, float pDimensionZ, float pGrowX, float pGrowY, float pGrowZ, boolean pMirror, float pTexWidthScaled, float pTexHeightScaled, Set<Direction> visibleFaces) {
            super(pTexCoordU, pTexCoordV, pMinX, pMinY, pMinZ, pDimensionX, pDimensionY, pDimensionZ, pGrowX, pGrowY, pGrowZ, pMirror, pTexWidthScaled, pTexHeightScaled, visibleFaces);
            this.xMin = pMinX;
            this.yMin = pMinY;
            this.zMin = pMinZ;

            this.xMax = pMinX + pDimensionX;
            this.yMax = pMinY + pDimensionY;
            this.zMax = pMinZ + pDimensionZ;

            this.feathers = new Ducling.Feather[6];
            float f = pMinX + pDimensionX;
            float f1 = pMinY + pDimensionY;
            float f2 = pMinZ + pDimensionZ;
            pMinX -= pGrowX;
            pMinY -= pGrowY;
            pMinZ -= pGrowZ;
            f += pGrowX;
            f1 += pGrowY;
            f2 += pGrowZ;
            if (pMirror) {
                float f3 = f;
                f = pMinX;
                pMinX = f3;
            }

            Ducling.Barb ducling$barb7 = new Ducling.Barb(pMinX, pMinY, pMinZ, 0.0F, 0.0F); // modelpart$vertex7
            Ducling.Barb ducling$barb = new Ducling.Barb(f, pMinY, pMinZ, 0.0F, 8.0F); // modelpart$vertex
            Ducling.Barb ducling$barb1 = new Ducling.Barb(f, f1, pMinZ, 8.0F, 8.0F); // modelpart$vertex1
            Ducling.Barb ducling$barb2 = new Ducling.Barb(pMinX, f1, pMinZ, 8.0F, 0.0F); // modelpart$vertex2
            Ducling.Barb ducling$barb3 = new Ducling.Barb(pMinX, pMinY, f2, 0.0F, 0.0F); // modelpart$vertex3
            Ducling.Barb ducling$barb4 = new Ducling.Barb(f, pMinY, f2, 0.0F, 8.0F); // modelpart$vertex4
            Ducling.Barb ducling$barb5 = new Ducling.Barb(f, f1, f2, 8.0F, 8.0F); // modelpart$vertex5
            Ducling.Barb ducling$barb6 = new Ducling.Barb(pMinX, f1, f2, 8.0F, 0.0F); // modelpart$vertex6
            float f4 = (float)pTexCoordU;
            float f5 = (float)pTexCoordU + pDimensionZ;
            float f6 = (float)pTexCoordU + pDimensionZ + pDimensionX;
            float f7 = (float)pTexCoordU + pDimensionZ + pDimensionX + pDimensionX;
            float f8 = (float)pTexCoordU + pDimensionZ + pDimensionX + pDimensionZ;
            float f9 = (float)pTexCoordU + pDimensionZ + pDimensionX + pDimensionZ + pDimensionX;
            float f10 = (float)pTexCoordV;
            float f11 = (float)pTexCoordV + pDimensionZ;
            float f12 = (float)pTexCoordV + pDimensionZ + pDimensionY;
            int i = 0;
            if (visibleFaces.contains(Direction.DOWN)) {
                this.feathers[i++] = new Ducling.Feather(new Ducling.Barb[]{ducling$barb4, ducling$barb3, ducling$barb7, ducling$barb}, f5, f10, f6, f11, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.DOWN);
            }

            if (visibleFaces.contains(Direction.UP)) {
                this.feathers[i++] = new Ducling.Feather(new Ducling.Barb[]{ducling$barb1, ducling$barb2, ducling$barb6, ducling$barb5}, f6, f11, f7, f10, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.UP);
            }

            if (visibleFaces.contains(Direction.WEST)) {
                this.feathers[i++] = new Ducling.Feather(new Ducling.Barb[]{ducling$barb7, ducling$barb3, ducling$barb6, ducling$barb2}, f4, f11, f5, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.WEST);
            }

            if (visibleFaces.contains(Direction.NORTH)) {
                this.feathers[i++] = new Ducling.Feather(new Ducling.Barb[]{ducling$barb, ducling$barb7, ducling$barb2, ducling$barb1}, f5, f11, f6, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.NORTH);
            }

            if (visibleFaces.contains(Direction.EAST)) {
                this.feathers[i++] = new Ducling.Feather(new Ducling.Barb[]{ducling$barb4, ducling$barb, ducling$barb1, ducling$barb5}, f6, f11, f8, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.EAST);
            }

            if (visibleFaces.contains(Direction.SOUTH)) {
                this.feathers[i] = new Ducling.Feather(new Ducling.Barb[]{ducling$barb3, ducling$barb4, ducling$barb5, ducling$barb6}, f8, f11, f9, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.SOUTH);
            }
        }


        /**
         * render the wing
         */
        @Override
        public void compile(PoseStack.Pose pPose, @NotNull VertexConsumer pVertexConsumer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
            Matrix4f matrix4f = pPose.pose();
            Matrix3f matrix3f = pPose.normal();

            for(Feather ducling$feather : this.feathers) {
                Vector3f vector3f = matrix3f.transform(new Vector3f(ducling$feather.normal));
                float f = vector3f.x();
                float f1 = vector3f.y();
                float f2 = vector3f.z();

                for(Barb ducling$barb : ducling$feather.vertices) {
                    float f3 = ducling$barb.pos.x() / 16.0F;
                    float f4 = ducling$barb.pos.y() / 16.0F;
                    float f5 = ducling$barb.pos.z() / 16.0F;
                    Vector4f vector4f = matrix4f.transform(new Vector4f(f3, f4, f5, 1.0F));
                    pVertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), pRed, pGreen, pBlue, pAlpha, ducling$barb.u, ducling$barb.v, pPackedOverlay, pPackedLight, f, f1, f2);
                }
            }

        }


    }

    /**
     * The DucLib equivalent of ModelPart$Polygon
     */
    @OnlyIn(Dist.CLIENT)
    static class Feather {
        public final Barb[] vertices;
        public final Vector3f normal;

        public Feather(Barb[] pVertices, float x1, float y1, float x2, float y2, float textureWidth, float textureHeight, boolean mirror, Direction pDirection) {
            this.vertices = pVertices;
            pVertices[0] = pVertices[0].remap(x2 / textureWidth, y1 / textureHeight);
            pVertices[1] = pVertices[1].remap(x1 / textureWidth, y1 / textureHeight);
            pVertices[2] = pVertices[2].remap(x1 / textureWidth, y2 / textureHeight);
            pVertices[3] = pVertices[3].remap(x2 / textureWidth, y2 / textureHeight);
            if (!mirror) {
                int i = pVertices.length;

                for(int j = 0; j < i / 2; ++j) {
                    Barb ducling$barb = pVertices[j];
                    pVertices[j] = pVertices[i - 1 - j];
                    pVertices[i - 1 - j] = ducling$barb;
                }
            }

            this.normal = pDirection.step();
            if (!mirror) {
                this.normal.mul(-1.0F, 1.0F, 1.0F);
            }

        }
    }

    /**
     * The DucLib equivalent of ModelPart$Vertex
     */
    @OnlyIn(Dist.CLIENT)
    static class Barb {
        public final Vector3f pos;
        public final float u;
        public final float v;

        public Barb(float pX, float pY, float pZ, float pU, float pV) {
            this(new Vector3f(pX, pY, pZ), pU, pV);
        }

        public Barb remap(float pU, float pV) {
            return new Barb(this.pos, pU, pV);
        }

        public Barb(Vector3f pPos, float pU, float pV) {
            this.pos = pPos;
            this.u = pU;
            this.v = pV;
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    public interface Visitor extends ModelPart.Visitor {
        @Override
        void visit(PoseStack.@NotNull Pose pPose, @NotNull String pPath, int pIndex, @NotNull Cube pWing);
    }

    @Override
    public String toString() {
        return "Ducling{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", xRot=" + xRot +
                ", yRot=" + yRot +
                ", zRot=" + zRot +
                ", xScale=" + xScale +
                ", yScale=" + yScale +
                ", zScale=" + zScale +
                ", visible=" + visible +
                ", skipDraw=" + skipDraw +
                ", wings=" + wings +
                ", children=" + children +
                ", initialPose=" + initialPose +
                '}';
    }
}
