package org.example.celestial;

import net.minecraft.world.item.ItemStack;
import org.rusherhack.client.api.render.IRenderer2D;
import org.rusherhack.client.api.render.RenderContext;
import org.rusherhack.client.api.render.font.IFontRenderer;

/**
 * Minecraft 26.2 specific calls. Common code only talks to this class,
 * every version module provides its own copy.
 */
public final class Compat {

	private Compat() {}

	/** blurs everything drawn before this point, no-op where the game has no such call */
	public static void blurBackground(RenderContext context) {
		try {
			context.graphics().blurBeforeThisStratum();
		} catch(IllegalStateException ignored) {
			//already blurred this frame
		}
	}

	/** starts a 2D renderer batch for the given context */
	public static void beginRenderer(IRenderer2D renderer, RenderContext context, IFontRenderer fontRenderer) {
		renderer.begin(context.graphics(), fontRenderer);
	}

	/** draws an item icon at the given position */
	public static void drawItem(RenderContext context, ItemStack stack, int x, int y) {
		context.graphics().item(stack, x, y);
	}

}
