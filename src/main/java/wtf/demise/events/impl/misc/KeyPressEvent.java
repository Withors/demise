package wtf.demise.events.impl.misc;

import lombok.Getter;
import wtf.demise.events.impl.CancellableEvent;

@Getter
public class KeyPressEvent extends CancellableEvent {
    private final int key;

    public KeyPressEvent(int key) {
        this.key = key;
    }
}
