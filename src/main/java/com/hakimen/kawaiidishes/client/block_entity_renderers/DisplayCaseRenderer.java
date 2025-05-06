package com.hakimen.kawaiidishes.client.block_entity_renderers;

import com.hakimen.kawaiidishes.blocks.DisplayCaseBlock;
import com.hakimen.kawaiidishes.blocks.block_entities.DisplayCaseBlockEntity;
import com.hakimen.kawaiidishes.registry.BlockRegister;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public class DisplayCaseRenderer implements BlockEntityRenderer<DisplayCaseBlockEntity> {
    BlockEntityRendererProvider.Context context;
    public DisplayCaseRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(DisplayCaseBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        int slots = pBlockEntity.getInventory().getSlots();
        for (int i = 0; i < slots; i++) {
            pPoseStack.pushPose();
            ItemStack stack = pBlockEntity.getInventory().getStackInSlot(i);
            if(!stack.equals(ItemStack.EMPTY)){
                int renderCount = stack.getMaxStackSize() == 1 ? 1 : stack.getCount() / (stack.getMaxStackSize() / 4) + 1;
                int x,y;

                x = (i % 4) % 2;
                y = (i % 4) / 2;

                pPoseStack.scale(0.33f,0.33f,0.33f);
                BlockState state = pBlockEntity.getLevel().getBlockState(pBlockEntity.getBlockPos());

                switch ((state.getBlock().equals(BlockRegister.DISPLAY_CASE.get()) ? state.getValue(HorizontalDirectionalBlock.FACING) : Direction.UP)){
                    case NORTH -> {
                        pPoseStack.translate(x + 1, (i >= 4 ? 0.5 : 1.97f), y + 1);
                    }
                    case SOUTH -> {
                        pPoseStack.mulPose(Quaternion.fromXYZ(0, (float)(Math.toRadians(180)), 0));
                        pPoseStack.translate(x - 2, (i >= 4 ? 0.5 : 1.97f), y - 2);
                    }
                    case EAST -> {
                        pPoseStack.mulPose(Quaternion.fromXYZ(0, (float)(Math.toRadians(270)), 0));
                        pPoseStack.translate(x + 1, (i >= 4 ? 0.5 : 1.97f), y - 2);
                    }
                    case WEST -> {
                        pPoseStack.mulPose(Quaternion.fromXYZ(0, (float)(Math.toRadians(90)), 0));
                        pPoseStack.translate(x - 2, (i >= 4 ? 0.5 : 1.97f), y + 1);
                    }
                }
                pPoseStack.mulPose(Quaternion.fromXYZ((float)Math.PI/2.5f,0,0));
                for (int j = 0; j < renderCount; j++) {
                    pPoseStack.mulPose(Quaternion.fromXYZ(0,0,(float)(Math.sin(j + (x + y))/4f)));
                    renderer.renderStatic(null,
                            stack,
                            ItemTransforms.TransformType.FIXED,
                            false,
                            pPoseStack,
                            pBufferSource,
                            Minecraft.getInstance().level,
                            pPackedLight,
                            pPackedOverlay,
                            0);
                    pPoseStack.translate(0f, 0f, -0.05f);
                }
            }
            pPoseStack.popPose();
        }
    }
}
