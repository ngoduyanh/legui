package com.spinyowl.legui.component.event.selectbox;

import com.spinyowl.legui.component.Frame;
import com.spinyowl.legui.component.SelectBox;
import com.spinyowl.legui.event.Event;
import com.spinyowl.legui.system.context.Context;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class SelectBoxChangeSelectionEvent<T> extends Event<SelectBox<T>> {

  private final T oldValue;
  private final T newValue;

  public SelectBoxChangeSelectionEvent(SelectBox<T> component, Context context, Frame frame,
                                       T oldValue, T newValue) {
    super(component, context, frame);
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  /**
   * Returns old value.
   *
   * @return old value.
   */
  public T getOldValue() {
    return oldValue;
  }

  /**
   * Returns new value.
   *
   * @return new value.
   */
  public T getNewValue() {
    return newValue;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("oldValue", oldValue)
        .append("newValue", newValue)
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

    SelectBoxChangeSelectionEvent<?> that = (SelectBoxChangeSelectionEvent<?>) o;

    return new EqualsBuilder()
        .appendSuper(super.equals(o))
        .append(oldValue, that.oldValue)
        .append(newValue, that.newValue)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .appendSuper(super.hashCode())
        .append(oldValue)
        .append(newValue)
        .toHashCode();
  }
}
