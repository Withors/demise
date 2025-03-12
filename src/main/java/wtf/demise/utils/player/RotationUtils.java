package wtf.demise.utils.player;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.*;
import org.jetbrains.annotations.NotNull;
import wtf.demise.Demise;
import wtf.demise.events.annotations.EventPriority;
import wtf.demise.events.annotations.EventTarget;
import wtf.demise.events.impl.misc.WorldChangeEvent;
import wtf.demise.events.impl.packet.PacketEvent;
import wtf.demise.events.impl.player.*;
import wtf.demise.features.modules.impl.visual.Rotation;
import wtf.demise.utils.InstanceAccess;

import java.util.Objects;

import static java.lang.Math.abs;
import static java.lang.Math.hypot;

public class RotationUtils implements InstanceAccess {
    public static float[] currentRotation = null, serverRotation = new float[]{}, previousRotation = null;
    public static MovementCorrection currentCorrection = MovementCorrection.OFF;
    private static boolean enabled;
    public static float cachedHSpeed;
    public static float cachedVSpeed;
    public static float cachedMidpoint;
    public static SmoothMode smoothMode = SmoothMode.Linear;
    private static final Rotation moduleRotation = Demise.INSTANCE.getModuleManager().getModule(Rotation.class);

    public static boolean shouldRotate() {
        return currentRotation != null;
    }

    public static void setRotation(float[] rotation) {
        setRotation(rotation, MovementCorrection.OFF);
    }

    public static void setRotation(float[] rotation, final MovementCorrection correction) {
        if (moduleRotation.silent.get()) {
            RotationUtils.currentRotation = applyGCDFix(serverRotation, rotation);
        } else {
            mc.thePlayer.rotationYaw = applyGCDFix(serverRotation, rotation)[0];
            mc.thePlayer.rotationPitch = applyGCDFix(serverRotation, rotation)[1];
        }
        currentCorrection = correction;
        enabled = true;
    }

    public static void setRotation(float[] rotation, final MovementCorrection correction, float hSpeed, float vSpeed) {
        if (moduleRotation.silent.get()) {
            RotationUtils.currentRotation = smoothLinear(serverRotation, rotation, hSpeed, vSpeed);
        } else {
            mc.thePlayer.rotationYaw = smoothLinear(serverRotation, rotation, hSpeed, vSpeed)[0];
            mc.thePlayer.rotationPitch = smoothLinear(serverRotation, rotation, hSpeed, vSpeed)[1];
        }

        currentCorrection = correction;
        cachedHSpeed = hSpeed;
        cachedVSpeed = vSpeed;
        RotationUtils.smoothMode = SmoothMode.Linear;

        enabled = true;
    }

    public static void setRotation(float[] rotation, final MovementCorrection correction, float hSpeed, float vSpeed, SmoothMode smoothMode) {
        if (moduleRotation.silent.get()) {
            switch (smoothMode) {
                case Linear:
                    RotationUtils.currentRotation = smoothLinear(serverRotation, rotation, hSpeed, vSpeed);
                    break;
                case Lerp:
                    RotationUtils.currentRotation = smoothLerp(serverRotation, rotation, hSpeed, vSpeed);
                    break;
            }
        } else {
            switch (smoothMode) {
                case Linear:
                    mc.thePlayer.rotationYaw = smoothLinear(serverRotation, rotation, hSpeed, vSpeed)[0];
                    mc.thePlayer.rotationPitch = smoothLinear(serverRotation, rotation, hSpeed, vSpeed)[1];
                    break;
                case Lerp:
                    mc.thePlayer.rotationYaw = smoothLerp(serverRotation, rotation, hSpeed, vSpeed)[0];
                    mc.thePlayer.rotationPitch = smoothLerp(serverRotation, rotation, hSpeed, vSpeed)[1];
                    break;
            }
        }

        currentCorrection = correction;
        cachedHSpeed = hSpeed;
        cachedVSpeed = vSpeed;
        RotationUtils.smoothMode = smoothMode;

        enabled = true;
    }

