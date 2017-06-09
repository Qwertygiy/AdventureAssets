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
package org.terasology.adventureassets.traps;

import org.terasology.adventureassets.traps.swingingblade.SwingingBlade;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.reflection.MappedContainer;
import org.terasology.structureTemplates.components.StructureTemplateTypeComponent;

import java.util.List;

/**
 * Add this component to a structure template entity in order to have it spawn a trap at the given position
 * of the given trap type.
 */
public class TrapsPlacementComponent implements Component {
    /** List of swinging blade type traps to spawn */
    public List<SwingingBlade> swingingBladeList;
}
