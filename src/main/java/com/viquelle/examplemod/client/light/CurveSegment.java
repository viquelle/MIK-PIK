package com.viquelle.examplemod.client.light;

public record CurveSegment(
    int startTick,
    int endTick,
    float from,
    float to,
    LightCurve curve
) {}