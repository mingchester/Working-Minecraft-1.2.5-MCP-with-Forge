package net.minecraft.src;

import io.github.pixee.security.BoundedLineReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.src.forge.ForgeHooks;
import net.minecraft.src.forge.ForgeHooksClient;
import net.minecraft.src.forge.MessageManager;
import net.minecraft.src.forge.ModCompatibilityClient;
import net.minecraft.src.forge.packets.ForgePacket;
import java.io.UnsupportedEncodingException;

public class NetClientHandler extends NetHandler
{
    /** True if kicked or disconnected from the server. */
    private boolean disconnected = false;

    /** Reference to the NetworkManager object. */
    private NetworkManager netManager;
    public String field_1209_a;

    /** Reference to the Minecraft object. */
    private Minecraft mc;
    private WorldClient worldClient;
    private boolean field_1210_g = false;
    public MapStorage mapStorage = new MapStorage((ISaveHandler)null);

    /** A HashMap of all player names and their player information objects */
    private Map playerInfoMap = new HashMap();

    /**
     * An ArrayList of GuiPlayerInfo (includes all the players' GuiPlayerInfo on the current server)
     */
    public List playerInfoList = new ArrayList();
    public int currentServerMaxPlayers = 20;

    /** RNG. */
    Random rand = new Random();

    public NetClientHandler(Minecraft par1Minecraft, String par2Str, int par3) throws UnknownHostException, IOException
    {
        this.mc = par1Minecraft;
        Socket var4 = new Socket(InetAddress.getByName(par2Str), par3);
        this.netManager = new NetworkManager(var4, "Client", this);
        
        ForgeHooks.onConnect(netManager);
    }

    /**
     * Processes the packets that have been read since the last call to this function.
     */
    public void processReadPackets()
    {
        if (!this.disconnected)
        {
            this.netManager.processReadPackets();
        }

        this.netManager.wakeThreads();
    }

    public void handleLogin(Packet1Login par1Packet1Login)
    {
        this.mc.playerController = new PlayerControllerMP(this.mc, this);
        this.mc.statFileWriter.readStat(StatList.joinMultiplayerStat, 1);
        this.worldClient = new WorldClient(this, new WorldSettings(0L, par1Packet1Login.serverMode, false, false, par1Packet1Login.terrainType), par1Packet1Login.field_48170_e, par1Packet1Login.difficultySetting);
        this.worldClient.isRemote = true;
        this.mc.changeWorld1(this.worldClient);
        this.mc.thePlayer.dimension = par1Packet1Login.field_48170_e;
        this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
        this.mc.thePlayer.entityId = par1Packet1Login.protocolVersion;
        this.currentServerMaxPlayers = par1Packet1Login.maxPlayers;
        ((PlayerControllerMP)this.mc.playerController).setCreative(par1Packet1Login.serverMode == 1);
        FMLClientHandler.instance().handleServerLogin(par1Packet1Login, this, netManager);
        ForgeHooksClient.onLogin(par1Packet1Login, this, netManager);
    }

    public void handlePickupSpawn(Packet21PickupSpawn par1Packet21PickupSpawn)
    {
        double var2 = (double)par1Packet21PickupSpawn.xPosition / 32.0D;
        double var4 = (double)par1Packet21PickupSpawn.yPosition / 32.0D;
        double var6 = (double)par1Packet21PickupSpawn.zPosition / 32.0D;
        EntityItem var8 = new EntityItem(this.worldClient, var2, var4, var6, new ItemStack(par1Packet21PickupSpawn.itemID, par1Packet21PickupSpawn.count, par1Packet21PickupSpawn.itemDamage));
        var8.motionX = (double)par1Packet21PickupSpawn.rotation / 128.0D;
        var8.motionY = (double)par1Packet21PickupSpawn.pitch / 128.0D;
        var8.motionZ = (double)par1Packet21PickupSpawn.roll / 128.0D;
        var8.serverPosX = par1Packet21PickupSpawn.xPosition;
        var8.serverPosY = par1Packet21PickupSpawn.yPosition;
        var8.serverPosZ = par1Packet21PickupSpawn.zPosition;
        this.worldClient.addEntityToWorld(par1Packet21PickupSpawn.entityId, var8);
    }

