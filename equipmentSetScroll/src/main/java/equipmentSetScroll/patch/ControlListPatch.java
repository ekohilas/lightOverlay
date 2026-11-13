package equipmentSetScroll.patch;

import equipmentSetScroll.ItemSetControls;
import necesse.engine.input.Control;
import necesse.engine.input.InputEvent;
import necesse.engine.modLoader.annotations.ModMethodPatch;
import necesse.engine.platforms.sharedOnPC.forms.FormKeyboardAndMouseControlListPopulator;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormContentBox;
import net.bytebuddy.asm.Advice;

public class ControlListPatch {
    /**
     * The controls menu passes each input event here while a control waits for a new bind.
     * Lets the mod's binds take sideways scrolling, and makes clicking an option row change its value.
     * Finishes the same way the game does after a rebind, then skips the original method.
     */
    @ModMethodPatch(target = FormKeyboardAndMouseControlListPopulator.class, name = "handleInputEvent", arguments = {InputEvent.class, Form.class, Runnable.class})
    public static class HandleInputEventPatch {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        static boolean onEnter(@Advice.Argument(0) InputEvent event,
                               @Advice.Argument(1) Form form,
                               @Advice.Argument(2) Runnable repopulate,
                               @Advice.FieldValue(value = "selectedControl", readOnly = false) Control selectedControl,
                               @Advice.FieldValue("onBindChanged") Runnable onBindChanged,
                               @Advice.FieldValue("contentBox") FormContentBox contentBox,
                               @Advice.FieldValue(value = "lastScrollY", readOnly = false) int lastScrollY) {
            if (selectedControl == null || !ItemSetControls.handleRebind(selectedControl, event))
                return false;
            selectedControl = null;
            form.playTickSound();
            lastScrollY = contentBox.getScrollY();
            if (onBindChanged != null)
                onBindChanged.run();
            repopulate.run();
            return true;
        }
    }
}
