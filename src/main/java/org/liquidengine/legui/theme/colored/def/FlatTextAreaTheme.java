package org.liquidengine.legui.theme.colored.def;

import org.liquidengine.legui.component.TextArea;
import org.liquidengine.legui.component.optional.align.HorizontalAlign;
import org.liquidengine.legui.style.color.ColorUtil;
import org.liquidengine.legui.style.shadow.Shadow;
import org.liquidengine.legui.theme.colored.FlatColoredTheme.FlatColoredThemeSettings;

/**
 * Dark TextArea Theme for all text areas. Used to make text area dark.
 *
 * @param <T> {@link TextArea} subclasses.
 */
public class FlatTextAreaTheme<T extends TextArea> extends FlatComponentTheme<T> {

    private FlatColoredThemeSettings settings;

    public FlatTextAreaTheme(FlatColoredThemeSettings settings) {
        super(settings);
        this.settings = settings;
    }

    /**
     * Used to apply theme only for component and not apply for child components.
     *
     * @param component component to apply theme.
     */
    @Override
    public void apply(T component) {
        super.apply(component);
        if (settings.shadowColor()== null || settings.shadowColor().length() > 0.00001f) {
            component.getStyle().setShadow(new Shadow(-4, 4, 17, -7, settings.shadowColor()));
        } else {
            component.getStyle().setShadow(null);
        }
        component.getFocusedStyle().getBackground()
            .setColor(settings.backgroundColor().mul(3).add(ColorUtil.oppositeBlackOrWhite(settings.backgroundColor())).div(4));
        component.getTextState().setTextColor(ColorUtil.oppositeBlackOrWhite(settings.backgroundColor()));
        component.getTextState().setHorizontalAlign(HorizontalAlign.LEFT);
        component.getTextState().setHighlightColor(settings.strokeColor());
        component.getStyle().getBackground().setColor(settings.backgroundColor());
    }
}