    public void handleVehicleSpawn(Packet23VehicleSpawn par1Packet23VehicleSpawn)
    {
        double var2 = (double)par1Packet23VehicleSpawn.xPosition / 32.0D;
        double var4 = (double)par1Packet23VehicleSpawn.yPosition / 32.0D;
        double var6 = (double)par1Packet23VehicleSpawn.zPosition / 32.0D;
        Object var8 = null;

        if (par1Packet23VehicleSpawn.type == 10)
        {
            var8 = new EntityMinecart(this.worldClient, var2, var4, var6, 0);
        }
        else if (par1Packet23VehicleSpawn.type == 11)
        {
            var8 = new EntityMinecart(this.worldClient, var2, var4, var6, 1);
        }
        else if (par1Packet23VehicleSpawn.type == 12)
        {
            var8 = new EntityMinecart(this.worldClient, var2, var4, var6, 2);
        }
        else if (par1Packet23VehicleSpawn.type == 90)
        {
            var8 = new EntityFishHook(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 60)
        {
            var8 = new EntityArrow(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 61)
        {
            var8 = new EntitySnowball(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 65)
        {
            var8 = new EntityEnderPearl(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 72)
        {
            var8 = new EntityEnderEye(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 63)
        {
            var8 = new EntityFireball(this.worldClient, var2, var4, var6, (double)par1Packet23VehicleSpawn.speedX / 8000.0D, (double)par1Packet23VehicleSpawn.speedY / 8000.0D, (double)par1Packet23VehicleSpawn.speedZ / 8000.0D);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 64)
        {
            var8 = new EntitySmallFireball(this.worldClient, var2, var4, var6, (double)par1Packet23VehicleSpawn.speedX / 8000.0D, (double)par1Packet23VehicleSpawn.speedY / 8000.0D, (double)par1Packet23VehicleSpawn.speedZ / 8000.0D);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 62)
        {
            var8 = new EntityEgg(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 73)
        {
            var8 = new EntityPotion(this.worldClient, var2, var4, var6, par1Packet23VehicleSpawn.throwerEntityId);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 75)
        {
            var8 = new EntityExpBottle(this.worldClient, var2, var4, var6);
            par1Packet23VehicleSpawn.throwerEntityId = 0;
        }
        else if (par1Packet23VehicleSpawn.type == 1)
        {
            var8 = new EntityBoat(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 50)
        {
            var8 = new EntityTNTPrimed(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 51)
        {
            var8 = new EntityEnderCrystal(this.worldClient, var2, var4, var6);
        }
        else if (par1Packet23VehicleSpawn.type == 70)
        {
            var8 = new EntityFallingSand(this.worldClient, var2, var4, var6, Block.sand.blockID);
        }
        else if (par1Packet23VehicleSpawn.type == 71)
        {
            var8 = new EntityFallingSand(this.worldClient, var2, var4, var6, Block.gravel.blockID);
        }
        else if (par1Packet23VehicleSpawn.type == 74)
        {
            var8 = new EntityFallingSand(this.worldClient, var2, var4, var6, Block.dragonEgg.blockID);
        }
        
        try 
        {
            var8 = ModCompatibilityClient.mlmpVehicleSpawn(par1Packet23VehicleSpawn.type,
                    worldClient, var2, var4, var6,
                    getEntityByID(par1Packet23VehicleSpawn.throwerEntityId), var8);
        }
        catch (Exception e)
        {
            ModLoader.getLogger().throwing("NetClientHandler", "handleVehicleSpawn", e);
            ModLoader.throwException(String.format("Error initalizing entity of type %d", par1Packet23VehicleSpawn.type), e);
            return;
        }

        if (var8 != null)
        {
            ((Entity)var8).serverPosX = par1Packet23VehicleSpawn.xPosition;
            ((Entity)var8).serverPosY = par1Packet23VehicleSpawn.yPosition;
            ((Entity)var8).serverPosZ = par1Packet23VehicleSpawn.zPosition;
            ((Entity)var8).rotationYaw = 0.0F;
            ((Entity)var8).rotationPitch = 0.0F;
            Entity[] var9 = ((Entity)var8).getParts();

            if (var9 != null)
            {
                int var10 = par1Packet23VehicleSpawn.entityId - ((Entity)var8).entityId;

                for (int var11 = 0; var11 < var9.length; ++var11)
                {
                    var9[var11].entityId += var10;
                }
            }

            ((Entity)var8).entityId = par1Packet23VehicleSpawn.entityId;
            this.worldClient.addEntityToWorld(par1Packet23VehicleSpawn.entityId, (Entity)var8);

            if (par1Packet23VehicleSpawn.throwerEntityId > 0)
            {
                if (par1Packet23VehicleSpawn.type == 60)
                {
                    Entity var12 = this.getEntityByID(par1Packet23VehicleSpawn.throwerEntityId);

                    if (var12 instanceof EntityLiving)
                    {
                        ((EntityArrow)var8).shootingEntity = (EntityLiving)var12;
                    }
                }

                ((Entity)var8).setVelocity((double)par1Packet23VehicleSpawn.speedX / 8000.0D, (double)par1Packet23VehicleSpawn.speedY / 8000.0D, (double)par1Packet23VehicleSpawn.speedZ / 8000.0D);
            }
        }
    }

    /**
     * Handle a entity experience orb packet.
     */
    public void handleEntityExpOrb(Packet26EntityExpOrb par1Packet26EntityExpOrb)
    {
        EntityXPOrb var2 = new EntityXPOrb(this.worldClient, (double)par1Packet26EntityExpOrb.posX, (double)par1Packet26EntityExpOrb.posY, (double)par1Packet26EntityExpOrb.posZ, par1Packet26EntityExpOrb.xpValue);
        var2.serverPosX = par1Packet26EntityExpOrb.posX;
        var2.serverPosY = par1Packet26EntityExpOrb.posY;
        var2.serverPosZ = par1Packet26EntityExpOrb.posZ;
        var2.rotationYaw = 0.0F;
        var2.rotationPitch = 0.0F;
        var2.entityId = par1Packet26EntityExpOrb.entityId;
        this.worldClient.addEntityToWorld(par1Packet26EntityExpOrb.entityId, var2);
    }

    /**
     * Handles weather packet
     */
    public void handleWeather(Packet71Weather par1Packet71Weather)
    {
        double var2 = (double)par1Packet71Weather.posX / 32.0D;
        double var4 = (double)par1Packet71Weather.posY / 32.0D;
        double var6 = (double)par1Packet71Weather.posZ / 32.0D;
        EntityLightningBolt var8 = null;

        if (par1Packet71Weather.isLightningBolt == 1)
        {
            var8 = new EntityLightningBolt(this.worldClient, var2, var4, var6);
        }

        if (var8 != null)
        {
            var8.serverPosX = par1Packet71Weather.posX;
            var8.serverPosY = par1Packet71Weather.posY;
            var8.serverPosZ = par1Packet71Weather.posZ;
            var8.rotationYaw = 0.0F;
            var8.rotationPitch = 0.0F;
            var8.entityId = par1Packet71Weather.entityID;
            this.worldClient.addWeatherEffect(var8);
        }
    }

    /**
     * Packet handler
     */
    public void handleEntityPainting(Packet25EntityPainting par1Packet25EntityPainting)
    {
        EntityPainting var2 = new EntityPainting(this.worldClient, par1Packet25EntityPainting.xPosition, par1Packet25EntityPainting.yPosition, par1Packet25EntityPainting.zPosition, par1Packet25EntityPainting.direction, par1Packet25EntityPainting.title);
        this.worldClient.addEntityToWorld(par1Packet25EntityPainting.entityId, var2);
    }

    /**
     * Packet handler
     */
    public void handleEntityVelocity(Packet28EntityVelocity par1Packet28EntityVelocity)
    {
        Entity var2 = this.getEntityByID(par1Packet28EntityVelocity.entityId);

        if (var2 != null)
        {
            var2.setVelocity((double)par1Packet28EntityVelocity.motionX / 8000.0D, (double)par1Packet28EntityVelocity.motionY / 8000.0D, (double)par1Packet28EntityVelocity.motionZ / 8000.0D);
        }
    }

    /**
     * Packet handler
     */
    public void handleEntityMetadata(Packet40EntityMetadata par1Packet40EntityMetadata)
    {
        Entity var2 = this.getEntityByID(par1Packet40EntityMetadata.entityId);

        if (var2 != null && par1Packet40EntityMetadata.getMetadata() != null)
        {
            var2.getDataWatcher().updateWatchedObjectsFromList(par1Packet40EntityMetadata.getMetadata());
        }
    }

    public void handleNamedEntitySpawn(Packet20NamedEntitySpawn par1Packet20NamedEntitySpawn)
    {
        double var2 = (double)par1Packet20NamedEntitySpawn.xPosition / 32.0D;
        double var4 = (double)par1Packet20NamedEntitySpawn.yPosition / 32.0D;
        double var6 = (double)par1Packet20NamedEntitySpawn.zPosition / 32.0D;
        float var8 = (float)(par1Packet20NamedEntitySpawn.rotation * 360) / 256.0F;
        float var9 = (float)(par1Packet20NamedEntitySpawn.pitch * 360) / 256.0F;
        EntityOtherPlayerMP var10 = new EntityOtherPlayerMP(this.mc.theWorld, par1Packet20NamedEntitySpawn.name);
        var10.prevPosX = var10.lastTickPosX = (double)(var10.serverPosX = par1Packet20NamedEntitySpawn.xPosition);
        var10.prevPosY = var10.lastTickPosY = (double)(var10.serverPosY = par1Packet20NamedEntitySpawn.yPosition);
        var10.prevPosZ = var10.lastTickPosZ = (double)(var10.serverPosZ = par1Packet20NamedEntitySpawn.zPosition);
        int var11 = par1Packet20NamedEntitySpawn.currentItem;

        if (var11 == 0)
        {
            var10.inventory.mainInventory[var10.inventory.currentItem] = null;
        }
        else
        {
            var10.inventory.mainInventory[var10.inventory.currentItem] = new ItemStack(var11, 1, 0);
        }

        var10.setPositionAndRotation(var2, var4, var6, var8, var9);
        this.worldClient.addEntityToWorld(par1Packet20NamedEntitySpawn.entityId, var10);
    }

    public void handleEntityTeleport(Packet34EntityTeleport par1Packet34EntityTeleport)
    {
        Entity var2 = this.getEntityByID(par1Packet34EntityTeleport.entityId);

        if (var2 != null)
        {
            var2.serverPosX = par1Packet34EntityTeleport.xPosition;
            var2.serverPosY = par1Packet34EntityTeleport.yPosition;
            var2.serverPosZ = par1Packet34EntityTeleport.zPosition;
            double var3 = (double)var2.serverPosX / 32.0D;
            double var5 = (double)var2.serverPosY / 32.0D + 0.015625D;
            double var7 = (double)var2.serverPosZ / 32.0D;
            float var9 = (float)(par1Packet34EntityTeleport.yaw * 360) / 256.0F;
            float var10 = (float)(par1Packet34EntityTeleport.pitch * 360) / 256.0F;
            var2.setPositionAndRotation2(var3, var5, var7, var9, var10, 3);
        }
    }

    public void handleEntity(Packet30Entity par1Packet30Entity)
    {
        Entity var2 = this.getEntityByID(par1Packet30Entity.entityId);

        if (var2 != null)
        {
            var2.serverPosX += par1Packet30Entity.xPosition;
            var2.serverPosY += par1Packet30Entity.yPosition;
            var2.serverPosZ += par1Packet30Entity.zPosition;
            double var3 = (double)var2.serverPosX / 32.0D;
            double var5 = (double)var2.serverPosY / 32.0D;
            double var7 = (double)var2.serverPosZ / 32.0D;
            float var9 = par1Packet30Entity.rotating ? (float)(par1Packet30Entity.yaw * 360) / 256.0F : var2.rotationYaw;
            float var10 = par1Packet30Entity.rotating ? (float)(par1Packet30Entity.pitch * 360) / 256.0F : var2.rotationPitch;
            var2.setPositionAndRotation2(var3, var5, var7, var9, var10, 3);
        }
    }

    public void handleEntityHeadRotation(Packet35EntityHeadRotation par1Packet35EntityHeadRotation)
    {
        Entity var2 = this.getEntityByID(par1Packet35EntityHeadRotation.entityId);

        if (var2 != null)
        {
            float var3 = (float)(par1Packet35EntityHeadRotation.headRotationYaw * 360) / 256.0F;
            var2.setHeadRotationYaw(var3);
        }
    }

    public void handleDestroyEntity(Packet29DestroyEntity par1Packet29DestroyEntity)
    {
        this.worldClient.removeEntityFromWorld(par1Packet29DestroyEntity.entityId);
    }

    public void handleFlying(Packet10Flying par1Packet10Flying)
    {
        EntityPlayerSP var2 = this.mc.thePlayer;
        double var3 = var2.posX;
        double var5 = var2.posY;
        double var7 = var2.posZ;
        float var9 = var2.rotationYaw;
        float var10 = var2.rotationPitch;

        if (par1Packet10Flying.moving)
        {
            var3 = par1Packet10Flying.xPosition;
            var5 = par1Packet10Flying.yPosition;
            var7 = par1Packet10Flying.zPosition;
        }

        if (par1Packet10Flying.rotating)
        {
            var9 = par1Packet10Flying.yaw;
            var10 = par1Packet10Flying.pitch;
        }

        var2.ySize = 0.0F;
        var2.motionX = var2.motionY = var2.motionZ = 0.0D;
        var2.setPositionAndRotation(var3, var5, var7, var9, var10);
        par1Packet10Flying.xPosition = var2.posX;
        par1Packet10Flying.yPosition = var2.boundingBox.minY;
        par1Packet10Flying.zPosition = var2.posZ;
        par1Packet10Flying.stance = var2.posY;
        this.netManager.addToSendQueue(par1Packet10Flying);

        if (!this.field_1210_g)
        {
            this.mc.thePlayer.prevPosX = this.mc.thePlayer.posX;
            this.mc.thePlayer.prevPosY = this.mc.thePlayer.posY;
            this.mc.thePlayer.prevPosZ = this.mc.thePlayer.posZ;
            this.field_1210_g = true;
            this.mc.displayGuiScreen((GuiScreen)null);
        }
    }

    public void handlePreChunk(Packet50PreChunk par1Packet50PreChunk)
    {
        this.worldClient.doPreChunk(par1Packet50PreChunk.xPosition, par1Packet50PreChunk.yPosition, par1Packet50PreChunk.mode);
    }

    public void handleMultiBlockChange(Packet52MultiBlockChange par1Packet52MultiBlockChange)
    {
        int var2 = par1Packet52MultiBlockChange.xPosition * 16;
        int var3 = par1Packet52MultiBlockChange.zPosition * 16;

        if (par1Packet52MultiBlockChange.metadataArray != null)
        {
            DataInputStream var4 = new DataInputStream(new ByteArrayInputStream(par1Packet52MultiBlockChange.metadataArray));

            try
            {
                for (int var5 = 0; var5 < par1Packet52MultiBlockChange.size; ++var5)
                {
                    short var6 = var4.readShort();
                    short var7 = var4.readShort();
                    int var8 = (var7 & 0xFFF0) >> 4; //4096 fix, And vanilla bug fix
                    int var9 = var7 & 15;
                    int var10 = var6 >> 12 & 15;
                    int var11 = var6 >> 8 & 15;
                    int var12 = var6 & 255;
                    this.worldClient.setBlockAndMetadataAndInvalidate(var10 + var2, var12, var11 + var3, var8, var9);
                }
            }
            catch (IOException var13)
            {
                ;
            }
        }
    }

    /**
     * Handle Packet51MapChunk (full chunk update of blocks, metadata, light levels, and optionally biome data)
     */
    public void handleMapChunk(Packet51MapChunk par1Packet51MapChunk)
    {
        this.worldClient.invalidateBlockReceiveRegion(par1Packet51MapChunk.xCh << 4, 0, par1Packet51MapChunk.zCh << 4, (par1Packet51MapChunk.xCh << 4) + 15, 256, (par1Packet51MapChunk.zCh << 4) + 15);
        Chunk var2 = this.worldClient.getChunkFromChunkCoords(par1Packet51MapChunk.xCh, par1Packet51MapChunk.zCh);

        if (par1Packet51MapChunk.includeInitialize && var2 == null)
        {
            this.worldClient.doPreChunk(par1Packet51MapChunk.xCh, par1Packet51MapChunk.zCh, true);
            var2 = this.worldClient.getChunkFromChunkCoords(par1Packet51MapChunk.xCh, par1Packet51MapChunk.zCh);
        }

        if (var2 != null)
        {
            var2.func_48494_a(par1Packet51MapChunk.chunkData, par1Packet51MapChunk.yChMin, par1Packet51MapChunk.yChMax, par1Packet51MapChunk.includeInitialize);
            this.worldClient.markBlocksDirty(par1Packet51MapChunk.xCh << 4, 0, par1Packet51MapChunk.zCh << 4, (par1Packet51MapChunk.xCh << 4) + 15, 256, (par1Packet51MapChunk.zCh << 4) + 15);

            if (!par1Packet51MapChunk.includeInitialize || !(this.worldClient.worldProvider instanceof WorldProviderSurface))
            {
                var2.resetRelightChecks();
            }
        }
    }

    public void handleBlockChange(Packet53BlockChange par1Packet53BlockChange)
    {
        this.worldClient.setBlockAndMetadataAndInvalidate(par1Packet53BlockChange.xPosition, par1Packet53BlockChange.yPosition, par1Packet53BlockChange.zPosition, par1Packet53BlockChange.type, par1Packet53BlockChange.metadata);
    }

    public void handleKickDisconnect(Packet255KickDisconnect par1Packet255KickDisconnect)
    {
        this.netManager.networkShutdown("disconnect.kicked", par1Packet255KickDisconnect.reason);
        this.disconnected = true;
        this.mc.changeWorld1((World)null);
        this.mc.displayGuiScreen(new GuiDisconnected("disconnect.disconnected", "disconnect.genericReason", new Object[] {par1Packet255KickDisconnect.reason}));
    }

    public void handleErrorMessage(String par1Str, Object[] par2ArrayOfObj)
    {
        if (!this.disconnected)
        {
            this.disconnected = true;
            this.mc.changeWorld1((World)null);
            this.mc.displayGuiScreen(new GuiDisconnected("disconnect.lost", par1Str, par2ArrayOfObj));
        }
    }

    public void quitWithPacket(Packet par1Packet)
    {
        if (!this.disconnected)
        {
            this.netManager.addToSendQueue(par1Packet);
            this.netManager.serverShutdown();
        }
    }

    /**
     * Adds the packet to the send queue
     */
    public void addToSendQueue(Packet par1Packet)
    {
        if (!this.disconnected)
        {
            this.netManager.addToSendQueue(par1Packet);
        }
    }

    public void handleCollect(Packet22Collect par1Packet22Collect)
    {
        Entity var2 = this.getEntityByID(par1Packet22Collect.collectedEntityId);
        Object var3 = (EntityLiving)this.getEntityByID(par1Packet22Collect.collectorEntityId);

        if (var3 == null)
        {
            var3 = this.mc.thePlayer;
        }

        if (var2 != null)
        {
            if (var2 instanceof EntityXPOrb)
            {
                this.worldClient.playSoundAtEntity(var2, "random.orb", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            else
            {
                this.worldClient.playSoundAtEntity(var2, "random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            this.mc.effectRenderer.addEffect(new EntityPickupFX(this.mc.theWorld, var2, (Entity)var3, -0.5F));
            this.worldClient.removeEntityFromWorld(par1Packet22Collect.collectedEntityId);
        }
    }

    public void handleChat(Packet3Chat par1Packet3Chat)
    {
        par1Packet3Chat.message = ForgeHooks.onClientChatRecv(par1Packet3Chat.message);
        if (par1Packet3Chat.message != null)
        {
            FMLClientHandler.instance().handleChatPacket(par1Packet3Chat);
            this.mc.ingameGUI.addChatMessage(par1Packet3Chat.message);
        }
    }

    public void handleAnimation(Packet18Animation par1Packet18Animation)
    {
        Entity var2 = this.getEntityByID(par1Packet18Animation.entityId);

        if (var2 != null)
        {
            EntityPlayer var3;

            if (par1Packet18Animation.animate == 1)
            {
                var3 = (EntityPlayer)var2;
                var3.swingItem();
            }
            else if (par1Packet18Animation.animate == 2)
            {
                var2.performHurtAnimation();
            }
            else if (par1Packet18Animation.animate == 3)
            {
                var3 = (EntityPlayer)var2;
                var3.wakeUpPlayer(false, false, false);
            }
            else if (par1Packet18Animation.animate == 4)
            {
                var3 = (EntityPlayer)var2;
                var3.func_6420_o();
            }
            else if (par1Packet18Animation.animate == 6)
            {
                this.mc.effectRenderer.addEffect(new EntityCrit2FX(this.mc.theWorld, var2));
            }
            else if (par1Packet18Animation.animate == 7)
            {
                EntityCrit2FX var4 = new EntityCrit2FX(this.mc.theWorld, var2, "magicCrit");
                this.mc.effectRenderer.addEffect(var4);
            }
            else if (par1Packet18Animation.animate == 5 && var2 instanceof EntityOtherPlayerMP)
            {
                ;
            }
        }
    }

    public void handleSleep(Packet17Sleep par1Packet17Sleep)
    {
        Entity var2 = this.getEntityByID(par1Packet17Sleep.entityID);

        if (var2 != null)
        {
            if (par1Packet17Sleep.field_22046_e == 0)
            {
                EntityPlayer var3 = (EntityPlayer)var2;
                var3.sleepInBedAt(par1Packet17Sleep.bedX, par1Packet17Sleep.bedY, par1Packet17Sleep.bedZ);
            }
        }
    }

    public void handleHandshake(Packet2Handshake par1Packet2Handshake)
    {
        boolean var2 = true;
        String var3 = par1Packet2Handshake.username;

        if (var3 != null && var3.trim().length() != 0)
        {
            if (!var3.equals("-"))
            {
                try
                {
                    Long.parseLong(var3, 16);
                }
                catch (NumberFormatException var8)
                {
                    var2 = false;
                }
            }
        }
        else
        {
            var2 = false;
        }

        if (!var2)
        {
            this.netManager.networkShutdown("disconnect.genericReason", new Object[] {"The server responded with an invalid server key"});
        }
        else if (par1Packet2Handshake.username.equals("-"))
        {
            this.addToSendQueue(ForgeHooksClient.onSendLogin(new Packet1Login(this.mc.session.username, 29)));
        }
        else
        {
            try
            {
                URL var4 = new URL("http://session.minecraft.net/game/joinserver.jsp?user=" + this.mc.session.username + "&sessionId=" + this.mc.session.sessionId + "&serverId=" + par1Packet2Handshake.username);
                BufferedReader var5 = new BufferedReader(new InputStreamReader(var4.openStream()));
                String var6 = BoundedLineReader.readLine(var5, 5_000_000);
                var5.close();

                if (var6.equalsIgnoreCase("ok"))
                {
                    this.addToSendQueue(ForgeHooksClient.onSendLogin(new Packet1Login(this.mc.session.username, 29)));
                }
                else
                {
                    this.netManager.networkShutdown("disconnect.loginFailedInfo", new Object[] {var6});
                }
            }
            catch (Exception var7)
            {
                var7.printStackTrace();
                this.netManager.networkShutdown("disconnect.genericReason", new Object[] {"Internal client error: " + var7.toString()});
            }
        }
    }

    /**
     * Disconnects the network connection.
     */
    public void disconnect()
    {
        this.disconnected = true;
        this.netManager.wakeThreads();
        this.netManager.networkShutdown("disconnect.closed", new Object[0]);
    }

    public void handleMobSpawn(Packet24MobSpawn par1Packet24MobSpawn)
    {
        double var2 = (double)par1Packet24MobSpawn.xPosition / 32.0D;
        double var4 = (double)par1Packet24MobSpawn.yPosition / 32.0D;
        double var6 = (double)par1Packet24MobSpawn.zPosition / 32.0D;
        float var8 = (float)(par1Packet24MobSpawn.yaw * 360) / 256.0F;
        float var9 = (float)(par1Packet24MobSpawn.pitch * 360) / 256.0F;
        EntityLiving var10 = (EntityLiving)EntityList.createEntityByID(par1Packet24MobSpawn.type, this.mc.theWorld);
        var10.serverPosX = par1Packet24MobSpawn.xPosition;
        var10.serverPosY = par1Packet24MobSpawn.yPosition;
        var10.serverPosZ = par1Packet24MobSpawn.zPosition;
        var10.rotationYawHead = (float)(par1Packet24MobSpawn.headYaw * 360) / 256.0F;
        Entity[] var11 = var10.getParts();

        if (var11 != null)
        {
            int var12 = par1Packet24MobSpawn.entityId - var10.entityId;

            for (int var13 = 0; var13 < var11.length; ++var13)
            {
                var11[var13].entityId += var12;
            }
        }

        var10.entityId = par1Packet24MobSpawn.entityId;
        var10.setPositionAndRotation(var2, var4, var6, var8, var9);
        this.worldClient.addEntityToWorld(par1Packet24MobSpawn.entityId, var10);
        List var14 = par1Packet24MobSpawn.getMetadata();

        if (var14 != null)
        {
            var10.getDataWatcher().updateWatchedObjectsFromList(var14);
        }
    }

    public void handleUpdateTime(Packet4UpdateTime par1Packet4UpdateTime)
    {
        this.mc.theWorld.setWorldTime(par1Packet4UpdateTime.time);
    }

    public void handleSpawnPosition(Packet6SpawnPosition par1Packet6SpawnPosition)
    {
        this.mc.thePlayer.setSpawnChunk(new ChunkCoordinates(par1Packet6SpawnPosition.xPosition, par1Packet6SpawnPosition.yPosition, par1Packet6SpawnPosition.zPosition));
        this.mc.theWorld.getWorldInfo().setSpawnPosition(par1Packet6SpawnPosition.xPosition, par1Packet6SpawnPosition.yPosition, par1Packet6SpawnPosition.zPosition);
    }

    /**
     * Packet handler
     */
    public void handleAttachEntity(Packet39AttachEntity par1Packet39AttachEntity)
    {
        Object var2 = this.getEntityByID(par1Packet39AttachEntity.entityId);
        Entity var3 = this.getEntityByID(par1Packet39AttachEntity.vehicleEntityId);

        if (par1Packet39AttachEntity.entityId == this.mc.thePlayer.entityId)
        {
            var2 = this.mc.thePlayer;
        }

        if (var2 != null)
        {
            ((Entity)var2).mountEntity(var3);
        }
    }

    /**
     * Packet handler
     */
    public void handleEntityStatus(Packet38EntityStatus par1Packet38EntityStatus)
    {
        Entity var2 = this.getEntityByID(par1Packet38EntityStatus.entityId);

        if (var2 != null)
        {
            var2.handleHealthUpdate(par1Packet38EntityStatus.entityStatus);
        }
    }

    private Entity getEntityByID(int par1)
    {
        return (Entity)(par1 == this.mc.thePlayer.entityId ? this.mc.thePlayer : this.worldClient.getEntityByID(par1));
    }

    /**
     * Recieves player health from the server and then proceeds to set it locally on the client.
     */
    public void handleUpdateHealth(Packet8UpdateHealth par1Packet8UpdateHealth)
    {
        this.mc.thePlayer.setHealth(par1Packet8UpdateHealth.healthMP);
        this.mc.thePlayer.getFoodStats().setFoodLevel(par1Packet8UpdateHealth.food);
        this.mc.thePlayer.getFoodStats().setFoodSaturationLevel(par1Packet8UpdateHealth.foodSaturation);
    }

    /**
     * Handle an experience packet.
     */
    public void handleExperience(Packet43Experience par1Packet43Experience)
    {
        this.mc.thePlayer.setXPStats(par1Packet43Experience.experience, par1Packet43Experience.experienceTotal, par1Packet43Experience.experienceLevel);
    }

    /**
     * respawns the player
     */
    public void handleRespawn(Packet9Respawn par1Packet9Respawn)
    {
        if (par1Packet9Respawn.respawnDimension != this.mc.thePlayer.dimension)
        {
            this.field_1210_g = false;
            this.worldClient = new WorldClient(this, new WorldSettings(0L, par1Packet9Respawn.creativeMode, false, false, par1Packet9Respawn.terrainType), par1Packet9Respawn.respawnDimension, par1Packet9Respawn.difficulty);
            this.worldClient.isRemote = true;
            this.mc.changeWorld1(this.worldClient);
            this.mc.thePlayer.dimension = par1Packet9Respawn.respawnDimension;
            this.mc.displayGuiScreen(new GuiDownloadTerrain(this));
        }

        this.mc.respawn(true, par1Packet9Respawn.respawnDimension, false);
        ((PlayerControllerMP)this.mc.playerController).setCreative(par1Packet9Respawn.creativeMode == 1);
    }

    public void handleExplosion(Packet60Explosion par1Packet60Explosion)
    {
        Explosion var2 = new Explosion(this.mc.theWorld, (Entity)null, par1Packet60Explosion.explosionX, par1Packet60Explosion.explosionY, par1Packet60Explosion.explosionZ, par1Packet60Explosion.explosionSize);
        var2.destroyedBlockPositions = par1Packet60Explosion.destroyedBlockPositions;
        var2.doExplosionB(true);
    }

    public void handleOpenWindow(Packet100OpenWindow par1Packet100OpenWindow)
    {
        EntityPlayerSP var2 = this.mc.thePlayer;

        switch (par1Packet100OpenWindow.inventoryType)
        {
            case 0:
                var2.displayGUIChest(new InventoryBasic(par1Packet100OpenWindow.windowTitle, par1Packet100OpenWindow.slotsCount));
                var2.craftingInventory.windowId = par1Packet100OpenWindow.windowId;
                break;
            case 1:
                var2.displayWorkbenchGUI(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
                var2.craftingInventory.windowId = par1Packet100OpenWindow.windowId;
                break;
            case 2:
                var2.displayGUIFurnace(new TileEntityFurnace());
                var2.craftingInventory.windowId = par1Packet100OpenWindow.windowId;
                break;
            case 3:
                var2.displayGUIDispenser(new TileEntityDispenser());
                var2.craftingInventory.windowId = par1Packet100OpenWindow.windowId;
                break;
            case 4:
                var2.displayGUIEnchantment(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
                var2.craftingInventory.windowId = par1Packet100OpenWindow.windowId;
                break;
            case 5:
                var2.displayGUIBrewingStand(new TileEntityBrewingStand());
                var2.craftingInventory.windowId = par1Packet100OpenWindow.windowId;
                break;
            default:
                ModCompatibilityClient.mlmpOpenWindow(par1Packet100OpenWindow);
        }
    }

    public void handleSetSlot(Packet103SetSlot par1Packet103SetSlot)
    {
        EntityPlayerSP var2 = this.mc.thePlayer;

        if (par1Packet103SetSlot.windowId == -1)
        {
            var2.inventory.setItemStack(par1Packet103SetSlot.myItemStack);
        }
        else if (par1Packet103SetSlot.windowId == 0 && par1Packet103SetSlot.itemSlot >= 36 && par1Packet103SetSlot.itemSlot < 45)
        {
            ItemStack var3 = var2.inventorySlots.getSlot(par1Packet103SetSlot.itemSlot).getStack();

            if (par1Packet103SetSlot.myItemStack != null && (var3 == null || var3.stackSize < par1Packet103SetSlot.myItemStack.stackSize))
            {
                par1Packet103SetSlot.myItemStack.animationsToGo = 5;
            }

            var2.inventorySlots.putStackInSlot(par1Packet103SetSlot.itemSlot, par1Packet103SetSlot.myItemStack);
        }
        else if (par1Packet103SetSlot.windowId == var2.craftingInventory.windowId)
        {
            var2.craftingInventory.putStackInSlot(par1Packet103SetSlot.itemSlot, par1Packet103SetSlot.myItemStack);
        }
    }

    public void handleTransaction(Packet106Transaction par1Packet106Transaction)
    {
        Container var2 = null;
        EntityPlayerSP var3 = this.mc.thePlayer;

        if (par1Packet106Transaction.windowId == 0)
        {
            var2 = var3.inventorySlots;
        }
        else if (par1Packet106Transaction.windowId == var3.craftingInventory.windowId)
        {
            var2 = var3.craftingInventory;
        }

        if (var2 != null)
        {
            if (par1Packet106Transaction.accepted)
            {
                var2.func_20113_a(par1Packet106Transaction.shortWindowId);
            }
            else
            {
                var2.func_20110_b(par1Packet106Transaction.shortWindowId);
                this.addToSendQueue(new Packet106Transaction(par1Packet106Transaction.windowId, par1Packet106Transaction.shortWindowId, true));
            }
        }
    }

    public void handleWindowItems(Packet104WindowItems par1Packet104WindowItems)
    {
        EntityPlayerSP var2 = this.mc.thePlayer;

        if (par1Packet104WindowItems.windowId == 0)
        {
            var2.inventorySlots.putStacksInSlots(par1Packet104WindowItems.itemStack);
        }
        else if (par1Packet104WindowItems.windowId == var2.craftingInventory.windowId)
        {
            var2.craftingInventory.putStacksInSlots(par1Packet104WindowItems.itemStack);
        }
    }

    /**
     * Updates Client side signs
     */
    public void handleUpdateSign(Packet130UpdateSign par1Packet130UpdateSign)
    {
        if (this.mc.theWorld.blockExists(par1Packet130UpdateSign.xPosition, par1Packet130UpdateSign.yPosition, par1Packet130UpdateSign.zPosition))
        {
            TileEntity var2 = this.mc.theWorld.getBlockTileEntity(par1Packet130UpdateSign.xPosition, par1Packet130UpdateSign.yPosition, par1Packet130UpdateSign.zPosition);

            if (var2 instanceof TileEntitySign)
            {
                TileEntitySign var3 = (TileEntitySign)var2;

                if (var3.isEditable())
                {
                    for (int var4 = 0; var4 < 4; ++var4)
                    {
                        var3.signText[var4] = par1Packet130UpdateSign.signLines[var4];
                    }

                    var3.onInventoryChanged();
                }
            }
        }
    }

    public void handleTileEntityData(Packet132TileEntityData par1Packet132TileEntityData)
    {
        if (this.mc.theWorld.blockExists(par1Packet132TileEntityData.xPosition, par1Packet132TileEntityData.yPosition, par1Packet132TileEntityData.zPosition))
        {
            TileEntity var2 = this.mc.theWorld.getBlockTileEntity(par1Packet132TileEntityData.xPosition, par1Packet132TileEntityData.yPosition, par1Packet132TileEntityData.zPosition);

            if (var2 != null && par1Packet132TileEntityData.actionType == 1 && var2 instanceof TileEntityMobSpawner)
            {
                ((TileEntityMobSpawner)var2).setMobID(EntityList.getStringFromID(par1Packet132TileEntityData.customParam1));
            }
            else if (var2 != null)
            {
                var2.onDataPacket(netManager,  par1Packet132TileEntityData);
            }
            else 
            {
                Packet132TileEntityData pkt = par1Packet132TileEntityData;
                ModLoader.getLogger().log(Level.WARNING, String.format(
                        "Received a TileEntityData packet for a location that did not have a TileEntity: (%d, %d, %d) %d: %d, %d, %d", 
                        pkt.xPosition, pkt.yPosition, pkt.zPosition,
                        pkt.actionType, 
                        pkt.customParam1, pkt.customParam2, pkt.customParam3));
            }
        }
    }

    public void handleUpdateProgressbar(Packet105UpdateProgressbar par1Packet105UpdateProgressbar)
    {
        EntityPlayerSP var2 = this.mc.thePlayer;
        this.registerPacket(par1Packet105UpdateProgressbar);

        if (var2.craftingInventory != null && var2.craftingInventory.windowId == par1Packet105UpdateProgressbar.windowId)
        {
            var2.craftingInventory.updateProgressBar(par1Packet105UpdateProgressbar.progressBar, par1Packet105UpdateProgressbar.progressBarValue);
        }
    }

    public void handlePlayerInventory(Packet5PlayerInventory par1Packet5PlayerInventory)
    {
        Entity var2 = this.getEntityByID(par1Packet5PlayerInventory.entityID);

        if (var2 != null)
        {
            var2.outfitWithItem(par1Packet5PlayerInventory.slot, par1Packet5PlayerInventory.itemID, par1Packet5PlayerInventory.itemDamage);
        }
    }

    public void handleCloseWindow(Packet101CloseWindow par1Packet101CloseWindow)
    {
        this.mc.thePlayer.closeScreen();
    }

    public void handlePlayNoteBlock(Packet54PlayNoteBlock par1Packet54PlayNoteBlock)
    {
        this.mc.theWorld.sendClientEvent(par1Packet54PlayNoteBlock.xLocation, par1Packet54PlayNoteBlock.yLocation, par1Packet54PlayNoteBlock.zLocation, par1Packet54PlayNoteBlock.instrumentType, par1Packet54PlayNoteBlock.pitch);
    }

    public void handleBed(Packet70Bed par1Packet70Bed)
    {
        EntityPlayerSP var2 = this.mc.thePlayer;
        int var3 = par1Packet70Bed.bedState;

        if (var3 >= 0 && var3 < Packet70Bed.bedChat.length && Packet70Bed.bedChat[var3] != null)
        {
            var2.addChatMessage(Packet70Bed.bedChat[var3]);
        }

        if (var3 == 1)
        {
            this.worldClient.getWorldInfo().setRaining(true);
            this.worldClient.setRainStrength(1.0F);
        }
        else if (var3 == 2)
        {
            this.worldClient.getWorldInfo().setRaining(false);
            this.worldClient.setRainStrength(0.0F);
        }
        else if (var3 == 3)
        {
            ((PlayerControllerMP)this.mc.playerController).setCreative(par1Packet70Bed.gameMode == 1);
        }
        else if (var3 == 4)
        {
            this.mc.displayGuiScreen(new GuiWinGame());
        }
    }

    /**
     * Contains logic for handling packets containing arbitrary unique item data. Currently this is only for maps.
     */
    public void handleMapData(Packet131MapData par1Packet131MapData)
    {
        if (par1Packet131MapData.itemID == Item.map.shiftedIndex)
        {
            ItemMap.getMPMapData(par1Packet131MapData.uniqueID, this.mc.theWorld).updateMPMapData(par1Packet131MapData.itemData);
        }
        else if (ForgeHooks.onItemDataPacket(netManager, par1Packet131MapData))
        {
            ;
        }
        else
        {
            System.out.println("Unknown itemid: " + par1Packet131MapData.uniqueID);
        }
    }

    public void handleDoorChange(Packet61DoorChange par1Packet61DoorChange)
    {
        this.mc.theWorld.playAuxSFX(par1Packet61DoorChange.sfxID, par1Packet61DoorChange.posX, par1Packet61DoorChange.posY, par1Packet61DoorChange.posZ, par1Packet61DoorChange.auxData);
    }

    /**
     * runs registerPacket on the given Packet200Statistic
     */
    public void handleStatistic(Packet200Statistic par1Packet200Statistic)
    {
        ((EntityClientPlayerMP)this.mc.thePlayer).incrementStat(StatList.getOneShotStat(par1Packet200Statistic.statisticId), par1Packet200Statistic.amount);
    }

    /**
     * Handle an entity effect packet.
     */
    public void handleEntityEffect(Packet41EntityEffect par1Packet41EntityEffect)
    {
        Entity var2 = this.getEntityByID(par1Packet41EntityEffect.entityId);

        if (var2 != null && var2 instanceof EntityLiving)
        {
            ((EntityLiving)var2).addPotionEffect(new PotionEffect(par1Packet41EntityEffect.effectId, par1Packet41EntityEffect.duration, par1Packet41EntityEffect.effectAmplifier));
        }
    }

    /**
     * Handle a remove entity effect packet.
     */
    public void handleRemoveEntityEffect(Packet42RemoveEntityEffect par1Packet42RemoveEntityEffect)
    {
        Entity var2 = this.getEntityByID(par1Packet42RemoveEntityEffect.entityId);

        if (var2 != null && var2 instanceof EntityLiving)
        {
            ((EntityLiving)var2).removePotionEffect(par1Packet42RemoveEntityEffect.effectId);
        }
    }

    /**
     * determine if it is a server handler
     */
    public boolean isServerHandler()
    {
        return false;
    }

    /**
     * Handle a player information packet.
     */
    public void handlePlayerInfo(Packet201PlayerInfo par1Packet201PlayerInfo)
    {
        GuiPlayerInfo var2 = (GuiPlayerInfo)this.playerInfoMap.get(par1Packet201PlayerInfo.playerName);

        if (var2 == null && par1Packet201PlayerInfo.isConnected)
        {
            var2 = new GuiPlayerInfo(par1Packet201PlayerInfo.playerName);
            this.playerInfoMap.put(par1Packet201PlayerInfo.playerName, var2);
            this.playerInfoList.add(var2);
        }

        if (var2 != null && !par1Packet201PlayerInfo.isConnected)
        {
            this.playerInfoMap.remove(par1Packet201PlayerInfo.playerName);
            this.playerInfoList.remove(var2);
        }

        if (par1Packet201PlayerInfo.isConnected && var2 != null)
        {
            var2.responseTime = par1Packet201PlayerInfo.ping;
        }
    }

    /**
     * Handle a keep alive packet.
     */
    public void handleKeepAlive(Packet0KeepAlive par1Packet0KeepAlive)
    {
        this.addToSendQueue(new Packet0KeepAlive(par1Packet0KeepAlive.randomId));
    }

    /**
     * Handle a player abilities packet.
     */
    public void handlePlayerAbilities(Packet202PlayerAbilities par1Packet202PlayerAbilities)
    {
        EntityPlayerSP var2 = this.mc.thePlayer;
        var2.capabilities.isFlying = par1Packet202PlayerAbilities.isFlying;
        var2.capabilities.isCreativeMode = par1Packet202PlayerAbilities.isCreativeMode;
        var2.capabilities.disableDamage = par1Packet202PlayerAbilities.disableDamage;
        var2.capabilities.allowFlying = par1Packet202PlayerAbilities.allowFlying;
    }
    
    /* (non-Javadoc)
     * @see net.minecraft.src.NetHandler#handleCustomPayload(net.minecraft.src.Packet250CustomPayload)
     */
    @Override
    public void handleCustomPayload(Packet250CustomPayload par1Packet250CustomPayload)
    {
        FMLClientHandler.instance().handlePacket250(par1Packet250CustomPayload);
        ForgeHooksClient.onCustomPayload(par1Packet250CustomPayload, netManager);
    }
}
