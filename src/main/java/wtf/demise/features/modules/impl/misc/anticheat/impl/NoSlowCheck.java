package wtf.demise.features.modules.impl.misc.anticheat.impl;

import net.minecraft.entity.player.EntityPlayer;
import wtf.demise.events.impl.packet.PacketEvent;
import wtf.demise.features.modules.impl.misc.anticheat.Check;

public class NoSlowCheck extends Check {

    private int sprintBuffer = 0, motionBuffer = 0;

    @Override
    public String getName() {
        return "NoSlow";
    }

    @Override
    public void onPacketReceive(PacketEvent event, EntityPlayer player) {
    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (player.isUsingItem() || player.isBlocking()) {
            if (player.isSprinting()) {
                if (++sprintBuffer > 5) {
                    flag(player, "Sprinting when using item or blocking");
                }
                return;
            }
            // a motion check
            double dx = player.prevPosX - player.posX, dz = player.prevPosZ - player.posZ;
            if (dx * dx + dz * dz > 0.07) { // sq: 0.25
                if (++motionBuffer > 10 && player.hurtTime == 0) {
                    flag(player, "Not sprinting but keep in sprint motion when blocking");
                    motionBuffer = 7;
                    return;
                }
            }
            motionBuffer -= (motionBuffer > 0 ? 1 : 0);
            sprintBuffer -= (sprintBuffer > 0 ? 1 : 0);
        }
    }


}