package wtf.demise.features.modules.impl.visual;

import net.minecraft.client.gui.GuiScreen;
import org.lwjglx.input.Keyboard;
import wtf.demise.features.modules.Module;
import wtf.demise.features.modules.ModuleCategory;
import wtf.demise.features.modules.ModuleInfo;
import wtf.demise.features.values.impl.BoolValue;
import wtf.demise.features.values.impl.ColorValue;
import wtf.demise.features.values.impl.ModeValue;

import java.awt.*;

@ModuleInfo(name = "ClickGUI", category = ModuleCategory.Visual, key = Keyboard.KEY_RSHIFT)
public class ClickGUI extends Module {
    public final ModeValue mode = new ModeValue("Mode", new String[]{"NeverLose", "DropDown","Exhi"}, "NeverLose", this);

    public final ColorValue color = new ColorValue("Color", new Color(80, 80, 80), this);
    public final BoolValue rainbow = new BoolValue("Rainbow",true,this,() -> mode.is("Exhi"));

    @Override
    public void onEnable() {
        GuiScreen guiScreen = null;
        switch (mode.get()) {
            case "NeverLose":
                guiScreen = INSTANCE.getNeverLose();
                break;
            case "DropDown":
                guiScreen = INSTANCE.getDropdownGUI();
                break;
            case "Exhi":
                guiScreen = INSTANCE.getSkeetGUI();
                break;
        }
        mc.displayGuiScreen(guiScreen);
        if(mode.is("Exhi")){
            INSTANCE.getSkeetGUI().alpha = 0.0;
            INSTANCE.getSkeetGUI().targetAlpha = 255.0;
            INSTANCE.getSkeetGUI().open = true;
            INSTANCE.getSkeetGUI().closed = false;
        }
        toggle();
        super.onEnable();
    }
}
