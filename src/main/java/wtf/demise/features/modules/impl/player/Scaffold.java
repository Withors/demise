package wtf.demise.features.modules.impl.player;

import lombok.AllArgsConstructor;
import net.minecraft.block.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjglx.input.Keyboard;
import wtf.demise.events.annotations.EventTarget;
import wtf.demise.events.impl.misc.GameEvent;
import wtf.demise.events.impl.misc.WorldChangeEvent;
import wtf.demise.events.impl.packet.PacketEvent;
import wtf.demise.events.impl.player.*;
import wtf.demise.events.impl.render.Render3DEvent;
import wtf.demise.features.modules.Module;
import wtf.demise.features.modules.ModuleCategory;
import wtf.demise.features.modules.ModuleInfo;
import wtf.demise.features.modules.impl.combat.KillAura;
import wtf.demise.features.modules.impl.movement.Speed;
import wtf.demise.features.modules.impl.visual.Interface;
import wtf.demise.features.values.impl.BoolValue;
import wtf.demise.features.values.impl.ModeValue;
import wtf.demise.features.values.impl.MultiBoolValue;
import wtf.demise.features.values.impl.SliderValue;
import wtf.demise.utils.math.MathUtils;
import wtf.demise.utils.math.TimerUtils;
import wtf.demise.utils.misc.ChatUtils;
import wtf.demise.utils.misc.SpoofSlotUtils;
import wtf.demise.utils.player.MoveUtil;
import wtf.demise.utils.player.PlayerUtils;
import wtf.demise.utils.player.rotation.RotationHandler;
import wtf.demise.utils.player.rotation.RotationUtils;
import wtf.demise.utils.render.RenderUtils;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.toDegrees;
import static wtf.demise.utils.player.rotation.RotationUtils.getAngleDifference;

@ModuleInfo(name = "Scaffold", description = "Automatically places blocks bellow you.", category = ModuleCategory.Player)
public class Scaffold extends Module {
    public final ModeValue mode = new ModeValue("Mode", new String[]{"Normal", "Telly", "GodBridge"}, "Normal", this);
    private final ModeValue resetMode = new ModeValue("Reset mode", new String[]{"Jump", "Sneak"}, "Jump", this, () -> mode.is("GodBridge"));
    private final SliderValue minTellyTicks = new SliderValue("Min Telly Ticks", 2, 1, 5, this, () -> mode.is("Telly"));
    private final SliderValue maxTellyTicks = new SliderValue("Max Telly Ticks", 4, 1, 5, this, () -> mode.is("Telly"));
    private final ModeValue rotations = new ModeValue("Rotations", new String[]{"Normal", "Center", "GodBridge", "Derp", "Reverse"}, "Normal", this);
    private final BoolValue staticify = new BoolValue("Static-ify", false, this, () -> !rotations.is("Normal") && !rotations.is("Center"));
    private final SliderValue minSearch = new SliderValue("Min search", 0.1f, 0.01f, 1f, 0.01f, this, () -> rotations.is("Normal"));
    private final SliderValue maxSearch = new SliderValue("Max search", 0.9f, 0.01f, 1f, 0.01f, this, () -> rotations.is("Normal"));
    private final BoolValue clutch = new BoolValue("Clutch", false, this);
    private final ModeValue clutchCriteria = new ModeValue("Clutch criteria", new String[]{"MouseOver", "Prediction"}, "MouseOver", this, clutch::get);
    private final ModeValue clutchRotMode = new ModeValue("Clutch rotation mode", new String[]{"Normal", "Center"}, "Center", this, clutch::get);
    private final SliderValue minCSearch = new SliderValue("Min C search", 0.1f, 0.01f, 1f, 0.01f, this, () -> clutchRotMode.canDisplay() && clutchRotMode.is("Normal"));
    private final SliderValue maxCSearch = new SliderValue("Max C search", 0.9f, 0.01f, 1f, 0.01f, this, () -> clutchRotMode.canDisplay() && clutchRotMode.is("Normal"));
    private final BoolValue instantRots = new BoolValue("Instant clutch rots", false, this, clutch::get);
    private final RotationHandler rotationHandler = new RotationHandler(this);
    private final ModeValue sprintMode = new ModeValue("Sprint mode", new String[]{"Normal", "Ground", "Air", "None"}, "Normal", this);
    private final MultiBoolValue addons = new MultiBoolValue("Addons", Arrays.asList(
            new BoolValue("Swing", true),
            new BoolValue("Ignore tick cycle", false),
            new BoolValue("Ray Trace", true),
            new BoolValue("Keep Y", false),
            new BoolValue("Speed Keep Y", false),
            new BoolValue("Auto Jump", false),
            new BoolValue("Safe Walk", false),
            new BoolValue("Safe Walk When No Data", false),
            new BoolValue("Hover", false),
            new BoolValue("Sneak", false),
            new BoolValue("Target Block ESP", false)
    ), this);
    private final SliderValue blocksToSneak = new SliderValue("Blocks To Sneak", 7, 0, 8, this, () -> addons.isEnabled("Sneak"));
    private final BoolValue onlySneakOnGround = new BoolValue("Only sneak on ground", true, this, () -> addons.isEnabled("Sneak"));
    private final ModeValue tower = new ModeValue("Tower", new String[]{"Jump", "Vanilla", "PullDown", "NCP"}, "Jump", this, () -> mode.is("Normal"));
    private final ModeValue towerMove = new ModeValue("Tower Move", new String[]{"Jump", "Vanilla", "PullDown", "NCP"}, "Jump", this, () -> mode.is("Normal"));
    private final SliderValue pullDownMotion = new SliderValue("PullDown motion", 0.95f, 0.5f, 1, 0.01f, this, () -> towerMove.is("PullDown"));

