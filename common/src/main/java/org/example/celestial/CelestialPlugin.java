package org.example.celestial;

import org.example.celestial.hud.CelestialArmorHud;
import org.example.celestial.hud.CelestialDurabilityHud;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

/**
 * Registers the Celestial theme and its HUD elements
 *
 * @author Esat32
 */
public class CelestialPlugin extends Plugin {

	public static CelestialTheme THEME;

	@Override
	public void onLoad() {
		THEME = new CelestialTheme();
		RusherHackAPI.getThemeManager().registerTheme(THEME);
		RusherHackAPI.getHudManager().registerFeature(new CelestialDurabilityHud());
		RusherHackAPI.getHudManager().registerFeature(new CelestialArmorHud());
		this.getLogger().info("Celestial theme loaded");
	}

	@Override
	public void onUnload() {
		this.getLogger().info("Celestial theme unloaded");
	}

}
