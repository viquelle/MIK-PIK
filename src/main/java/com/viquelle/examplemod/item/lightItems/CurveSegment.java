package com.viquelle.examplemod.item.lightItems;

public record CurveSegment(
        int startTick,
        int endTick,
        float from,
        float to,
        LightCurve curve
) {}