    private BlockPos previousBlock;
    public BlockPos targetBlock;
    private double onGroundY;
    private int oldSlot = -1;
    private int blocksPlaced;
    private int tellyTicks;
    public boolean placed;
    public PlaceData data;
    private boolean isOnRightSide;
    private float yaw, pitch;
    private float initialYaw, initialPitch;
    private boolean clutching;
    private boolean startClutch;
    private final TimerUtils clutchTime = new TimerUtils();

    private final List<Block> blacklistedBlocks = Arrays.asList(Blocks.air, Blocks.water, Blocks.flowing_water, Blocks.lava, Blocks.wooden_slab, Blocks.chest, Blocks.flowing_lava,
            Blocks.enchanting_table, Blocks.carpet, Blocks.glass_pane, Blocks.skull, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.snow_layer, Blocks.ice, Blocks.packed_ice,
            Blocks.coal_ore, Blocks.diamond_ore, Blocks.emerald_ore, Blocks.trapped_chest, Blocks.torch, Blocks.anvil,
            Blocks.noteblock, Blocks.jukebox, Blocks.tnt, Blocks.gold_ore, Blocks.iron_ore, Blocks.lapis_ore, Blocks.lit_redstone_ore, Blocks.quartz_ore, Blocks.redstone_ore,
            Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate,
            Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.tallgrass, Blocks.tripwire, Blocks.tripwire_hook, Blocks.rail, Blocks.waterlily, Blocks.red_flower,
            Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.vine, Blocks.trapdoor, Blocks.yellow_flower, Blocks.ladder, Blocks.furnace, Blocks.sand, Blocks.cactus,
            Blocks.dispenser, Blocks.noteblock, Blocks.dropper, Blocks.crafting_table, Blocks.pumpkin, Blocks.sapling, Blocks.cobblestone_wall,
            Blocks.oak_fence, Blocks.activator_rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.redstone_torch, Blocks.acacia_stairs,
            Blocks.birch_stairs, Blocks.brick_stairs, Blocks.dark_oak_stairs, Blocks.jungle_stairs, Blocks.nether_brick_stairs, Blocks.oak_stairs,
            Blocks.quartz_stairs, Blocks.red_sandstone_stairs, Blocks.sandstone_stairs, Blocks.spruce_stairs, Blocks.stone_brick_stairs, Blocks.stone_stairs, Blocks.double_wooden_slab, Blocks.stone_slab, Blocks.double_stone_slab, Blocks.stone_slab2, Blocks.double_stone_slab2,
            Blocks.web, Blocks.gravel, Blocks.daylight_detector_inverted, Blocks.daylight_detector, Blocks.soul_sand, Blocks.piston, Blocks.piston_extension,
            Blocks.piston_head, Blocks.sticky_piston, Blocks.iron_trapdoor, Blocks.ender_chest, Blocks.end_portal, Blocks.end_portal_frame, Blocks.standing_banner,
            Blocks.wall_banner, Blocks.deadbush, Blocks.slime_block, Blocks.acacia_fence_gate, Blocks.birch_fence_gate, Blocks.dark_oak_fence_gate,
            Blocks.jungle_fence_gate, Blocks.spruce_fence_gate, Blocks.oak_fence_gate);

