package com.spinyowl.legui.event;

import com.spinyowl.legui.component.Component;
import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.system.context.Context;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


public class WindowIconifyEvent<T extends Component> extends Event<T> {

  private final boolean iconified;

  public WindowIconifyEvent(T component, Context context, Frame frame, boolean iconified) {
    super(component, context, frame);
    this.iconified = iconified;
  }

  public boolean isIconified() {
    return iconified;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
        .append("targetComponent", getTargetComponent().getClass().getSimpleName())
        .append("iconified", iconified)
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

    WindowIconifyEvent<?> that = (WindowIconifyEvent<?>) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(o))
        .append(iconified, that.iconified)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .appendSuper(super.hashCode())
        .append(iconified)
        .toHashCode();
  }
}
