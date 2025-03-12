package wtf.demise.features.modules.impl.misc.anticheat.impl;

import net.minecraft.entity.player.EntityPlayer;
import wtf.demise.events.impl.packet.PacketEvent;
import wtf.demise.features.modules.impl.misc.anticheat.Check;

public class NoFallCheck extends Check {
    boolean fall;

    @Override
    public String getName() {
        return "No Fall";
    }

    @Override
    public void onPacketReceive(PacketEvent event, EntityPlayer player) {
    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (player.fallDistance > 3) {
            fall = true;
        }
        if (fall && player.fallDistance == 0 && player.hurtTime == 0 && !player.isInWater()) {
            flag(player, "Not taking any fall damage");
            fall = false;
        }
    }
}