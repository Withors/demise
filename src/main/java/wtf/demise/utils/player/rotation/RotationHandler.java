package wtf.demise.utils.player.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.Range;
import wtf.demise.events.impl.player.UpdateEvent;
import wtf.demise.features.modules.Module;
import wtf.demise.features.modules.impl.combat.KillAura;
import wtf.demise.features.values.impl.BoolValue;
import wtf.demise.features.values.impl.ModeValue;
import wtf.demise.features.values.impl.SliderValue;
import wtf.demise.utils.InstanceAccess;
import wtf.demise.utils.math.MathUtils;
import wtf.demise.utils.math.TimerUtils;
import wtf.demise.utils.player.MovementCorrection;
import wtf.demise.utils.player.PlayerUtils;
import wtf.demise.utils.player.SmoothMode;

@Getter
public class RotationHandler implements InstanceAccess {
    final ModeValue smoothMode;
    final BoolValue imperfectCorrelation;
    final SliderValue yawRotationSpeedMin;
    final SliderValue yawRotationSpeedMax;
    final SliderValue pitchRotationSpeedMin;
    final SliderValue pitchRotationSpeedMax;
    final BoolValue distanceBasedRotationSpeed;
    final SliderValue minRange;
    final SliderValue maxRange;
    final SliderValue decrementPerCycle;
    final BoolValue movementFix;
    final BoolValue shortStop;
    final SliderValue shortStopDuration;
    final SliderValue rotationDiffBuildUpToStop;
    final SliderValue maxThresholdAttemptsToStop;
    final BoolValue silent;
    final BoolValue rotateLegit;
    private EntityLivingBase target;
    private final Module module;
    final BoolValue accel;
    final SliderValue yawAccelFactor;
    final SliderValue pitchAccelFactor;

    public RotationHandler(Module module) {
        this.module = module;

        silent = new BoolValue("Silent", true, module);
        rotateLegit = new BoolValue("Rotate legit", false, module);
        smoothMode = new ModeValue("Smooth mode", new String[]{"Linear", "Relative", "Polar", "None"}, "Linear", module, rotateLegit::get);
        accel = new BoolValue("Accelerate", false, module, () -> !smoothMode.is("None") && rotateLegit.get());
        yawAccelFactor = new SliderValue("Yaw accel factor", 0.25f, 0.01f, 0.9f, 0.01f, module, () -> accel.get() && accel.canDisplay());
        pitchAccelFactor = new SliderValue("Pitch accel factor", 0.25f, 0.01f, 0.9f, 0.01f, module, () -> accel.get() && accel.canDisplay());
        imperfectCorrelation = new BoolValue("Imperfect correlation", false, module, () -> !smoothMode.is("None") && rotateLegit.get());
        yawRotationSpeedMin = new SliderValue("Yaw rotation speed (min)", 180, 0.01f, 180, 0.01f, module, () -> !smoothMode.is("None") && !smoothMode.is("Polar"));
        yawRotationSpeedMax = new SliderValue("Yaw rotation speed (max)", 180, 0.01f, 180, 0.01f, module, () -> !smoothMode.is("None") && !smoothMode.is("Polar"));
        pitchRotationSpeedMin = new SliderValue("Pitch rotation speed (min)", 180, 0.01f, 180, 0.01f, module, () -> !smoothMode.is("None") && !smoothMode.is("Polar"));
        pitchRotationSpeedMax = new SliderValue("Pitch rotation speed (max)", 180, 0.01f, 180, 0.01f, module, () -> !smoothMode.is("None") && !smoothMode.is("Polar"));
        distanceBasedRotationSpeed = new BoolValue("Distance based rotation speed", false, module, () -> !smoothMode.is("None") && module.getClass() == KillAura.class && rotateLegit.get());
        minRange = new SliderValue("Min range", 0, 0, 8, 0.1f, module, () -> !smoothMode.is("None") && distanceBasedRotationSpeed.get() && distanceBasedRotationSpeed.canDisplay());
        maxRange = new SliderValue("Max range", 8, 0, 8, 0.1f, module, () -> !smoothMode.is("None") && distanceBasedRotationSpeed.get() && distanceBasedRotationSpeed.canDisplay());
        decrementPerCycle = new SliderValue("Decrement per cycle", 0.5f, 0.1f, 2, 0.1f, module, () -> !smoothMode.is("None") && distanceBasedRotationSpeed.get() && distanceBasedRotationSpeed.canDisplay());
        movementFix = new BoolValue("Movement fix", false, module);
        shortStop = new BoolValue("Short stop", false, module, rotateLegit::get);
        shortStopDuration = new SliderValue("Duration", 50, 25, 1000, 25, module, () -> shortStop.get() && shortStop.canDisplay());
        rotationDiffBuildUpToStop = new SliderValue("Rotation diff buildup to stop", 180, 50, 720, 1, module, () -> shortStop.get() && shortStop.canDisplay());
        maxThresholdAttemptsToStop = new SliderValue("Max threshold attempts to stop", 1, 0, 5, 1, module, () -> shortStop.get() && shortStop.canDisplay());
    }

