package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;
import java.util.Map;

public class GuiLanguage extends GuiScreen {
    protected final GuiScreen parentScreen;
    private GuiLanguage.List list;
    private final GameSettings game_settings_3;
    private final LanguageManager languageManager;
    private GuiOptionButton forceUnicodeFontBtn;
    private GuiOptionButton confirmSettingsBtn;

    public GuiLanguage(GuiScreen screen, GameSettings gameSettingsObj, LanguageManager manager) {
        this.parentScreen = screen;
        this.game_settings_3 = gameSettingsObj;
        this.languageManager = manager;
    }

    public void initGui() {
        this.buttonList.add(this.forceUnicodeFontBtn = new GuiOptionButton(100, width / 2 - 155, height - 38, GameSettings.Options.FORCE_UNICODE_FONT, this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT)));
        this.buttonList.add(this.confirmSettingsBtn = new GuiOptionButton(6, width / 2 - 155 + 160, height - 38, I18n.format("gui.done")));
        this.list = new GuiLanguage.List(mc);
        this.list.registerScrollButtons(7, 8);
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 5:
                    break;

                case 6:
                    mc.displayGuiScreen(this.parentScreen);
                    break;

                case 100:
                    if (button instanceof GuiOptionButton) {
                        this.game_settings_3.setOptionValue(((GuiOptionButton) button).returnEnumOptions(), 1);
                        button.displayString = this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
                        ScaledResolution scaledresolution = new ScaledResolution(mc);
                        int i = scaledresolution.getScaledWidth();
                        int j = scaledresolution.getScaledHeight();
                        this.setWorldAndResolution(mc, i, j);
                    }

                    break;

                default:
                    this.list.actionPerformed(button);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(this.fontRendererObj, I18n.format("options.language"), (float) width / 2, 16, 16777215);
        drawCenteredString(this.fontRendererObj, "(" + I18n.format("options.languageWarning") + ")", (float) width / 2, height - 56, 8421504);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class List extends GuiSlot {
        private final java.util.List<String> langCodeList = Lists.newArrayList();
        private final Map<String, Language> languageMap = Maps.newHashMap();

        public List(Minecraft mcIn) {
            super(mcIn, GuiScreen.width, GuiScreen.height, 32, GuiScreen.height - 65 + 4, 18);

            for (Language language : GuiLanguage.this.languageManager.getLanguages()) {
                this.languageMap.put(language.getLanguageCode(), language);
                this.langCodeList.add(language.getLanguageCode());
            }
        }

        protected int getSize() {
            return this.langCodeList.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            Language language = this.languageMap.get(this.langCodeList.get(slotIndex));
            GuiLanguage.this.languageManager.setCurrentLanguage(language);
            GuiLanguage.this.game_settings_3.language = language.getLanguageCode();
            this.mc.refreshResources();
            GuiLanguage.this.fontRendererObj.setUnicodeFlag(GuiLanguage.this.languageManager.isCurrentLocaleUnicode() || GuiLanguage.this.game_settings_3.forceUnicodeFont);
            GuiLanguage.this.fontRendererObj.setBidiFlag(GuiLanguage.this.languageManager.isCurrentLanguageBidirectional());
            GuiLanguage.this.confirmSettingsBtn.displayString = I18n.format("gui.done");
            GuiLanguage.this.forceUnicodeFontBtn.displayString = GuiLanguage.this.game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
            GuiLanguage.this.game_settings_3.saveOptions();
        }

        protected boolean isSelected(int slotIndex) {
            return this.langCodeList.get(slotIndex).equals(GuiLanguage.this.languageManager.getCurrentLanguage().getLanguageCode());
        }

        protected int getContentHeight() {
            return this.getSize() * 18;
        }

        protected void drawBackground() {
            GuiLanguage.this.drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            GuiLanguage.this.fontRendererObj.setBidiFlag(true);
            drawCenteredString(GuiLanguage.this.fontRendererObj, this.languageMap.get(this.langCodeList.get(entryID)).toString(), (float) this.width / 2, p_180791_3_ + 1, 16777215);
            GuiLanguage.this.fontRendererObj.setBidiFlag(GuiLanguage.this.languageManager.getCurrentLanguage().isBidirectional());
        }
    }
}
