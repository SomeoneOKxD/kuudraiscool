package someoneok.kic.models.kuudra.pearls;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DoublePearlConfig {
    public int version = 1;
    public Set<String> disabledDefaults = new LinkedHashSet<>();
    public List<CustomEntry> custom = new ArrayList<>();

    public static final class CustomEntry {
        public String id;
        public double x, y, z;
        public String pre;
        public String drop;
    }
}
