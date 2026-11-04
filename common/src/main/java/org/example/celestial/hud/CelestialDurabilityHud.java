package org.example.celestial.hud;

import net.minecraft.world.item.ItemStack;
import org.rusherhack.client.api.feature.hud.HudElement;
import org.rusherhack.client.api.render.RenderContext;
import org.rusherhack.client.api.render.font.IFontRenderer;
import org.rusherhack.client.api.setting.ColorSetting;
import org.rusherhack.core.setting.BooleanSetting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Durability of the held items, one "Mainhand: 100%" style line per hand like RusherHack's own Durability element.
 * The Offhand setting adds the offhand line below the main hand line.
 */
public class CelestialDurabilityHud extends HudElement {

	private static final double PADDING = 1;

	private final ColorSetting color = new ColorSetting("Color", new Color(255, 255, 255)).setThemeSync(true).setAlphaAllowed(false);
	private final BooleanSetting label = new BooleanSetting("Label", true);
	private final ColorSetting labelColor = new ColorSetting("Color", new Color(0, 100, 255)).setThemeSync(true).setAlphaAllowed(false);
	private final BooleanSetting colon = new BooleanSetting("Colon", true);
	private final BooleanSetting percentage = new BooleanSetting("Percentage", "Show the durability left as a percentage", true);
	private final BooleanSetting offhand = new BooleanSetting("Offhand", "Also show the durability of the offhand item below the main hand", false);

	public CelestialDurabilityHud() {
		super("CelestialDurability");
		this.setDisplayName("Celestial Durability");
		this.label.addSubSettings(this.labelColor, this.colon);
		this.registerSettings(this.color, this.label, this.percentage, this.offhand);
	}

	/** one displayed line: its label and its value */
	private record Line(String name, String value) {}

	private List<Line> lines() {
		final List<Line> lines = new ArrayList<>(2);
		if(mc.player == null) {
			lines.add(new Line("Mainhand", "N/A"));
			return lines;
		}
		lines.add(new Line("Mainhand", this.durability(mc.player.getMainHandItem())));
		if(this.offhand.getValue()) {
			lines.add(new Line("Offhand", this.durability(mc.player.getOffhandItem())));
		}
		return lines;
	}

	private String durability(ItemStack stack) {
		if(stack.isEmpty() || !stack.isDamageableItem()) {
			return "N/A";
		}
		final int max = stack.getMaxDamage();
		final int left = max - stack.getDamageValue();
		if(!this.percentage.getValue()) {
			return String.valueOf(left);
		}
		//one decimal like RusherHack's own element, without a trailing .0
		final double percent = Math.round(1000.0 * left / max) / 10.0;
		return (percent == Math.rint(percent) ? String.valueOf((int) percent) : String.valueOf(percent)) + "%";
	}

	private String prefix(Line line) {
		return this.label.getValue() ? line.name() + (this.colon.getValue() ? ": " : " ") : "";
	}

	@Override
	public void renderContent(RenderContext context, double mouseX, double mouseY) {
		final IFontRenderer fr = this.getFontRenderer();
		double y = PADDING;
		for(Line line : this.lines()) {
			double x = PADDING;
			final String prefix = this.prefix(line);
			if(!prefix.isEmpty()) {
				x = fr.drawString(prefix, x, y, this.labelColor.getValueRGB());
			}
			fr.drawString(line.value(), x, y, this.color.getValueRGB());
			y += fr.getFontHeight() + PADDING;
		}
	}

	@Override
	public double getWidth() {
		final IFontRenderer fr = this.getFontRenderer();
		double width = 0;
		for(Line line : this.lines()) {
			width = Math.max(width, fr.getStringWidth(this.prefix(line) + line.value()));
		}
		return width + PADDING * 2;
	}

	@Override
	public double getHeight() {
		return this.lines().size() * (this.getFontRenderer().getFontHeight() + PADDING) + PADDING;
	}

	//simple text, no need to re-align
	@Override
	protected boolean shouldUpdateAlignment() {
		return true;
	}

}
