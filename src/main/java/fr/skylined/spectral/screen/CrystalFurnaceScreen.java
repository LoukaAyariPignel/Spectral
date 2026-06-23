package fr.skylined.spectral.screen;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.client.color.WavelengthTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CrystalFurnaceScreen extends AbstractContainerScreen<CrystalFurnaceMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/container/crystal_furnace/crystal_furnace.png");
    private static final Identifier TARGET_TEX =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/container/selector/target.png");
    private static final Identifier ACTUAL_TEX =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/container/selector/actual.png");
    private static final Identifier BURN_PROGRESS_TEX =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/sprites/container/crystal_furnace/burn_progress.png");

    // Spectrum bar position in the GUI panel (matches the baked PNG)
    private static final int BAR_X = 25;
    private static final int BAR_Y = 9;
    private static final int BAR_H = 54;
    private static final float WL_MIN = 380f;
    private static final float WL_MAX = 780f;

    private static int wlToBarRow(float wl) {
        return Math.round((wl - WL_MIN) / (WL_MAX - WL_MIN) * (BAR_H - 1));
    }

    public CrystalFurnaceScreen(CrystalFurnaceMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        // Titre décalé à droite pour éviter la superposition avec le texte λ actuelle (x≈36-67)
        this.titleLabelX = Math.max(BAR_X + 50, (this.imageWidth - this.font.width(this.title)) / 2);
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        // Vanilla : imageHeight - 94 = 72 (au-dessus des slots inventaire à y=84)
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractBackground(g, mx, my, pt);
        int x = leftPos, y = topPos;

        // Background: spectrum gradient + target marker at 700 nm baked in
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f, imageWidth, imageHeight, 256, 256);

        float wl    = menu.getWavelength();
        boolean active = menu.isBeamActive();
        int eff     = menu.getEfficiencyPct();

        float optWl = menu.getOptimalWavelength();

        // Target selector — position fixe selon la longueur d'onde optimale
        if (optWl >= WL_MIN && optWl <= WL_MAX) {
            int targetRow = wlToBarRow(optWl);
            g.blit(RenderPipelines.GUI_TEXTURED, TARGET_TEX,
                    x + BAR_X, y + BAR_Y + targetRow, 0f, 0f, 9, 1, 9, 1);
            // Label centré sous le gradient (position fixe)
            String targetLabel = String.format("%.2f nm", optWl);
            int labelX = x + BAR_X + 4 - this.font.width(targetLabel) / 2;
            g.text(this.font, targetLabel, labelX, y + BAR_Y + BAR_H + 2, 0xFF707070,false);
        }

        // Actual selector — suit la longueur d'onde reçue en temps réel
        if (active && wl >= WL_MIN && wl <= WL_MAX) {
            int row = wlToBarRow(wl);
            int ay = y + BAR_Y + row;
            g.blit(RenderPipelines.GUI_TEXTURED, ACTUAL_TEX,
                    x + BAR_X, ay, 0f, 0f, 9, 1, 9, 1);
            // Label court à droite du sélecteur (~31px, reste avant le slot input à x=73)
            int wlCol = WavelengthTintSource.colorFromWavelength(wl) | 0xFF000000;
            g.text(this.font, String.format("%.2f", wl), x + BAR_X + 11, ay - 3, wlCol,false);
        }

        // Cook progress arrow
        int prog  = menu.getCookProgress();
        int total = menu.getCookTimeTotal();
        if (prog > 0 && total > 0) {
            int filled = 24 * prog / total;
            g.blit(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_TEX,
                    x + 96, y + 35, 0f, 0f, filled, 16, 24, 16);
        }

        // Efficacité sous la flèche (toujours affichée quand le beam est actif, même à 0%)
        if (active) {
            int effCol = eff >= 75 ? 0xFF00AA00 : eff >= 25 ? 0xFFAA6600 : 0xFFAA0000;
            g.text(this.font, "Eff: " + eff + "%", x + 79, y + 55, effCol,false);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        super.extractLabels(g, mx, my);
    }
}
