package someoneok.kic.utils.overlay;

public class OverlaySegment {
    private final String text;
    private final boolean clickable;
    private final Runnable onClick;
    private final boolean hoverable;
    private final Runnable onHover;
    private final Runnable onHoverEnd;
    private final boolean scrollable;
    private final Runnable onScrollUp;
    private final Runnable onScrollDown;
    private final int customColor;

    public OverlaySegment(String text, boolean clickable, Runnable onClick, boolean hoverable, Runnable onHover, Runnable onHoverEnd) {
        this.text = text;
        this.clickable = clickable;
        this.onClick = onClick;
        this.hoverable = hoverable;
        this.onHover = onHover;
        this.onHoverEnd = onHoverEnd;
        this.scrollable = false;
        this.onScrollUp = null;
        this.onScrollDown = null;
        this.customColor = 0;
    }

    public OverlaySegment(String text, boolean clickable, Runnable onClick, boolean hoverable, Runnable onHover, Runnable onHoverEnd, boolean scrollable, Runnable onScrollUp, Runnable onScrollDown) {
        this.text = text;
        this.clickable = clickable;
        this.onClick = onClick;
        this.hoverable = hoverable;
        this.onHover = onHover;
        this.onHoverEnd = onHoverEnd;
        this.scrollable = scrollable;
        this.onScrollUp = onScrollUp;
        this.onScrollDown = onScrollDown;
        this.customColor = 0;
    }

    public OverlaySegment(String text) {
        this.text = text;
        this.clickable = false;
        this.onClick = null;
        this.hoverable = false;
        this.onHover = null;
        this.onHoverEnd = null;
        this.scrollable = false;
        this.onScrollUp = null;
        this.onScrollDown = null;
        this.customColor = 0;
    }

    public OverlaySegment(String text, boolean scrollable, Runnable onScrollUp, Runnable onScrollDown) {
        this.text = text;
        this.clickable = false;
        this.onClick = null;
        this.hoverable = false;
        this.onHover = null;
        this.onHoverEnd = null;
        this.scrollable = scrollable;
        this.onScrollUp = onScrollUp;
        this.onScrollDown = onScrollDown;
        this.customColor = 0;
    }

    public OverlaySegment(String text, Runnable onClick, boolean hoverable, Runnable onScrollUp, Runnable onScrollDown) {
        this.text = text;
        this.clickable = true;
        this.onClick = onClick;
        this.hoverable = hoverable;
        this.onHover = null;
        this.onHoverEnd = null;
        this.scrollable = true;
        this.onScrollUp = onScrollUp;
        this.onScrollDown = onScrollDown;
        this.customColor = 0;
    }

    public OverlaySegment(String text, Runnable onClick, boolean hoverable) {
        this.text = text;
        this.clickable = true;
        this.onClick = onClick;
        this.hoverable = hoverable;
        this.onHover = null;
        this.onHoverEnd = null;
        this.scrollable = false;
        this.onScrollUp = null;
        this.onScrollDown = null;
        this.customColor = 0;
    }

    public OverlaySegment(String text, boolean clickable, Runnable onClick, boolean hoverable) {
        this.text = text;
        this.clickable = clickable;
        this.onClick = onClick;
        this.hoverable = hoverable;
        this.onHover = null;
        this.onHoverEnd = null;
        this.scrollable = false;
        this.onScrollUp = null;
        this.onScrollDown = null;
        this.customColor = 0;
    }

    public OverlaySegment(String text, Runnable onHover, Runnable onHoverEnd) {
        this.text = text;
        this.clickable = false;
        this.onClick = null;
        this.hoverable = true;
        this.onHover = onHover;
        this.onHoverEnd = onHoverEnd;
        this.scrollable = false;
        this.onScrollUp = null;
        this.onScrollDown = null;
        this.customColor = 0;
    }

    public OverlaySegment(String text, int color) {
        this.text = text;
        this.clickable = false;
        this.onClick = null;
        this.hoverable = false;
        this.onHover = null;
        this.onHoverEnd = null;
        this.scrollable = false;
        this.onScrollUp = null;
        this.onScrollDown = null;
        this.customColor = color;
    }

    public String getText() {
        return text;
    }

    public boolean isClickable() {
        return clickable;
    }

    public void executeClick() {
        if (onClick != null) onClick.run();
    }

    public boolean isHoverable() {
        return hoverable;
    }

    public void executeHover() {
        if (onHover != null) onHover.run();
    }

    public void executeHoverEnd() {
        if (onHoverEnd != null) onHoverEnd.run();
    }

    public int getColor() {
        return customColor == 0 ? 0xFFFFFF : customColor;
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public void executeScroll(boolean up) {
        Runnable action = up ? onScrollUp : onScrollDown;
        if (action != null) action.run();
    }
}
