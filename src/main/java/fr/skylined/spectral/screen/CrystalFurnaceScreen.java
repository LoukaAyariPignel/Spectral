package fr.skylined.spectral.screen;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.block.entity.CrystalFurnaceBlockEntity;
import fr.skylined.spectral.client.color.WavelengthTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CrystalFurnaceScreen extends AbstractContainerScreen<CrystalFurnaceMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/crystal_furnace.png");

    public CrystalFurnaceScreen(CrystalFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 5;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 83;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractBackground(g, mx, my, pt);
        int x = leftPos, y = topPos;
        // Blit the dark spectral background texture
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, imageWidth, imageHeight, 256, 256);

        float wl = menu.getWavelength();
        boolean active = menu.isBeamActive();
        int eff = menu.getEfficiencyPct();

        // ── Crystal beam indicator in the arrow area ──────────────────
        // Glow the input slot crystal socket (small gem glow at 58,38..69,49)
        if (active && wl > 0) {
            int wlCol = WavelengthTintSource.colorFromWavelength(wl) | 0xFF000000;
            // Light up crystal spot
            g.fill(x+58, y+38, x+70, y+50, blendColor(wlCol, 0xFF000000, 0.35f));
        }

        // ── Progress arrow (80..101 in panel, y 28..37) ───────────────
        int prog  = menu.getCookProgress();
        int total = menu.getCookTimeTotal();
        if (prog > 0 && total > 0) {
            int arrowFill = 22 * prog / total;
            int col = active && wl > 0
                    ? (WavelengthTintSource.colorFromWavelength(wl) | 0xFF000000)
                    : 0xFF554433;
            g.fill(x+80, y+28, x+80+arrowFill, y+38, col);
        }

        // ── Spectrum bar marker (7..PW-9, y=46..54) ──────────────────
        if (active && wl > 0) {
            int barW = imageWidth - 14;
            float t = (wl - 380f) / 400f;
            int markerX = 7 + (int)(t * barW);
            // White tick mark above and below spectrum
            g.fill(x+markerX-1, y+43, x+markerX+1, y+46, 0xFFFFFFFF);
            g.fill(x+markerX-1, y+54, x+markerX+1, y+57, 0xFFFFFFFF);
            // Outer glow of marker
            int wlCol = WavelengthTintSource.colorFromWavelength(wl) | 0xFF000000;
            g.fill(x+markerX, y+46, x+markerX+1, y+55, wlCol);
        }

        // ── Info strip fill (y=58..66) ───────────────────────────────
        if (active && wl > 0) {
            int wlCol = WavelengthTintSource.colorFromWavelength(wl);
            int r=(wlCol>>16)&0xFF, gr=(wlCol>>8)&0xFF, b=wlCol&0xFF;
            // λ text
            g.text(this.font, String.format("λ %.1f nm", wl), x+9, y+59, wlCol|0xFF000000);
            // Efficiency % with color
            int effCol = eff>=75 ? 0xFF44DD66 : eff>=25 ? 0xFFDDAA22 : 0xFFDD4444;
            g.text(this.font, "Eff: "+eff+"%", x+100, y+59, effCol);
        } else {
            g.text(this.font, "No beam — optimum 700 nm", x+9, y+59, 0xFF665566);
        }

        // ── Efficiency bar fill (y=71..74) ────────────────────────────
        if (active && eff > 0) {
            int barPx = (imageWidth-14) * eff / 100;
            int c1 = eff>=75 ? 0xFF228B44 : eff>=25 ? 0xFF886611 : 0xFF882222;
            int c2 = eff>=75 ? 0xFF44FF88 : eff>=25 ? 0xFFFFCC33 : 0xFFFF4444;
            g.fillGradient(x+7, y+71, x+7+barPx, y+75, c2, c1);
        }
    }

    private static int blendColor(int c1, int c2, float t) {
        int r=(int)(((c1>>16)&0xFF)*(1-t)+((c2>>16)&0xFF)*t);
        int g=(int)(((c1>>8)&0xFF)*(1-t)+((c2>>8)&0xFF)*t);
        int b=(int)((c1&0xFF)*(1-t)+(c2&0xFF)*t);
        return 0xFF000000|(r<<16)|(g<<8)|b;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        // Title (centered, white glow look)
        g.centeredText(this.font, this.title, imageWidth/2, titleLabelY, 0xFFDDCCFF);
        // Inventory label
        g.text(this.font, this.playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF8877AA);
    }
}
