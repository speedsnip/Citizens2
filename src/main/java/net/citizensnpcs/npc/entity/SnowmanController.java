package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_8_R2.Block;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.EntitySnowman;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import net.minecraft.server.v1_8_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R2.CraftServer;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftSnowman;
import org.bukkit.entity.Snowman;
import org.bukkit.util.Vector;

public class SnowmanController extends MobEntityController {
    public SnowmanController() {
        super(EntitySnowmanNPC.class);
    }

    @Override
    public Snowman getBukkitEntity() {
        return (Snowman) super.getBukkitEntity();
    }

    public static class EntitySnowmanNPC extends EntitySnowman implements NPCHolder {
        private final CitizensNPC npc;

        public EntitySnowmanNPC(World world) {
            this(world, null);
        }

        public EntitySnowmanNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        protected void a(double d0, boolean flag, Block block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        protected String bo() {
            return npc == null ? super.bo() : npc.data().get(NPC.HURT_SOUND_METADATA, super.bo());
        }

        @Override
        protected String bp() {
            return npc == null ? super.bp() : npc.data().get(NPC.DEATH_SOUND_METADATA, super.bp());
        }

        @Override
        public boolean cc() {
            if (npc == null)
                return super.cc();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.cc();
            if (super.cc()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void collide(net.minecraft.server.v1_8_R2.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        protected void D() {
            if (npc == null) {
                super.D();
            }
        }

        @Override
        public void E() {
            super.E();
            if (npc != null)
                npc.update();
        }

        @Override
        public void e(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.e(f, f1);
            }
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public void g(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.g(f, f1);
            } else {
                NMS.flyingMoveLogic(this, f, f1);
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new SnowmanNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean k_() {
            if (npc == null || !npc.isFlyable()) {
                return super.k_();
            } else {
                return false;
            }
        }

        @Override
        protected String z() {
            return npc == null || !npc.data().has(NPC.AMBIENT_SOUND_METADATA) ? super.z() : npc.data().get(
                    NPC.AMBIENT_SOUND_METADATA, super.z());
        }
    }

    public static class SnowmanNPC extends CraftSnowman implements NPCHolder {
        private final CitizensNPC npc;

        public SnowmanNPC(EntitySnowmanNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}