package wtf.demise.features.command.impl;

import net.minecraft.util.EnumChatFormatting;
import wtf.demise.Demise;
import wtf.demise.features.command.Command;
import wtf.demise.features.command.CommandExecutionException;
import wtf.demise.features.modules.Module;
import wtf.demise.utils.misc.DebugUtils;

import java.util.Optional;

public final class HideCommand extends Command {
    @Override
    public String[] getAliases() {
        return new String[]{"hide", "h", "visible", "v"};
    }

    @Override
    public void execute(final String[] arguments) throws CommandExecutionException {
        if (arguments.length == 2) {
            final String arg = arguments[1];
            if (arg.equalsIgnoreCase("clear")) {
                for (final Module module : Demise.INSTANCE.getModuleManager().getModules()) {
                    module.setHidden(false);
                }
                DebugUtils.sendMessage("Cleared all hidden module.");
            } else if (arg.equalsIgnoreCase("list")) {
                DebugUtils.sendMessage("Hidden Modules");
                for (final Module module : Demise.INSTANCE.getModuleManager().getModules()) {
                    if (module.isHidden()) {
                        DebugUtils.sendMessage(EnumChatFormatting.GRAY + "- " + EnumChatFormatting.RED + module.getName());
                    }
                }
            } else {
                final Optional<Module> module2 = Optional.ofNullable(Demise.INSTANCE.getModuleManager().getModule(arg));
                if (module2.isPresent()) {
                    final Module m = module2.get();
                    m.setHidden(!m.isHidden());
                    DebugUtils.sendMessage(m.getName() + " is now " + (m.isHidden() ? "\u00a7Chidden\u00a77." : "\u00a7Ashown\u00a77."));
                }
            }
            return;
        }
        throw new CommandExecutionException(this.getUsage());
    }

    @Override
    public String getUsage() {
        return "hide <module> | clear | list";
    }
}
