package net.minecraft.client.resources;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.optifine.reflect.ReflectorForge;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.Set;

public class DefaultResourcePack implements IResourcePack {
    public static final Set<String> defaultResourceDomains = ImmutableSet.of("minecraft", "realms");
    private final Map<String, File> mapAssets;

    public DefaultResourcePack(Map<String, File> mapAssetsIn) {
        this.mapAssets = mapAssetsIn;
    }

    public InputStream getInputStream(ResourceLocation location) throws IOException {
        InputStream inputstream = this.getResourceStream(location);

        if (inputstream != null) {
            return inputstream;
        } else {
            InputStream inputstream1 = this.getInputStreamAssets(location);

            if (inputstream1 != null) {
                return inputstream1;
            } else {
                throw new FileNotFoundException(location.getResourcePath());
            }
        }
    }

    public InputStream getInputStreamAssets(ResourceLocation location) throws IOException {
        File file1 = this.mapAssets.get(location.toString());
        return file1 != null && file1.isFile() ? new FileInputStream(file1) : null;
    }

    private InputStream getResourceStream(ResourceLocation location) {
        String s = "/assets/" + location.getResourceDomain() + "/" + location.getResourcePath();
        InputStream inputstream = ReflectorForge.getOptiFineResourceStream(s);
        return inputstream != null ? inputstream : DefaultResourcePack.class.getResourceAsStream(s);
    }

    public boolean resourceExists(ResourceLocation location) {
        return this.getResourceStream(location) != null || this.mapAssets.containsKey(location.toString());
    }

    public Set<String> getResourceDomains() {
        return defaultResourceDomains;
    }

    public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer metadataSerializer, String metadataSectionName) {
        try {
            InputStream inputstream = new FileInputStream(this.mapAssets.get("pack.mcmeta"));
            return AbstractResourcePack.readMetadata(metadataSerializer, inputstream, metadataSectionName);
        } catch (RuntimeException | FileNotFoundException var4) {
            return null;
        }
    }

    public BufferedImage getPackImage() throws IOException {
        return TextureUtil.readBufferedImage(DefaultResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("pack.png")).getResourcePath()));
    }

    public String getPackName() {
        return "Default";
    }
}
