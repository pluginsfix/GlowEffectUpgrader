package pluginsfix.gloweffectupgrader.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppliedEffectTest {

    @Test
    void testSerializationAndDeserialization() {
        AppliedEffect original = new AppliedEffect("speed", "SPEED", 2, "<aqua>Скорость II");
        String serialized = original.serialize();

        AppliedEffect deserialized = AppliedEffect.deserialize(serialized);

        assertThat(deserialized).isNotNull();
        assertThat(deserialized.effectId()).isEqualTo("speed");
        assertThat(deserialized.typeName()).isEqualTo("SPEED");
        assertThat(deserialized.level()).isEqualTo(2);
        assertThat(deserialized.displayName()).isEqualTo("<aqua>Скорость II");
        assertThat(deserialized).isEqualTo(original);
    }
}
