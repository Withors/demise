package net.minecraft.util;

import com.google.gson.*;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;

import java.lang.reflect.Type;

public class ChatStyle {
    private ChatStyle parentStyle;
    private EnumChatFormatting color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private ClickEvent chatClickEvent;
    private HoverEvent chatHoverEvent;
    private String insertion;
    private static final ChatStyle rootStyle = new ChatStyle() {
        public EnumChatFormatting getColor() {
            return null;
        }

        public boolean getBold() {
            return false;
        }

        public boolean getItalic() {
            return false;
        }

        public boolean getStrikethrough() {
            return false;
        }

        public boolean getUnderlined() {
            return false;
        }

        public boolean getObfuscated() {
            return false;
        }

        public ClickEvent getChatClickEvent() {
            return null;
        }

        public HoverEvent getChatHoverEvent() {
            return null;
        }

        public String getInsertion() {
            return null;
        }

        public ChatStyle setColor(EnumChatFormatting color) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setBold(Boolean boldIn) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setItalic(Boolean italic) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setStrikethrough(Boolean strikethrough) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setUnderlined(Boolean underlined) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setObfuscated(Boolean obfuscated) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setChatClickEvent(ClickEvent event) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setChatHoverEvent(HoverEvent event) {
            throw new UnsupportedOperationException();
        }

        public ChatStyle setParentStyle(ChatStyle parent) {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            return "Style.ROOT";
        }

        public ChatStyle createShallowCopy() {
            return this;
        }

        public ChatStyle createDeepCopy() {
            return this;
        }

        public String getFormattingCode() {
            return "";
        }
    };

    public EnumChatFormatting getColor() {
        return this.color == null ? this.getParent().getColor() : this.color;
    }

    public boolean getBold() {
        return this.bold == null ? this.getParent().getBold() : this.bold;
    }

    public boolean getItalic() {
        return this.italic == null ? this.getParent().getItalic() : this.italic;
    }

    public boolean getStrikethrough() {
        return this.strikethrough == null ? this.getParent().getStrikethrough() : this.strikethrough;
    }

    public boolean getUnderlined() {
        return this.underlined == null ? this.getParent().getUnderlined() : this.underlined;
    }

    public boolean getObfuscated() {
        return this.obfuscated == null ? this.getParent().getObfuscated() : this.obfuscated;
    }

    public boolean isEmpty() {
        return this.bold == null && this.italic == null && this.strikethrough == null && this.underlined == null && this.obfuscated == null && this.color == null && this.chatClickEvent == null && this.chatHoverEvent == null;
    }

    public ClickEvent getChatClickEvent() {
        return this.chatClickEvent == null ? this.getParent().getChatClickEvent() : this.chatClickEvent;
    }

    public HoverEvent getChatHoverEvent() {
        return this.chatHoverEvent == null ? this.getParent().getChatHoverEvent() : this.chatHoverEvent;
    }

    public String getInsertion() {
        return this.insertion == null ? this.getParent().getInsertion() : this.insertion;
    }

    public ChatStyle setColor(EnumChatFormatting color) {
        this.color = color;
        return this;
    }

    public ChatStyle setBold(Boolean boldIn) {
        this.bold = boldIn;
        return this;
    }

    public ChatStyle setItalic(Boolean italic) {
        this.italic = italic;
        return this;
    }

