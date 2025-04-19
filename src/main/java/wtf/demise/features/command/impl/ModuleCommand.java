package wtf.demise.features.command.impl;

import wtf.demise.features.command.Command;
import wtf.demise.features.command.CommandExecutionException;
import wtf.demise.features.modules.Module;
import wtf.demise.features.values.Value;
import wtf.demise.features.values.impl.*;
import wtf.demise.utils.misc.ChatUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ModuleCommand extends Command {
    private final Module module;
    private final List<Value> values;

    public ModuleCommand(Module module, List<Value> values) {
        this.module = module;
        this.values = values;
    }

    @Override
    public String getUsage() {
        return module.getName().toLowerCase(Locale.getDefault()) + " <setting> <value>";
    }

    @Override
    public String[] getAliases() {
        return new String[]{module.getName()};
    }

    @Override
    public void execute(String[] args) throws CommandExecutionException {
        if (args.length == 1) {
            ChatUtils.sendMessageClient("Usage: " + getUsage());
            return;
        }

        Value value = module.getValue(args[1]);

        if (value == null) return;

        if (value instanceof BoolValue boolValue) {
            boolean newValue = !boolValue.get();
            boolValue.set(newValue);

            ChatUtils.sendMessageClient(module.getName() + " " + args[1] + " was toggled " + (newValue ? "§8on" : "§8off") + ".");
        } else {
            if (args.length < 3) {
                if (value instanceof SliderValue || value instanceof ColorValue)
                    ChatUtils.sendMessageClient(args[1].toLowerCase() + " <value>");
                else if (value instanceof ModeValue modeValue)
                    ChatUtils.sendMessageClient(args[1].toLowerCase() + " <" + Arrays.stream(modeValue.getModes())
                            .map(String::toLowerCase).reduce((s1, s2) -> s1 + "/" + s2).orElse("") + ">");
                return;
            }

            if (value instanceof ColorValue colorValue) {
                colorValue.set(new Color(Integer.parseInt(args[2])));
                ChatUtils.sendMessageClient(module.getName() + " " + args[1] + " was set to " + colorValue.get() + ".");
            } else if (value instanceof SliderValue sliderValue) {
                sliderValue.setValue(Float.parseFloat(args[2]));
                ChatUtils.sendMessageClient(module.getName() + " " + args[1] + " was set to " + sliderValue.get() + ".");
            } else if (value instanceof MultiBoolValue multiBoolValue) {
                multiBoolValue.getValues().forEach(boolValue -> {
                    if (Objects.equals(boolValue.getName(), args[2])) {
                        boolean newValue = !boolValue.get();
                        boolValue.set(newValue);
                        ChatUtils.sendMessageClient(module.getName() + " " + args[1] + " was set to " + boolValue.get() + ".");
                    }
                });
            } else if (value instanceof ModeValue modeValue) {
                modeValue.set(args[2]);
                ChatUtils.sendMessageClient(module.getName() + " " + args[1] + " was set to " + modeValue.get() + ".");
            } else if (value instanceof TextValue textValue) {
                textValue.setText(args[2]);
                ChatUtils.sendMessageClient(module.getName() + " " + args[1] + " was set to " + textValue.get() + ".");
            }
        }
    }
}
