package net.minecraft.inventory;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ContainerMerchant extends Container {
    private final IMerchant theMerchant;
    private final InventoryMerchant merchantInventory;
    private final World theWorld;

    public ContainerMerchant(InventoryPlayer playerInventory, IMerchant merchant, World worldIn) {
        this.theMerchant = merchant;
        this.theWorld = worldIn;
        this.merchantInventory = new InventoryMerchant(playerInventory.player, merchant);
        this.addSlotToContainer(new Slot(this.merchantInventory, 0, 36, 53));
        this.addSlotToContainer(new Slot(this.merchantInventory, 1, 62, 53));
        this.addSlotToContainer(new SlotMerchantResult(playerInventory.player, merchant, this.merchantInventory, 2, 120, 53));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public InventoryMerchant getMerchantInventory() {
        return this.merchantInventory;
    }

    public void onCraftGuiOpened(ICrafting listener) {
        super.onCraftGuiOpened(listener);
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();
    }

    public void onCraftMatrixChanged(IInventory inventoryIn) {
        this.merchantInventory.resetRecipeAndSlots();
        super.onCraftMatrixChanged(inventoryIn);
    }

    public void setCurrentRecipeIndex(int currentRecipeIndex) {
        this.merchantInventory.setCurrentRecipeIndex(currentRecipeIndex);
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.theMerchant.getCustomer() == playerIn;
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = null;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 2) {
                if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
                    return null;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (index != 0 && index != 1) {
                if (index < 30) {
                    if (!this.mergeItemStack(itemstack1, 30, 39, false)) {
                        return null;
                    }
                } else if (index < 39 && !this.mergeItemStack(itemstack1, 3, 30, false)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 3, 39, false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }

    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.theMerchant.setCustomer(null);
        super.onContainerClosed(playerIn);

        if (!this.theWorld.isRemote) {
            ItemStack itemstack = this.merchantInventory.removeStackFromSlot(0);

            if (itemstack != null) {
                playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
            }

            itemstack = this.merchantInventory.removeStackFromSlot(1);

            if (itemstack != null) {
                playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
            }
        }
    }
}
