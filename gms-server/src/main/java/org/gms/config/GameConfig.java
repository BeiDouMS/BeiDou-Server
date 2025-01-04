package org.gms.config;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.gms.dao.entity.GameConfigDO;
import org.gms.manager.ServerManager;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.server.life.MonsterInformationProvider;
import org.gms.service.ConfigService;
import org.gms.util.Pair;

import java.util.List;
import java.util.function.Function;

/**
 * 北斗动态参数计划，结构
 * {"world":{"0":{"server_message":{"clazz":"java.lang.String","value":"Welcome to Scania!"},"exp_rate":{"clazz":"java.lang.Float","value":"1.0"}}},"server":{"global":{"WORLDS":{"clazz":"java.lang.Integer","value":"1"}},"npc":{"NPCS_SCRIPTABLE":{"clazz":"java.util.Map","value":"{9001105:\"Rescue Gaga!\"}"}}}}
 */
public class GameConfig {
    private static final GameConfig config = new GameConfig();
    private final JSONObject properties = new JSONObject();

    private GameConfig() {
        ConfigService configService = ServerManager.getApplicationContext().getBean(ConfigService.class);
        List<GameConfigDO> gameConfigDOS = configService.loadGameConfigs();
        gameConfigDOS.forEach(gameConfigDO -> add(this, gameConfigDO));
    }

    public static void add(GameConfigDO gameConfigDO) {
        add(config, gameConfigDO);
    }

    private static void add(GameConfig config, GameConfigDO gameConfigDO) {
        JSONObject typeProp = config.properties.getJSONObject(gameConfigDO.getConfigType());
        if (typeProp == null) {
            typeProp = new JSONObject();
            config.properties.put(gameConfigDO.getConfigType(), typeProp);
        }
        JSONObject subProp = typeProp.getJSONObject(gameConfigDO.getConfigSubType());
        if (subProp == null) {
            subProp = new JSONObject();
            typeProp.put(gameConfigDO.getConfigSubType(), subProp);
        }
        JSONObject valueProp = subProp.getJSONObject(gameConfigDO.getConfigCode());
        if (valueProp == null) {
            valueProp = new JSONObject();
            subProp.put(gameConfigDO.getConfigCode(), valueProp);
        }
        valueProp.put("value", gameConfigDO.getConfigValue());
        valueProp.put("clazz", gameConfigDO.getConfigClazz());
    }

    public static void remove(GameConfigDO gameConfigDO) {
        JSONObject typeProp = config.properties.getJSONObject(gameConfigDO.getConfigType());
        if (typeProp == null) {
            return;
        }
        JSONObject subProp = typeProp.getJSONObject(gameConfigDO.getConfigSubType());
        if (subProp == null) {
            return;
        }
        subProp.remove(gameConfigDO.getConfigCode());
        if (subProp.isEmpty()) {
            typeProp.remove(gameConfigDO.getConfigSubType());
        }
        if (typeProp.isEmpty()) {
            config.properties.remove(gameConfigDO.getConfigType());
        }
    }

