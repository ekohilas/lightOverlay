package lightOverlay;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.gameTexture.GameTexture;

public class OverlayDrawable extends LevelSortedDrawable {
    private final TextureDrawOptions options;

    public OverlayDrawable(int tileX, int tileY, GameCamera camera) {
        this(tileX, tileY, camera, LightOverlay.lowLightTexture, LightOverlay.settings.alpha);
    }

    public OverlayDrawable(int tileX, int tileY, GameCamera camera, GameTexture gameTexture, float alpha) {
        super(null, tileX, tileY);
        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);

        options = gameTexture.initDraw().alpha(alpha).pos(drawX, drawY);
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
