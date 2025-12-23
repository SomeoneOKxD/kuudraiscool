package someoneok.kic.tweaker;

import cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker;
import net.hypixel.modapi.tweaker.HypixelModAPITweaker;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinTweaker;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused")
public class KICModTweaker implements ITweaker {

    private File gameDir;

    @Override
    @SuppressWarnings("unchecked")
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        this.gameDir = (gameDir != null) ? gameDir : new File(".");

        List<String> tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");
        tweakClasses.add(MixinTweaker.class.getName());
        tweakClasses.add(ModLoadingTweaker.class.getName());
        tweakClasses.add(LaunchWrapperTweaker.class.getName());
        tweakClasses.add(HypixelModAPITweaker.class.getName());
    }

    @Override public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        AddonMixinsBootstrap.run(new File(gameDir, "config/kuudraiscool/addons").toPath());
    }

    @Override public String getLaunchTarget() { return null; }
    @Override public String[] getLaunchArguments() { return new String[0]; }
}
