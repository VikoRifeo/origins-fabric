package io.github.apace100.origins.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.power.PowerType;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class ViewOriginScreen extends Screen {

	private static final Identifier WINDOW = new Identifier(Origins.MODID, "textures/gui/choose_origin.png");

	private Origin origin;
	private static final int windowWidth = 176;
	private static final int windowHeight = 182;
	private int scrollPos = 0;
	private int currentMaxScroll = 0;
	private int border = 13;

	private int guiTop, guiLeft;

	public ViewOriginScreen() {
		super(new TranslatableText(Origins.MODID + ".screen.view_origin"));
		this.origin = ModComponents.ORIGIN.get(MinecraftClient.getInstance().player).getOrigin();
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	protected void init() {
		super.init();
		guiLeft = (this.width - windowWidth) / 2;
        guiTop = (this.height - windowHeight) / 2;
        addButton(new ButtonWidget(guiLeft + windowWidth / 2 - 50, guiTop + windowHeight + 5, 100, 20, new TranslatableText(Origins.MODID + ".gui.close"), b -> {
			MinecraftClient.getInstance().openScreen(null);
        }));
        Origin.HUMAN.getDisplayItem().getOrCreateTag().putString("SkullOwner", MinecraftClient.getInstance().player.getDisplayName().getString());
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.renderOriginWindow(matrices, mouseX, mouseY);
		super.render(matrices, mouseX, mouseY, delta);
	}

	private void renderOriginWindow(MatrixStack matrices, int mouseX, int mouseY) {
		RenderSystem.enableBlend();
		renderWindowBackground(matrices, 16, 0);
		this.renderOriginContent(matrices, mouseX, mouseY);
		this.client.getTextureManager().bindTexture(WINDOW);
		this.drawTexture(matrices, guiLeft, guiTop, 0, 0, windowWidth, windowHeight);
		renderOriginName(matrices);
		this.client.getTextureManager().bindTexture(WINDOW);
		this.renderOriginImpact(matrices, mouseX, mouseY);
		Text title = new TranslatableText(Origins.MODID + ".gui.view_origin.title");
		this.drawCenteredString(matrices, this.textRenderer, title.getString(), width / 2, guiTop - 15, 0xFFFFFF);
		RenderSystem.disableBlend();
	}
	
	private void renderOriginImpact(MatrixStack matrices, int mouseX, int mouseY) {
		Impact impact = origin.getImpact();
		int impactValue = impact.getImpactValue();
		int wOffset = impactValue * 8;
		for(int i = 0; i < 3; i++) {
			if(i < impactValue) {
				this.drawTexture(matrices, guiLeft + 128 + i * 10, guiTop + 19, windowWidth + wOffset, 16, 8, 8);
			} else {
				this.drawTexture(matrices, guiLeft + 128 + i * 10, guiTop + 19, windowWidth, 16, 8, 8);
			}
		}
		if(mouseX >= guiLeft + 128 && mouseX <= guiLeft + 158
		&& mouseY >= guiTop + 19 && mouseY <= guiTop + 27) {
			TranslatableText ttc = (TranslatableText) new TranslatableText(Origins.MODID + ".gui.impact.impact").append(": ").append(impact.getTextComponent());
			this.renderTooltip(matrices, ttc, mouseX, mouseY);
		}
	}
	
	private void renderOriginName(MatrixStack matrices) {
		StringRenderable originName = textRenderer.trimToWidth(origin.getName(), windowWidth - 36);
		this.drawStringWithShadow(matrices, textRenderer, originName.getString(), guiLeft + 39, guiTop + 19, 0xFFFFFF);
		ItemStack is = origin.getDisplayItem();
		this.itemRenderer.renderInGui(is, guiLeft + 15, guiTop + 15);
	}
	
	private void renderWindowBackground(MatrixStack matrices, int offsetYStart, int offsetYEnd) {
		int endX = guiLeft + windowWidth - border;
		int endY = guiTop + windowHeight - border;
		this.client.getTextureManager().bindTexture(WINDOW);
		for(int x = guiLeft; x < endX; x += 16) {
			for(int y = guiTop + offsetYStart; y < endY + offsetYEnd; y += 16) {
				this.drawTexture(matrices, x, y, windowWidth, 0, Math.max(16, endX - x), Math.max(16, endY + offsetYEnd - y));
			}
		}
	}
	
	@Override
	public boolean mouseScrolled(double x, double y, double z) {
		boolean retValue = super.mouseScrolled(x, y, z);
		int np = this.scrollPos - (int)z * 4;
		this.scrollPos = np < 0 ? 0 : Math.min(np, this.currentMaxScroll);
		return retValue;
	}

	private void renderOriginContent(MatrixStack matrices, int mouseX, int mouseY) {
		int x = guiLeft + 18;
		int y = guiTop + 50;
		int startY = y;
		int endY = y - 72 + windowHeight;
		y -= scrollPos;
		
		Text orgDesc = origin.getDescription();
		List<StringRenderable> descLines = textRenderer.wrapLines(orgDesc, windowWidth - 36);
		for(StringRenderable line : descLines) {
			if(y >= startY - 18 && y <= endY + 12) {
				textRenderer.draw(matrices, line, x + 2, y - 6, 0xCCCCCC);
			}
			y += 12;
		}
		
		for(PowerType<?> p : origin.getPowerTypes()) {
			if(p.isHidden()) {
				continue;
			}
			StringRenderable name = textRenderer.trimToWidth(p.getName().formatted(Formatting.UNDERLINE), windowWidth - 36);
			Text desc = p.getDescription();
			List<StringRenderable> drawLines = textRenderer.wrapLines(desc, windowWidth - 36);
			if(y >= startY - 24 && y <= endY + 12) {
				textRenderer.draw(matrices, name, x, y, 0xFFFFFF);
			}
			for(StringRenderable line : drawLines) {
				y += 12;
				if(y >= startY - 24 && y <= endY + 12) {
					textRenderer.draw(matrices, line, x + 2, y, 0xCCCCCC);
				}
			}

			y += 14;
			
		}
		y += scrollPos;
		currentMaxScroll = y - windowHeight - 15;
		if(currentMaxScroll < 0) {
			currentMaxScroll = 0;
		}
	}
}
