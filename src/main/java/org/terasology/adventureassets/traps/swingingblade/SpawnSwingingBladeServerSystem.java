/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.adventureassets.traps.swingingblade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.adventureassets.traps.RequestTrapPlaceholderPrefabSelection;
import org.terasology.adventureassets.traps.TrapPlaceholderComponent;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Side;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.AddItemsToChestComponent;
import org.terasology.structureTemplates.components.ScheduleStructurePlacementComponent;
import org.terasology.structureTemplates.events.BuildStructureTemplateEntityEvent;
import org.terasology.structureTemplates.events.SpawnTemplateEvent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.internal.components.StructurePlaceholderComponent;
import org.terasology.structureTemplates.internal.events.BuildStructureTemplateStringEvent;
import org.terasology.structureTemplates.internal.events.RequestStructurePlaceholderPrefabSelection;
import org.terasology.structureTemplates.util.transform.BlockRegionTransform;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.HorizontalBlockFamily;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains the logic to make {@link SpawnSwingingBladeComponent} work.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SpawnSwingingBladeServerSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;
    @In
    private PrefabManager prefabManager;
    @In
    private BlockManager blockManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private WorldProvider worldProvider;

    private static final Logger logger = LoggerFactory.getLogger(SpawnSwingingBladeServerSystem.class);

    @ReceiveEvent
    public void onSpawnStructureWithSwingingBlade(StructureBlocksSpawnedEvent event, EntityRef entity,
                                                  SpawnSwingingBladeComponent component) {
        spawnSwingingBlades(event.getTransformation(), component);
    }

    //TODO: Remove hardcoding for Swinging blade
    @ReceiveEvent
    public void onTemplateSpawned(SpawnTemplateEvent event, EntityRef entity, SpawnSwingingBladeComponent spawnSwingingBladeComponent) {
        spawnSwingingBlades(event.getTransformation(), spawnSwingingBladeComponent);
        logger.info("mark 2");

        BlockRegionTransform transformation = event.getTransformation();
        for (SpawnSwingingBladeComponent.SwingingBlade swingingBlade : spawnSwingingBladeComponent.bladeList) {
            Vector3i actualPosition = transformation.transformVector3i(swingingBlade.position);
            Prefab selectedTrapType = prefabManager.getPrefab("AdventureAssets:SwingingBladePlaceholder");

            BlockFamily blockFamily = blockManager.getBlockFamily("AdventureAssets:TrapPlaceholder");
            HorizontalBlockFamily horizontalBlockFamily = (HorizontalBlockFamily) blockFamily;
            //TODO: Use rotation to remove FRONT hardcoding
            Block block = horizontalBlockFamily.getBlockForSide(Side.FRONT);
            Vector3i positionAbove = new Vector3i(actualPosition);
            positionAbove.addY(1);
            worldProvider.setBlock(positionAbove, block);
            EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(positionAbove);
            TrapPlaceholderComponent trapPlaceholderComponent = blockEntity.getComponent(TrapPlaceholderComponent.class);
            trapPlaceholderComponent.selectedPrefab = selectedTrapType;
            blockEntity.saveComponent(trapPlaceholderComponent);
        }
    }

    //TODO: Make changes to JSON
    @ReceiveEvent
    public void onBuildTemplateStringWithBlockRegions(BuildStructureTemplateStringEvent event, EntityRef template,
                                                      SpawnSwingingBladeComponent component) {
        logger.info("works");
    }

    private void spawnSwingingBlades(BlockRegionTransform transformation, SpawnSwingingBladeComponent component) {
        for (SpawnSwingingBladeComponent.SwingingBlade swingingBlade : component.bladeList) {
            Vector3i position = transformation.transformVector3i(swingingBlade.position);
            Quat4f rotation = transformation.transformRotation(swingingBlade.rotation);

            EntityBuilder entityBuilder = entityManager.newBuilder(swingingBlade.prefab);
            LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
            locationComponent.setWorldPosition(position.toVector3f());
            locationComponent.setWorldRotation(rotation);
            SwingingBladeComponent swingingBladeComponent = entityBuilder.getComponent(SwingingBladeComponent.class);
            swingingBladeComponent.timePeriod = swingingBlade.timePeriod;
            swingingBladeComponent.amplitude = swingingBlade.amplitude;
            swingingBladeComponent.offset = swingingBlade.offset;

            entityBuilder.build();
        }
    }

    //TODO: Remove hardcoding for SwingingBlades
    @ReceiveEvent
    public void onBuildTemplateWithScheduledTrapPlacement(BuildStructureTemplateEntityEvent event, EntityRef entity) {
        BlockRegionTransform transformToRelative = event.getTransformToRelative();
        BlockFamily blockFamily = blockManager.getBlockFamily("AdventureAssets:TrapPlaceholder");

        List<SpawnSwingingBladeComponent.SwingingBlade> bladeList = new ArrayList<>();
        for (Vector3i position: event.findAbsolutePositionsOf(blockFamily)) {
            EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(position);
            TrapPlaceholderComponent trapPlaceholderComponent = blockEntity.getComponent(TrapPlaceholderComponent.class);
            if (trapPlaceholderComponent.selectedPrefab == null) {
                continue;
            }
            BlockComponent blockComponent = blockEntity.getComponent(BlockComponent.class);
            SpawnSwingingBladeComponent.SwingingBlade swingingBlade = new SpawnSwingingBladeComponent.SwingingBlade();
            swingingBlade.position = transformToRelative.transformVector3i(blockComponent.getPosition());
            swingingBlade.position.subY(1); // placeholder is on top of marked block
            swingingBlade.rotation = transformToRelative.transformRotation(swingingBlade.rotation);
            bladeList.add(swingingBlade);
        }
        if (bladeList.size() > 0) {
            SpawnSwingingBladeComponent spawnSwingingBladeComponent = new SpawnSwingingBladeComponent();
            spawnSwingingBladeComponent.bladeList = bladeList;
            event.getTemplateEntity().addOrSaveComponent(spawnSwingingBladeComponent);
        }
    }

    @ReceiveEvent
    public void onRequestTrapPlaceholderPrefabSelection(RequestTrapPlaceholderPrefabSelection event, EntityRef characterEntity,
                                                             CharacterComponent characterComponent) {
        EntityRef interactionTarget = characterComponent.authorizedInteractionTarget;
        TrapPlaceholderComponent trapPlaceholderComponent = interactionTarget.getComponent(TrapPlaceholderComponent.class);
        if (trapPlaceholderComponent == null) {
            logger.error("Ignored RequestTrapPlaceholderPrefabSelection event since there was no interaction with a trap placeholder");
            return;
        }

        trapPlaceholderComponent.selectedPrefab = event.getPrefab();
        interactionTarget.saveComponent(trapPlaceholderComponent);
    }
}
