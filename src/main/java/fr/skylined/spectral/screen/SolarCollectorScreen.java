package fr.skylined.spectral.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SolarCollectorScreen extends AbstractContainerScreen<SolarCollectorMenu> {

    private static final int BG_COLOR    = 0xFF1A1A2E;
    private static final int BAR_BG      = 0xFF2C2C44;
    private static final int BAR_FG      = 0xFFFF8C00;
    private static final int BORDER      = 0xFF4A4A6A;
    private static final int TITLE_COLOR = 0xFFFFD700;
    private static final int TEXT_COLOR  = 0xFFCCCCCC;
    private static final int ACTIVE_COL  = 0xFF00CC66;
    private static final int IDLE_COL    = 0xFFCC3333;

    public SolarCollectorScreen(SolarCollectorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 120);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // Background
        g.fill(x, y, x + w, y + h, BG_COLOR);
        g.fill(x, y, x + w, y + 18, 0xFF16213E);
        // Border
        g.fill(x,         y,         x + w,     y + 1,     BORDER);
        g.fill(x,         y + h - 1, x + w,     y + h,     BORDER);
        g.fill(x,         y,         x + 1,     y + h,     BORDER);
        g.fill(x + w - 1, y,         x + w,     y + h,     BORDER);

        // Title
        g.centeredText(this.font, this.title, x + w / 2, y + 5, TITLE_COLOR);

        int stored = menu.getStoredPhotons();
        int max    = Math.max(1, menu.getMaxPhotons());
        int prod   = menu.getProduction();

        // Energy label
        g.text(this.font, "Energy", x + 8, y + 24, TEXT_COLOR);

        // Bar
        int barX = x + 8, barY = y + 34, barW = w - 16, barH = 12;
        g.fill(barX, barY, barX + barW, barY + barH, BAR_BG);
        int fill = barW * stored / max;
        if (fill > 0) g.fill(barX, barY, barX + fill, barY + barH, BAR_FG);
        g.centeredText(this.font, stored + " / " + max + " PH", x + w / 2, barY + 2, 0xFFFFFFFF);

        // Production
        g.text(this.font, "Production : " + prod + " PH/t", x + 8, y + 54, TEXT_COLOR);

        // Status
        boolean active = prod > 0;
        g.text(this.font, "Status : ", x + 8, y + 72, TEXT_COLOR);
        g.text(this.font, active ? "Active" : "Inactive",
                x + 8 + this.font.width("Status : "), y + 72, active ? ACTIVE_COL : IDLE_COL);

        // Solar efficiency bar
        g.text(this.font, "Solar efficiency", x + 8, y + 88, TEXT_COLOR);
        int effBarY = y + 100;
        g.fill(x + 8, effBarY, x + w - 8, effBarY + 6, BAR_BG);
        int effFill = (w - 16) * prod / 10;
        if (effFill > 0) g.fill(x + 8, effBarY, x + 8 + effFill, effBarY + 6, active ? 0xFF88CC44 : IDLE_COL);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // Suppress default title + player inventory labels
    }
}
