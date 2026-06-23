package fr.skylined.spectral.screen;

import fr.skylined.spectral.Spectral;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class LightEmitterScreen extends AbstractContainerScreen<LightEmitterMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/container/light_emitter/light_emitter.png");

    private static final int BAR_X = 8, BAR_Y = 19, BAR_W = 159, BAR_H = 9;

    public LightEmitterScreen(LightEmitterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 74;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractBackground(g, mx, my, pt);
        int x = leftPos, y = topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, imageWidth, imageHeight, 256, 256);

        int stored  = menu.getStoredPhotons();
        int max     = Math.max(1, menu.getMaxPhotons());
        boolean emit = menu.isEmitting();

        // ── Photon buffer bar ─────────────────────────────────────────
        int bufW = BAR_W * stored / max;
        if (bufW > 0) {
            g.fillGradient(x + BAR_X, y + BAR_Y,
                           x + BAR_X + bufW, y + BAR_Y + BAR_H,
                           0xFFDD88FF, 0xFF9933CC);
        }
        // Texte centré sans ombre
        String bufText = stored + " / " + max + " PH";
        g.text(this.font, bufText,
                x + BAR_X + (BAR_W - this.font.width(bufText)) / 2, y + BAR_Y + 1,
                0xFF1A1A1A, false);

        // ── Label gem + statut ────────────────────────────────────────
        g.text(this.font, "Gem Filter", x + 9, y + 31, 0xFF404040, false);
        String status = emit ? "Emitting" : "Standby";
        int statusCol = emit ? 0xFF2A8A55 : 0xFF886666;
        g.text(this.font, status, x + 168 - this.font.width(status), y + 31, statusCol, false);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        super.extractLabels(g, mx, my);
    }
}
