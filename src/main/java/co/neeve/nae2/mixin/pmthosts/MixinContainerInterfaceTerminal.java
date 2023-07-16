package co.neeve.nae2.mixin.pmthosts;

import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.container.slot.AppEngSlot;
import appeng.helpers.InventoryAction;
import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.util.helpers.ItemHandlerUtil;
import co.neeve.nae2.common.interfaces.IPatternMultiToolToolboxHost;
import co.neeve.nae2.mixin.MixinAEBaseContainer;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ContainerInterfaceTerminal.class)
public class MixinContainerInterfaceTerminal extends MixinAEBaseContainer implements IPatternMultiToolToolboxHost {
	@Inject(at = @At(value = "INVOKE", target = "Lappeng/container/implementations/ContainerInterfaceTerminal;" +
		"bindPlayerInventory(Lnet/minecraft/entity/player/InventoryPlayer;II)V"), method = "<init>(Lnet/minecraft" +
		"/entity/player/InventoryPlayer;" + "Lappeng/parts" + "/reporting/PartInterfaceTerminal;)V")
	public void ctor(InventoryPlayer ip, PartInterfaceTerminal anchor, CallbackInfo cb) {
		this.initializePatternMultiTool();
	}

	@Inject(method = "Lappeng/container/implementations/ContainerInterfaceTerminal;doAction" + "(Lnet/minecraft/entity"
		+ "/player/EntityPlayerMP;Lappeng/helpers/InventoryAction;IJ)V", at = @At(value = "INVOKE_ASSIGN", target =
		"Lappeng/util/InventoryAdaptor;getAdaptor" + "(Lnet/minecraft/entity/player/EntityPlayer;)" + "Lappeng/util" + "/InventoryAdaptor;", ordinal = 0), remap = false)
	public void injectPMTInventory(EntityPlayerMP player, InventoryAction action, int slot, long id, CallbackInfo ci,
	                               @Local(ordinal = 0) IItemHandler theSlot) {
		if (this.patternMultiToolObject != null) {
			IItemHandler pmtPatterns = this.patternMultiToolObject.getPatternInventory();
			List<AppEngSlot> pmtSlots = this.getPatternMultiToolSlots();
			if (pmtSlots == null) return;
			ItemStack partialIs = theSlot.getStackInSlot(0);

			for (int i = 0; i < pmtPatterns.getSlots(); i++) {
				if (!pmtSlots.get(i).isItemValid(partialIs)) continue;

				partialIs = pmtPatterns.insertItem(i, partialIs, false);
				if (partialIs.isEmpty()) {
					break;
				}
			}
			ItemHandlerUtil.setStackInSlot(theSlot, 0, partialIs);
		}
	}

	@Override
	public int getPatternMultiToolToolboxOffsetX() {
		return -63 - 18;
	}

	@Override
	public int getPatternMultiToolToolboxOffsetY() {
		return 43 + 16;
	}
}