    public ChatStyle setStrikethrough(Boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public ChatStyle setUnderlined(Boolean underlined) {
        this.underlined = underlined;
        return this;
    }

    public ChatStyle setObfuscated(Boolean obfuscated) {
        this.obfuscated = obfuscated;
        return this;
    }

    public ChatStyle setChatClickEvent(ClickEvent event) {
        this.chatClickEvent = event;
        return this;
    }

    public ChatStyle setChatHoverEvent(HoverEvent event) {
        this.chatHoverEvent = event;
        return this;
    }

    public ChatStyle setInsertion(String insertion) {
        this.insertion = insertion;
        return this;
    }

    public ChatStyle setParentStyle(ChatStyle parent) {
        this.parentStyle = parent;
        return this;
    }

    public String getFormattingCode() {
        if (this.isEmpty()) {
            return this.parentStyle != null ? this.parentStyle.getFormattingCode() : "";
        } else {
            StringBuilder stringbuilder = new StringBuilder();

            if (this.getColor() != null) {
                stringbuilder.append(this.getColor());
            }

            if (this.getBold()) {
                stringbuilder.append(EnumChatFormatting.BOLD);
            }

            if (this.getItalic()) {
                stringbuilder.append(EnumChatFormatting.ITALIC);
            }

            if (this.getUnderlined()) {
                stringbuilder.append(EnumChatFormatting.UNDERLINE);
            }

            if (this.getObfuscated()) {
                stringbuilder.append(EnumChatFormatting.OBFUSCATED);
            }

            if (this.getStrikethrough()) {
                stringbuilder.append(EnumChatFormatting.STRIKETHROUGH);
            }

            return stringbuilder.toString();
        }
    }

    private ChatStyle getParent() {
        return this.parentStyle == null ? rootStyle : this.parentStyle;
    }

    public String toString() {
        return "Style{hasParent=" + (this.parentStyle != null) + ", color=" + this.color + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", obfuscated=" + this.obfuscated + ", clickEvent=" + this.getChatClickEvent() + ", hoverEvent=" + this.getChatHoverEvent() + ", insertion=" + this.getInsertion() + '}';
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof ChatStyle chatstyle)) {
            return false;
        } else {
            boolean flag;
            label0:
            {

                if (this.getBold() == chatstyle.getBold() && this.getColor() == chatstyle.getColor() && this.getItalic() == chatstyle.getItalic() && this.getObfuscated() == chatstyle.getObfuscated() && this.getStrikethrough() == chatstyle.getStrikethrough() && this.getUnderlined() == chatstyle.getUnderlined()) {
                    label85:
                    {
                        if (this.getChatClickEvent() != null) {
                            if (!this.getChatClickEvent().equals(chatstyle.getChatClickEvent())) {
                                break label85;
                            }
                        } else if (chatstyle.getChatClickEvent() != null) {
                            break label85;
                        }

                        if (this.getChatHoverEvent() != null) {
                            if (!this.getChatHoverEvent().equals(chatstyle.getChatHoverEvent())) {
                                break label85;
                            }
                        } else if (chatstyle.getChatHoverEvent() != null) {
                            break label85;
                        }

                        if (this.getInsertion() != null) {
                            if (this.getInsertion().equals(chatstyle.getInsertion())) {
                                break label0;
                            }
                        } else if (chatstyle.getInsertion() == null) {
                            break label0;
                        }
                    }
                }

                flag = false;
                return false;
            }
            flag = true;
            return true;
        }
    }

    public int hashCode() {
        int i = this.color.hashCode();
        i = 31 * i + this.bold.hashCode();
        i = 31 * i + this.italic.hashCode();
        i = 31 * i + this.underlined.hashCode();
        i = 31 * i + this.strikethrough.hashCode();
        i = 31 * i + this.obfuscated.hashCode();
        i = 31 * i + this.chatClickEvent.hashCode();
        i = 31 * i + this.chatHoverEvent.hashCode();
        i = 31 * i + this.insertion.hashCode();
        return i;
    }

    public ChatStyle createShallowCopy() {
        ChatStyle chatstyle = new ChatStyle();
        chatstyle.bold = this.bold;
        chatstyle.italic = this.italic;
        chatstyle.strikethrough = this.strikethrough;
        chatstyle.underlined = this.underlined;
        chatstyle.obfuscated = this.obfuscated;
        chatstyle.color = this.color;
        chatstyle.chatClickEvent = this.chatClickEvent;
        chatstyle.chatHoverEvent = this.chatHoverEvent;
        chatstyle.parentStyle = this.parentStyle;
        chatstyle.insertion = this.insertion;
        return chatstyle;
    }

    public ChatStyle createDeepCopy() {
        ChatStyle chatstyle = new ChatStyle();
        chatstyle.setBold(this.getBold());
        chatstyle.setItalic(this.getItalic());
        chatstyle.setStrikethrough(this.getStrikethrough());
        chatstyle.setUnderlined(this.getUnderlined());
        chatstyle.setObfuscated(this.getObfuscated());
        chatstyle.setColor(this.getColor());
        chatstyle.setChatClickEvent(this.getChatClickEvent());
        chatstyle.setChatHoverEvent(this.getChatHoverEvent());
        chatstyle.setInsertion(this.getInsertion());
        return chatstyle;
    }

    public static class Serializer implements JsonDeserializer<ChatStyle>, JsonSerializer<ChatStyle> {
        public ChatStyle deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
            if (p_deserialize_1_.isJsonObject()) {
                ChatStyle chatstyle = new ChatStyle();
                JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();

                if (jsonobject == null) {
                    return null;
                } else {
                    if (jsonobject.has("bold")) {
                        chatstyle.bold = jsonobject.get("bold").getAsBoolean();
                    }

                    if (jsonobject.has("italic")) {
                        chatstyle.italic = jsonobject.get("italic").getAsBoolean();
                    }

                    if (jsonobject.has("underlined")) {
                        chatstyle.underlined = jsonobject.get("underlined").getAsBoolean();
                    }

                    if (jsonobject.has("strikethrough")) {
                        chatstyle.strikethrough = jsonobject.get("strikethrough").getAsBoolean();
                    }

                    if (jsonobject.has("obfuscated")) {
                        chatstyle.obfuscated = jsonobject.get("obfuscated").getAsBoolean();
                    }

                    if (jsonobject.has("color")) {
                        chatstyle.color = p_deserialize_3_.deserialize(jsonobject.get("color"), EnumChatFormatting.class);
                    }

                    if (jsonobject.has("insertion")) {
                        chatstyle.insertion = jsonobject.get("insertion").getAsString();
                    }

                    if (jsonobject.has("clickEvent")) {
                        JsonObject jsonobject1 = jsonobject.getAsJsonObject("clickEvent");

                        if (jsonobject1 != null) {
                            JsonPrimitive jsonprimitive = jsonobject1.getAsJsonPrimitive("action");
                            ClickEvent.Action clickevent$action = jsonprimitive == null ? null : ClickEvent.Action.getValueByCanonicalName(jsonprimitive.getAsString());
                            JsonPrimitive jsonprimitive1 = jsonobject1.getAsJsonPrimitive("value");
                            String s = jsonprimitive1 == null ? null : jsonprimitive1.getAsString();

                            if (clickevent$action != null && s != null && clickevent$action.shouldAllowInChat()) {
                                chatstyle.chatClickEvent = new ClickEvent(clickevent$action, s);
                            }
                        }
                    }

                    if (jsonobject.has("hoverEvent")) {
                        JsonObject jsonobject2 = jsonobject.getAsJsonObject("hoverEvent");

                        if (jsonobject2 != null) {
                            JsonPrimitive jsonprimitive2 = jsonobject2.getAsJsonPrimitive("action");
                            HoverEvent.Action hoverevent$action = jsonprimitive2 == null ? null : HoverEvent.Action.getValueByCanonicalName(jsonprimitive2.getAsString());
                            IChatComponent ichatcomponent = p_deserialize_3_.deserialize(jsonobject2.get("value"), IChatComponent.class);

                            if (hoverevent$action != null && ichatcomponent != null && hoverevent$action.shouldAllowInChat()) {
                                chatstyle.chatHoverEvent = new HoverEvent(hoverevent$action, ichatcomponent);
                            }
                        }
                    }

                    return chatstyle;
                }
            } else {
                return null;
            }
        }

        public JsonElement serialize(ChatStyle p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
            if (p_serialize_1_.isEmpty()) {
                return null;
            } else {
                JsonObject jsonobject = new JsonObject();

                if (p_serialize_1_.bold != null) {
                    jsonobject.addProperty("bold", p_serialize_1_.bold);
                }

                if (p_serialize_1_.italic != null) {
                    jsonobject.addProperty("italic", p_serialize_1_.italic);
                }

                if (p_serialize_1_.underlined != null) {
                    jsonobject.addProperty("underlined", p_serialize_1_.underlined);
                }

                if (p_serialize_1_.strikethrough != null) {
                    jsonobject.addProperty("strikethrough", p_serialize_1_.strikethrough);
                }

                if (p_serialize_1_.obfuscated != null) {
                    jsonobject.addProperty("obfuscated", p_serialize_1_.obfuscated);
                }

                if (p_serialize_1_.color != null) {
                    jsonobject.add("color", p_serialize_3_.serialize(p_serialize_1_.color));
                }

                if (p_serialize_1_.insertion != null) {
                    jsonobject.add("insertion", p_serialize_3_.serialize(p_serialize_1_.insertion));
                }

                if (p_serialize_1_.chatClickEvent != null) {
                    JsonObject jsonobject1 = new JsonObject();
                    jsonobject1.addProperty("action", p_serialize_1_.chatClickEvent.getAction().getCanonicalName());
                    jsonobject1.addProperty("value", p_serialize_1_.chatClickEvent.getValue());
                    jsonobject.add("clickEvent", jsonobject1);
                }

                if (p_serialize_1_.chatHoverEvent != null) {
                    JsonObject jsonobject2 = new JsonObject();
                    jsonobject2.addProperty("action", p_serialize_1_.chatHoverEvent.getAction().getCanonicalName());
                    jsonobject2.add("value", p_serialize_3_.serialize(p_serialize_1_.chatHoverEvent.getValue()));
                    jsonobject.add("hoverEvent", jsonobject2);
                }

                return jsonobject;
            }
        }
    }
}
