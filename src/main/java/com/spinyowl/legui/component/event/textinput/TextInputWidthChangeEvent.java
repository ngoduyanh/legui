package com.spinyowl.legui.component.event.textinput;

import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.component.TextInput;
import com.spinyowl.legui.event.Event;
import com.spinyowl.legui.system.context.Context;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class TextInputWidthChangeEvent extends Event<TextInput> {

  private final float width;

  public TextInputWidthChangeEvent(TextInput targetComponent, Context context, Frame frame,
                                   float width) {
    super(targetComponent, context, frame);
    this.width = width;
  }

  public float getWidth() {
    return width;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("width", width)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    TextInputWidthChangeEvent that = (TextInputWidthChangeEvent) o;
    return Float.compare(that.width, width) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), width);
  }
}
