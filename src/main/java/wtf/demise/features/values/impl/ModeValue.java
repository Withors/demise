package wtf.demise.features.values.impl;

import lombok.Getter;
import lombok.Setter;
import wtf.demise.features.modules.Module;
import wtf.demise.features.values.Value;

import java.util.Arrays;
import java.util.function.Supplier;

@Getter
public class ModeValue extends Value {
    @Setter
    private int index;
    public String[] modes;

    public ModeValue(String name, String[] modes, String current, Module module, Supplier<Boolean> visible) {
        super(name, module, visible);
        this.modes = modes;
        this.index = Arrays.asList(modes).indexOf(current);
    }

    public ModeValue(String name, String[] modes, String current, Module module) {
        super(name, module, () -> true);
        this.modes = modes;
        this.index = Arrays.asList(modes).indexOf(current);
    }

    public boolean is(String mode) {
        return get().equals(mode);
    }

    public String get() {
        try {
            if (index < 0 || index >= modes.length) {
                return modes[0];
            }
            return modes[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "ERROR";
        }
    }

    public void set(String mode) {
        this.index = Arrays.asList(modes).indexOf(mode);
    }

    public void set(int mode) {
        this.index = mode;
    }
}
