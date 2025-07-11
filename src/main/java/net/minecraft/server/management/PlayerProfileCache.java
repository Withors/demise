package net.minecraft.server.management;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.*;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerProfileCache {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private final Map<String, PlayerProfileCache.ProfileEntry> usernameToProfileEntryMap = Maps.newHashMap();
    private final Map<UUID, PlayerProfileCache.ProfileEntry> uuidToProfileEntryMap = Maps.newHashMap();
    private final LinkedList<GameProfile> gameProfiles = Lists.newLinkedList();
    private final MinecraftServer mcServer;
    protected final Gson gson;
    private final File usercacheFile;
    private static final ParameterizedType TYPE = new ParameterizedType() {
        public Type @NotNull [] getActualTypeArguments() {
            return new Type[]{PlayerProfileCache.ProfileEntry.class};
        }

        public @NotNull Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }
    };

    public PlayerProfileCache(MinecraftServer server, File cacheFile) {
        this.mcServer = server;
        this.usercacheFile = cacheFile;
        GsonBuilder gsonbuilder = new GsonBuilder();
        gsonbuilder.registerTypeHierarchyAdapter(PlayerProfileCache.ProfileEntry.class, new PlayerProfileCache.Serializer());
        this.gson = gsonbuilder.create();
        this.load();
    }

    private static GameProfile getGameProfile(MinecraftServer server, String username) {
        final GameProfile[] agameprofile = new GameProfile[1];
        ProfileLookupCallback profilelookupcallback = new ProfileLookupCallback() {
            public void onProfileLookupSucceeded(GameProfile p_onProfileLookupSucceeded_1_) {
                agameprofile[0] = p_onProfileLookupSucceeded_1_;
            }

            public void onProfileLookupFailed(GameProfile p_onProfileLookupFailed_1_, Exception p_onProfileLookupFailed_2_) {
                agameprofile[0] = null;
            }
        };
        server.getGameProfileRepository().findProfilesByNames(new String[]{username}, Agent.MINECRAFT, profilelookupcallback);

        if (!server.isServerInOnlineMode() && agameprofile[0] == null) {
            UUID uuid = EntityPlayer.getUUID(new GameProfile(null, username));
            GameProfile gameprofile = new GameProfile(uuid, username);
            profilelookupcallback.onProfileLookupSucceeded(gameprofile);
        }

        return agameprofile[0];
    }

    public void addEntry(GameProfile gameProfile) {
        this.addEntry(gameProfile, null);
    }

    private void addEntry(GameProfile gameProfile, Date expirationDate) {
        UUID uuid = gameProfile.getId();

        if (expirationDate == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(2, 1);
            expirationDate = calendar.getTime();
        }

        String s = gameProfile.getName().toLowerCase(Locale.ROOT);
        PlayerProfileCache.ProfileEntry playerprofilecache$profileentry = new ProfileEntry(gameProfile, expirationDate);

        if (this.uuidToProfileEntryMap.containsKey(uuid)) {
            PlayerProfileCache.ProfileEntry playerprofilecache$profileentry1 = this.uuidToProfileEntryMap.get(uuid);
            this.usernameToProfileEntryMap.remove(playerprofilecache$profileentry1.getGameProfile().getName().toLowerCase(Locale.ROOT));
            this.gameProfiles.remove(gameProfile);
        }

        this.usernameToProfileEntryMap.put(gameProfile.getName().toLowerCase(Locale.ROOT), playerprofilecache$profileentry);
        this.uuidToProfileEntryMap.put(uuid, playerprofilecache$profileentry);
        this.gameProfiles.addFirst(gameProfile);
        this.save();
    }

    public GameProfile getGameProfileForUsername(String username) {
        String s = username.toLowerCase(Locale.ROOT);
        PlayerProfileCache.ProfileEntry playerprofilecache$profileentry = this.usernameToProfileEntryMap.get(s);

        if (playerprofilecache$profileentry != null && (new Date()).getTime() >= playerprofilecache$profileentry.expirationDate.getTime()) {
            this.uuidToProfileEntryMap.remove(playerprofilecache$profileentry.getGameProfile().getId());
            this.usernameToProfileEntryMap.remove(playerprofilecache$profileentry.getGameProfile().getName().toLowerCase(Locale.ROOT));
            this.gameProfiles.remove(playerprofilecache$profileentry.getGameProfile());
            playerprofilecache$profileentry = null;
        }

        if (playerprofilecache$profileentry != null) {
            GameProfile gameprofile = playerprofilecache$profileentry.getGameProfile();
            this.gameProfiles.remove(gameprofile);
            this.gameProfiles.addFirst(gameprofile);
        } else {
            GameProfile gameprofile1 = getGameProfile(this.mcServer, s);

            if (gameprofile1 != null) {
                this.addEntry(gameprofile1);
                playerprofilecache$profileentry = this.usernameToProfileEntryMap.get(s);
            }
        }

        this.save();
        return playerprofilecache$profileentry == null ? null : playerprofilecache$profileentry.getGameProfile();
    }

    public String[] getUsernames() {
        List<String> list = Lists.newArrayList(this.usernameToProfileEntryMap.keySet());
        return list.toArray(new String[0]);
    }

    public GameProfile getProfileByUUID(UUID uuid) {
        PlayerProfileCache.ProfileEntry playerprofilecache$profileentry = this.uuidToProfileEntryMap.get(uuid);
        return playerprofilecache$profileentry == null ? null : playerprofilecache$profileentry.getGameProfile();
    }

    private PlayerProfileCache.ProfileEntry getByUUID(UUID uuid) {
        PlayerProfileCache.ProfileEntry playerprofilecache$profileentry = this.uuidToProfileEntryMap.get(uuid);

        if (playerprofilecache$profileentry != null) {
            GameProfile gameprofile = playerprofilecache$profileentry.getGameProfile();
            this.gameProfiles.remove(gameprofile);
            this.gameProfiles.addFirst(gameprofile);
        }

        return playerprofilecache$profileentry;
    }

    public void load() {
        BufferedReader bufferedreader = null;

        try {
            bufferedreader = Files.newReader(this.usercacheFile, Charsets.UTF_8);
            List<PlayerProfileCache.ProfileEntry> list = this.gson.fromJson(bufferedreader, TYPE);
            this.usernameToProfileEntryMap.clear();
            this.uuidToProfileEntryMap.clear();
            this.gameProfiles.clear();

            for (PlayerProfileCache.ProfileEntry playerprofilecache$profileentry : Lists.reverse(list)) {
                if (playerprofilecache$profileentry != null) {
                    this.addEntry(playerprofilecache$profileentry.getGameProfile(), playerprofilecache$profileentry.getExpirationDate());
                }
            }
        } catch (FileNotFoundException | JsonParseException ignored) {
        } finally {
            IOUtils.closeQuietly(bufferedreader);
        }
    }

    public void save() {
        String s = this.gson.toJson(this.getEntriesWithLimit());
        BufferedWriter bufferedwriter = null;

        try {
            bufferedwriter = Files.newWriter(this.usercacheFile, Charsets.UTF_8);
            bufferedwriter.write(s);
        } catch (IOException ignored) {
        } finally {
            IOUtils.closeQuietly(bufferedwriter);
        }
    }

    private List<PlayerProfileCache.ProfileEntry> getEntriesWithLimit() {
        ArrayList<PlayerProfileCache.ProfileEntry> arraylist = Lists.newArrayList();

        for (GameProfile gameprofile : Lists.newArrayList(Iterators.limit(this.gameProfiles.iterator(), 1000))) {
            PlayerProfileCache.ProfileEntry playerprofilecache$profileentry = this.getByUUID(gameprofile.getId());

            if (playerprofilecache$profileentry != null) {
                arraylist.add(playerprofilecache$profileentry);
            }
        }

        return arraylist;
    }

    static class ProfileEntry {
        private final GameProfile gameProfile;
        private final Date expirationDate;

        private ProfileEntry(GameProfile gameProfileIn, Date expirationDateIn) {
            this.gameProfile = gameProfileIn;
            this.expirationDate = expirationDateIn;
        }

        public GameProfile getGameProfile() {
            return this.gameProfile;
        }

        public Date getExpirationDate() {
            return this.expirationDate;
        }
    }

    class Serializer implements JsonDeserializer<PlayerProfileCache.ProfileEntry>, JsonSerializer<PlayerProfileCache.ProfileEntry> {
        private Serializer() {
        }

        public JsonElement serialize(PlayerProfileCache.ProfileEntry p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("name", p_serialize_1_.getGameProfile().getName());
            UUID uuid = p_serialize_1_.getGameProfile().getId();
            jsonobject.addProperty("uuid", uuid == null ? "" : uuid.toString());
            jsonobject.addProperty("expiresOn", PlayerProfileCache.dateFormat.format(p_serialize_1_.getExpirationDate()));
            return jsonobject;
        }

        public PlayerProfileCache.ProfileEntry deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
            if (p_deserialize_1_.isJsonObject()) {
                JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
                JsonElement jsonelement = jsonobject.get("name");
                JsonElement jsonelement1 = jsonobject.get("uuid");
                JsonElement jsonelement2 = jsonobject.get("expiresOn");

                if (jsonelement != null && jsonelement1 != null) {
                    String s = jsonelement1.getAsString();
                    String s1 = jsonelement.getAsString();
                    Date date = null;

                    if (jsonelement2 != null) {
                        try {
                            date = PlayerProfileCache.dateFormat.parse(jsonelement2.getAsString());
                        } catch (ParseException var14) {
                            date = null;
                        }
                    }

                    if (s1 != null && s != null) {
                        UUID uuid;

                        try {
                            uuid = UUID.fromString(s);
                        } catch (Throwable var13) {
                            return null;
                        }

                        return new ProfileEntry(new GameProfile(uuid, s1), date);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}
