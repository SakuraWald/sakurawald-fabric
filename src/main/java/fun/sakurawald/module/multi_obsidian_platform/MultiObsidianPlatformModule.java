package fun.sakurawald.module.multi_obsidian_platform;

import fun.sakurawald.ServerMain;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

@Slf4j
public class MultiObsidianPlatformModule {

    private static BlockPos findCenterEndPortalBlock(BlockPos bp) {
        ServerLevel overworld = ServerMain.SERVER.overworld();
        if (overworld.getBlockState(bp.north()) != Blocks.END_PORTAL.defaultBlockState()) {
            if (overworld.getBlockState(bp.west()) != Blocks.END_PORTAL.defaultBlockState()) {
                return bp.south().east();
            } else if (overworld.getBlockState(bp.east()) != Blocks.END_PORTAL.defaultBlockState()) {
                return bp.south().west();
            }
            return bp.south();
        }
        if (overworld.getBlockState(bp.south()) != Blocks.END_PORTAL.defaultBlockState()) {
            if (overworld.getBlockState(bp.west()) != Blocks.END_PORTAL.defaultBlockState()) {
                return bp.north().east();
            } else if (overworld.getBlockState(bp.east()) != Blocks.END_PORTAL.defaultBlockState()) {
                return bp.north().west();
            }
            return bp.north();
        }
        if (overworld.getBlockState(bp.west()) != Blocks.END_PORTAL.defaultBlockState()) {
            return bp.east();
        }
        if (overworld.getBlockState(bp.east()) != Blocks.END_PORTAL.defaultBlockState()) {
            return bp.west();
        }
        // This is the center block.
        return bp;
    }

    public static BlockPos transform(BlockPos bp) {
        bp = findCenterEndPortalBlock(bp);
        int factor = 4;
        int x = bp.getX() / factor;
        int y = 50;
        int z = bp.getZ() / factor;
        int x_offset = x % 16;
        int z_offset = z % 16;
        x -= x_offset;
        z -= z_offset;
        x += 100;
        return new BlockPos(x, y, z);
    }

    public static void makeObsidianPlatform(ServerLevel serverLevel, BlockPos centerBlockPos) {
        int i = centerBlockPos.getX();
        int j = centerBlockPos.getY() - 2;
        int k = centerBlockPos.getZ();
        BlockPos.betweenClosed(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2).forEach(blockPos -> serverLevel.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState()));
        BlockPos.betweenClosed(i - 2, j, k - 2, i + 2, j, k + 2).forEach(blockPos -> serverLevel.setBlockAndUpdate(blockPos, Blocks.OBSIDIAN.defaultBlockState()));
    }

}
