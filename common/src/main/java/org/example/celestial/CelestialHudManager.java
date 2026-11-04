package org.example.celestial;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.hud.HudElement;
import org.rusherhack.client.api.render.IRenderer2D;
import org.rusherhack.client.api.render.RenderContext;
import org.rusherhack.client.api.ui.panel.PanelHandlerBase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The panel shown in the HUD editor. Lists every HUD element with its settings, same look as the ClickGUI.
 */
public class CelestialHudManager extends PanelHandlerBase<CelestialPanel> {

	public CelestialHudManager() {
		super(true);
	}

	@Override
	public CelestialPanel createPanel(String name) {
		return new CelestialPanel(this, name);
	}

	@Override
	public void initialize() {
		this.panels.clear();

		final List<HudElement> elements = new ArrayList<>();
		for(HudElement element : RusherHackAPI.getHudManager().getFeatures()) {
			elements.add(element);
		}
		elements.sort(Comparator.comparing(e -> e.getDisplayName().toLowerCase()));

		final CelestialPanel panel = this.createPanel("HUD");
		for(HudElement element : elements) {
			panel.addItem(element);
		}
		this.addPanel(panel);
		this.setDefaultPositions();
	}

	@Override
	public void setDefaultPositions() {
		for(CelestialPanel panel : this.panels) {
			panel.setX(CelestialPanel.MARGIN);
			panel.setY(CelestialPanel.MARGIN);
		}
	}

	// panels draw in absolute coordinates so hit testing matches what is drawn
	@Override
	public void renderElements(RenderContext context, double mouseX, double mouseY) {
		final IRenderer2D renderer = this.getRenderer();
		final boolean building = renderer.isBuilding();
		if(!building) Compat.beginRenderer(renderer, context, this.getFontRenderer());

		for(CelestialPanel panel : this.panels) {
			if(!this.isEnabled(panel)) continue;
			panel.render(context, mouseX, mouseY);
		}

		if(!building) renderer.end();
	}

}
