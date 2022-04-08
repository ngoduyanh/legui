package com.spinyowl.legui.system.handler;

import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.system.context.GLFWContext;
import com.spinyowl.legui.system.event.SystemEvent;


public interface SystemEventHandler<E extends SystemEvent> {

  void handle(E event, Frame frame, GLFWContext context);
}

