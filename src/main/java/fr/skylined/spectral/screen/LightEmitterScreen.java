package fr.skylined.spectral.screen;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.client.color.WavelengthTintSource;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class LightEmitterScreen extends AbstractContainerScreen<LightEmitterMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "textures/gui/light_emitter.png");

    public LightEmitterScreen(LightEmitterMenu menu, Inventory inv, Component title) {
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
        boolean emit = menu.isEmitting();
        Direction dir = menu.getFacing();

        // ── Buffer bar fill (y 34..44) ───────────────────────────────
        int barW = (imageWidth-14) * stored / max;
        if (barW > 0) {
            g.fillGradient(x+7, y+34, x+7+barW, y+44, 0xFFCC88FF, 0xFF8833CC);
        }
        g.centeredText(this.font, stored + " / " + max + " PH", x+imageWidth/2, y+36, 0xFFFFFFFF);

        // ── Compass needle ────────────────────────────────────────────
        // Draw a colored arrow on the compass at the center (cx=88, cy=74)
        int cx = x + 88, cy = y + 74;
        double angle = switch (dir) {
            case NORTH -> -Math.PI/2; case SOUTH -> Math.PI/2;
            case EAST  -> 0;          default     -> Math.PI;
        };
        int needleCol = emit ? 0xFFCC88FF : 0xFF553366;
        for (int r = 2; r <= 18; r++) {
            int nx = cx + (int)(r * Math.cos(angle));
            int ny = cy + (int)(r * Math.sin(angle));
            g.fill(nx-1, ny-1, nx+1, ny+1, needleCol);
        }
        // Crosshair center
        g.fill(cx-2, cy-1, cx+2, cy+1, 0xFF9966CC);
        g.fill(cx-1, cy-2, cx+1, cy+2, 0xFF9966CC);

        // Direction label around compass
        int LG=0xFF6633AA, ACT=emit?0xFFCC88FF:0xFF665577;
        g.centeredText(this.font, "N", x+88, y+46, LG);
        g.centeredText(this.font, "S", x+88, y+98, LG);
        g.text(this.font, "W", x+57, y+71, LG);
        g.text(this.font, "E", x+115, y+71, LG);
        // Direction name
        g.centeredText(this.font, dir.getName().toUpperCase(), x+88, y+108, ACT);

        // ── Status strip (y 97..106) ──────────────────────────────────
        String status = emit ? "● Emitting" : "○ Standby";
        g.centeredText(this.font, status, x+88, y+98, emit ? 0xFF88EEBB : 0xFF664466);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        g.centeredText(this.font, this.title, imageWidth/2, titleLabelY, 0xFFDDAAFF);
    }
}