    @Override
    public void onEnable() {
        oldSlot = mc.thePlayer.inventory.currentItem;
        onGroundY = mc.thePlayer.getEntityBoundingBox().minY;
    }

    @Override
    public void onDisable() {
        mc.thePlayer.inventory.currentItem = oldSlot;
        SpoofSlotUtils.stopSpoofing();

        blocksPlaced = 0;
        tellyTicks = 0;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
    }

    private void updateState() {
        mc.thePlayer.inventory.currentItem = getBlockSlot();
        SpoofSlotUtils.startSpoofing(oldSlot);

        data = null;

        if (mc.thePlayer.onGround) {
            onGroundY = mc.thePlayer.getEntityBoundingBox().minY;
        }

        double posY = mc.thePlayer.getEntityBoundingBox().minY;

        if ((addons.isEnabled("Keep Y") || addons.isEnabled("Speed Keep Y") && isEnabled(Speed.class) && !mc.gameSettings.keyBindJump.isKeyDown())) {
            posY = onGroundY;
        }

        if (towerMoving() || towering()) {
            onGroundY = posY = mc.thePlayer.getEntityBoundingBox().minY;
        }

        targetBlock = new BlockPos(mc.thePlayer.posX, posY - 1, mc.thePlayer.posZ);

        if (mode.is("Telly") && mc.thePlayer.onGround) {
            tellyTicks = MathUtils.randomizeInt((int) minTellyTicks.get(), (int) maxTellyTicks.get());
        }

        data = findBlock(targetBlock);

        if (isEnabled(KillAura.class) && KillAura.currentTarget != null)
            return;

        if (mode.is("Telly") && mc.thePlayer.onGround) {
            tellyTicks = MathUtils.randomizeInt((int) minTellyTicks.get(), (int) maxTellyTicks.get());
        }

        switch (sprintMode.get()) {
            case "Normal":
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
                break;
            case "Ground":
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), mc.thePlayer.onGround);

