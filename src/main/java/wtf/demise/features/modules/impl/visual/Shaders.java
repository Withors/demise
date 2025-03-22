package wtf.demise.features.modules.impl.visual;

import net.minecraft.client.shader.Framebuffer;
import wtf.demise.events.impl.render.Shader2DEvent;
import wtf.demise.features.modules.Module;
import wtf.demise.features.modules.ModuleCategory;
import wtf.demise.features.modules.ModuleInfo;
import wtf.demise.features.values.impl.BoolValue;
import wtf.demise.features.values.impl.SliderValue;
import wtf.demise.utils.render.RenderUtils;
import wtf.demise.utils.render.shader.impl.Bloom;
import wtf.demise.utils.render.shader.impl.Blur;
import wtf.demise.utils.render.shader.impl.Shadow;

@ModuleInfo(name = "Shaders", category = ModuleCategory.Visual)
public class Shaders extends Module {
    private final BoolValue blur = new BoolValue("Blur", true, this);
    private final SliderValue blurRadius = new SliderValue("Blur Radius", 25, 1, 50, 1, this, this.blur::get);
    private final SliderValue blurCompression = new SliderValue("Blur Compression", 1, 1, 50, 1f, this, this.blur::get);
    private final BoolValue shadow = new BoolValue("Shadow", true, this);
    private final SliderValue shadowRadius = new SliderValue("Shadow Radius", 50, 1, 50, 1, this, shadow::get);
    private final SliderValue shadowOffset = new SliderValue("Shadow Offset", 1, 1, 15, 1, this, shadow::get);
    private final BoolValue bloom = new BoolValue("Bloom", false, this);
    private final SliderValue glowRadius = new SliderValue("Bloom Radius", 3, 1, 9, 1, this, bloom::get);
    private final SliderValue glowOffset = new SliderValue("Bloom Offset", 1, 1, 10, 1, this, bloom::get);

    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    public void renderShaders() {
        if (!this.isEnabled()) return;

        if (this.blur.get()) {
            RenderUtils.resetColor();
            Blur.startBlur();
            INSTANCE.getEventManager().call(new Shader2DEvent(Shader2DEvent.ShaderType.BLUR));
            Blur.endBlur(blurRadius.get(), (int) blurCompression.get());
            RenderUtils.resetColor();
        }

        if (bloom.get()) {
            stencilFramebuffer = RenderUtils.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);
            RenderUtils.resetColor();
            INSTANCE.getEventManager().call(new Shader2DEvent(Shader2DEvent.ShaderType.GLOW));
            RenderUtils.resetColor();
            stencilFramebuffer.unbindFramebuffer();

            Bloom.renderBlur(stencilFramebuffer.framebufferTexture, (int) glowRadius.get(), (int) glowOffset.get());
        }

        if (shadow.get()) {
            stencilFramebuffer = RenderUtils.createFrameBuffer(stencilFramebuffer, true);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(true);
            RenderUtils.resetColor();
            INSTANCE.getEventManager().call(new Shader2DEvent(Shader2DEvent.ShaderType.SHADOW));
            stencilFramebuffer.unbindFramebuffer();
            RenderUtils.resetColor();

            Shadow.renderBloom(stencilFramebuffer.framebufferTexture, (int) shadowRadius.get(), (int) shadowOffset.get());
        }
    }
}
