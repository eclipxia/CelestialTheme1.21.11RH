package org.example.celestial.hud;

import net.minecraft.world.item.ItemStack;
import org.rusherhack.client.api.feature.hud.TextHudElement;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.Setting;

import java.util.List;

/**
 * Durability of the held item (and optionally the offhand item), with a % sign in percentage mode
 */
public class CelestialDurabilityHud extends TextHudElement {

	private final BooleanSetting percentage = new BooleanSetting("Percentage", "Show the durability left as a percentage", true);
	private final BooleanSetting offhand = new BooleanSetting("Offhand", "Also show the durability of the offhand item next to the main hand", false);

	public CelestialDurabilityHud() {
		super("CelestialDurability");
		this.setDisplayName("Celestial Durability");
		this.registerSettings(this.percentage, this.offhand);
		//follow the theme color like the other HUD elements
		syncColors(this.getSettings());
	}

	private static void syncColors(List<Setting<?>> settings) {
		for(Setting<?> setting : settings) {
			if(setting instanceof ColorSetting color && color.isThemeSyncAllowed()) {
				color.setThemeSync(true);
			}
			syncColors(setting.getSubSettings());
		}
	}

	@Override
	public String getLabel() {
		return "Durability";
	}

	@Override
	public String getText() {
		if(mc.player == null) {
			return "N/A";
		}
		final String main = this.durability(mc.player.getMainHandItem());
		if(!this.offhand.getValue()) {
			return main != null ? main : "N/A";
		}
		final String off = this.durability(mc.player.getOffhandItem());
		if(main != null && off != null) {
			return main + " | " + off;
		}
		return main != null ? main : off != null ? off : "N/A";
	}

	// null when the item has no durability
	private String durability(ItemStack stack) {
		if(stack.isEmpty() || !stack.isDamageableItem()) {
			return null;
		}
		final int max = stack.getMaxDamage();
		final int left = max - stack.getDamageValue();
		return this.percentage.getValue() ? Math.round(100f * left / max) + "%" : String.valueOf(left);
	}

}
