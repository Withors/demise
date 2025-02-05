package wtf.demise.gui.click;

import wtf.demise.utils.InstanceAccess;

public interface IComponent extends InstanceAccess {
    default void drawScreen(int mouseX, int mouseY) {
    }

    default void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    default void mouseReleased(int mouseX, int mouseY, int state) {
    }

    default void keyTyped(char typedChar, int keyCode) {
    }
}
