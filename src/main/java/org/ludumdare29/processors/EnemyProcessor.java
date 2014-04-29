package org.ludumdare29.processors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import org.entityflow.entity.Entity;
import org.entityflow.system.BaseEntityProcessor;
import org.flowutils.time.Time;
import org.ludumdare29.EntityFactory;
import org.ludumdare29.components.*;

import java.util.Random;

/**
 *
 */
public class EnemyProcessor extends BaseEntityProcessor {

    private static final float PROCESSING_INTERVAL_SECONDS = 0.1f;
    private static final int MAX_ENEMIES = 100;
    private Entity player;

    private final Vector3 tempPos = new Vector3();

    private final EntityFactory entityFactory;

    public EnemyProcessor(EntityFactory entityFactory) {
        super(EnemyProcessor.class,
              PROCESSING_INTERVAL_SECONDS, EnemyAi.class, ShipComponent.class, SubmarineComponent.class, TorpedoTubeComponent.class, LocationComponent.class);
        this.entityFactory = entityFactory;
    }

    public Entity getPlayer() {
        return player;
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }

    private Random random = new Random();

    @Override protected void preProcess(Time time) {
        float spread = 1000;
        if (onProbability(0.1f) && getHandledEntities().size() < MAX_ENEMIES) {
            tempPos.set((float) random.nextGaussian() * spread,
                        (float) random.nextGaussian() * spread ,
                        (float) random.nextGaussian() * spread);

            entityFactory.createEnemySubmarine(tempPos,
                                               random.nextFloat() * random.nextFloat(),
                                               random.nextFloat() * random.nextFloat());
        }
    }

    @Override protected void processEntity(Time time, Entity entity) {
        final ShipComponent ship = entity.getComponent(ShipComponent.class);
        final TorpedoTubeComponent tube = entity.getComponent(TorpedoTubeComponent.class);
        final LocationComponent location = entity.getComponent(LocationComponent.class);
        SubmarineComponent submarine = entity.getComponent(SubmarineComponent.class);

        if (onProbability(3)) {
            tube.requestLaunch();
        }

        if (onProbability(2)) {
            ship.rudder_turns_per_second.setTarget(randomMinusOneToOne());
        }

        if (onProbability(1)) {
            ship.dieselEngineForwardThrust_N.setTarget(randomMinusOneToOne());
        }

        if (onProbability(1)) {
            submarine.altitudeTankPumpSpeed_m3_per_s.setTarget(randomMinusOneToOne());
        }

        if (onProbability(1)) {
            submarine.diveFins_turns_per_sec.setTarget(randomMinusOneToOne());
        }

        if (onProbability(1)) {
            submarine.electricalMotorThrust_N.setTarget(randomMinusOneToOne());
        }

        if (player != null) {
            final LocationComponent playerLocation = player.getComponent(LocationComponent.class);
        }


    }

    private boolean onProbability(final float percentPerSecond) {
        return random.nextFloat() < percentPerSecond * PROCESSING_INTERVAL_SECONDS / 100f;
    }

    private float randomMinusOneToOne() {return random.nextFloat()*2 -1;}
}
