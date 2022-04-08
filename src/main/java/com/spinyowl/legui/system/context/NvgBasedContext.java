package com.spinyowl.legui.system.context;

import org.joml.Vector2f;
import org.joml.Vector2i;

public abstract class NvgBasedContext extends Context {
    public abstract long getNanoVGContext();
    public abstract void setNanoVGContext(long ctx);

    public abstract Vector2i getWindowSize();
    public abstract float getPixelRatio();
}
