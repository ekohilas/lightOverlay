package equipmentSetScroll;

import java.lang.reflect.Method;
import necesse.engine.input.Control;
import necesse.engine.input.InputEvent;
import necesse.engine.localization.message.StaticMessage;

/**
 * Everything the mod can be configured with, shown as rows in the game's
 * Settings > Controls > Item sets group, under the "Item set 1-4" hotkeys.
 *
 * The scroll binds are normal controls with two extra key IDs for horizontal scrolling.
 * The options are {@link OptionControl}s: their "key" holds the option's value, so the game
 * saves, loads and restores defaults for them like any other control (in the mod's cfg).
 */
public class ItemSetControls {
    /** Key IDs for horizontal scrolling. The game only has IDs for vertical scrolling (-102, -103). */
    public static final int SCROLL_RIGHT = -150;
    public static final int SCROLL_LEFT = -151;

    public static ScrollBindControl next;
    public static ScrollBindControl previous;
    public static OptionControl wrap;
    public static OptionControl showText;
    public static OptionControl cooldown;

    private static final int[] COOLDOWN_STEPS = {0, 50, 100, 150, 250, 400, 600, 1000};

    public static void register() {
        next = add(new ScrollBindControl(SCROLL_RIGHT, "equipsetscrollnext", "Next item set"));
        previous = add(new ScrollBindControl(SCROLL_LEFT, "equipsetscrollprev", "Previous item set"));
        next.setTooltip(new StaticMessage("Click, then scroll sideways or press a key to bind"));
        previous.setTooltip(new StaticMessage("Click, then scroll sideways or press a key to bind"));
        wrap = add(new OptionControl(1, "equipsetscrollwrap", "Wrap around item sets", new int[]{1, 0}) {
            @Override
            public String valueName(int value) {
                return value != 0 ? "ON" : "OFF";
            }
        });
        wrap.setTooltip(new StaticMessage("Click to toggle. Go from the last set to the first and back"));
        showText = add(new OptionControl(1, "equipsetscrollshowtext", "Show item set number", new int[]{1, 0}) {
            @Override
            public String valueName(int value) {
                return value != 0 ? "ON" : "OFF";
            }
        });
        showText.setTooltip(new StaticMessage("Click to toggle. Show the new set's number next to the cursor"));
        cooldown = add(new OptionControl(150, "equipsetscrollcooldown", "Item set scroll cooldown", COOLDOWN_STEPS) {
            @Override
            public String valueName(int value) {
                return value + " MS";
            }
        });
        cooldown.setTooltip(new StaticMessage("Click to change. Sideways scrolling is ignored this long after a switch, so one trackpad swipe moves one set"));
    }

    /** Adds the control to the game's Item sets group, or to the mod's own group if that can't be found. */
    private static <T extends Control> T add(T control) {
        try {
            Control.ControlGroup itemSets = Control.ITEM_SET_0 == null ? null : Control.streamGroups()
                    .filter(g -> {
                        for (Control c : g.getControls())
                            if (c == Control.ITEM_SET_0)
                                return true;
                        return false;
                    })
                    .findFirst().orElse(null);
            if (itemSets != null) {
                Method addControl = Control.class.getDeclaredMethod("addControl", Control.class, Control.ControlGroup.class);
                addControl.setAccessible(true);
                addControl.invoke(null, control, itemSets);
                return control;
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            System.err.println("Equipment Set Scroll: could not add " + control.id + " to the Item sets controls, using the mod's own group");
            e.printStackTrace();
        }
        return Control.addModControl(control);
    }

    public static boolean isHorizontalScroll(InputEvent event) {
        if (!event.isMouseWheelEvent())
            return false;
        double x = event.getMouseWheelX();
        return x != 0 && Math.abs(x) > Math.abs(event.getMouseWheelY());
    }

    /** GLFW reports scrolling left as positive x. */
    public static int horizontalScrollKey(double wheelX) {
        return wheelX < 0 ? SCROLL_RIGHT : SCROLL_LEFT;
    }

    /**
     * Called by the controls menu with each input event while one of its controls waits for a new bind.
     * Returns true when this mod handled it (the menu then finishes the rebind as usual).
     */
    public static boolean handleRebind(Control selected, InputEvent event) {
        if (selected instanceof OptionControl) {
            // Clicking an option row cycles its value straight away instead of waiting for a key
            ((OptionControl) selected).cycle();
            event.use();
            return true;
        }
        if (selected instanceof ScrollBindControl && isHorizontalScroll(event)) {
            selected.changeKey(horizontalScrollKey(event.getMouseWheelX()));
            event.use();
            return true;
        }
        return false;
    }

    /** A rebindable control that can also be bound to scrolling left or right. */
    public static class ScrollBindControl extends Control {
        public ScrollBindControl(int defaultKey, String id, String text) {
            super(defaultKey, id, new StaticMessage(text));
        }

        public boolean isHorizontalScrollBind() {
            return getKey() == SCROLL_RIGHT || getKey() == SCROLL_LEFT;
        }

        @Override
        public String getKeyName() {
            return keyName(getKey());
        }

        @Override
        public String getDefaultKeyName() {
            return keyName(defaultKey);
        }

        private static String keyName(int key) {
            if (key == SCROLL_RIGHT)
                return "SCROLLRIGHT";
            if (key == SCROLL_LEFT)
                return "SCROLLLEFT";
            return necesse.engine.input.Input.getName(key);
        }
    }

    /** A row in the controls menu holding a setting value instead of a key. */
    public abstract static class OptionControl extends Control {
        private final int[] values;

        public OptionControl(int defaultValue, String id, String text, int[] values) {
            // Own overlap group, so the value is never reported as a conflicting bind
            super(defaultValue, id, new StaticMessage(text), id);
            this.values = values;
        }

        public abstract String valueName(int value);

        public int getValue() {
            return getKey();
        }

        public boolean isOn() {
            return getKey() != 0;
        }

        public void cycle() {
            int index = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i] == getKey()) {
                    index = i + 1;
                    break;
                }
            }
            changeKey(values[index % values.length]);
        }

        @Override
        public void changeKey(int key) {
            // Values come from the menu (cycle) or the saved cfg; anything else falls back to the default
            for (int v : values) {
                if (v == key) {
                    super.changeKey(key);
                    return;
                }
            }
            super.changeKey(defaultKey);
        }

        @Override
        public void activate(InputEvent event) {
            // Not a real key: never pressed
        }

        @Override
        public String getKeyName() {
            return valueName(getKey());
        }

        @Override
        public String getDefaultKeyName() {
            return valueName(defaultKey);
        }
    }
}
