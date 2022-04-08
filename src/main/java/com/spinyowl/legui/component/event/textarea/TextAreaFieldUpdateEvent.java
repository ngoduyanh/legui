package com.spinyowl.legui.component.event.textarea;

import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.component.TextAreaField;
import com.spinyowl.legui.event.Event;
import com.spinyowl.legui.system.context.GLFWContext;

/**
 * Generated when any key combination is pressed..
 */
public class TextAreaFieldUpdateEvent extends Event<TextAreaField> {

  public TextAreaFieldUpdateEvent(TextAreaField component, GLFWContext context, Frame frame) {
    super(component, context, frame);
  }

}
