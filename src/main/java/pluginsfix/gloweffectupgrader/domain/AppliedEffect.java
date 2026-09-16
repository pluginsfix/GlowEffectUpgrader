package pluginsfix.gloweffectupgrader.domain;

import java.util.Base64;
import java.util.Objects;

public record AppliedEffect(
        String effectId,
        String typeName,
        int level,
        String displayName
) {
    public String serialize() {
        String encodedName = Base64.getEncoder().encodeToString(displayName.getBytes());
        return effectId + ":" + typeName + ":" + level + ":" + encodedName;
    }

    public static AppliedEffect deserialize(String raw) {
        String[] parts = raw.split(":", 4);
        if (parts.length < 4) {
            return null;
        }
        String id = parts[0];
        String type = parts[1];
        int level = Integer.parseInt(parts[2]);
        String displayName = new String(Base64.getDecoder().decode(parts[3]));
        return new AppliedEffect(id, type, level, displayName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppliedEffect that)) return false;
        return Objects.equals(effectId, that.effectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(effectId);
    }
}
