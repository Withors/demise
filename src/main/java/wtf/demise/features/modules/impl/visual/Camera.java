package wtf.demise.features.modules.impl.visual;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import wtf.demise.events.annotations.EventTarget;
import wtf.demise.events.impl.misc.GameEvent;
import wtf.demise.events.impl.render.Render2DEvent;
import wtf.demise.features.modules.Module;
import wtf.demise.features.modules.ModuleCategory;
import wtf.demise.features.modules.ModuleInfo;
import wtf.demise.features.values.impl.BoolValue;
import wtf.demise.features.values.impl.MultiBoolValue;
import wtf.demise.features.values.impl.SliderValue;
import wtf.demise.utils.render.ColorUtils;
import wtf.demise.utils.render.GLUtils;
import wtf.demise.utils.render.RenderUtils;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "Camera", category = ModuleCategory.Visual)
public class Camera extends Module {
    public final MultiBoolValue setting = new MultiBoolValue("Option", Arrays.asList(
            new BoolValue("View Clip", true),
            new BoolValue("Third Person Distance", false),
            new BoolValue("No Hurt Cam", false),
            new BoolValue("FPS Hurt Cam", false),
            new BoolValue("No Fire", false),
            new BoolValue("Shader Sky", false),
            new BoolValue("Bright Players", false),
            new BoolValue("Motion Camera", false),
            new BoolValue("Motion Blur", false)
    ), this);
    public final SliderValue cameraDistance = new SliderValue("Distance", 4.0f, 1.0f, 8.0f, 1.0f, this, () -> setting.isEnabled("Third Person Distance"));
    public final SliderValue interpolation = new SliderValue("Motion Interpolation", 0.15f, 0.05f, 0.5f, 0.05f, this, () -> setting.isEnabled("Motion Camera"));
    public final SliderValue amount = new SliderValue("Motion Blur Amount", 1, 1, 10, 1, this, () -> setting.isEnabled("Motion Blur"));

    @EventTarget
    public void onGameEvent(GameEvent e) {
        if (mc.theWorld != null) {
            if (setting.isEnabled("Motion Blur")) {
                if ((mc.entityRenderer.getShaderGroup() == null)) {
                    mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));
                }

                if (mc.entityRenderer.getShaderGroup() != null) {
                    float uniform = 1F - Math.min(amount.get() / 10F, 0.9f);
                    mc.entityRenderer.getShaderGroup().listShaders.get(0).getShaderManager().getShaderUniform("Phosphor").set(uniform, 0F, 0F);
                }
            } else {
                if (mc.entityRenderer.isShaderActive()) {
                    mc.entityRenderer.stopUseShader();
                }
            }
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (setting.isEnabled("FPS Hurt Cam")) {
            final float hurtTimePercentage = (mc.thePlayer.hurtTime - event.partialTicks()) / mc.thePlayer.maxHurtTime;

            if (hurtTimePercentage > 0.0) {
                glDisable(GL_TEXTURE_2D);
                GLUtils.startBlend();
                glShadeModel(GL_SMOOTH);
                glDisable(GL_ALPHA_TEST);

                final ScaledResolution scaledResolution = event.scaledResolution();

                final float lineWidth = 20.f;

                glLineWidth(lineWidth);

                final int width = scaledResolution.getScaledWidth();
                final int height = scaledResolution.getScaledHeight();

                final int fadeOutColour = ColorUtils.fadeTo(0x00FF0000, 0xFFFF0000, hurtTimePercentage);

                glBegin(GL_QUADS);
                {
                    // Left
                    RenderUtils.color(fadeOutColour);
                    glVertex2f(0, 0);
                    glVertex2f(0, height);
                    RenderUtils.color(0x00FF0000);
                    glVertex2f(lineWidth, height - lineWidth);
                    glVertex2f(lineWidth, lineWidth);

                    // Right
                    RenderUtils.color(0x00FF0000);
                    glVertex2f(width - lineWidth, lineWidth);
                    glVertex2f(width - lineWidth, height - lineWidth);
                    RenderUtils.color(fadeOutColour);
                    glVertex2f(width, height);
                    glVertex2f(width, 0);

                    // Top
                    RenderUtils.color(fadeOutColour);
                    glVertex2f(0, 0);
                    RenderUtils.color(0x00FF0000);
                    glVertex2d(lineWidth, lineWidth);
                    glVertex2f(width - lineWidth, lineWidth);
                    RenderUtils.color(fadeOutColour);
                    glVertex2f(width, 0);

                    // Bottom
                    RenderUtils.color(0x00FF0000);
                    glVertex2f(lineWidth, height - lineWidth);
                    RenderUtils.color(fadeOutColour);
                    glVertex2d(0, height);
                    glVertex2f(width, height);
                    RenderUtils.color(0x00FF0000);
                    glVertex2f(width - lineWidth, height - lineWidth);
                }
                glEnd();

                glEnable(GL_ALPHA_TEST);
                glShadeModel(GL_FLAT);
                GLUtils.endBlend();
                glEnable(GL_TEXTURE_2D);
            }
        }
    }
}
