package com.viquelle.examplemod.client.light.modifiers;

import com.viquelle.examplemod.client.light.AbstractLight;

public interface LightModifier {
    enum Type{
        ADDITIVE,
        MULTIPLICATIVE
    }

    Type getType();
    float getDuration();

    /**
     *
     * @param light ссылка на  свет
     * @param deltaTIme время кадра
     * @param progress Прогресс модификатора(0т 0.0 до 0.1)
     * @return
     */
    float getValue(AbstractLight<?> light, float deltaTIme, float progress);

    default void onExpire(AbstractLight<?> light) {}
}
