package wtf.demise.utils.misc;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import wtf.demise.utils.InstanceAccess;

public class ChatUtils implements InstanceAccess {
    public static void sendMessageClient(String message) {
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GRAY + "demise" + EnumChatFormatting.GRAY + " » " + EnumChatFormatting.WHITE + message));
    }

    public static void sendMessageClient(boolean message) {
        sendMessageClient(String.valueOf(message));
    }

    public static void sendMessageClient(int message) {
        sendMessageClient(String.valueOf(message));
    }

    public static void sendMessageClient(float message) {
        sendMessageClient(String.valueOf(message));
    }

    public static void sendMessageClient(double message) {
        sendMessageClient(String.valueOf(message));
    }

    public static void sendMessageServer(String message) {
        mc.thePlayer.sendChatMessage(message);
    }
}