    private final TimerUtils shortStopTimer = new TimerUtils();
    @Setter
    private float randYawSpeed;
    @Setter
    private float randPitchSpeed;
    private int maxThresholdReachAttempts;

    public void setRotation(float[] targetRotation) {
        SmoothMode mode = SmoothMode.valueOf(smoothMode.get());

        float hSpeed = randYawSpeed;
        float vSpeed = randPitchSpeed;

        if (shortStop.get() && shouldShortStop()) {
            hSpeed = MathUtils.randomizeFloat(0, 0.1f);
            vSpeed = MathUtils.randomizeFloat(0, 0.1f);
        }

        if (imperfectCorrelation.get()) {
            hSpeed *= MathUtils.randomizeFloat(0.9F, 1.1F);
            vSpeed *= MathUtils.randomizeFloat(0.9F, 1.1F);
        }

        if (module.getClass() == KillAura.class) {
            target = KillAura.currentTarget;

            if (distanceBasedRotationSpeed.get() && rotateLegit.get() && target != null) {
                float distance = (float) PlayerUtils.getDistanceToEntityBox(target);
                if (Range.between(minRange.get(), maxRange.get()).contains(distance)) {
                    float decreaseAmount = ((distance - minRange.get()) / 0.01f) * decrementPerCycle.get();
                    hSpeed -= decreaseAmount;
                    vSpeed -= decreaseAmount;
                }
            }
        }

        if (!rotateLegit.get() && !smoothMode.is("Linear")) {
            smoothMode.set("Linear");
        }

        hSpeed = MathHelper.clamp_float(hSpeed, 0, 180);
        vSpeed = MathHelper.clamp_float(vSpeed, 0, 180);

        if (rotateLegit.get()) {
            RotationManager.setRotation(targetRotation, movementFix.get(), new float[]{hSpeed, vSpeed}, accel.get(), new float[]{yawAccelFactor.get(), pitchAccelFactor.get()}, mode, silent.get());
        } else {
            OldRotationUtils.setRotation(targetRotation, movementFix.get() ? MovementCorrection.Silent : MovementCorrection.None, hSpeed, vSpeed);
        }
    }

    public float[] getSimpleRotationsToEntity(Entity entity) {
        float yaw;
        float pitch;
        Vec3 currentVec;

        Vec3 playerPos = mc.thePlayer.getPositionEyes(1);

        AxisAlignedBB bb = entity.getHitbox();

        Vec3 boxCenter = bb.getCenter();
        Vec3 entityPos = new Vec3(boxCenter.xCoord, bb.minY, boxCenter.zCoord);

        currentVec = entityPos.add(0.0, entity.getEyeHeight(), 0.0);

        double deltaX = currentVec.xCoord - playerPos.xCoord;
        double deltaY = currentVec.yCoord - playerPos.yCoord;
        double deltaZ = currentVec.zCoord - playerPos.zCoord;

        yaw = (float) -(Math.atan2(deltaX, deltaZ) * (180.0 / Math.PI));
        pitch = (float) (-Math.toDegrees(Math.atan2(deltaY, Math.hypot(deltaX, deltaZ))));

        pitch = MathHelper.clamp_float(pitch, -90, 90);

        return new float[]{yaw, pitch};
    }

    private boolean shouldShortStop() {
        if (!rotateLegit.get()) return false;

        if (!shortStopTimer.hasTimeElapsed(shortStopDuration.get())) {
            return true;
        }

        if (Math.abs(RotationManager.rotDiffBuildUp) < rotationDiffBuildUpToStop.get()) return false;

        if (maxThresholdReachAttempts < maxThresholdAttemptsToStop.get()) {
            maxThresholdReachAttempts++;
            return false;
        }

        shortStopTimer.reset();
        return true;
    }

    public void updateRotSpeed(UpdateEvent e) {
        randYawSpeed = MathUtils.randomizeFloat(yawRotationSpeedMin.get(), yawRotationSpeedMax.get());
        randPitchSpeed = MathUtils.randomizeFloat(pitchRotationSpeedMin.get(), pitchRotationSpeedMax.get());
    }
}