                if (!mc.thePlayer.onGround) {
                    mc.thePlayer.setSprinting(false);
                }
                break;
            case "Air":
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), !mc.thePlayer.onGround);

                if (mc.thePlayer.onGround) {
                    mc.thePlayer.setSprinting(false);
                }
                break;
            case "None":
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
                mc.thePlayer.setSprinting(false);
                break;
        }

        if (tower.canDisplay() && (!tower.is("Jump") && towering() || !towerMove.is("Jump") && towerMoving())) {
            blocksPlaced = 0;
        }
    }

    private void updateRotations() {
        mc.entityRenderer.getMouseOver(1);

        if (mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !mc.objectMouseOver.getBlockPos().equalsBlockPos(data.blockPos.offset(data.facing)) || rotations.is("Derp")) {
            switch (rotations.get()) {
                case "Normal": {
                    initialYaw = getBestRotation(data.blockPos, data.facing, minSearch.get(), maxSearch.get())[0];
                    initialPitch = getBestRotation(data.blockPos, data.facing, minSearch.get(), maxSearch.get())[1];
                }
                break;
                case "Center": {
                    Vec3 hitVec = getVec3(data);

                    initialYaw = RotationUtils.getRotations(hitVec)[0];
                    initialPitch = RotationUtils.getRotations(hitVec)[1];
                }
                break;
                case "GodBridge": {
                    float movingYaw = MoveUtil.isMoving() ? MoveUtil.getYawFromKeybind() - 180 : mc.thePlayer.rotationYaw - 180;

                    if (mc.thePlayer.onGround) {
                        isOnRightSide = Math.floor(mc.thePlayer.posX + Math.cos(Math.toRadians(movingYaw)) * 0.5) != Math.floor(mc.thePlayer.posX) ||
                                Math.floor(mc.thePlayer.posZ + Math.sin(Math.toRadians(movingYaw)) * 0.5) != Math.floor(mc.thePlayer.posZ);
                    }

                    float yaw = MoveUtil.isMovingStraight() ? (movingYaw + (isOnRightSide ? 45 : -45)) : movingYaw;

                    initialYaw = Math.round(yaw / 45) * 45;
                    initialPitch = staticify.get() ? 75.7f : getBestRotation(data.blockPos, data.facing, 0.1f, 0.9f)[1];
                }
                break;
                case "Reverse": {
                    initialYaw = MoveUtil.getYawFromKeybind() - 180;
                    initialPitch = staticify.get() ? 80 : getBestRotation(data.blockPos, data.facing, 0.1f, 0.9f)[1];
                }
                break;
                case "Derp": {
                    initialYaw += 30;
                    initialPitch = staticify.get() ? 80 : getBestRotation(data.blockPos, data.facing, 0.1f, 0.9f)[1];
                }
                break;
            }
        }

        float[] rotation = new float[]{initialYaw, initialPitch};

        if (clutch.get()) {
            switch (clutchCriteria.get()) {
                case "MouseOver":
                    MovingObjectPosition ray = RotationUtils.rayTraceSafe(rotation, 4.5, 1);

                    if (ray.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !MoveUtil.isMoving()) {
                        if (instantRots.get()) {
                            rotationHandler.setRandYawSpeed(180);
                            rotationHandler.setRandPitchSpeed(180);
                        }

                        setClutchRot();

                        clutching = true;
                    } else {
                        clutching = false;
                    }
                    break;
                case "Prediction":
                    boolean isLeaningOffBlock = PlayerUtils.getBlock(targetBlock.offset(data.facing.getOpposite())) instanceof BlockAir;
                    boolean nextBlockIsAir = mc.theWorld.getBlockState(mc.thePlayer.getPosition().offset(EnumFacing.fromAngle(yaw), 1).down()).getBlock() instanceof BlockAir;

                    if ((isLeaningOffBlock && nextBlockIsAir) || !MoveUtil.isMoving()) {
                        startClutch = true;
                    } else if (startClutch) {
                        clutchTime.reset();
                        startClutch = false;
                    }

                    clutching = startClutch || !clutchTime.hasTimeElapsed(200);

                    if (clutching) {
                        setClutchRot();
                    }
                    break;
            }
        } else {
            clutching = false;
        }

        if (!mode.is("Telly") || mode.is("Telly") && mc.thePlayer.offGroundTicks >= tellyTicks) {
            rotationHandler.setRotation(new float[]{clutching ? yaw : initialYaw, clutching ? pitch : initialPitch});
        }
    }

    private void setClutchRot() {
        Vec3 hitVec = getVec3(data);

        switch (clutchRotMode.get()) {
            case "Normal": {
                this.yaw = getBestRotation(data.blockPos, data.facing, minCSearch.get(), maxCSearch.get())[0];
                this.pitch = getBestRotation(data.blockPos, data.facing, minCSearch.get(), maxCSearch.get())[1];
            }
            break;
            case "Center": {
                this.yaw = RotationUtils.getRotations(hitVec)[0];
                this.pitch = RotationUtils.getRotations(hitVec)[1];
            }
            break;
        }
    }

    @EventTarget
    public void onAngle(AngleEvent e) {
        updateState();
        updateRotations();
    }

    private void place() {
        if (!mode.is("Telly") || mode.is("Telly") && mc.thePlayer.offGroundTicks >= tellyTicks) {
            place(data.blockPos, data.facing, getVec3(data));
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent e) {
        setTag(mode.get());

        rotationHandler.updateRotSpeed(e);

        if (!addons.isEnabled("Ignore tick cycle") || (clutching && addons.isEnabled("Ray Trace"))) {
            place();
        }
    }

    @EventTarget
    public void onGameUpdate(GameEvent e) {
        if (addons.isEnabled("Ignore tick cycle")) {
            if (addons.isEnabled("Ray Trace")) {
                if (!clutching) {
                    placeAlternative();
                }
            } else {
                place();
            }
        }
    }

    // if it works, don't touch it
    private void placeAlternative() {
        MovingObjectPosition ray = mc.objectMouseOver;

        if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (mc.objectMouseOver.sideHit != EnumFacing.UP) {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), ray.getBlockPos(), ray.sideHit, ray.hitVec)) {
                    if (addons.isEnabled("Swing")) {
                        mc.thePlayer.swingItem();
                        mc.getItemRenderer().resetEquippedProgress();
                    } else {
                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                    }

                    blocksPlaced += 1;
                    placed = true;
                }

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            } else if (mc.thePlayer.isJumping) {
                mc.rightClickDelayTimer = 1;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
            } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            }
        }

        previousBlock = ray.getBlockPos().offset(ray.sideHit);
    }

    @EventTarget
    public void onSafeWalk(SafeWalkEvent event) {
        if (addons.isEnabled("Safe Walk") && mc.thePlayer.onGround || addons.isEnabled("Safe Walk When No Data") && data == null) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onMovementInput(MoveInputEvent e) {
        if (data == null || data.blockPos == null || data.facing == null || getBlockSlot() == -1 || isEnabled(KillAura.class) && KillAura.currentTarget != null && !(mc.theWorld.getBlockState(getModule(Scaffold.class).targetBlock).getBlock() instanceof BlockAir))
            return;

        //todo
        if (mode.is("GodBridge")) {
            MovingObjectPosition ray = RotationUtils.rayTraceSafe(new float[]{initialYaw, initialPitch}, 4.5, 1);

            if ((ray.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || ray.getBlockPos().distanceSq(data.blockPos.offset(data.facing)) > 1)) {
                if (resetMode.is("Jump")) {
                    if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
                        e.setJumping(true);
                    }
                    ChatUtils.sendMessageClient("wtf");
                } else {
                    e.setSneaking(true);
                }
            }
        }

        if (addons.isEnabled("Sneak")) {
            if (!mc.thePlayer.onGround && onlySneakOnGround.get()) {
                return;
            }

            if (blocksPlaced > blocksToSneak.get()) {
                blocksPlaced = 0;
            }

            if (blocksPlaced == blocksToSneak.get()) {
                e.setSneaking(true);
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent e) {
        if (data == null || data.blockPos == null || data.facing == null || getBlockSlot() == -1 || isEnabled(KillAura.class) && KillAura.currentTarget != null && !(mc.theWorld.getBlockState(getModule(Scaffold.class).targetBlock).getBlock() instanceof BlockAir))
            return;

        if (mc.thePlayer.onGround) {
            if ((addons.isEnabled("Auto Jump") || mode.is("Telly")) && !towering() && !towerMoving() && (!isEnabled(Speed.class))) {
                if (mode.is("Telly") && !mc.thePlayer.isSprinting()) {
                    return;
                }

                if (MoveUtil.isMoving()) {
                    mc.thePlayer.jump();
                }
            }
        }
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if (data == null || data.blockPos == null || data.facing == null || getBlockSlot() == -1 || isEnabled(KillAura.class) && KillAura.currentTarget != null && !(mc.theWorld.getBlockState(getModule(Scaffold.class).targetBlock).getBlock() instanceof BlockAir))
            return;

        if (tower.canDisplay()) {
            if (tower.is("Vanilla")) {
                if (!mc.thePlayer.isPotionActive(Potion.jump)) {
                    if (towering()) {
                        event.setY(mc.thePlayer.motionY = 0.42);
                    }
                }
            }
        }

        if (towerMove.canDisplay()) {
            if (towerMove.is("Vanilla")) {
                if (MoveUtil.isMoving() && MoveUtil.getSpeed() > 0.1 && !mc.thePlayer.isPotionActive(Potion.jump)) {
                    if (towerMoving()) {
                        mc.thePlayer.motionY = 0.42f;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onPreMotion(MotionEvent event) {
        if (event.isPost())
            return;

        if (data == null || data.blockPos == null || data.facing == null || getBlockSlot() == -1 || isEnabled(KillAura.class) && KillAura.currentTarget != null && !(mc.theWorld.getBlockState(getModule(Scaffold.class).targetBlock).getBlock() instanceof BlockAir))
            return;

        if (tower.canDisplay()) {
            if (tower.is("NCP") && towering()) {
                sendPacketNoEvent(new C08PacketPlayerBlockPlacement(null));

                if (mc.thePlayer.posY % 1 <= 0.00153598) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY), mc.thePlayer.posZ);
                    mc.thePlayer.motionY = 0.42F;
                } else if (mc.thePlayer.posY % 1 < 0.1 && mc.thePlayer.offGroundTicks != 0) {
                    mc.thePlayer.motionY = 0;
                    mc.thePlayer.setPosition(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY), mc.thePlayer.posZ);
                }
            }
        }

        if (towerMove.canDisplay()) {
            if (towerMove.is("NCP") && towerMoving()) {
                sendPacketNoEvent(new C08PacketPlayerBlockPlacement(null));

                if (mc.thePlayer.posY % 1 <= 0.00153598) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY), mc.thePlayer.posZ);
                    mc.thePlayer.motionY = 0.42F;
                } else if (mc.thePlayer.posY % 1 < 0.1 && mc.thePlayer.offGroundTicks != 0) {
                    mc.thePlayer.motionY = 0;
                    mc.thePlayer.setPosition(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY), mc.thePlayer.posZ);
                }
            }
        }

        if ((towerMove.canDisplay() && towerMoving() && towerMove.is("PullDown")) || (tower.canDisplay() && towering() && tower.is("PullDown"))) {
            if (mc.thePlayer.onGround) mc.thePlayer.motionY = 0.42F;
            mc.thePlayer.motionX *= pullDownMotion.get();
            mc.thePlayer.motionZ *= pullDownMotion.get();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent e) {
        if (e.getState() == PacketEvent.State.OUTGOING) {
            if ((towerMove.canDisplay() && towerMoving() && towerMove.is("PullDown")) || (tower.canDisplay() && towering() && tower.is("PullDown"))) {
                if (mc.thePlayer.motionY > -0.0784000015258789 && e.getPacket() instanceof C08PacketPlayerBlockPlacement wrapper) {
                    if (wrapper.getPosition().equals(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.4, mc.thePlayer.posZ))) {
                        mc.thePlayer.motionY = -0.0784000015258789;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent e) {
        if (addons.isEnabled("Target Block ESP")) {
            RenderUtils.renderBlock(data.blockPos.offset(data.facing), getModule(Interface.class).color(0, 100), false, true);
        }
    }

    @EventTarget
    public void onWorld(WorldChangeEvent e) {
        setEnabled(false);
    }

    public boolean towering() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && !isEnabled(Speed.class) && !MoveUtil.isMoving();
    }

    public boolean towerMoving() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && !isEnabled(Speed.class) && MoveUtil.isMoving();
    }

    private int getBlockSlot() {
        int slot = mc.thePlayer.inventory.currentItem;

        if (getBlockCount() == 0) {
            return slot;
        }

        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];

            if (itemStack != null && itemStack.stackSize > 0) {
                final Item item = itemStack.getItem();

                if (item instanceof ItemBlock && !blacklistedBlocks.contains(((ItemBlock) item).getBlock())) {
                    slot = i;
                }
            }
        }

        return slot;
    }

    public int getBlockCount() {
        int blockCount = 0;

        for (int i = 36; i < 45; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue;

            final ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (!(is.getItem() instanceof ItemBlock && !blacklistedBlocks.contains(((ItemBlock) is.getItem()).getBlock()))) {
                continue;
            }

            blockCount += is.stackSize;
        }

        return blockCount;
    }

    private static boolean isInteractable(Block block) {
        return block instanceof BlockFurnace || block instanceof BlockFenceGate || block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockEnchantmentTable || block instanceof BlockBrewingStand || block instanceof BlockBed || block instanceof BlockDispenser || block instanceof BlockHopper || block instanceof BlockAnvil || block == Blocks.crafting_table;
    }

    private void place(BlockPos pos, EnumFacing facing, Vec3 hitVec) {
        if (!addons.isEnabled("Ray Trace")) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), pos, facing, hitVec)) {
                if (addons.isEnabled("Swing")) {
                    mc.thePlayer.swingItem();
                    mc.getItemRenderer().resetEquippedProgress();
                } else {
                    mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                }
                blocksPlaced += 1;
                placed = true;
            }
            previousBlock = data.blockPos.offset(data.facing);
        } else {
            MovingObjectPosition ray = RotationUtils.rayTrace(4.5, 1);

            if ((!addons.isEnabled("Keep Y") && !addons.isEnabled("Speed Keep Y") && ray.getBlockPos().distanceSq(pos) <= 1) || ((addons.isEnabled("Keep Y") || addons.isEnabled("Speed Keep Y")) && ray.getBlockPos().equalsBlockPos(pos))) {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), ray.getBlockPos(), ray.sideHit, ray.hitVec)) {
                    if (addons.isEnabled("Swing")) {
                        mc.thePlayer.swingItem();
                        mc.getItemRenderer().resetEquippedProgress();
                    } else {
                        mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                    }
                    blocksPlaced += 1;
                    placed = true;
                }
            }

            previousBlock = ray.getBlockPos().offset(ray.sideHit);
        }
    }

    private Vec3 getVec3(PlaceData data) {
        BlockPos pos = data.blockPos;
        EnumFacing face = data.facing;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        x += face.getFrontOffsetX() / 2.0D;
        z += face.getFrontOffsetZ() / 2.0D;
        y += face.getFrontOffsetY() / 2.0D;

        return new Vec3(x, y, z);
    }

    private PlaceData findBlock(BlockPos pos) {
        EnumFacing[] facings = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP};
        BlockPos[] offsets = {
                new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0),
                new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
                new BlockPos(0, -1, 0)
        };

        if (previousBlock != null && previousBlock.getY() > mc.thePlayer.posY) {
            previousBlock = null;
        }

        PlaceData result = checkPositions(pos, offsets, facings);
        if (result != null) {
            return result;
        }

        for (BlockPos offset : offsets) {
            result = checkPositions(pos.add(offset), offsets, facings);
            if (result != null) {
                return result;
            }
        }

        for (BlockPos offset1 : offsets) {
            for (BlockPos offset2 : offsets) {
                result = checkPositions(pos.add(offset1).add(offset2), offsets, facings);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private PlaceData checkPositions(BlockPos basePos, BlockPos[] offsets, EnumFacing[] facings) {
        for (int i = 0; i < offsets.length; i++) {
            BlockPos newPos = basePos.add(offsets[i]);
            Block block = mc.theWorld.getBlockState(newPos).getBlock();

            if (newPos.equals(previousBlock)) {
                return new PlaceData(facings[i], newPos);
            }

            if (!block.getMaterial().isReplaceable() && !isInteractable(block)) {
                return new PlaceData(facings[i], newPos);
            }
        }
        return null;
    }

    private float[] getBestRotation(BlockPos blockPos, EnumFacing face, float min, float max) {
        Vec3i faceVec = face.getDirectionVec();

        float minX, maxX, minY, maxY, minZ, maxZ;

        if (faceVec.getX() == 0) {
            minX = min;
            maxX = max;
        } else if (faceVec.getX() == 1) {
            minX = maxX = 1.0f;
        } else if (faceVec.getX() == -1) {
            minX = maxX = 0.0f;
        } else {
            minX = min;
            maxX = max;
        }

        if (faceVec.getY() == 0) {
            minY = min;
            maxY = max;
        } else if (faceVec.getY() == 1) {
            minY = maxY = 1.0f;
        } else if (faceVec.getY() == -1) {
            minY = maxY = 0.0f;
        } else {
            minY = min;
            maxY = max;
        }

        if (faceVec.getZ() == 0) {
            minZ = min;
            maxZ = max;
        } else if (faceVec.getZ() == 1) {
            minZ = maxZ = 1.0f;
        } else if (faceVec.getZ() == -1) {
            minZ = maxZ = 0.0f;
        } else {
            minZ = min;
            maxZ = max;
        }

        float[] bestRot = RotationUtils.getRotations(getVec3(data));
        double bestDist = RotationUtils.getRotationDifference(bestRot);
        boolean picked = false;

        for (float x = minX; x <= maxX; x += 0.01f) {
            for (float y = minY; y <= maxY; y += 0.01f) {
                for (float z = minZ; z <= maxZ; z += 0.01f) {
                    Vec3 candidateLocal = new Vec3(x, y, z);
                    Vec3 candidateWorld = candidateLocal.add(new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));

                    double diff = RotationUtils.getRotationDifference(candidateWorld);
                    if (diff < bestDist && RotationUtils.rayTraceSafe(bestRot, 4.5, 1).typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        bestDist = diff;
                        bestRot = RotationUtils.getRotations(candidateWorld);
                        picked = true;
                    }
                }
            }
        }

        if (!picked) {
            return RotationUtils.getRotations(getVec3(data));
        }

        return bestRot;
    }

    @AllArgsConstructor
    private static class PlaceData {
        public EnumFacing facing;
        public BlockPos blockPos;
    }
}
