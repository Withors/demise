package net.minecraft.scoreboard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import java.util.List;

public class ScoreHealthCriteria extends ScoreDummyCriteria {
    public ScoreHealthCriteria(String name) {
        super(name);
    }

    public int setScore(List<EntityPlayer> p_96635_1_) {
        float f = 0.0F;

        for (EntityPlayer entityplayer : p_96635_1_) {
            f += entityplayer.getHealth() + entityplayer.getAbsorptionAmount();
        }

        if (!p_96635_1_.isEmpty()) {
            f /= (float) p_96635_1_.size();
        }

        return MathHelper.ceiling_float_int(f);
    }

    public boolean isReadOnly() {
        return true;
    }

    public IScoreObjectiveCriteria.EnumRenderType getRenderType() {
        return IScoreObjectiveCriteria.EnumRenderType.HEARTS;
    }
}
