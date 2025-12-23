package someoneok.kic.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.elements.OptionPage;

import java.util.function.Supplier;

public final class EphemeralConfig extends Config {
    private transient String lastPagePrefix;

    public EphemeralConfig(Mod hostMod) { super(hostMod, "ephemeral.json", true); }

    public OptionPage buildPage(Object pageInstance, String pageName) {
        OptionPage page = new OptionPage(pageName, this.mod);
        generateOptionList(pageInstance, page, this.mod, false);
        this.lastPagePrefix = pageName != null && !pageName.isEmpty() ? (pageName + ".") : null;
        return page;
    }

    public void hideIfPublic(String option, Supplier<Boolean> condition) {
        super.hideIf(resolveOptionKey(option), condition);
    }

    public void addDependencyPublic(String option, String conditionName, Supplier<Boolean> condition) {
        super.addDependency(resolveOptionKey(option), conditionName, condition);
    }

    public void registerKeyBindPublic(OneKeyBind keyBind, Runnable action) {
        super.registerKeyBind(keyBind, action);
    }

    private String resolveOptionKey(String raw) {
        if (raw == null) return null;
        if (this.optionNames.containsKey(raw)) return raw;
        if (lastPagePrefix != null) {
            String prefixed = lastPagePrefix + raw;
            if (this.optionNames.containsKey(prefixed)) return prefixed;
        }
        return raw;
    }
}
