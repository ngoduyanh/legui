package com.spinyowl.legui.event;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.system.context.GLFWContext;


public class WindowRefreshEvent<T extends Component> extends Event<T> {

  public WindowRefreshEvent(T component, GLFWContext context, Frame frame) {
    super(component, context, frame);
  }
}
