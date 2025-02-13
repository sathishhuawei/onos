/*
 * Copyright 2016-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.net.intent.impl.compiler;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.intent.FlowObjectiveIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentCompiler;
import org.onosproject.net.intent.LinkCollectionIntent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compiler to produce flow objectives from link collections.
 */
@Component(immediate = true)
public class LinkCollectionIntentFlowObjectivesCompiler implements IntentCompiler<LinkCollectionIntent> {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentConfigurableRegistrator registrator;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    private ApplicationId appId;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.net.intent");
        registrator.registerCompiler(LinkCollectionIntent.class, this, true);
    }

    @Deactivate
    public void deactivate() {
        registrator.unregisterCompiler(LinkCollectionIntent.class, true);
    }

    @Override
    public List<Intent> compile(LinkCollectionIntent intent, List<Intent> installable) {
        SetMultimap<DeviceId, PortNumber> inputPorts = HashMultimap.create();
        SetMultimap<DeviceId, PortNumber> outputPorts = HashMultimap.create();

        for (Link link : intent.links()) {
            inputPorts.put(link.dst().deviceId(), link.dst().port());
            outputPorts.put(link.src().deviceId(), link.src().port());
        }

        for (ConnectPoint ingressPoint : intent.ingressPoints()) {
            inputPorts.put(ingressPoint.deviceId(), ingressPoint.port());
        }

        for (ConnectPoint egressPoint : intent.egressPoints()) {
            outputPorts.put(egressPoint.deviceId(), egressPoint.port());
        }

        List<Objective> objectives = new ArrayList<>();
        List<DeviceId> devices = new ArrayList<>();
        for (DeviceId deviceId: outputPorts.keys()) {
            List<Objective> deviceObjectives =
                    createRules(intent,
                                deviceId,
                                inputPorts.get(deviceId),
                                outputPorts.get(deviceId));
            deviceObjectives.forEach(objective -> {
                objectives.add(objective);
                devices.add(deviceId);
            });
        }
        return Collections.singletonList(
                new FlowObjectiveIntent(appId, devices, objectives, intent.resources()));
    }

    private List<Objective> createRules(LinkCollectionIntent intent, DeviceId deviceId,
                                       Set<PortNumber> inPorts, Set<PortNumber> outPorts) {
        Set<PortNumber> ingressPorts = intent.ingressPoints().stream()
                .filter(point -> point.deviceId().equals(deviceId))
                .map(ConnectPoint::port)
                .collect(Collectors.toSet());

        TrafficTreatment.Builder defaultTreatmentBuilder = DefaultTrafficTreatment.builder();
        outPorts.stream()
                .forEach(defaultTreatmentBuilder::setOutput);
        TrafficTreatment defaultTreatment = defaultTreatmentBuilder.build();

        TrafficTreatment.Builder ingressTreatmentBuilder = DefaultTrafficTreatment.builder(intent.treatment());
        outPorts.stream()
                .forEach(ingressTreatmentBuilder::setOutput);
        TrafficTreatment ingressTreatment = ingressTreatmentBuilder.build();

        List<Objective> objectives = new ArrayList<>(inPorts.size());
        for (PortNumber inPort: inPorts) {
            TrafficSelector selector = DefaultTrafficSelector.builder(intent.selector()).matchInPort(inPort).build();
            TrafficTreatment treatment;
            if (ingressPorts.contains(inPort)) {
                treatment = ingressTreatment;
            } else {
                treatment = defaultTreatment;
            }

            Objective objective = DefaultForwardingObjective.builder()
                    .withSelector(selector)
                    .withTreatment(treatment)
                    .withPriority(intent.priority())
                    .fromApp(appId)
                    .makePermanent()
                    .withFlag(ForwardingObjective.Flag.SPECIFIC)
                    .add();

            objectives.add(objective);
        }

        return objectives;
    }
}
