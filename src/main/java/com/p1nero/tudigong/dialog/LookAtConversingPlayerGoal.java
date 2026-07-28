package com.p1nero.tudigong.dialog;

import com.p1nero.tudigong.entity.TudiGongEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public final class LookAtConversingPlayerGoal extends LookAtPlayerGoal {
    private final TudiGongEntity npc;

    public LookAtConversingPlayerGoal(TudiGongEntity npc) {
        super(npc, Player.class, 8.0F);
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.LOOK, Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        Player player = this.npc.getConversingPlayer();
        if (player != null && player.isAlive() && !this.npc.hurtMarked && this.npc.distanceToSqr(player) <= 64.0D) {
            this.lookAt = player;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        super.start();
        this.npc.getNavigation().stop();
    }

    @Override
    public void stop() {
        super.stop();
        this.npc.setConversingPlayer(null);
    }
}
