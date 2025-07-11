package net.optifine.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;

import java.io.IOException;
import java.util.List;

public class GuiScreenOF extends GuiScreen {
    protected void actionPerformedRightClick(GuiButton button) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 1) {
            GuiButton guibutton = getSelectedButton(mouseX, mouseY, this.buttonList);

            if (guibutton != null && guibutton.enabled) {
                guibutton.playPressSound(mc.getSoundHandler());
                this.actionPerformedRightClick(guibutton);
            }
        }
    }

    public static GuiButton getSelectedButton(int x, int y, List<GuiButton> listButtons) {
        for (GuiButton guibutton : listButtons) {
            if (guibutton.visible) {
                float j = GuiVideoSettings.getButtonWidth(guibutton);
                float k = GuiVideoSettings.getButtonHeight(guibutton);

                if (x >= guibutton.xPosition && y >= guibutton.yPosition && x < guibutton.xPosition + j && y < guibutton.yPosition + k) {
                    return guibutton;
                }
            }
        }

        return null;
    }
}
