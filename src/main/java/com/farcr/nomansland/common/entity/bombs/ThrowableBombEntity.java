package com.farcr.nomansland.common.entity.bombs;

import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowableBombEntity extends ThrowableProjectile {

    private static final int MAX_LIFE = 200;
    private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(ThrowableBombEntity.class, EntityDataSerializers.INT);

    private float oRoll;
    private float roll;

    protected ThrowableBombEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
        this.setFuse(60);
    }

    protected ThrowableBombEntity(EntityType<? extends ThrowableProjectile> entityType, double d, double e, double f, Level level) {
        super(entityType, d, e, f, level);
        this.setFuse(60);
    }

    protected ThrowableBombEntity(EntityType<? extends ThrowableProjectile> entityType, LivingEntity livingEntity, Level level) {
        super(entityType, livingEntity, level);
        this.setFuse(60);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FUSE_ID, 80);
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE_ID);
    }

    @Override
    protected void updateRotation() {
        Vec3 deltaMovement = this.getDeltaMovement();
        double distanceSq = deltaMovement.horizontalDistanceSqr();
        double distance = Math.sqrt(distanceSq);
        this.setXRot(lerpRotation(this.xRotO, (float) (Mth.atan2(deltaMovement.y, distance) * 180.0F / (float) Math.PI)));
        if (distanceSq > 0.01) {
            this.setYRot(lerpRotation(this.yRotO, (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 180.0F / (float) Math.PI)));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putShort("fuse", (short)this.getFuse());
        super.addAdditionalSaveData(nbt);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.setFuse(nbt.getShort("fuse"));
        super.readAdditionalSaveData(nbt);
    }

    public void setFuse(int life) {
        this.entityData.set(DATA_FUSE_ID, life);
    }


    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {

            this.oRoll = this.roll;

            double distance = this.getDeltaMovement().lengthSqr();
            if (distance > 0.01) {
                this.roll += Math.sqrt(distance) * 45;
            }

            if (!this.onGround()) {
                this.level().addParticle(this.getParticle(), this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 0, 0, 0);
            }
        } else {
            if (this.level().getBlockState(this.blockPosition()).is(NMLTags.BOMB_EXPLODE)) {
                this.explode();
            }
        }
    }

    protected abstract void explode();

    protected abstract ParticleOptions getParticle();

    public float getRoll(float partialTicks) {
        return Mth.lerp(partialTicks, this.oRoll, this.roll);
    }
}