    public static void setRotation(float[] rotation, final MovementCorrection correction, float hSpeed, float vSpeed, SmoothMode smoothMode, float midpoint) {
        if (moduleRotation.silent.get()) {
            switch (smoothMode) {
                case Linear:
                    RotationUtils.currentRotation = smoothLinear(serverRotation, rotation, hSpeed, vSpeed);
                    break;
                case Lerp:
                    RotationUtils.currentRotation = smoothLerp(serverRotation, rotation, hSpeed, vSpeed);
                    break;
                case Bezier:
                    RotationUtils.currentRotation = smoothBezier(serverRotation, rotation, hSpeed, vSpeed, midpoint);
                    break;
            }
        } else {
            switch (smoothMode) {
                case Linear:
                    mc.thePlayer.rotationYaw = smoothLinear(serverRotation, rotation, hSpeed, vSpeed)[0];
                    mc.thePlayer.rotationPitch = smoothLinear(serverRotation, rotation, hSpeed, vSpeed)[1];
                    break;
                case Lerp:
                    mc.thePlayer.rotationYaw = smoothLerp(serverRotation, rotation, hSpeed, vSpeed)[0];
                    mc.thePlayer.rotationPitch = smoothLerp(serverRotation, rotation, hSpeed, vSpeed)[1];
                    break;
                case Bezier:
                    mc.thePlayer.rotationYaw = smoothBezier(serverRotation, rotation, hSpeed, vSpeed, midpoint)[0];
                    mc.thePlayer.rotationPitch = smoothBezier(serverRotation, rotation, hSpeed, vSpeed, midpoint)[1];
                    break;
            }
        }

        currentCorrection = correction;
        cachedHSpeed = hSpeed;
        cachedVSpeed = vSpeed;
        cachedMidpoint = midpoint;
        RotationUtils.smoothMode = smoothMode;

        enabled = true;
    }

