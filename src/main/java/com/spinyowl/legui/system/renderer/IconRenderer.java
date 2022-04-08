package com.spinyowl.legui.system.renderer;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.icon.Icon;
import com.spinyowl.legui.system.context.Context;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Icon renderer base.
 */
public abstract class IconRenderer<I extends Icon> {

  private AtomicBoolean initialized = new AtomicBoolean(false);


  public void render(I icon, Component component, Context context) {
    if (!initialized.getAndSet(true)) {
      initialize();
    }
    renderIcon(icon, component, context);
  }

  public abstract void renderIcon(I icon, Component component, Context context);

  public void initialize() {
    // this method should be reimplemented if need to initialize some data in renderer before it can be used
    // called only once
  }

  public void destroy() {
    // this method should be reimplemented if need to destroy some data in renderer before exit
  }
}
