package lightOverlay;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.LevelSortedDrawable;

public class OverlayDrawable extends LevelSortedDrawable {
    private final TextureDrawOptions options;

    public OverlayDrawable(int tileX, int tileY, TextureDrawOptions options) {
        super(null, tileX, tileY);
        this.options = options;
    }

    @Override
    public int getSortY() {
        return 1;
    }

    @Override
    public void draw(TickManager tickManager) {
        options.draw();
    }
}