    @EventTarget
    @EventPriority(-100)
    public void onRotationUpdate(UpdateEvent event) {
        if (!enabled && currentRotation != null) {
            double distanceToPlayerRotation = getRotationDifference(currentRotation, new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch});

            if (distanceToPlayerRotation < 1) {
                resetRotation();
                return;
            }

            if (distanceToPlayerRotation > 0) {
                RotationUtils.currentRotation =
                        switch (smoothMode) {
                            case Linear ->
                                    smoothLinear(Objects.requireNonNullElse(currentRotation, serverRotation), new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch}, cachedHSpeed, cachedVSpeed);
                            case Lerp ->
                                    smoothLerp(Objects.requireNonNullElse(currentRotation, serverRotation), new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch}, cachedHSpeed, cachedVSpeed);
                            case Bezier ->
                                    smoothBezier(Objects.requireNonNullElse(currentRotation, serverRotation), new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch}, cachedHSpeed, cachedVSpeed, cachedMidpoint);
                        };
            }
        }

        enabled = false;
    }

    @EventTarget
    private void onMove(MoveInputEvent e) {
        if (currentCorrection == MovementCorrection.SILENT) {
            /*
             * Calculating movement fix
             */
            final float yaw = currentRotation[0];
            MoveUtil.fixMovement(e, yaw);
        }
    }

    @EventTarget
    private void onStrafe(StrafeEvent e) {
        if (shouldRotate()) {
            if (currentCorrection != MovementCorrection.OFF) {
                e.setYaw(currentRotation[0]);
            }
        }
    }

    @EventTarget
    private void onJump(JumpEvent event) {
        if (shouldRotate()) {
            if (currentCorrection != MovementCorrection.OFF) {
                event.setYaw(currentRotation[0]);
            }
        }
    }

    @EventTarget
    @EventPriority(-100)
    public void onPacket(final PacketEvent e) {
        final Packet<?> packet = e.getPacket();

        if (!(packet instanceof C03PacketPlayer packetPlayer)) return;

        if (!packetPlayer.rotating) return;

        if (shouldRotate()) {
            packetPlayer.yaw = currentRotation[0];
            packetPlayer.pitch = currentRotation[1];
        }

        serverRotation = new float[]{packetPlayer.yaw, packetPlayer.pitch};
    }

    @EventTarget
    public void onWorld(WorldChangeEvent e) {
        resetRotation();
    }

    @EventTarget
    @EventPriority(-100)
    public void onMotion(MotionEvent event) {
        if (event.isPost() && currentRotation != null) {
            double distanceToPlayerRotation = getRotationDifference(currentRotation, new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch});

            if (!enabled) {
                if (distanceToPlayerRotation < 1) {
                    resetRotation();
                    return;
                }

                if (distanceToPlayerRotation > 0) {
                    RotationUtils.currentRotation =
                            switch (smoothMode) {
                                case Linear ->
                                        smoothLinear(Objects.requireNonNullElse(currentRotation, serverRotation), new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch}, cachedHSpeed, cachedVSpeed);
                                case Lerp ->
                                        smoothLerp(Objects.requireNonNullElse(currentRotation, serverRotation), new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch}, cachedHSpeed, cachedVSpeed);
                                case Bezier ->
                                        smoothBezier(Objects.requireNonNullElse(currentRotation, serverRotation), new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch}, cachedHSpeed, cachedVSpeed, cachedMidpoint);
                            };
                }
            }

            enabled = false;
        }
    }

    @EventTarget
    public void onLook(LookEvent event) {
        if (shouldRotate()) {
            event.rotation = currentRotation;
        }
    }

    private static void resetRotation() {
        enabled = false;
        RotationUtils.currentRotation = null;
        currentCorrection = MovementCorrection.OFF;
    }

    public static float[] smoothLinear(final float[] currentRotation, final float[] targetRotation, float hSpeed, float vSpeed) {
        float yawDifference = getAngleDifference(targetRotation[0], currentRotation[0]);
        float pitchDifference = getAngleDifference(targetRotation[1], currentRotation[1]);

        double rotationDifference = hypot(abs(yawDifference), abs(pitchDifference));

        float straightLineYaw = (float) (abs(yawDifference / rotationDifference) * hSpeed);
        float straightLinePitch = (float) (abs(pitchDifference / rotationDifference) * vSpeed);

        float[] finalTargetRotation = new float[]{
                currentRotation[0] + Math.max(-straightLineYaw, Math.min(straightLineYaw, yawDifference)),
                currentRotation[1] + Math.max(-straightLinePitch, Math.min(straightLinePitch, pitchDifference))
        };

        return applyGCDFix(currentRotation, finalTargetRotation);
    }

    public static float[] smoothBezier(final float[] currentRotation, final float[] targetRotation, float hSpeed, float vSpeed, float midpoint) {
        float yawDifference = getAngleDifference(targetRotation[0], currentRotation[0]);
        float pitchDifference = getAngleDifference(targetRotation[1], currentRotation[1]);

        double rotationDifference = hypot(abs(yawDifference), abs(pitchDifference));

        float straightLineYaw = (float) (abs(yawDifference / rotationDifference) * hSpeed);
        float straightLinePitch = (float) (abs(pitchDifference / rotationDifference) * vSpeed);

        float[] finalTargetRotation = new float[]{
                currentRotation[0] + Math.max(-straightLineYaw, Math.min(straightLineYaw, yawDifference)),
                currentRotation[1] + Math.max(-straightLinePitch, Math.min(straightLinePitch, pitchDifference))
        };

        float yawDirection = yawDifference / (float) rotationDifference;
        float pitchDirection = pitchDifference / (float) rotationDifference;

        float controlYaw = currentRotation[0] + yawDirection * midpoint * (float) rotationDifference;
        float controlPitch = currentRotation[1] + pitchDirection * midpoint * (float) rotationDifference;

        float[] t = new float[]{hSpeed / 180, vSpeed / 180};

        float finalYaw = (1 - t[0]) * (1 - t[0]) * currentRotation[0] + 2 * (1 - t[0]) * t[0] * controlYaw + t[0] * t[0] * finalTargetRotation[0];
        float finalPitch = (1 - t[1]) * (1 - t[1]) * currentRotation[1] + 2 * (1 - t[1]) * t[1] * controlPitch + t[1] * t[1] * finalTargetRotation[1];

        float[] finalRotation = new float[]{finalYaw, finalPitch};

        return applyGCDFix(currentRotation, finalRotation);
    }

    public static float[] smoothLerp(final float[] currentRotation, final float[] targetRotation, float hSpeed, float vSpeed) {
        float yawDifference = getAngleDifference(targetRotation[0], currentRotation[0]);
        float pitchDifference = getAngleDifference(targetRotation[1], currentRotation[1]);

        float newYaw = currentRotation[0] + (yawDifference * hSpeed / 180);
        float newPitch = currentRotation[1] + (pitchDifference * vSpeed / 180);

        float[] finalTargetRotation = new float[]{newYaw, newPitch};

        return applyGCDFix(currentRotation, finalTargetRotation);
    }

    public static float[] applyGCDFix(float[] prevRotation, float[] currentRotation) {
        final float f = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 100000) * 0.6F + 0.2F);
        final double gcd = f * f * f * 8.0F * 0.15D;
        final float yaw = prevRotation[0] + (float) (Math.round((currentRotation[0] - prevRotation[0]) / gcd) * gcd);
        final float pitch = prevRotation[1] + (float) (Math.round((currentRotation[1] - prevRotation[1]) / gcd) * gcd);

        return new float[]{yaw, pitch};
    }

    public static float getAngleDifference(float a, float b) {
        return MathHelper.wrapAngleTo180_float(a - b);
    }

    public static float[] getAngles(Entity entity) {
        if (entity == null) return null;
        final EntityPlayerSP player = mc.thePlayer;

        final double diffX = entity.posX - player.posX,
                diffY = entity.posY + (entity.getEyeHeight() / 5 * 3) - (player.posY + player.getEyeHeight()),
                diffZ = entity.posZ - player.posZ, dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

        final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F,
                pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);

        return new float[]{player.rotationYaw + MathHelper.wrapAngleTo180_float(
                yaw - player.rotationYaw), player.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - player.rotationPitch)};
    }

    public static float i(final double n, final double n2) {
        return (float) (Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * 57.295780181884766 * -1.0);
    }

    public static double distanceFromYaw(final Entity entity) {
        return abs(MathHelper.wrapAngleTo180_double(i(entity.posX, entity.posZ) - mc.thePlayer.rotationYaw));
    }

    public static double getRotationDifference(float[] e) {
        return getRotationDifference(serverRotation, e);
    }

    public static double getRotationDifference(Vec3 e) {
        float[] entityRotation = getRotations(e.xCoord, e.yCoord, e.zCoord);
        return getRotationDifference(entityRotation);
    }

    public static float getRotationDifference(final Entity entity) {
        float[] target = RotationUtils.getRotations(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        return (float) hypot(abs(getAngleDifference(target[0], mc.thePlayer.rotationYaw)), abs(target[1] - mc.thePlayer.rotationPitch));
    }

    public static float getRotationDifference(final Entity entity, final Entity entity2) {
        float[] target = RotationUtils.getRotations(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        float[] target2 = RotationUtils.getRotations(entity2.posX, entity2.posY + entity2.getEyeHeight(), entity2.posZ);
        return (float) hypot(abs(getAngleDifference(target[0], target2[0])), abs(target[1] - target2[1]));
    }

    public static float getRotationDifference(final float[] a, final float[] b) {
        return (float) hypot(abs(getAngleDifference(a[0], b[0])), abs(a[1] - b[1]));
    }

    public static MovingObjectPosition rayTrace(float[] rot, double blockReachDistance, float partialTicks) {
        Vec3 vec3 = mc.thePlayer.getPositionEyes(partialTicks);
        Vec3 vec31 = mc.thePlayer.getLookCustom(rot[0], rot[1]);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, true, true);
    }

    public static MovingObjectPosition rayTrace(double blockReachDistance, float partialTicks) {
        Vec3 vec3 = mc.thePlayer.getPositionEyes(partialTicks);
        Vec3 vec31 = mc.thePlayer.getLookCustom(currentRotation[0], currentRotation[1]);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, true, true);
    }

    public static float[] getRotations(BlockPos blockPos, EnumFacing enumFacing) {
        return getRotations(blockPos, enumFacing, 0.25, 0.25);
    }

    public static float[] getRotations(BlockPos blockPos, EnumFacing enumFacing, double xz, double y) {
        double d = blockPos.getX() + 0.5 - mc.thePlayer.posX + enumFacing.getFrontOffsetX() * xz;
        double d2 = blockPos.getZ() + 0.5 - mc.thePlayer.posZ + enumFacing.getFrontOffsetZ() * xz;
        double d3 = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - blockPos.getY() - enumFacing.getFrontOffsetY() * y;
        double d4 = MathHelper.sqrt_double(d * d + d2 * d2);
        float f = (float) (Math.atan2(d2, d) * 180.0 / Math.PI) - 90.0f;
        float f2 = (float) (Math.atan2(d3, d4) * 180.0 / Math.PI);
        return new float[]{MathHelper.wrapAngleTo180_float(f), f2};
    }

    public static float[] getRotations(double rotX, double rotY, double rotZ, double startX, double startY, double startZ) {
        double x = rotX - startX;
        double y = rotY - startY;
        double z = rotZ - startZ;
        double dist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotations(double posX, double posY, double posZ) {
        return getRotations(posX, posY, posZ, mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }

    public static float[] getRotations(Vec3 vec) {
        return getRotations(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static float[] getRotationToBlock(BlockPos blockPos, EnumFacing direction) {

        double centerX = blockPos.getX() + 0.5 + direction.getFrontOffsetX() * 0.5;
        double centerY = blockPos.getY() + 0.5 + direction.getFrontOffsetY() * 0.5;
        double centerZ = blockPos.getZ() + 0.5 + direction.getFrontOffsetZ() * 0.5;

        double playerX = mc.thePlayer.posX;
        double playerY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        double playerZ = mc.thePlayer.posZ;

        double deltaX = centerX - playerX;
        double deltaY = centerY - playerY;
        double deltaZ = centerZ - playerZ;

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F);
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));

        return new float[]{yaw, pitch};
    }

    public static float clampTo90(final float n) {
        return MathHelper.clamp_float(n, -90, 90);
    }

    public static float calculateYawFromSrcToDst(final float yaw,
                                                 final double srcX,
                                                 final double srcZ,
                                                 final double dstX,
                                                 final double dstZ) {
        final double xDist = dstX - srcX;
        final double zDist = dstZ - srcZ;
        final float var1 = (float) (StrictMath.atan2(zDist, xDist) * 180.0 / Math.PI) - 90.0F;
        return yaw + MathHelper.wrapAngleTo180_float(var1 - yaw);
    }

    public static Vec3 getBestHitVec(final Entity entity) {
        final Vec3 positionEyes = mc.thePlayer.getPositionEyes(1);
        final AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        final double ex = MathHelper.clamp_double(positionEyes.xCoord, entityBoundingBox.minX, entityBoundingBox.maxX);
        final double ey = MathHelper.clamp_double(positionEyes.yCoord, entityBoundingBox.minY, entityBoundingBox.maxY);
        final double ez = MathHelper.clamp_double(positionEyes.zCoord, entityBoundingBox.minZ, entityBoundingBox.maxZ);
        return new Vec3(ex, ey, ez);
    }

    public static float getYaw(@NotNull BlockPos pos) {
        return getYaw(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    public static float getYaw(@NotNull AbstractClientPlayer from, @NotNull Vec3 pos) {
        return from.rotationYaw +
                MathHelper.wrapAngleTo180_float(
                        (float) Math.toDegrees(Math.atan2(pos.zCoord - from.posZ, pos.xCoord - from.posX)) - 90f - from.rotationYaw
                );
    }

    public static float getYaw(@NotNull Vec3 pos) {
        return getYaw(mc.thePlayer, pos);
    }

    public static float getPitch(@NotNull BlockPos pos) {
        return getPitch(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    public static float getPitch(@NotNull AbstractClientPlayer from, @NotNull Vec3 pos) {
        double diffX = pos.xCoord - from.posX;
        double diffY = pos.yCoord - (from.posY + from.getEyeHeight());
        double diffZ = pos.zCoord - from.posZ;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return from.rotationPitch + MathHelper.wrapAngleTo180_float((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - from.rotationPitch);
    }

    public static float getPitch(@NotNull Vec3 pos) {
        return getPitch(mc.thePlayer, pos);
    }

    public static float angleDifference(float a, float b) {
        return MathHelper.wrapAngleTo180_float(a - b);
    }

    public static float[] faceTrajectory(Entity target, boolean predict, float predictSize, float gravity, float velocity) {
        EntityPlayerSP player = mc.thePlayer;

        double posX = target.posX + (predict ? (target.posX - target.prevPosX) * predictSize : 0.0) - (player.posX + (predict ? player.posX - player.prevPosX : 0.0));
        double posY = target.getEntityBoundingBox().minY + (predict ? (target.getEntityBoundingBox().minY - target.prevPosY) * predictSize : 0.0) + target.getEyeHeight() - 0.15 - (player.getEntityBoundingBox().minY + (predict ? player.posY - player.prevPosY : 0.0)) - player.getEyeHeight();
        double posZ = target.posZ + (predict ? (target.posZ - target.prevPosZ) * predictSize : 0.0) - (player.posZ + (predict ? player.posZ - player.prevPosZ : 0.0));
        double posSqrt = Math.sqrt(posX * posX + posZ * posZ);

        velocity = Math.min((velocity * velocity + velocity * 2) / 3, 1f);

        float gravityModifier = 0.12f * gravity;

        return new float[]{
                (float) Math.toDegrees(Math.atan2(posZ, posX)) - 90f,
                (float) -Math.toDegrees(Math.atan((velocity * velocity - Math.sqrt(
                        velocity * velocity * velocity * velocity - gravityModifier * (gravityModifier * posSqrt * posSqrt + 2 * posY * velocity * velocity)
                )) / (gravityModifier * posSqrt)))
        };
    }

    public static float[] faceTrajectory(Entity target, boolean predict, float predictSize) {
        float gravity = 0.03f;
        float velocity = 0;

        return faceTrajectory(target, predict, predictSize, gravity, velocity);
    }
}