package com.viquelle.mikpik.client.coloredlights;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;

public final class ColoredLightBuffer {
    public static final int MAX_LIGHTS = 256;
    private static final List<ActiveLight> LIGHTS = new ArrayList<>(MAX_LIGHTS);
    private static final ActiveLight[] LIGHTS_BUF = new ActiveLight[MAX_LIGHTS];
    private static final double[] DIST_BUF = new double[MAX_LIGHTS];
    private static int size = 0;

    private static final LongOpenHashSet PREVIOUS_LIGHTS = new LongOpenHashSet();
    private static final LongOpenHashSet CURRENT_LIGHTS = new LongOpenHashSet();

    private ColoredLightBuffer() {}

    public static void clear() {
        LIGHTS.clear();
        size = 0;
    }

    public static void frame() {
        PREVIOUS_LIGHTS.clear();
        PREVIOUS_LIGHTS.addAll(CURRENT_LIGHTS);
        CURRENT_LIGHTS.clear();
    }

    public static boolean wasVisibleLastFrame(long id) {
        return PREVIOUS_LIGHTS.contains(id);
    }

    public static void addWithDistance(ActiveLight light, double distSq) {
        CURRENT_LIGHTS.add(light.id());

        if (size < MAX_LIGHTS) {
            LIGHTS_BUF[size] = light;
            DIST_BUF[size] = distSq;
            size++;
            siftUp(size - 1);
            return;
        }

        // Если новый свет дальше, чем самый дальний, то игнорируем его
        if (distSq >= DIST_BUF[0]) {
            CURRENT_LIGHTS.remove(light.id());
            return;
        }

        ActiveLight old = LIGHTS_BUF[0];
        CURRENT_LIGHTS.remove(old.id());

        LIGHTS_BUF[0] = light;
        DIST_BUF[0] = distSq;
        siftDown(0);
    }

    private static void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) >>> 1;
            if (DIST_BUF[index] <= DIST_BUF[parent]) break;

            swap(index, parent);
            index = parent;
        }
    }

    private static void siftDown(int index) {
        int half = size >>> 1;
        while (index < half) {
            int child = (index << 1) + 1;
            int right = child + 1;

            int largest = index;
            if (DIST_BUF[child] > DIST_BUF[largest]) largest = child;
            if (right < size && DIST_BUF[right] > DIST_BUF[largest]) largest = right;

            if (largest == index) break;

            swap(index, largest);
            index = largest;
        }
    }

    private static void swap(int i, int j) {
        double tempD = DIST_BUF[i];
        DIST_BUF[i] = DIST_BUF[j];
        DIST_BUF[j] = tempD;

        ActiveLight tempL = LIGHTS_BUF[i];
        LIGHTS_BUF[i] = LIGHTS_BUF[j];
        LIGHTS_BUF[j] = tempL;
    }

    public static void upload() {
        LIGHTS.clear();
        for (int i = 0; i < size; i++) {
            LIGHTS.add(LIGHTS_BUF[i]);
        }
    }

    public static List<ActiveLight> get() {
        return LIGHTS;
    }

    public static int size() {
        return size;
    }
}