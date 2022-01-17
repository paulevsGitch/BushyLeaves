package paulevs.bushyleaves.mixin;

import net.modificationstation.stationapi.api.client.texture.TextureHelper;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.client.texture.atlas.ExpandableAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import paulevs.bushyleaves.AtlasImageAdder;
import paulevs.bushyleaves.listeners.TextureListener;

import java.awt.image.BufferedImage;
import java.io.InputStream;

@Mixin(value = ExpandableAtlas.class, remap = false)
public abstract class ExpandableAtlasMixin extends Atlas implements AtlasImageAdder {
	public ExpandableAtlasMixin(String spritesheet, int size, boolean fixedSize) {
		super(spritesheet, size, fixedSize);
	}
	
	@Redirect(method = "addTexture(Ljava/lang/String;)Lnet/modificationstation/stationapi/api/client/texture/atlas/Atlas$Sprite;", at = @At(
		value = "INVOKE",
		target = "Lnet/modificationstation/stationapi/api/client/texture/TextureHelper;readTextureStream(Ljava/io/InputStream;)Ljava/awt/image/BufferedImage;"
	))
	private BufferedImage leaves_addTexture(InputStream stream) {
		if (TextureListener.localTexture != null) {
			return TextureListener.localTexture;
		}
		return TextureHelper.readTextureStream(stream);
	}
}