    public static void update(GameConfigDO gameConfigDO) {
        JSONObject valueProp = getValueProp(gameConfigDO.getConfigType(), gameConfigDO.getConfigSubType(), gameConfigDO.getConfigCode());
        if (valueProp == null) {
            add(gameConfigDO);
            return;
        }
        valueProp.put("value", gameConfigDO.getConfigValue());

        // 手动重载不能重载的部分
        if ("world".equals(gameConfigDO.getConfigType())) {
            int index = Integer.parseInt(gameConfigDO.getConfigSubType());
            World world = Server.getInstance().getWorld(index);
            switch (gameConfigDO.getConfigCode()) {
                case "exp_rate":
                    world.setExpRate(Float.parseFloat(gameConfigDO.getConfigValue()));
                    break;
                case "meso_rate":
                    world.setMesoRate(Float.parseFloat(gameConfigDO.getConfigValue()));
                    break;
                case "drop_rate":
                    world.setDropRate(Float.parseFloat(gameConfigDO.getConfigValue()));
                    break;
                case "boss_drop_rate":
                    world.setBossDropRate(Float.parseFloat(gameConfigDO.getConfigValue()));
                    break;
                case "quest_rate":
                    world.setQuestRate(Float.parseFloat(gameConfigDO.getConfigValue()));
                    break;
                case "travel_rate":
                    world.setTravelRate(Float.parseFloat(gameConfigDO.getConfigValue()));
                    break;
                case "fishing_rate":
                    world.setFishingRate(Float.parseFloat(gameConfigDO.getConfigValue()));
                    break;
                case "server_message":
                    world.setServerMessage(GameConfig.getWorldString(index, "server_message"));
                    break;
                case "event_message":
                    world.setEventMessage(GameConfig.getWorldString(index, "event_message"));
                    break;
                case "recommend_message":
                    Server.getInstance().worldRecommendedList().set(index, new Pair<>(index, GameConfig.getWorldString(index, "recommend_message")));
                    break;
                case "flag":
                    world.setFlag(GameConfig.getWorldByte(index, "flag"));
                    break;
            }
        }
        // 重载其余部分
        switch (gameConfigDO.getConfigCode()){
            case "allow_steal_quest_item":
                MonsterInformationProvider.getInstance().clearDrops();
                break;
        }
    }

    public static Object getObject(String key) {
        return get(key, null);
    }

    public static <T> T get(String key) {
        return get(key, null);
    }

    public static <T> T get(String key, T defaultValue) {
        for (String type : config.properties.keySet()) {
            T obj = get(type, key, null);
            if (obj != null) {
                return obj;
            }
        }
        return defaultValue;
    }

    public static <T> T get(String type, String key) {
        return get(type, key, null);
    }

    public static <T> T get(String type, String key, T defaultVal) {
        JSONObject valueProp = getValueProp(type, key);
        if (valueProp == null) {
            return defaultVal;
        }
        T t = getValue(valueProp);
        return t == null ? defaultVal : t;
    }

    public static <T> T get(String type, String subType, String key) {
        return get(type, subType, key, null);
    }

