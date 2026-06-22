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
        // Standard furnace size
    }

    @Override
    protected void init() {
        super.init();
        // Center the title like vanilla furnace
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        // Blit the 176×166 panel from our 256×256 texture
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);

        drawBeamIndicator(g, x, y);
        drawProgressArrow(g, x, y);
        drawInfoStrip(g, x, y);
    }

    private void drawBeamIndicator(GuiGraphicsExtractor g, int x, int y) {
        boolean active = menu.isBeamActive();
        float wl = menu.getWavelength();
        // Fill the crystal indicator area (58,38 to 69,49 in panel coords)
        int col = active && wl > 0
                ? WavelengthTintSource.colorFromWavelength(wl)
                : 0xFF332211;
        g.fill(x + 58, y + 38, x + 70, y + 50, col | 0xFF000000);
    }

    private void drawProgressArrow(GuiGraphicsExtractor g, int x, int y) {
        int prog  = menu.getCookProgress();
        int total = menu.getCookTimeTotal();
        int w = (24 * prog / total);
        if (w <= 0) return;

        float wl = menu.getWavelength();
        int col = wl > 0
                ? WavelengthTintSource.colorFromWavelength(wl)
                : 0xFFFF8833;
        // Fill progress inside the arrow area (80,35 to 101,48) in panel coords
        g.fill(x + 80, y + 35, x + 80 + w, y + 48, col | 0xFF000000);
    }

    private void drawInfoStrip(GuiGraphicsExtractor g, int x, int y) {
        boolean active = menu.isBeamActive();
        float wl = menu.getWavelength();
        int eff = menu.getEfficiencyPct();

        // Info strip is baked at y=55 in the texture (interior at y=56 to 67)
        if (active && wl > 0) {
            int wlColor = WavelengthTintSource.colorFromWavelength(wl);
            g.text(this.font, String.format("%.1f nm", wl), x + 7, y + 57, wlColor);
        } else {
            g.text(this.font, "No beam", x + 7, y + 57, 0xFF888888);
        }

        String effStr = active ? eff + "%" : "0%";
        int effCol = eff >= 75 ? 0xFF228B22 : eff >= 25 ? 0xFF886600 : 0xFF8B0000;
        g.text(this.font, "Efficiency: " + effStr, x + 90, y + 57, active ? effCol : 0xFF888888);

        // Optimal hint, tiny text
        g.text(this.font, "Opt: 700 nm", x + 7, y + 65, 0xFF666666);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // Use default dark text color (0x404040) — classic MC style
        g.text(this.font, this.title,              this.titleLabelX, this.titleLabelY, 0x404040);
        g.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040);
    }
}
