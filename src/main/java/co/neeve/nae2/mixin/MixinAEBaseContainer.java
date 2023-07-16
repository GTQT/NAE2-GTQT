package co.neeve.nae2.mixin;

import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import co.neeve.nae2.common.containers.ContainerPatternMultiTool;
import co.neeve.nae2.common.helpers.PlayerHelper;
import co.neeve.nae2.common.interfaces.IPatternMultiToolToolboxHost;
import co.neeve.nae2.common.slots.SlotPatternMultiTool;
import co.neeve.nae2.items.patternmultitool.ObjPatternMultiTool;
import co.neeve.nae2.items.patternmultitool.ToolPatternMultiTool;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings({ "EmptyMethod", "SameReturnValue" })
@Mixin(AEBaseContainer.class)
public class MixinAEBaseContainer extends Container {
	protected ObjPatternMultiTool patternMultiToolObject = null;
	protected ArrayList<AppEngSlot> patternMultiToolSlots = null;

	@Shadow
	public void lockPlayerInventorySlot(int idx) {}

	@Shadow
	public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
		return false;
	}

	@Shadow
	public InventoryPlayer getInventoryPlayer() {
		return null;
	}

	@Shadow
	@SuppressWarnings({ "DataFlowIssue" }) // shadowed method, never called
	protected @NotNull Slot addSlotToContainer(@NotNull Slot newSlot) {
		return null;
	}

	@SuppressWarnings("ConstantValue")
	@Inject(method = "transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;",
		at = @At(value = "INVOKE", target = "Lappeng/container/slot/AppEngSlot;isPlayerSide()Z", ordinal = 2))
	public void injectPMTSlots(EntityPlayer p, int idx, CallbackInfoReturnable<ItemStack> cir,
	                           @Local(ordinal = 1) AppEngSlot cs, @Local List<AppEngSlot> selectedSlots,
	                           @Local ItemStack tis) {
		if (!(((Object) this) instanceof ContainerPatternMultiTool) && cs instanceof SlotPatternMultiTool && cs.isItemValid(tis)) {
			selectedSlots.add(cs);
		}
	}

	@Inject(method = "transferStackInSlot(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 4))
	public void sortPMTSlots(EntityPlayer p, int idx, CallbackInfoReturnable<ItemStack> cir,
	                         @Local List<Slot> selectedSlots) {
		selectedSlots.sort(Comparator.comparing(o -> !(o instanceof SlotPatternMultiTool)));
	}

	public ObjPatternMultiTool getPatternMultiToolObject() {
		return this.patternMultiToolObject;
	}

	public List<AppEngSlot> getPatternMultiToolSlots() {
		return this.patternMultiToolSlots;
	}

	public void initializePatternMultiTool() {
		if (this instanceof IPatternMultiToolToolboxHost host) {
			final InventoryPlayer inventoryPlayer = this.getInventoryPlayer();
			final ItemStack patternMultiTool = PlayerHelper.getPatternMultiTool(inventoryPlayer.player);
			if (patternMultiTool == null) return;

			this.lockPlayerInventorySlot(inventoryPlayer.getSlotFor(patternMultiTool));
			this.patternMultiToolObject = ToolPatternMultiTool.getGuiObject(patternMultiTool);
			this.patternMultiToolSlots = new ArrayList<>();

			for (int v = 0; v < 9; v++) {
				for (int u = 0; u < 4; u++) {
					Slot slotPatternMultiTool =
						(new SlotPatternMultiTool(this.patternMultiToolObject.getPatternInventory(), host, v + u * 9,
							host.getPatternMultiToolToolboxOffsetX() + u * 18,
							host.getPatternMultiToolToolboxOffsetY() + v * 18, u, inventoryPlayer)).setPlayerSide();
					this.addSlotToContainer(slotPatternMultiTool);
					this.patternMultiToolSlots.add((AppEngSlot) slotPatternMultiTool);
				}
			}
		}
	}
}