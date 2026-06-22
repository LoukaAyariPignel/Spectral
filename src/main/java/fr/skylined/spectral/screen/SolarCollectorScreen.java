package fr.skylined.spectral.screen;

import fr.skylined.spectral.Spectral;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SolarCollectorScreen extends AbstractContainerScreen<SolarCollectorMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/solar_collector.png");

    public SolarCollectorScreen(SolarCollectorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 120);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 5;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractBackground(g, mx, my, pt);
        int x = leftPos, y = topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, imageWidth, imageHeight, 256, 256);

        int stored = menu.getStoredPhotons();
        int max    = Math.max(1, menu.getMaxPhotons());
        int prod   = menu.getProduction();
        boolean active = prod > 0;

        // ── Energy bar fill (y 34..44) ──────────────────────────────
        int barW = (imageWidth - 14) * stored / max;
        if (barW > 0) {
            g.fillGradient(x+7, y+34, x+7+barW, y+44, 0xFFFFDD44, 0xFFCC8800);
        }
        // Bar text
        g.centeredText(this.font, stored + " / " + max + " PH", x+imageWidth/2, y+36, 0xFFFFFFFF);

        // ── Info strip (y 54..62) ────────────────────────────────────
        g.text(this.font, "Production: " + prod + " PH/t", x+9, y+55,
                active ? 0xFFFFCC44 : 0xFF887755);
        String status = active ? "● Active" : "○ Inactive";
        g.text(this.font, status, x+9, y+64, active ? 0xFF44EE88 : 0xFF885544);

        // ── Efficiency bar (y 70..75) ────────────────────────────────
        int effW = (imageWidth-14) * prod / 10;
        if (effW > 0) {
            g.fillGradient(x+7, y+70, x+7+effW, y+75,
                    active ? 0xFFFFCC44 : 0xFF444433,
                    active ? 0xFFCC8800 : 0xFF222211);
        }
        g.text(this.font, "Solar yield", x+9, y+79, 0xFF997744);

        // ── Status detail (y 83..92) ─────────────────────────────────
        String sky = active ? "Sky: clear" : "Sky: no light";
        g.text(this.font, sky, x+9, y+84, active ? 0xFFAACC88 : 0xFF665544);
        g.text(this.font, "Max: 10 PH/t (clear noon)", x+9, y+95, 0xFF665544);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        g.centeredText(this.font, this.title, imageWidth/2, titleLabelY, 0xFFFFDD88);
    }
}
