package net.minecraft.world.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;

import java.io.File;

public class SaveHandlerMP implements ISaveHandler {
    public WorldInfo loadWorldInfo() {
        return null;
    }

    public void checkSessionLock() {
    }

    public IChunkLoader getChunkLoader(WorldProvider provider) {
        return null;
    }

    public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
    }

    public void saveWorldInfo(WorldInfo worldInformation) {
    }

    public IPlayerFileData getPlayerNBTManager() {
        return null;
    }

    public void flush() {
    }

    public File getMapFileFromName(String mapName) {
        return null;
    }

    public String getWorldDirectoryName() {
        return "none";
    }

    public File getWorldDirectory() {
        return null;
    }
}