    public static <T> T get(String type, String subType, String key, T defaultVal) {
        JSONObject valueProp = getValueProp(type, subType, key);
        if (valueProp == null) {
            return defaultVal;
        }
        T t = getValue(valueProp);
        return t == null ? defaultVal : t;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getValue(JSONObject valueProp) {
        String clazz = valueProp.getString("clazz");
        Class<?> clz;
        try {
            clz = Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            return (T) valueProp.getObject("value", clz);
        } catch (JSONException e) {
            return (T) JSONObject.parseObject(valueProp.getString("value"), clz);
        }
    }

    /* -------------------- 以上根据数据库的clazz类型获取，以下根据传入的类型获取 -------------------- */

    public static JSONObject getValueProp(String type, String subType, String key) {
        JSONObject typeProp = config.properties.getJSONObject(type);
        if (typeProp == null) {
            return null;
        }
        JSONObject subProp = typeProp.getJSONObject(subType);
        if (subProp == null) {
            return null;
        }
        return subProp.getJSONObject(key.toLowerCase());
    }

    public static JSONObject getValueProp(String type, String key) {
        JSONObject typeProp = config.properties.getJSONObject(type);
        if (typeProp == null) {
            return null;
        }
        for (String subType : typeProp.keySet()) {
            JSONObject subProp = typeProp.getJSONObject(subType);
            if (subProp == null) {
                continue;
            }
            JSONObject valueProp = subProp.getJSONObject(key.toLowerCase());
            if (valueProp != null) {
                return valueProp;
            }
        }
        return null;
    }

    public static Integer getInteger(String key) {
        return getValue(key, null, valueProp -> valueProp.getInteger("value"));
    }

    public static int getIntValue(String key) {
        return getValue(key, 0, valueProp -> valueProp.getIntValue("value"));
    }

    public static Long getLong(String key) {
        return getValue(key, null, valueProp -> valueProp.getLong("value"));
    }

    public static long getLongValue(String key) {
        return getValue(key, 0L, valueProp -> valueProp.getLongValue("value"));
    }

    public static Short getShort(String key) {
        return getValue(key, null, valueProp -> valueProp.getShort("value"));
    }

    public static short getShortValue(String key) {
        return getValue(key, (short) 0, valueProp -> valueProp.getShortValue("value"));
    }

    public static Byte getByte(String key) {
        return getValue(key, null, valueProp -> valueProp.getByte("value"));
    }

    public static byte getByteValue(String key) {
        return getValue(key, (byte) 0, valueProp -> valueProp.getByteValue("value"));
    }

    public static float getFloat(String key) {
        return getValue(key, null, valueProp -> valueProp.getFloat("value"));
    }

    public static float getFloatValue(String key) {
        return getValue(key, 0F, valueProp -> valueProp.getFloatValue("value"));
    }

    public static Double getDouble(String key) {
        return getValue(key, null, valueProp -> valueProp.getDouble("value"));
    }

    public static double getDoubleValue(String key) {
        return getValue(key, 0D, valueProp -> valueProp.getDoubleValue("value"));
    }

    public static Boolean getBoolean(String key) {
        return getValue(key, null, valueProp -> valueProp.getBoolean("value"));
    }

    public static boolean getBooleanValue(String key) {
        return getValue(key, false, valueProp -> valueProp.getBooleanValue("value"));
    }

    public static String getString(String key) {
        return getValue(key, null, valueProp -> valueProp.getString("value"));
    }

    public static String getStringValue(String key) {
        return getValue(key, "", valueProp -> valueProp.getString("value") == null ? "" : valueProp.getString("value"));
    }

    public static <T> T getObject(String key, Class<T> clz) {
        return getValue(key, null, valueProp -> {
            try {
                return valueProp.getObject("value", clz);
            } catch (JSONException e) {
                return JSONObject.parseObject(valueProp.getString("value"), clz);
            }
        });
    }

    private static <T> T getValue(String key, T defaultVal, Function<JSONObject, T> mapper) {
        for (String type : config.properties.keySet()) {
            JSONObject valueProp = getValueProp(type, key);
            if (valueProp != null) {
                return mapper.apply(valueProp);
            }
        }
        return defaultVal;
    }

    /* -------------------- 以下根据参数大类获取，可以避免同一个参数，不同大区的场景获取错误 -------------------- */
    public static <T> T getWorld(int worldId, String key) {
        return get("world", String.valueOf(worldId), key);
    }

    public static <T> T getServer(String key) {
        return get("server", key);
    }

    public static int getWorldInt(int worldId, String key) {
        JSONObject valueProp = getValueProp("world", String.valueOf(worldId), key);
        if (valueProp == null) {
            return 0;
        }
        return valueProp.getIntValue("value");
    }

    public static int getServerInt(String key) {
        JSONObject valueProp = getValueProp("server", key);
        if (valueProp == null) {
            return 0;
        }
        return valueProp.getIntValue("value");
    }

    public static byte getWorldByte(int worldId, String key) {
        JSONObject valueProp = getValueProp("world", String.valueOf(worldId), key);
        if (valueProp == null) {
            return (byte) 0;
        }
        return valueProp.getByteValue("value");
    }

    public static byte getServerByte(String key) {
        JSONObject valueProp = getValueProp("server", key);
        if (valueProp == null) {
            return (byte) 0;
        }
        return valueProp.getByteValue("value");
    }

    public static long getWorldLong(int worldId, String key) {
        JSONObject valueProp = getValueProp("world", String.valueOf(worldId), key);
        if (valueProp == null) {
            return 0L;
        }
        return valueProp.getLongValue("value");
    }

    public static long getServerLong(String key) {
        JSONObject valueProp = getValueProp("server", key);
        if (valueProp == null) {
            return 0L;
        }
        return valueProp.getLongValue("value");
    }

    public static short getWorldShort(int worldId, String key) {
        JSONObject valueProp = getValueProp("world", String.valueOf(worldId), key);
        if (valueProp == null) {
            return (short) 0;
        }
        return valueProp.getShortValue("value");
    }

    public static short getServerShort(String key) {
        JSONObject valueProp = getValueProp("server", key);
        if (valueProp == null) {
            return (short) 0;
        }
        return valueProp.getShortValue("value");
    }

    public static float getWorldFloat(int worldId, String key) {
        JSONObject valueProp = getValueProp("world", String.valueOf(worldId), key);
        if (valueProp == null) {
            return 0F;
        }
        return valueProp.getFloatValue("value");
    }

    public static float getServerFloat(String key) {
        JSONObject valueProp = getValueProp("server", key);
        if (valueProp == null) {
            return 0F;
        }
        return valueProp.getFloatValue("value");
    }

    public static double getWorldDouble(int worldId, String key) {
        JSONObject valueProp = getValueProp("world", String.valueOf(worldId), key);
        if (valueProp == null) {
            return 0D;
        }
        return valueProp.getDoubleValue("value");
    }

    public static double getServerDouble(String key) {
        JSONObject valueProp = getValueProp("server", key);
        if (valueProp == null) {
            return 0D;
        }
        return valueProp.getDoubleValue("value");
    }

    public static String getWorldString(int worldId, String key) {
        JSONObject valueProp = getValueProp("world", String.valueOf(worldId), key);
        if (valueProp == null) {
            return "";
        }
        return valueProp.getString("value");
    }

    public static String getServerString(String key) {
        JSONObject valueProp = getValueProp("server", key);
        if (valueProp == null) {
            return "";
        }
        return valueProp.getString("value");
    }

    public static boolean getWorldBoolean(int worldId, String key) {
        JSONObject valueProp = getValueProp("world", String.valueOf(worldId), key);
        if (valueProp == null) {
            return false;
        }
        return valueProp.getBooleanValue("value");
    }

    public static boolean getServerBoolean(String key) {
        JSONObject valueProp = getValueProp("server", key);
        if (valueProp == null) {
            return false;
        }
        return valueProp.getBooleanValue("value");
    }

    public static <T> T getWorldObject(int worldId, String key, Class<T> clz) {
        return getValue(false, String.valueOf(worldId), key, clz);
    }

    public static <T> T getWorldObject(int worldId, String key, T defaultVal) {
        T t = getValue(false, String.valueOf(worldId), key, defaultVal.getClass());
        return t == null ? defaultVal : t;
    }

    public static <T> T getServerObject(String key, Class<T> clz) {
        return getValue(true, null, key, clz);
    }

    public static <T> T getServerObject(String key, T defaultVal) {
        T t = getValue(true, null, key, defaultVal.getClass());
        return t == null ? defaultVal : t;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getValue(boolean isServer, String subType, String key, Class<?> clz) {
        JSONObject valueProp;
        if (isServer) {
            valueProp = getValueProp("server", key);
        } else {
            valueProp = getValueProp("world", subType, key);
        }
        if (valueProp == null) {
            return null;
        }
        try {
            return (T) valueProp.getObject("value", clz);
        } catch (JSONException e) {
            return (T) JSONObject.parseObject(valueProp.getString("value"), clz);
        }
    }

    public static <T> T getWorldObject(int worldId, String key, TypeReference<T> type) {
        JSONObject valueProp = getValueProp("world", String.valueOf(worldId), key);
        if (valueProp == null) {
            return null;
        }
        try {
            return valueProp.getObject("value", type);
        } catch (JSONException e) {
            return JSONObject.parseObject(valueProp.getString("value"), type);
        }
    }

    public static <T> T getServerObject(String key, TypeReference<T> type) {
        JSONObject valueProp = getValueProp("server", key);
        if (valueProp == null) {
            return null;
        }
        try {
            return valueProp.getObject("value", type);
        } catch (JSONException e) {
            return JSONObject.parseObject(valueProp.getString("value"), type);
        }
    }

    public static JSONObject getConfig() {
        return config.properties;
    }
}
