package fr.skylined.spectral.screen;

import fr.skylined.spectral.block.entity.CrystalFurnaceBlockEntity;
import fr.skylined.spectral.client.color.WavelengthTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CrystalFurnaceScreen extends AbstractContainerScreen<CrystalFurnaceMenu> {

    private static final int BG      = 0xFF1C1410;
    private static final int PANEL   = 0xFF2A1E14;
    private static final int BORDER  = 0xFF5A3A28;
    private static final int SLOT_BG = 0xFF3A2A1A;
    private static final int TEXT    = 0xFFCCBBAA;
    private static final int INACTIVE= 0xFF664433;

    public CrystalFurnaceScreen(CrystalFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 166);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor g, int mx, int my, float pt) {
        int x = leftPos, y = topPos, w = imageWidth, h = imageHeight;

        // Background
        g.fill(x, y, x + w, y + h, BG);
        g.fill(x, y, x + w, y + 18, PANEL);
        // Borders
        g.fill(x, y,     x+w,   y+1,   BORDER);
        g.fill(x, y+h-1, x+w,   y+h,   BORDER);
        g.fill(x, y,     x+1,   y+h,   BORDER);
        g.fill(x+w-1, y, x+w,   y+h,   BORDER);
        g.fill(x, y+75, x+w, y+76, BORDER);

        // Title
        g.centeredText(this.font, this.title, x + w/2, y + 5, 0xFFFFAA55);

        // Slot backgrounds
        g.fill(x+43, y+34, x+61, y+52, SLOT_BG);  // input
        g.fill(x+115, y+34, x+133, y+52, SLOT_BG); // output

        // Progress arrow
        int prog = menu.getCookProgress();
        int total = menu.getCookTimeTotal();
        int arrowW = total > 0 ? (22 * prog / total) : 0;
        g.fill(x+72, y+37, x+94, y+49, 0xFF332211);         // arrow bg
        if (arrowW > 0) {
            float wl = menu.getWavelength();
            int col = wl > 0 ? WavelengthTintSource.colorFromWavelength(wl) : 0xFFFF8833;
            g.fill(x+72, y+37, x+72+arrowW, y+49, col);
        }
        g.text(this.font, ">", x+82, y+39, 0xFFFFFFFF);

        // Info panel
        boolean beamOn = menu.isBeamActive();
        float wl = menu.getWavelength();
        int eff = menu.getEfficiencyPct();

        if (beamOn && wl > 0) {
            int wlColor = WavelengthTintSource.colorFromWavelength(wl);
            g.text(this.font, String.format("λ = %.1f nm", wl), x+8, y+57, wlColor);
        } else {
            g.text(this.font, "λ = -- nm", x+8, y+57, INACTIVE);
        }

        String effStr = beamOn ? eff + "%" : "0%";
        int effCol = eff >= 75 ? 0xFF00CC66 : eff >= 25 ? 0xFFFFAA00 : 0xFFCC3333;
        g.text(this.font, "Efficiency : ", x+8, y+68, TEXT);
        g.text(this.font, effStr, x+8+this.font.width("Efficiency : "), y+68, beamOn ? effCol : INACTIVE);

        // Efficiency bar
        g.fill(x+8, y+78, x+w-8, y+82, 0xFF332211);
        if (beamOn && eff > 0) {
            int barW = (w-16) * eff / 100;
            g.fill(x+8, y+78, x+8+barW, y+82, effCol);
        }

        // Optimal wavelength hint
        g.text(this.font, "Optimum: 700 nm (red)", x+8, y+85, 0xFF886655);

        // Player inventory label
        g.text(this.font, "Inventory", x+8, y+92, TEXT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        // Suppress default labels (title + "Inventory" from default)
    }
}
