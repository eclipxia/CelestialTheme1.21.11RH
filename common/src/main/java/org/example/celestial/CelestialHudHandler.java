package org.example.celestial;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import com.mojang.blaze3d.platform.Window;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.hud.HudElement;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.core.setting.Setting;
import org.rusherhack.client.api.render.IRenderer2D;
import org.rusherhack.client.api.render.RenderContext;
import org.rusherhack.client.api.ui.hud.HudHandlerBase;
import org.rusherhack.client.api.ui.panel.PanelHandlerBase;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Celestial look for HUD elements: translucent gradient background
 */
public class CelestialHudHandler extends HudHandlerBase {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final CelestialHudManager manager = new CelestialHudManager();

	public CelestialHudHandler() {
		super(true);
	}

	@Override
	public void tick() {
		super.tick();

		final CelestialTheme theme = CelestialPlugin.THEME;
		if(theme.layoutOnLoad.getValue() && !this.layoutApplied && Minecraft.getInstance().getWindow() != null) {
			this.layoutApplied = true;
			this.applyHudLayout();
			LOGGER.info("[Celestial] applied preset HUD layout");
		}
		// these are real toggles that stay on, the action runs when they are switched on
		// the first tick only records the saved state, so a toggle saved as on doesn't re-run its action every launch
		final boolean sync = theme.syncHudColors.getValue();
		final boolean layout = theme.applyHudLayout.getValue();
		if(!this.stateRead) {
			this.stateRead = true;
			this.syncWasOn = sync;
			this.layoutWasOn = layout;
		}
		if(sync && !this.syncWasOn) {
			this.applyHudColors();
		}
		this.syncWasOn = sync;

		if(layout && !this.layoutWasOn) {
			this.applyHudLayout();
		}
		this.layoutWasOn = layout;
	}

	private boolean stateRead = false;
	private boolean syncWasOn = false;
	private boolean layoutWasOn = false;

	private void applyHudColors() {
		RusherHackAPI.colors().primaryColor().setValue(CelestialPlugin.THEME.getPrimaryColor());
		for(HudElement element : RusherHackAPI.getHudManager().getFeatures()) {
			syncColors(element.getSettings());
		}
	}

	private static void syncColors(List<Setting<?>> settings) {
		for(Setting<?> setting : settings) {
			if(setting instanceof ColorSetting color && color.isThemeSyncAllowed()) {
				color.setThemeSync(true);
			}
			syncColors(setting.getSubSettings());
		}
	}

	// the preset is applied once per game start
	private boolean layoutApplied = false;

	private static final int STACK = 14;

	/**
	 * Preset layout. Everything is in gui units, the bottom cluster is placed relative to the centered hotbar.
	 * Edit positions in the HUD editor afterwards.
	 */
	private void applyHudLayout() {
		final Window window = Minecraft.getInstance().getWindow();
		final double w = window.getGuiScaledWidth();
		final double h = window.getGuiScaledHeight();
		final HudElement.SnapPoint topLeft = HudElement.SnapPoint.TOP_LEFT;
		final HudElement.SnapPoint bottomLeft = HudElement.SnapPoint.BOTTOM_LEFT;

		//top left, stacked
		this.place("Weather", topLeft, 0, 0);
		this.place("FPS", topLeft, 0, STACK);

		//top right
		this.place("FeatureList", HudElement.SnapPoint.TOP_RIGHT, w, 0);

		//bottom left, stacked upwards
		this.place("Position", bottomLeft, 0, h);
		this.place("Biome", bottomLeft, 0, h - STACK);

		//around the hotbar, which is 182 gui units wide and centered
		final double center = w / 2;
		this.place("PlayerView", bottomLeft, center - 142, h);
		//armor on the right half above the hunger bar
		this.place("CelestialArmor", bottomLeft, center + 11, h - 40);
		//durability sits flush on top of minecraft's own armor bar (its top edge is 49 gui units above the bottom),
		//speed is stacked above it using the real height, so the offhand line doesn't overlap it
		final double durabilityY = h - 49;
		this.place("CelestialDurability", bottomLeft, center - 91, durabilityY);
		this.place("Speed", bottomLeft, center - 91, durabilityY - this.height("CelestialDurability", STACK));
		//the Celestial versions show a % sign in percent mode, so they replace the built in ones
		this.disable("Durability");
		this.disable("Armor");
		//the Celestial layout has no watermark
		this.disable("Watermark");
		//saved settings override the elements' defaults, so make their text follow the theme color here
		for(HudElement element : RusherHackAPI.getHudManager().getFeatures()) {
			if(element.getName().equals("CelestialDurability") || element.getName().equals("CelestialArmor")) {
				syncColors(element.getSettings());
			}
		}
		this.place("Inventory", bottomLeft, center + 91 + 4, h);
	}

	/**
	 * An element keeps its alignment in a separate field that starts as top left. If it differs from the one the
	 * snap point gives, the element shifts itself by its own height on the next render, which pushed bottom
	 * aligned elements off screen. There is no setter, so the field is set directly.
	 */
	private static void syncAlignment(HudElement element) {
		try {
			final Field field = HudElement.class.getDeclaredField("alignment");
			field.setAccessible(true);
			field.set(element, HudElement.calculateAlignment(element));
		} catch(ReflectiveOperationException | RuntimeException e) {
			LOGGER.warn("[Celestial] could not set alignment of {}", element.getName(), e);
		}
	}

	private double height(String name, double fallback) {
		for(HudElement element : RusherHackAPI.getHudManager().getFeatures()) {
			if(element.getName().equalsIgnoreCase(name)) {
				try {
					return element.getHeight();
				} catch(RuntimeException e) {
					return fallback;
				}
			}
		}
		return fallback;
	}

	private void disable(String name) {
		for(HudElement element : RusherHackAPI.getHudManager().getFeatures()) {
			if(element.getName().equalsIgnoreCase(name)) {
				element.setToggled(false);
				return;
			}
		}
	}

	private void place(String name, HudElement.SnapPoint snap, double x, double y) {
		for(HudElement element : RusherHackAPI.getHudManager().getFeatures()) {
			if(element.getName().equalsIgnoreCase(name)) {
				element.setSnapPoint(snap);
				element.setX(x);
				element.setY(y);
				syncAlignment(element);
				element.setToggled(true);
				return;
			}
		}
	}

	@Override
	protected Color getBackgroundColor(HudElement hudElement, double mouseX, double mouseY) {
		return new Color(CelestialPlugin.THEME.bodyFill(), true);
	}

	@Override
	protected void renderHudElementBackground(HudElement hudElement, RenderContext renderContext, IRenderer2D renderer, double width, double height, int color) {
		CelestialPlugin.THEME.fillGradient(renderer, 0, 0, width, height, CelestialPlugin.THEME.hudOpacity.getValue());
	}

	/**
	 * The HUD editor's element list, drawn in the Celestial style
	 */
	@Override
	public PanelHandlerBase<?> getHudManagerPanel() {
		return this.manager;
	}

}
