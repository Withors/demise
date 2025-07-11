package net.minecraft.world.biome;

import net.minecraft.util.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WorldChunkManagerHell extends WorldChunkManager {
    private final BiomeGenBase biomeGenerator;
    private final float rainfall;

    public WorldChunkManagerHell(BiomeGenBase p_i45374_1_, float p_i45374_2_) {
        this.biomeGenerator = p_i45374_1_;
        this.rainfall = p_i45374_2_;
    }

    public BiomeGenBase getBiomeGenerator(BlockPos pos) {
        return this.biomeGenerator;
    }

    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomes, int x, int z, int width, int height) {
        if (biomes == null || biomes.length < width * height) {
            biomes = new BiomeGenBase[width * height];
        }

        Arrays.fill(biomes, 0, width * height, this.biomeGenerator);
        return biomes;
    }

    public void getRainfall(float[] listToReuse, int x, int z, int width, int length) {
        if (listToReuse == null || listToReuse.length < width * length) {
            listToReuse = new float[width * length];
        }

        Arrays.fill(listToReuse, 0, width * length, this.rainfall);
    }

    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] oldBiomeList, int x, int z, int width, int depth) {
        if (oldBiomeList == null || oldBiomeList.length < width * depth) {
            oldBiomeList = new BiomeGenBase[width * depth];
        }

        Arrays.fill(oldBiomeList, 0, width * depth, this.biomeGenerator);
        return oldBiomeList;
    }

    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
        return this.loadBlockGeneratorData(listToReuse, x, z, width, length);
    }

    public BlockPos findBiomePosition(int x, int z, int range, List<BiomeGenBase> biomes, Random random) {
        return biomes.contains(this.biomeGenerator) ? new BlockPos(x - range + random.nextInt(range * 2 + 1), 0, z - range + random.nextInt(range * 2 + 1)) : null;
    }

    public boolean areBiomesViable(int p_76940_1_, int p_76940_2_, int p_76940_3_, List<BiomeGenBase> p_76940_4_) {
        return p_76940_4_.contains(this.biomeGenerator);
    }
}
