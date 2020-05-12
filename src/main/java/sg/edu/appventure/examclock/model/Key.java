package sg.edu.appventure.examclock.model;

import net.minidev.json.JSONObject;
import sg.edu.appventure.examclock.connection.Base64;
import sg.edu.appventure.examclock.connection.Encryption;

import java.util.UUID;

public class Key {
    public final String id;
    public byte[] key;
    public final KeyType type;

    public Key() {
        this(UUID.randomUUID().toString(), Encryption.createKey(), KeyType.READ_ONLY);
    }

    public Key(KeyType keyType) {
        this(UUID.randomUUID().toString(), Encryption.createKey(), keyType);
    }

    public Key(String id, byte[] key, KeyType type) {
        this.id = id;
        this.key = key;
        this.type = type;
    }

    public enum KeyType {
        ADMIN, TOILET, READ_ONLY
    }

    public JSONObject toJsonObject() {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("key", new String(Base64.encode(key)));
        object.put("type", type.toString());
        return object;
    }

    public static Key fromJsonObject(JSONObject object) {
        return new Key(object.getAsString("id"),
                Base64.decode(object.getAsString("key").toCharArray()),
                KeyType.valueOf(object.getAsString("type")));
    }
}
