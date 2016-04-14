package crazypants.enderio.machine.wireless;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.enderio.core.api.client.gui.IResourceTooltipProvider;

import crazypants.enderio.BlockEio;
import crazypants.enderio.ModObject;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.paint.IPaintable;
import crazypants.enderio.paint.PainterUtil2;
import crazypants.enderio.render.EnumRenderMode;
import crazypants.enderio.render.IBlockStateWrapper;
import crazypants.enderio.render.IRenderMapper;
import crazypants.enderio.render.IRenderMapper.IItemRenderMapper;
import crazypants.enderio.render.ISmartRenderAwareBlock;
import crazypants.enderio.render.SmartModelAttacher;
import crazypants.enderio.render.pipeline.BlockStateWrapperBase;

public class BlockWirelessCharger extends BlockEio<TileWirelessCharger> implements IResourceTooltipProvider, ISmartRenderAwareBlock,
    IPaintable.IBlockPaintableBlock, IPaintable.IWrenchHideablePaint {

  public static BlockWirelessCharger create() {

    PacketHandler.INSTANCE.registerMessage(PacketStoredEnergy.class, PacketStoredEnergy.class, PacketHandler.nextID(), Side.CLIENT);

    BlockWirelessCharger res = new BlockWirelessCharger();
    res.init();
    return res;
  }

  protected BlockWirelessCharger() {
    super(ModObject.blockWirelessCharger.getUnlocalisedName(), TileWirelessCharger.class);
    setLightOpacity(1);
    initDefaultState();
  }

  protected void initDefaultState() {
    setDefaultState(this.blockState.getBaseState().withProperty(EnumRenderMode.RENDER, EnumRenderMode.AUTO));
    registerInSmartModelAttacher();
  }

  protected void registerInSmartModelAttacher() {
    SmartModelAttacher.register(this);
  }

  @Override
  protected BlockState createBlockState() {
    return new BlockState(this, new IProperty[] { EnumRenderMode.RENDER });
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState();
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return 0;
  }

  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    return getDefaultState();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public final IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
    if (state != null && world != null && pos != null) {
      IBlockStateWrapper blockStateWrapper = createBlockStateWrapper(state, world, pos);
      TileWirelessCharger tileEntity = getTileEntity(world, pos);
      if (tileEntity != null) {
        setBlockStateWrapperCache(blockStateWrapper, world, pos, tileEntity);
      }
      blockStateWrapper.bakeModel();
      return blockStateWrapper;
    } else {
      return state;
    }
  }

  protected void setBlockStateWrapperCache(@Nonnull IBlockStateWrapper blockStateWrapper, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
      @Nonnull TileWirelessCharger tileEntity) {
    blockStateWrapper.addCacheKey(tileEntity.isActive());
  }

  protected @Nonnull BlockStateWrapperBase createBlockStateWrapper(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
    return new BlockStateWrapperBase(state, world, pos, getBlockRenderMapper());
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IItemRenderMapper getItemRenderMapper() {
    return WirelessRenderMapper.instance;
  }

  @SideOnly(Side.CLIENT)
  public IRenderMapper.IBlockRenderMapper getBlockRenderMapper() {
    return WirelessRenderMapper.instance;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public String getUnlocalizedNameForTooltip(ItemStack itemStack) {
    return getUnlocalizedName();
  }

  @Override
  public boolean doNormalDrops(IBlockAccess world, BlockPos pos) {
    return false;
  }

  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
    if (stack.getTagCompound() != null) {
      TileEntity te = world.getTileEntity(pos);
      if (te instanceof TileWirelessCharger) {
        ((TileWirelessCharger) te).readContentsFromNBT(stack.getTagCompound());
        ((TileWirelessCharger) te).setPaintSource(PainterUtil2.getSourceBlock(stack));
      }
    }
  }

  @Override
  protected void processDrop(IBlockAccess world, BlockPos pos, @Nullable TileWirelessCharger te, ItemStack drop) {
    drop.setTagCompound(new NBTTagCompound());
    te.writeContentsToNBT(drop.getTagCompound());
    PainterUtil2.setSourceBlock(drop, getPaintSource(null, world, pos));
  }

  // ///////////////////////////////////////////////////////////////////////
  // PAINT START
  // ///////////////////////////////////////////////////////////////////////

  @Override
  public IBlockState getFacade(IBlockAccess world, BlockPos pos, EnumFacing side) {
    return getPaintSource(getDefaultState(), world, pos);
  }

  @Override
  public void setPaintSource(IBlockState state, IBlockAccess world, BlockPos pos, IBlockState paintSource) {
    TileWirelessCharger te = getTileEntity(world, pos);
    if (te != null) {
      te.setPaintSource(paintSource);
    }
  }

  @Override
  public void setPaintSource(Block block, ItemStack stack, IBlockState paintSource) {
    PainterUtil2.setSourceBlock(stack, paintSource);
  }

  @Override
  public IBlockState getPaintSource(IBlockState state, IBlockAccess world, BlockPos pos) {
    TileWirelessCharger te = getTileEntity(world, pos);
    if (te != null) {
      return te.getPaintSource();
    }
    return null;
  }

  @Override
  public IBlockState getPaintSource(Block block, ItemStack stack) {
    return PainterUtil2.getSourceBlock(stack);
  }

  @Override
  public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
    return true;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
    IBlockState paintSource = getPaintSource(worldIn.getBlockState(pos), worldIn, pos);
    if (paintSource != null) {
      try {
        return paintSource.getBlock().colorMultiplier(worldIn, pos, renderPass);
      } catch (Throwable e) {
      }
    }
    return super.colorMultiplier(worldIn, pos, renderPass);
  }

  // ///////////////////////////////////////////////////////////////////////
  // PAINT END
  // ///////////////////////////////////////////////////////////////////////

}
