package wtf.demise.utils.animations;

import lombok.Getter;
import wtf.demise.utils.animations.impl.SmoothStepAnimation;

public class ContinualAnimation {

    private float output, endpoint;

    @Getter
    private Animation animation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);

    public void animate(float destination, int ms) {
        output = (float) (endpoint - animation.getOutput());
        endpoint = destination;
        if (output != (endpoint - destination)) {
            animation = new SmoothStepAnimation(ms, endpoint - output, Direction.BACKWARDS);
        }
    }

    public boolean isDone() {
        return output == endpoint || animation.isDone();
    }

    public float getOutput() {
        output = (float) (endpoint - animation.getOutput());
        return output;
    }
}
