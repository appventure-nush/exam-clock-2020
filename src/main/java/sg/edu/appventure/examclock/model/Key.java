package sg.edu.appventure.examclock.model;

import sg.edu.appventure.examclock.connection.Encryption;

import java.util.UUID;

public class Key {
    public enum KeyType {
        ADMIN, TOILET, READ_ONLY
    }

    public final String id;
    public final byte[] key;
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
}
