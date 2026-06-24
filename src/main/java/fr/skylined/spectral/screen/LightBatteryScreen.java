package fr.skylined.spectral.screen;

import fr.skylined.spectral.Spectral;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class LightBatteryScreen extends AbstractContainerScreen<LightBatteryMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Spectral.MOD_ID, "textures/gui/container/light_battery/light_battery.png");

    // Zone de remplissage de la barre PH (inner area, dans le slot baked)
    private static final int BAR_X = 9,  BAR_Y = 22,  BAR_W = 157, BAR_H = 14;
    // Zone de remplissage de la barre de décharge
    private static final int DIS_X = 9,  DIS_Y = 64,  DIS_W = 157, DIS_H = 6;

    public LightBatteryScreen(LightBatteryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, 176, 104);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX  = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY  = 6;
        this.inventoryLabelY = this.imageHeight + 10; // hors écran, cache "Inventory"
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractBackground(g, mx, my, pt);
        int x = leftPos, y = topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0f, 0f,
                imageWidth, imageHeight, 256, 256);

        long stored = menu.getStoredPhotons();
        long max    = Math.max(1L, menu.getMaxPhotons());
        boolean recv = menu.isReceiving();
        int pct = (int)(stored * 100L / max);

        // ── Section Photon Storage ────────────────────────────────────────────
        g.text(this.font, "Photon Storage",
                x + 9, y + 14, 0xFF404040, false);
        String pctStr = pct + "%";
        g.text(this.font, pctStr,
                x + 167 - this.font.width(pctStr), y + 14, 0xFF505050, false);

        // Gradient fill proportionnel au stockage
        int fillW = (int)(BAR_W * stored / max);
        if (fillW > 0) {
            g.fillGradient(
                x + BAR_X,         y + BAR_Y,
                x + BAR_X + fillW, y + BAR_Y + BAR_H,
                0xFFDD99FF, 0xFF7722BB
            );
        }
        // Texte "X / 50000 PH" centré dans la barre (visible en blanc sur fond violet)
        String barText = stored + " / " + max + " PH";
        g.text(this.font, barText,
                x + BAR_X + (BAR_W - this.font.width(barText)) / 2,
                y + BAR_Y + 3, 0xFFE8E8E8, false);

        // ── Section Status (panneau gris rows 52-78) ──────────────────────────
        g.text(this.font, "Status",
                x + 9, y + 53, 0xFF404040, false);
        String statusLabel = recv ? "Charging" : (stored > 0 ? "Discharging" : "Empty");
        int statusColor    = recv ? 0xFF33BB66 : (stored > 0 ? 0xFF4488FF : 0xFF888888);
        g.text(this.font, statusLabel,
                x + 167 - this.font.width(statusLabel), y + 53, statusColor, false);

        // Label "Discharge Rate" + valeur
        g.text(this.font, "Discharge Rate",
                x + 9, y + 60, 0xFF404040, false);
        String rateStr = "20 PH/t";
        g.text(this.font, rateStr,
                x + 167 - this.font.width(rateStr), y + 60, 0xFF505050, false);

        // Gradient fill barre de décharge (bleu = actif, vide = inactif)
        if (!recv && stored > 0) {
            g.fillGradient(
                x + DIS_X, y + DIS_Y,
                x + DIS_X + DIS_W, y + DIS_Y + DIS_H,
                0xFF88AAFF, 0xFF2244CC
            );
        }

        // ── Tip bas (rows 81+) ────────────────────────────────────────────────
        String tip = recv
                ? "Absorbing beam  +5 PH/t"
                : stored == 0
                    ? "Point a beam at this block to charge"
                    : "Feeding adjacent Light Emitter";
        g.text(this.font, tip,
                x + 9, y + 84, 0xFF666666, false);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        super.extractLabels(g, mx, my);
    }
}
