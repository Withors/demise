package net.minecraft.world.biome;

import com.google.common.collect.Lists;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

public class BiomeGenMutated extends BiomeGenBase {
    protected final BiomeGenBase baseBiome;

    public BiomeGenMutated(int id, BiomeGenBase biome) {
        super(id);
        this.baseBiome = biome;
        this.func_150557_a(biome.color, true);
        this.biomeName = biome.biomeName + " M";
        this.topBlock = biome.topBlock;
        this.fillerBlock = biome.fillerBlock;
        this.fillerBlockMetadata = biome.fillerBlockMetadata;
        this.minHeight = biome.minHeight;
        this.maxHeight = biome.maxHeight;
        this.temperature = biome.temperature;
        this.rainfall = biome.rainfall;
        this.waterColorMultiplier = biome.waterColorMultiplier;
        this.enableSnow = biome.enableSnow;
        this.enableRain = biome.enableRain;
        this.spawnableCreatureList = Lists.newArrayList(biome.spawnableCreatureList);
        this.spawnableMonsterList = Lists.newArrayList(biome.spawnableMonsterList);
        this.spawnableCaveCreatureList = Lists.newArrayList(biome.spawnableCaveCreatureList);
        this.spawnableWaterCreatureList = Lists.newArrayList(biome.spawnableWaterCreatureList);
        this.temperature = biome.temperature;
        this.rainfall = biome.rainfall;
        this.minHeight = biome.minHeight + 0.1F;
        this.maxHeight = biome.maxHeight + 0.2F;
    }

    public void decorate(World worldIn, Random rand, BlockPos pos) {
        this.baseBiome.theBiomeDecorator.decorate(worldIn, rand, this, pos);
    }

    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        this.baseBiome.genTerrainBlocks(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
    }

    public float getSpawningChance() {
        return this.baseBiome.getSpawningChance();
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand) {
        return this.baseBiome.genBigTreeChance(rand);
    }

    public int getFoliageColorAtPos(BlockPos pos) {
        return this.baseBiome.getFoliageColorAtPos(pos);
    }

    public int getGrassColorAtPos(BlockPos pos) {
        return this.baseBiome.getGrassColorAtPos(pos);
    }

    public Class<? extends BiomeGenBase> getBiomeClass() {
        return this.baseBiome.getBiomeClass();
    }

    public boolean isEqualTo(BiomeGenBase biome) {
        return this.baseBiome.isEqualTo(biome);
    }

    public BiomeGenBase.TempCategory getTempCategory() {
        return this.baseBiome.getTempCategory();
    }
}
