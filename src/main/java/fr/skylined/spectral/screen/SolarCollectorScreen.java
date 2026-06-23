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
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/container/solar_collector/solar_collector.png");

    private static final int BAR_X = 8, BAR_Y = 25, BAR_W = 159, BAR_H = 9;
    private static final int PROD_X = 8, PROD_Y = 58, PROD_W = 159, PROD_H = 7;

    public SolarCollectorScreen(SolarCollectorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 104);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight + 10;
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

        // ── Photon buffer bar ─────────────────────────────────────────
        int bufW = BAR_W * stored / max;
        if (bufW > 0) {
            g.fillGradient(x + BAR_X, y + BAR_Y,
                           x + BAR_X + bufW, y + BAR_Y + BAR_H,
                           0xFFFFDD44, 0xFFCC8800);
        }
        String bufText = stored + " / " + max + " PH";
        g.text(this.font, bufText,
                x + BAR_X + (BAR_W - this.font.width(bufText)) / 2, y + BAR_Y + 1,
                0xFF1A1A1A, false);

        // ── Labels production ─────────────────────────────────────────
        g.text(this.font, "Production", x + 9, y + 45, 0xFF404040, false);
        String prodVal = prod + " PH/t";
        g.text(this.font, prodVal,
                x + 168 - this.font.width(prodVal), y + 45, 0xFF404040, false);

        // ── Barre de production ───────────────────────────────────────
        int prodW = PROD_W * prod / 10;
        if (prodW > 0) {
            g.fillGradient(x + PROD_X, y + PROD_Y,
                           x + PROD_X + prodW, y + PROD_Y + PROD_H,
                           active ? 0xFFFFEE66 : 0xFF555544,
                           active ? 0xFFDD9900 : 0xFF333322);
        }

        // ── Infos bas ─────────────────────────────────────────────────
        String status = active ? "Active" : "Inactive";
        g.text(this.font, status, x + 9, y + 77,
                active ? 0xFF44BB66 : 0xFF886655, false);

        String sky = active ? "Sky light: clear" : "Sky blocked or nighttime";
        g.text(this.font, sky, x + 9, y + 87, 0xFF404040, false);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        super.extractLabels(g, mx, my);
    }
}
