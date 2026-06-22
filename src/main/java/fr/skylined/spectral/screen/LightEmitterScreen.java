package fr.skylined.spectral.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class LightEmitterScreen extends AbstractContainerScreen<LightEmitterMenu> {

    private static final int BG_COLOR    = 0xFF1A1A2E;
    private static final int BAR_BG      = 0xFF2C2C44;
    private static final int BAR_FG      = 0xFF9B59B6;
    private static final int BORDER      = 0xFF4A4A6A;
    private static final int TITLE_COLOR = 0xFFBB88FF;
    private static final int TEXT_COLOR  = 0xFFCCCCCC;
    private static final int EMIT_COL    = 0xFF00CC66;
    private static final int IDLE_COL    = 0xFFCC3333;
    private static final int DIR_COL     = 0xFF88CCFF;

    public LightEmitterScreen(LightEmitterMenu menu, Inventory inv, Component title) {
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

        int stored  = menu.getStoredPhotons();
        int max     = Math.max(1, menu.getMaxPhotons());
        boolean emit = menu.isEmitting();
        Direction dir = menu.getFacing();

        // Buffer label
        g.text(this.font, "Buffer", x + 8, y + 24, TEXT_COLOR);

        // Bar
        int barX = x + 8, barY = y + 34, barW = w - 16, barH = 12;
        g.fill(barX, barY, barX + barW, barY + barH, BAR_BG);
        int fill = barW * stored / max;
        if (fill > 0) g.fill(barX, barY, barX + fill, barY + barH, BAR_FG);
        g.centeredText(this.font, stored + " / " + max + " PH", x + w / 2, barY + 2, 0xFFFFFFFF);

        // Status
        g.text(this.font, "Status : ", x + 8, y + 54, TEXT_COLOR);
        g.text(this.font, emit ? "Emitting" : "Standby",
                x + 8 + this.font.width("Status : "), y + 54, emit ? EMIT_COL : IDLE_COL);

        // Direction
        g.text(this.font, "Direction : ", x + 8, y + 72, TEXT_COLOR);
        g.text(this.font, directionName(dir),
                x + 8 + this.font.width("Direction : "), y + 72, DIR_COL);

        // Beam indicator
        int midX = x + w / 2;
        int arrY = y + 97;
        g.fill(midX - 20, arrY, midX + 20, arrY + 2, BAR_BG);
        if (emit) g.fill(midX - 18, arrY, midX + 18, arrY + 2, 0xFFFFDD44);
        g.centeredText(this.font, dirArrow(dir) + (emit ? " [ON]" : " [OFF]"),
                midX, arrY + 6, emit ? 0xFFFFDD44 : 0xFF555566);
    }

    private static String directionName(Direction dir) {
        return switch (dir) {
            case NORTH -> "North";
            case SOUTH -> "South";
            case EAST  -> "East";
            case WEST  -> "West";
            default    -> "?";
        };
    }

    private static String dirArrow(Direction dir) {
        return switch (dir) {
            case NORTH -> "^ N";
            case SOUTH -> "v S";
            case EAST  -> "> E";
            case WEST  -> "< W";
            default    -> "?";
        };
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // Suppress default labels
    }
}
