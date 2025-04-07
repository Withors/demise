package wtf.demise.gui.altmanager.repository;

import net.minecraft.client.Minecraft;
import wtf.demise.gui.altmanager.group.GuiRoundedGroupWithLines;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class GuiGroupPlayerBox extends GuiRoundedGroupWithLines<Alt> {

    private final Predicate<Alt> shouldRender = alt -> alt != null && alt.getPlayer() != null;

    public GuiGroupPlayerBox(int xPosition, int yPosition, int width, int height, Supplier<Alt> supplier) {
        super("Alt Info", xPosition, yPosition, width, height, 15, supplier);
    }

    @Override
    public void drawGroup(Minecraft mc, int mouseX, int mouseY) {
        //superDrawGroup(mc, mouseX, mouseY);

        final Alt alt = this.supplier.get();

        if (this.shouldRender.test(alt)) {
            drawLines();
        }
    }

}