package wtf.demise.features.modules.impl.misc.anticheat.impl;

import net.minecraft.entity.player.EntityPlayer;
import wtf.demise.events.impl.packet.PacketEvent;
import wtf.demise.features.modules.impl.misc.anticheat.Check;

public class VelocityCheck extends Check {

    @Override
    public String getName() {
        return "Velocity";
    }

    @Override
    public void onPacketReceive(PacketEvent event, EntityPlayer player) {
    }

    public int vl;

    @Override
    public void onUpdate(EntityPlayer player) {
        if (player.hurtResistantTime > 6 && player.hurtResistantTime < 12 && player.lastTickPosX == player.posX && player.posZ == player.lastTickPosZ && !mc.theWorld.checkBlockCollision(player.getEntityBoundingBox().expand(0.05, 0.0, 0.05))) {
            vl++;
            if (vl >= 50) {
                flag(player, "Invalid velocity");
                vl = 0;
            }
        }
    }
}