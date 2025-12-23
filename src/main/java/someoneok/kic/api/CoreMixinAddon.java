package someoneok.kic.api;

/**
 * Marker for addons that ship mixins and must be present at launch.
 *
 * Semantics:
 * - Must be present when the game starts (otherwise cannot be activated).
 * - Not hot-reloadable or unloadable at runtime.
 */
public interface CoreMixinAddon extends ModAddon {
    // marker only
}
