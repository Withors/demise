package net.minecraft.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Explosion {
    private final boolean isFlaming;
    private final boolean isSmoking;
    private final Random explosionRNG;
    private final World worldObj;
    private final double explosionX;
    private final double explosionY;
    private final double explosionZ;
    private final Entity exploder;
    private final float explosionSize;
    private final List<BlockPos> affectedBlockPositions;
    private final Map<EntityPlayer, Vec3> playerKnockbackMap;

    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, List<BlockPos> affectedPositions) {
        this(worldIn, entityIn, x, y, z, size, false, true, affectedPositions);
    }

    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean flaming, boolean smoking, List<BlockPos> affectedPositions) {
        this(worldIn, entityIn, x, y, z, size, flaming, smoking);
        this.affectedBlockPositions.addAll(affectedPositions);
    }

    public Explosion(World worldIn, Entity entityIn, double x, double y, double z, float size, boolean flaming, boolean smoking) {
        this.explosionRNG = new Random();
        this.affectedBlockPositions = Lists.newArrayList();
        this.playerKnockbackMap = Maps.newHashMap();
        this.worldObj = worldIn;
        this.exploder = entityIn;
        this.explosionSize = size;
        this.explosionX = x;
        this.explosionY = y;
        this.explosionZ = z;
        this.isFlaming = flaming;
        this.isSmoking = smoking;
    }

    public void doExplosionA() {
        Set<BlockPos> set = Sets.newHashSet();
        int i = 16;

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (float) j / 15.0F * 2.0F - 1.0F;
                        double d1 = (float) k / 15.0F * 2.0F - 1.0F;
                        double d2 = (float) l / 15.0F * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = this.explosionSize * (0.7F + this.worldObj.rand.nextFloat() * 0.6F);
                        double d4 = this.explosionX;
                        double d6 = this.explosionY;
                        double d8 = this.explosionZ;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

                            if (iblockstate.getBlock().getMaterial() != Material.air) {
                                float f2 = this.exploder != null ? this.exploder.getExplosionResistance(this, this.worldObj, blockpos, iblockstate) : iblockstate.getBlock().getExplosionResistance(null);
                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (this.exploder == null || this.exploder.verifyExplosion(this, this.worldObj, blockpos, iblockstate, f))) {
                                set.add(blockpos);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d6 += d1 * 0.30000001192092896D;
                            d8 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        this.affectedBlockPositions.addAll(set);
        float f3 = this.explosionSize * 2.0F;
        int k1 = MathHelper.floor_double(this.explosionX - (double) f3 - 1.0D);
        int l1 = MathHelper.floor_double(this.explosionX + (double) f3 + 1.0D);
        int i2 = MathHelper.floor_double(this.explosionY - (double) f3 - 1.0D);
        int i1 = MathHelper.floor_double(this.explosionY + (double) f3 + 1.0D);
        int j2 = MathHelper.floor_double(this.explosionZ - (double) f3 - 1.0D);
        int j1 = MathHelper.floor_double(this.explosionZ + (double) f3 + 1.0D);
        List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB(k1, i2, j2, l1, i1, j1));
        Vec3 vec3 = new Vec3(this.explosionX, this.explosionY, this.explosionZ);

        for (Entity entity : list) {
            if (!entity.isImmuneToExplosions()) {
                double d12 = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / (double) f3;

                if (d12 <= 1.0D) {
                    double d5 = entity.posX - this.explosionX;
                    double d7 = entity.posY + (double) entity.getEyeHeight() - this.explosionY;
                    double d9 = entity.posZ - this.explosionZ;
                    double d13 = MathHelper.sqrt_double(d5 * d5 + d7 * d7 + d9 * d9);

                    if (d13 != 0.0D) {
                        d5 = d5 / d13;
                        d7 = d7 / d13;
                        d9 = d9 / d13;
                        double d14 = this.worldObj.getBlockDensity(vec3, entity.getEntityBoundingBox());
                        double d10 = (1.0D - d12) * d14;
                        entity.attackEntityFrom(DamageSource.setExplosionSource(this), (float) ((int) ((d10 * d10 + d10) / 2.0D * 8.0D * (double) f3 + 1.0D)));
                        double d11 = EnchantmentProtection.func_92092_a(entity, d10);
                        entity.motionX += d5 * d11;
                        entity.motionY += d7 * d11;
                        entity.motionZ += d9 * d11;

                        if (entity instanceof EntityPlayer && !((EntityPlayer) entity).capabilities.disableDamage) {
                            this.playerKnockbackMap.put((EntityPlayer) entity, new Vec3(d5 * d10, d7 * d10, d9 * d10));
                        }
                    }
                }
            }
        }
    }

    public void doExplosionB(boolean spawnParticles) {
        this.worldObj.playSoundEffect(this.explosionX, this.explosionY, this.explosionZ, "random.explode", 4.0F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);

        if (this.explosionSize >= 2.0F && this.isSmoking) {
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D);
        } else {
            this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D);
        }

        if (this.isSmoking) {
            for (BlockPos blockpos : this.affectedBlockPositions) {
                Block block = this.worldObj.getBlockState(blockpos).getBlock();

                if (spawnParticles) {
                    double d0 = (float) blockpos.getX() + this.worldObj.rand.nextFloat();
                    double d1 = (float) blockpos.getY() + this.worldObj.rand.nextFloat();
                    double d2 = (float) blockpos.getZ() + this.worldObj.rand.nextFloat();
                    double d3 = d0 - this.explosionX;
                    double d4 = d1 - this.explosionY;
                    double d5 = d2 - this.explosionZ;
                    double d6 = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / (double) this.explosionSize + 0.1D);
                    d7 = d7 * (double) (this.worldObj.rand.nextFloat() * this.worldObj.rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.explosionX) / 2.0D, (d1 + this.explosionY) / 2.0D, (d2 + this.explosionZ) / 2.0D, d3, d4, d5);
                    this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5);
                }

                if (block.getMaterial() != Material.air) {
                    if (block.canDropFromExplosion(this)) {
                        block.dropBlockAsItemWithChance(this.worldObj, blockpos, this.worldObj.getBlockState(blockpos), 1.0F / this.explosionSize, 0);
                    }

                    this.worldObj.setBlockState(blockpos, Blocks.air.getDefaultState(), 3);
                    block.onBlockDestroyedByExplosion(this.worldObj, blockpos, this);
                }
            }
        }

        if (this.isFlaming) {
            for (BlockPos blockpos1 : this.affectedBlockPositions) {
                if (this.worldObj.getBlockState(blockpos1).getBlock().getMaterial() == Material.air && this.worldObj.getBlockState(blockpos1.down()).getBlock().isFullBlock() && this.explosionRNG.nextInt(3) == 0) {
                    this.worldObj.setBlockState(blockpos1, Blocks.fire.getDefaultState());
                }
            }
        }
    }

    public Map<EntityPlayer, Vec3> getPlayerKnockbackMap() {
        return this.playerKnockbackMap;
    }

    public EntityLivingBase getExplosivePlacedBy() {
        return this.exploder == null ? null : (this.exploder instanceof EntityTNTPrimed ? ((EntityTNTPrimed) this.exploder).getTntPlacedBy() : (this.exploder instanceof EntityLivingBase ? (EntityLivingBase) this.exploder : null));
    }

    public void clearAffectedBlockPositions() {
        this.affectedBlockPositions.clear();
    }

    public List<BlockPos> getAffectedBlockPositions() {
        return this.affectedBlockPositions;
    }
}
