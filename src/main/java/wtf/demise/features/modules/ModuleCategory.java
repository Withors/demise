package wtf.demise.features.modules;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModuleCategory {

    Combat("Combat"),
    Legit("Legit"),
    Movement("Movement"),
    Player("Player"),
    Misc("Misc"),
    Exploit("Exploit"),
    Visual("Visuals"),
    Config("Configs"),
    Search("Search");

    private final String name;

}