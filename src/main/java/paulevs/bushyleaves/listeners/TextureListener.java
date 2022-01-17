package paulevs.bushyleaves.listeners;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.BlockBase;
import net.minecraft.block.Leaves;
import net.minecraft.util.maths.MathHelper;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas.Sprite;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.client.texture.atlas.CustomAtlasProvider;
import net.modificationstation.stationapi.api.registry.BlockRegistry;
import net.modificationstation.stationapi.api.registry.Identifier;
import org.jetbrains.annotations.NotNull;
import paulevs.bushyleaves.JsonUtil;

import javax.imageio.ImageIO;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TextureListener {
	private static final Map<BlockBase, Map<Byte, Integer>> TEXTURES = new HashMap<>();
	private static final Map<BlockBase, Map<Byte, Integer>> SNOWY = new HashMap<>();
	private static int[] defaultMask;
	
	public static final String MOD_ID = "bushyleaves";
	public static BufferedImage localTexture;
	
	@EventListener
	public void registerBlocks(TextureRegisterEvent event) {
		BufferedImage mask = imageFromPath("/assets/bushyleaves/textures/leaf_mask.png");
		int innerID = 0;
		
		Byte negative = Byte.valueOf((byte) -1);
		List<Byte> meta16 = new ArrayList<>(16);
		for (byte meta = 0; meta < 16; meta++) {
			meta16.add(meta);
		}
		
		Collection<LeafInfo> leaves = collectLeaves();
		
		for (LeafInfo leaf : leaves) {
			System.out.println(leaf);
			
			int lastTexture = -1;
			int lastSprite = -1;
			int lastSpriteSnow = -1;
			BlockBase block = leaf.block;
			List<Byte> metas = leaf.meta;
			if (metas.contains(negative)) {
				metas = meta16;
			}
			
			for (byte meta : metas) {
				int texture = block.getTextureForSide(0, meta);
				if (texture != lastTexture) {
					BufferedImage image = extractTexture(block, texture);
					localTexture = makeTexture(image, mask);
					Sprite sprite = Atlases.getStationTerrain().addTexture(MOD_ID + "_" + (innerID++));
					getTextures(block).put(meta, sprite.index);
					lastSprite = sprite.index;
					lastTexture = texture;
					
					localTexture = makeSnow(localTexture);
					sprite = Atlases.getStationTerrain().addTexture(MOD_ID + "_" + (innerID++));
					getTexturesSnow(block).put(meta, sprite.index);
					lastSpriteSnow = sprite.index;
					localTexture = null;
				}
				else {
					getTextures(block).put(meta, lastSprite);
					getTexturesSnow(block).put(meta, lastSpriteSnow);
				}
			}
		}
	}
	
	private Collection<LeafInfo> collectLeaves() {
		File configFile = new File(
			FabricLoader.getInstance().getConfigDir().toString(),
			"bushyleaves.json"
		);
		JsonObject config = JsonUtil.loadJson(configFile);
		
		boolean save = false;
		JsonArray exclude = config.getAsJsonArray("exclude");
		JsonArray include = config.getAsJsonArray("include");
		if (exclude == null) {
			exclude = new JsonArray();
			config.add("exclude", exclude);
			save = true;
		}
		if (include == null) {
			include = new JsonArray();
			config.add("include", include);
			save = true;
		}
		if (save) {
			JsonUtil.saveJson(configFile, config);
		}
		
		Map<String, LeafInfo> leaves = new HashMap<>();
		for (BlockBase block : BlockBase.BY_ID) {
			if (block instanceof Leaves) {
				String name = BlockRegistry.INSTANCE.getIdentifier(block).toString();
				LeafInfo info = leaves.computeIfAbsent(name, i -> new LeafInfo(block));
				info.meta.add((byte) -1);
			}
		}
		
		int size = include.size();
		for (int i = 0; i < size; i++) {
			JsonObject obj = JsonObject.class.cast(include.get(i));
			String name = obj.get("block").getAsString();
			byte meta = obj.has("meta") ? obj.get("meta").getAsByte() : -1;
			@NotNull Optional<BlockBase> optional = BlockRegistry.INSTANCE.get(Identifier.of(name));
			if (optional.isPresent()) {
				LeafInfo info = leaves.computeIfAbsent(name, j -> new LeafInfo(optional.get()));
				info.meta.add(meta);
			}
		}
		
		size = exclude.size();
		for (int i = 0; i < size; i++) {
			JsonObject obj = JsonObject.class.cast(exclude.get(i));
			String name = obj.get("block").getAsString();
			byte meta = obj.has("meta") ? obj.get("meta").getAsByte() : -1;
			if (meta == -1) {
				leaves.remove(name);
			}
			else {
				LeafInfo info = leaves.get(name);
				if (info != null) {
					int index = info.meta.indexOf(meta);
					if (index > -1) {
						info.meta.remove(index);
					}
				}
				if (info.meta.isEmpty()) {
					leaves.remove(name);
				}
			}
		}
		
		return leaves.values();
	}
	
	private Map<Byte, Integer> getTextures(BlockBase block) {
		return TEXTURES.computeIfAbsent(block, i -> new HashMap<>());
	}
	
	private Map<Byte, Integer> getTexturesSnow(BlockBase block) {
		return SNOWY.computeIfAbsent(block, i -> new HashMap<>());
	}
	
	public static int getTexture(BlockBase block, byte meta) {
		Map<Byte, Integer> map = TEXTURES.get(block);
		return map == null ? -1 : map.getOrDefault(meta, -1);
	}
	
	public static int getTextureSnow(BlockBase block, byte meta) {
		Map<Byte, Integer> map = SNOWY.get(block);
		return map == null ? -1 : map.getOrDefault(meta, -1);
	}
	
	private BufferedImage extractTexture(BlockBase block, int textureID) {
		Atlas atlas;
		if (textureID < 256) {
			atlas = Atlases.getTerrain();
		}
		else {
			atlas = ((CustomAtlasProvider) block).getAtlas().of(textureID);
		}
		
		Sprite sprite = atlas.getTexture(textureID);
		BufferedImage image = atlas.getImage();
		int x = MathHelper.floor(sprite.getStartU() * image.getWidth() + 0.5F);
		int y = MathHelper.floor(sprite.getStartV() * image.getHeight() + 0.5F);
		return image.getSubimage(x, y, sprite.getWidth(), sprite.getHeight());
	}
	
	private BufferedImage makeTexture(BufferedImage source, BufferedImage mask) {
		int w = source.getWidth();
		int h = source.getHeight();
		int side = 16;
		if (w != 16 || h != 16) {
			side = Math.max(Math.min(w, h), 16);
			side = 1 << MathHelper.floor(Math.log(side) / Math.log(2) + 0.5F);
		}
		
		int side2 = side << 1;
		BufferedImage result = new BufferedImage(side2, side2, BufferedImage.TYPE_INT_ARGB);
		Graphics g = result.getGraphics();
		g.drawImage(source, 0, 0, side, side, null);
		g.drawImage(source, side, 0, side, side, null);
		g.drawImage(source, side, side, side, side, null);
		g.drawImage(source, 0, side, side, side, null);
		
		
		int[] maskData;
		int[] imgData = new int[side2 * side2];
		if (side2 != mask.getWidth()) {
			BufferedImage mask2 = new BufferedImage(side2, side2, BufferedImage.TYPE_INT_ARGB);
			Graphics g2 = mask2.getGraphics();
			g2.drawImage(mask, 0, 0, side2, side2, null);
			mask = mask2;
			maskData = new int[imgData.length];
			mask.getRGB(0, 0, side2, side2, maskData, 0, side2);
		}
		else {
			if (defaultMask == null) {
				defaultMask = new int[side2 * side2];
				mask.getRGB(0, 0, side2, side2, defaultMask, 0, side2);
			}
			maskData = defaultMask;
		}
		
		result.getRGB(0, 0, side2, side2, imgData, 0, side2);
		for (int i = 0; i < maskData.length; i++) {
			if ((maskData[i] & 255) == 0) {
				imgData[i] = 0;
			}
		}
		result.setRGB(0, 0, side2, side2, imgData, 0, side2);
		
		return result;
	}
	
	private BufferedImage imageFromPath(String path) {
		try {
			InputStream stream = TextureListener.class.getResourceAsStream(path);
			BufferedImage image = ImageIO.read(stream);
			stream.close();
			return image;
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
		return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	}
	
	private BufferedImage makeSnow(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		BufferedImage snow = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] dataSource = new int[width * height];
		int[] dataResult = new int[dataSource.length];
		image.getRGB(0, 0, width, height, dataSource, 0, width);
		
		int white = 255 << 16 | 255 << 8 | 255;
		int gradientStart = height * 2 / 3;
		int gradientEnd = height / 3;
		
		for (int i = 0; i < dataSource.length; i++) {
			int alpha = (dataSource[i] >> 24) & 255;
			int y = i / width;
			if (y > gradientEnd) {
				if (y < gradientStart && alpha > 0) {
					if (((dataSource[i - width] >> 24) & 255) == 0) {
						dataResult[i] = alpha << 24 | white;
					}
					else if (((dataSource[i - width * 2] >> 24) & 255) == 0) {
						dataResult[i] = alpha << 24 | white;
					}
					else if (((dataSource[i - width * 3] >> 24) & 255) == 0) {
						dataResult[i] = alpha << 24 | white;
					}
				}
			}
			else if (alpha > 0) {
				dataResult[i] = alpha << 24 | white;
			}
		}
		
		snow.setRGB(0, 0, width, height, dataResult, 0, width);
		return snow;
	}
	
	private class LeafInfo {
		final List<Byte> meta = new ArrayList<>();
		final BlockBase block;
		
		private LeafInfo(BlockBase block) {
			this.block = block;
		}
	}
}
