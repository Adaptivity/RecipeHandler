package assets.recipehandler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

public class ClientEventHandler {
	private final Minecraft mc;
    public final KeyBinding key;
    public int recipeIndex;
    private ItemStack oldItem = null;

    public ClientEventHandler() {
        mc = FMLClientHandler.instance().getClient();
        key = new KeyBinding("RecipeSwitch", Keyboard.KEY_ADD, "key.categories.gui");
    }

    public void register(){
        ClientRegistry.registerKeyBinding(key);
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

	@SubscribeEvent
	public void onRenderGui(RenderGameOverlayEvent.Text event) {
		if (mc.theWorld != null && mc.thePlayer != null) {
            InventoryCrafting craft = CraftingHandler.getCraftingMatrix(mc.thePlayer.openContainer);
            if (craft != null) {
                int result = CraftingHandler.getCraftResult(craft, mc.theWorld).size();
                if (result > 1) {
                    event.right.add(StatCollector.translateToLocal("handler.found.text") + ": " + result);
                }
            }
		}
	}

    @SubscribeEvent
    public void keyDown(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && mc.theWorld != null && mc.thePlayer != null) {
            if (Keyboard.isKeyDown(key.getKeyCode())) {
                EntityClientPlayerMP player = mc.thePlayer;
                InventoryCrafting craft = CraftingHandler.getCraftingMatrix(player.openContainer);
                if (craft != null) {
                    if (recipeIndex == Integer.MAX_VALUE) {
                        recipeIndex = 0;
                    } else {
                        recipeIndex++;
                    }
                    ItemStack res = CraftingHandler.findMatchingRecipe(craft, mc.theWorld, recipeIndex);
                    if (res != null && !ItemStack.areItemStacksEqual(res, oldItem)) {
                        RecipeMod.networkWrapper.sendToServer(new ChangePacket(player, res).toProxy(Side.SERVER));
                        oldItem = res;
                    }
                }
            }
        }
    }
}
