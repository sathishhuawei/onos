/*
 * Copyright 2015-present Open Networking Laboratory
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
package org.onosproject.vtn.manager.impl;

import static org.onosproject.net.flow.instructions.ExtensionTreatmentType.ExtensionTreatmentTypes.NICIRA_SET_TUNNEL_DST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.util.KryoNamespace;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.behaviour.BridgeConfig;
import org.onosproject.net.behaviour.BridgeDescription;
import org.onosproject.net.behaviour.ExtensionTreatmentResolver;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.BasicDeviceConfig;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.driver.DriverHandler;
import org.onosproject.net.driver.DriverService;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficTreatment.Builder;
import org.onosproject.net.flow.instructions.ExtensionTreatment;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.group.DefaultGroupBucket;
import org.onosproject.net.group.DefaultGroupDescription;
import org.onosproject.net.group.DefaultGroupKey;
import org.onosproject.net.group.GroupBucket;
import org.onosproject.net.group.GroupBuckets;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.GroupKey;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.LogicalClockService;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.Versioned;
import org.onosproject.vtn.manager.VtnService;
import org.onosproject.vtn.table.ArpService;
import org.onosproject.vtn.table.ClassifierService;
import org.onosproject.vtn.table.DnatService;
import org.onosproject.vtn.table.L2ForwardService;
import org.onosproject.vtn.table.L3ForwardService;
import org.onosproject.vtn.table.SnatService;
import org.onosproject.vtn.table.impl.ArpServiceImpl;
import org.onosproject.vtn.table.impl.ClassifierServiceImpl;
import org.onosproject.vtn.table.impl.DnatServiceImpl;
import org.onosproject.vtn.table.impl.L2ForwardServiceImpl;
import org.onosproject.vtn.table.impl.L3ForwardServiceImpl;
import org.onosproject.vtn.table.impl.SnatServiceImpl;
import org.onosproject.vtn.util.DataPathIdGenerator;
import org.onosproject.vtn.util.VtnConfig;
import org.onosproject.vtn.util.VtnData;
import org.onosproject.vtnrsc.AllowedAddressPair;
import org.onosproject.vtnrsc.BindingHostId;
import org.onosproject.vtnrsc.DefaultVirtualPort;
import org.onosproject.vtnrsc.FixedIp;
import org.onosproject.vtnrsc.FloatingIp;
import org.onosproject.vtnrsc.RouterInterface;
import org.onosproject.vtnrsc.SecurityGroup;
import org.onosproject.vtnrsc.SegmentationId;
import org.onosproject.vtnrsc.Subnet;
import org.onosproject.vtnrsc.SubnetId;
import org.onosproject.vtnrsc.TenantId;
import org.onosproject.vtnrsc.TenantNetwork;
import org.onosproject.vtnrsc.TenantNetworkId;
import org.onosproject.vtnrsc.TenantRouter;
import org.onosproject.vtnrsc.VirtualPort;
import org.onosproject.vtnrsc.VirtualPortId;
import org.onosproject.vtnrsc.event.VtnRscEvent;
import org.onosproject.vtnrsc.event.VtnRscEventFeedback;
import org.onosproject.vtnrsc.event.VtnRscListener;
import org.onosproject.vtnrsc.floatingip.FloatingIpService;
import org.onosproject.vtnrsc.routerinterface.RouterInterfaceService;
import org.onosproject.vtnrsc.service.VtnRscService;
import org.onosproject.vtnrsc.subnet.SubnetService;
import org.onosproject.vtnrsc.tenantnetwork.TenantNetworkService;
import org.onosproject.vtnrsc.virtualport.VirtualPortService;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Provides implementation of VTNService.
 */
@Component(immediate = true)
@Service
public class VtnManager implements VtnService {
    private final Logger log = getLogger(getClass());
    private static final String APP_ID = "org.onosproject.app.vtn";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigService configService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TenantNetworkService tenantNetworkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VirtualPortService virtualPortService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DriverService driverService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LogicalClockService clockService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected GroupService groupService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected SubnetService subnetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected VtnRscService vtnRscService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FloatingIpService floatingIpService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected RouterInterfaceService routerInterfaceService;

    private ApplicationId appId;
    private ClassifierService classifierService;
    private L2ForwardService l2ForwardService;
    private ArpService arpService;
    private L3ForwardService l3ForwardService;
    private SnatService snatService;
    private DnatService dnatService;

    private final HostListener hostListener = new InnerHostListener();
    private final DeviceListener deviceListener = new InnerDeviceListener();
    private final VtnRscListener l3EventListener = new VtnL3EventListener();

    private static final String EX_PORT_KEY = "exPortKey";
    private static final String IFACEID = "ifaceid";
    private static final String CONTROLLER_IP_KEY = "ipaddress";
    public static final String DRIVER_NAME = "onosfw";
    private static final String VIRTUALPORT = "vtn-virtual-port";
    private static final String SWITCHES_OF_CONTROLLER = "switchesOfController";
    private static final String SWITCH_OF_LOCAL_HOST_PORTS = "switchOfLocalHostPorts";
    private static final String ROUTERINF_FLAG_OF_TENANTROUTER = "routerInfFlagOfTenantRouter";
    private static final String HOSTS_OF_SUBNET = "hostsOfSubnet";
    private static final String EX_PORT_OF_DEVICE = "exPortOfDevice";
    private static final String EX_PORT_MAP = "exPortMap";
    private static final String DEFAULT_IP = "0.0.0.0";
    private static final String USERDATA_IP = "169.254.169.254";
    private static final int SUBNET_NUM = 2;

    private EventuallyConsistentMap<VirtualPortId, VirtualPort> vPortStore;
    private EventuallyConsistentMap<IpAddress, Boolean> switchesOfController;
    private EventuallyConsistentMap<DeviceId, NetworkOfLocalHostPorts> switchOfLocalHostPorts;
    private EventuallyConsistentMap<SubnetId, Map<HostId, Host>> hostsOfSubnet;
    private EventuallyConsistentMap<TenantRouter, Boolean> routerInfFlagOfTenantRouter;
    private EventuallyConsistentMap<DeviceId, Port> exPortOfDevice;
    private static ConsistentMap<String, String> exPortMap;

    @Activate
    public void activate() {
        appId = coreService.registerApplication(APP_ID);
        classifierService = new ClassifierServiceImpl(appId);
        l2ForwardService = new L2ForwardServiceImpl(appId);
        arpService = new ArpServiceImpl(appId);
        l3ForwardService = new L3ForwardServiceImpl(appId);
        snatService = new SnatServiceImpl(appId);
        dnatService = new DnatServiceImpl(appId);

        deviceService.addListener(deviceListener);
        hostService.addListener(hostListener);
        vtnRscService.addListener(l3EventListener);

        KryoNamespace.Builder serializer = KryoNamespace.newBuilder()
                                .register(KryoNamespaces.API)
                                .register(NetworkOfLocalHostPorts.class)
                                .register(TenantNetworkId.class)
                                .register(Host.class)
                                .register(TenantNetwork.class)
                                .register(TenantId.class)
                                .register(SubnetId.class)
                                .register(VirtualPortId.class)
                                .register(VirtualPort.State.class)
                                .register(AllowedAddressPair.class)
                                .register(FixedIp.class)
                                .register(BindingHostId.class)
                                .register(SecurityGroup.class)
                                .register(IpAddress.class)
                                .register(DefaultVirtualPort.class);

        vPortStore = storageService
                .<VirtualPortId, VirtualPort>eventuallyConsistentMapBuilder()
                .withName(VIRTUALPORT).withSerializer(serializer)
                .withTimestampProvider((k, v) -> clockService.getTimestamp())
                .build();

        switchesOfController = storageService
                .<IpAddress, Boolean>eventuallyConsistentMapBuilder()
                .withName(SWITCHES_OF_CONTROLLER).withSerializer(serializer)
                .withTimestampProvider((k, v) -> clockService.getTimestamp())
                .build();

        switchOfLocalHostPorts = storageService
                .<DeviceId, NetworkOfLocalHostPorts>eventuallyConsistentMapBuilder()
                .withName(SWITCH_OF_LOCAL_HOST_PORTS).withSerializer(serializer)
                .withTimestampProvider((k, v) -> clockService.getTimestamp())
                .build();

        hostsOfSubnet = storageService
                .<SubnetId, Map<HostId, Host>>eventuallyConsistentMapBuilder()
                .withName(HOSTS_OF_SUBNET).withSerializer(serializer)
                .withTimestampProvider((k, v) -> clockService.getTimestamp())
                .build();

        routerInfFlagOfTenantRouter = storageService
                .<TenantRouter, Boolean>eventuallyConsistentMapBuilder()
                .withName(ROUTERINF_FLAG_OF_TENANTROUTER).withSerializer(serializer)
                .withTimestampProvider((k, v) -> clockService.getTimestamp())
                .build();

        exPortOfDevice = storageService
                .<DeviceId, Port>eventuallyConsistentMapBuilder()
                .withName(EX_PORT_OF_DEVICE).withSerializer(serializer)
                .withTimestampProvider((k, v) -> clockService.getTimestamp())
                .build();

        exPortMap = storageService
                .<String, String>consistentMapBuilder()
                .withName(EX_PORT_MAP)
                .withApplicationId(appId)
                .withPurgeOnUninstall()
                .withSerializer(Serializer.using(Arrays.asList(KryoNamespaces.API)))
                .build();

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        deviceService.removeListener(deviceListener);
        hostService.removeListener(hostListener);
        vtnRscService.removeListener(l3EventListener);
        log.info("Stopped");
    }

    @Override
    public void onControllerDetected(Device controllerDevice) {
        if (controllerDevice == null) {
            log.error("The controller device is null");
            return;
        }
        String localIpAddress = controllerDevice.annotations()
                .value(CONTROLLER_IP_KEY);
        IpAddress localIp = IpAddress.valueOf(localIpAddress);
        DeviceId controllerDeviceId = controllerDevice.id();
        DriverHandler handler = driverService.createHandler(controllerDeviceId);
        if (mastershipService.isLocalMaster(controllerDeviceId)) {
            // Get DataPathIdGenerator
            String ipaddress = controllerDevice.annotations().value("ipaddress");
            DataPathIdGenerator dpidGenerator = DataPathIdGenerator.builder()
                                            .addIpAddress(ipaddress).build();
            DeviceId deviceId = dpidGenerator.getDeviceId();
            String dpid = dpidGenerator.getDpId();
            // Inject pipeline driver name
            BasicDeviceConfig config = configService.addConfig(deviceId,
                                                               BasicDeviceConfig.class);
            config.driver(DRIVER_NAME);
            configService.applyConfig(deviceId, BasicDeviceConfig.class, config.node());
            // Add Bridge
            Versioned<String> exPortVersioned = exPortMap.get(EX_PORT_KEY);
            if (exPortVersioned != null) {
                VtnConfig.applyBridgeConfig(handler, dpid, exPortVersioned.value());
                log.info("A new ovs is created in node {}", localIp.toString());
            }
            switchesOfController.put(localIp, true);
        }
        // Create tunnel in br-int on all controllers
        programTunnelConfig(controllerDeviceId, localIp, handler);
    }

    @Override
    public void onControllerVanished(Device controllerDevice) {
        if (controllerDevice == null) {
            log.error("The device is null");
            return;
        }
        String dstIp = controllerDevice.annotations().value(CONTROLLER_IP_KEY);
        IpAddress dstIpAddress = IpAddress.valueOf(dstIp);
        DeviceId controllerDeviceId = controllerDevice.id();
        if (mastershipService.isLocalMaster(controllerDeviceId)) {
            switchesOfController.remove(dstIpAddress);
        }
    }

    @Override
    public void onOvsDetected(Device device) {
        // Create tunnel out flow rules
        applyTunnelOut(device, Objective.Operation.ADD);
    }

    @Override
    public void onOvsVanished(Device device) {
        // Remove Tunnel out flow rules
        applyTunnelOut(device, Objective.Operation.REMOVE);
    }

    @Override
    public void onHostDetected(Host host) {
        DeviceId deviceId = host.location().deviceId();
        if (!mastershipService.isLocalMaster(deviceId)) {
            return;
        }
        String ifaceId = host.annotations().value(IFACEID);
        if (ifaceId == null) {
            log.error("The ifaceId of Host is null");
            return;
        }
        programSffAndClassifierHost(host, Objective.Operation.ADD);
        // apply L2 openflow rules
        applyHostMonitoredL2Rules(host, Objective.Operation.ADD);
        // apply L3 openflow rules
        applyHostMonitoredL3Rules(host, Objective.Operation.ADD);
    }

    @Override
    public void onHostVanished(Host host) {
        DeviceId deviceId = host.location().deviceId();
        if (!mastershipService.isLocalMaster(deviceId)) {
            return;
        }
        String ifaceId = host.annotations().value(IFACEID);
        if (ifaceId == null) {
            log.error("The ifaceId of Host is null");
            return;
        }
        programSffAndClassifierHost(host, Objective.Operation.REMOVE);
        // apply L2 openflow rules
        applyHostMonitoredL2Rules(host, Objective.Operation.REMOVE);
        // apply L3 openflow rules
        applyHostMonitoredL3Rules(host, Objective.Operation.REMOVE);
        VirtualPortId virtualPortId = VirtualPortId.portId(ifaceId);
        vPortStore.remove(virtualPortId);
    }

    private void programTunnelConfig(DeviceId localDeviceId, IpAddress localIp,
                                     DriverHandler localHandler) {
        if (mastershipService.isLocalMaster(localDeviceId)) {
            VtnConfig.applyTunnelConfig(localHandler, localIp, IpAddress.valueOf(DEFAULT_IP));
            log.info("Add tunnel on {}", localIp);
        }
    }

    private void applyTunnelOut(Device device, Objective.Operation type) {
        if (device == null) {
            log.error("The device is null");
            return;
        }
        if (!mastershipService.isLocalMaster(device.id())) {
            return;
        }
        String controllerIp = VtnData.getControllerIpOfSwitch(device);
        if (controllerIp == null) {
            log.error("Can't find controller of device: {}",
                      device.id().toString());
            return;
        }
        IpAddress ipAddress = IpAddress.valueOf(controllerIp);
        if (!switchesOfController.containsKey(ipAddress)) {
            log.error("Can't find controller of device: {}",
                      device.id().toString());
            return;
        }
        if (type == Objective.Operation.ADD) {
            // Save external port
            Port export = getExPort(device.id());
            if (export != null) {
                exPortOfDevice.put(device.id(), export);
            }
            switchOfLocalHostPorts.put(device.id(), new NetworkOfLocalHostPorts());
        } else if (type == Objective.Operation.REMOVE) {
            exPortOfDevice.remove(device.id());
            switchOfLocalHostPorts.remove(device.id());
        }
        Iterable<Device> devices = deviceService.getAvailableDevices();
        DeviceId localControllerId = VtnData.getControllerId(device, devices);
        DriverHandler handler = driverService.createHandler(localControllerId);
        Set<PortNumber> ports = VtnConfig.getPortNumbers(handler);
        Iterable<Host> allHosts = hostService.getHosts();
        String tunnelName = "vxlan-" + DEFAULT_IP;
        if (allHosts != null) {
            Sets.newHashSet(allHosts).stream().forEach(host -> {
                MacAddress hostMac = host.mac();
                String ifaceId = host.annotations().value(IFACEID);
                if (ifaceId == null) {
                    log.error("The ifaceId of Host is null");
                    return;
                }
                VirtualPortId virtualPortId = VirtualPortId.portId(ifaceId);
                VirtualPort virtualPort = virtualPortService
                        .getPort(virtualPortId);
                TenantNetwork network = tenantNetworkService
                        .getNetwork(virtualPort.networkId());
                SegmentationId segmentationId = network.segmentationId();
                DeviceId remoteDeviceId = host.location().deviceId();
                Device remoteDevice = deviceService.getDevice(remoteDeviceId);
                String remoteControllerIp = VtnData
                        .getControllerIpOfSwitch(remoteDevice);
                if (remoteControllerIp == null) {
                    log.error("Can't find remote controller of device: {}",
                              remoteDeviceId.toString());
                    return;
                }
                IpAddress remoteIpAddress = IpAddress
                        .valueOf(remoteControllerIp);
                ports.stream()
                        .filter(p -> p.name().equalsIgnoreCase(tunnelName))
                        .forEach(p -> {
                    l2ForwardService
                            .programTunnelOut(device.id(), segmentationId, p,
                                              hostMac, type, remoteIpAddress);
                });
            });
        }
    }

    private void programSffAndClassifierHost(Host host, Objective.Operation type) {
        DeviceId deviceId = host.location().deviceId();
        String ifaceId = host.annotations().value(IFACEID);
        VirtualPortId virtualPortId = VirtualPortId.portId(ifaceId);
        VirtualPort virtualPort = virtualPortService.getPort(virtualPortId);
        if (virtualPort == null) {
            virtualPort = VtnData.getPort(vPortStore, virtualPortId);
        }
        TenantId tenantId = virtualPort.tenantId();
        if (Objective.Operation.ADD == type) {
            vtnRscService.addDeviceIdOfOvsMap(virtualPortId, tenantId, deviceId);
        } else if (Objective.Operation.REMOVE == type) {
            vtnRscService.removeDeviceIdOfOvsMap(host, tenantId, deviceId);
        }
    }

    private void applyHostMonitoredL2Rules(Host host, Objective.Operation type) {
        DeviceId deviceId = host.location().deviceId();
        if (!mastershipService.isLocalMaster(deviceId)) {
            return;
        }
        String ifaceId = host.annotations().value(IFACEID);
        if (ifaceId == null) {
            log.error("The ifaceId of Host is null");
            return;
        }
        VirtualPortId virtualPortId = VirtualPortId.portId(ifaceId);
        VirtualPort virtualPort = virtualPortService.getPort(virtualPortId);
        if (virtualPort == null) {
            virtualPort = VtnData.getPort(vPortStore, virtualPortId);
        }
        Iterator<FixedIp> fixip = virtualPort.fixedIps().iterator();
        SubnetId subnetId = null;
        if (fixip.hasNext()) {
            subnetId = fixip.next().subnetId();
        }
        if (subnetId != null) {
            Map<HostId, Host> hosts = new ConcurrentHashMap();
            if (hostsOfSubnet.get(subnetId) != null) {
                hosts = hostsOfSubnet.get(subnetId);
            }
            if (type == Objective.Operation.ADD) {
                hosts.put(host.id(), host);
                hostsOfSubnet.put(subnetId, hosts);
            } else if (type == Objective.Operation.REMOVE) {
                hosts.remove(host.id());
                if (hosts.size() != 0) {
                    hostsOfSubnet.put(subnetId, hosts);
                } else {
                    hostsOfSubnet.remove(subnetId);
                }
            }
        }

        Iterable<Device> devices = deviceService.getAvailableDevices();
        PortNumber inPort = host.location().port();
        MacAddress mac = host.mac();
        Device device = deviceService.getDevice(deviceId);
        String controllerIp = VtnData.getControllerIpOfSwitch(device);
        IpAddress ipAddress = IpAddress.valueOf(controllerIp);
        TenantNetwork network = tenantNetworkService.getNetwork(virtualPort.networkId());
        if (network == null) {
            log.error("Can't find network of the host");
            return;
        }
        SegmentationId segmentationId = network.segmentationId();
        // Get all the tunnel PortNumber in the current node
        Iterable<Port> ports = deviceService.getPorts(deviceId);
        Collection<PortNumber> localTunnelPorts = VtnData.getLocalTunnelPorts(ports);
        // Get all the local vm's PortNumber in the current node
        Map<TenantNetworkId, Set<PortNumber>> localHostPorts = switchOfLocalHostPorts
                .get(deviceId).getNetworkOfLocalHostPorts();
        Set<PortNumber> networkOflocalHostPorts = localHostPorts.get(network.id());
        for (PortNumber p : localTunnelPorts) {
            programGroupTable(deviceId, appId, p, devices, type);
        }
        Subnet subnet = subnetService.getSubnet(subnetId);
        String deviceOwner = virtualPort.deviceOwner();
        if (deviceOwner != null) {
            if (deviceOwner.equalsIgnoreCase("network:dhcp")) {
                Sets.newHashSet(devices).stream()
                        .filter(d -> d.type() == Device.Type.SWITCH)
                        .forEach(d -> {
                            if (subnet != null) {
                                IpAddress dstIp = IpAddress
                                        .valueOf(USERDATA_IP);
                                classifierService
                                        .programUserdataClassifierRules(d.id(),
                                                                        subnet.cidr(),
                                                                        dstIp,
                                                                        mac,
                                                                        segmentationId,
                                                                        type);
                            }
                        });
            }
        }
        if (type == Objective.Operation.ADD) {
            vPortStore.put(virtualPortId, virtualPort);
            if (networkOflocalHostPorts == null) {
                networkOflocalHostPorts = new HashSet<PortNumber>();
                localHostPorts.putIfAbsent(network.id(), networkOflocalHostPorts);
            }
            networkOflocalHostPorts.add(inPort);
            l2ForwardService.programLocalBcastRules(deviceId, segmentationId,
                                                    inPort, networkOflocalHostPorts,
                                                    localTunnelPorts,
                                                    type);
            classifierService.programTunnelIn(deviceId, segmentationId,
                                              localTunnelPorts,
                                              type);
        } else if (type == Objective.Operation.REMOVE) {
            if (networkOflocalHostPorts != null) {
                l2ForwardService.programLocalBcastRules(deviceId, segmentationId,
                                                        inPort, networkOflocalHostPorts,
                                                        localTunnelPorts,
                                                        type);
                networkOflocalHostPorts.remove(inPort);
                if (networkOflocalHostPorts.isEmpty()) {
                    classifierService.programTunnelIn(deviceId, segmentationId,
                                                      localTunnelPorts,
                                                      type);
                    switchOfLocalHostPorts.get(deviceId).getNetworkOfLocalHostPorts()
                                                .remove(virtualPort.networkId());
                }
            }
        }

        l2ForwardService.programLocalOut(deviceId, segmentationId, inPort, mac,
                                         type);

        l2ForwardService.programTunnelBcastRules(deviceId, segmentationId,
                                                 networkOflocalHostPorts,
                                                 localTunnelPorts,
                                                 type);

        programTunnelOuts(devices, ipAddress, segmentationId, mac,
                          type);

        classifierService.programLocalIn(deviceId, segmentationId, inPort, mac,
                                         appId, type);
    }

    private void programTunnelOuts(Iterable<Device> devices,
                                   IpAddress ipAddress,
                                   SegmentationId segmentationId,
                                   MacAddress dstMac,
                                   Objective.Operation type) {
        String tunnelName = "vxlan-" + DEFAULT_IP;
        Sets.newHashSet(devices).stream()
                .filter(d -> d.type() == Device.Type.CONTROLLER)
                .filter(d -> !("ovsdb:" + ipAddress).equals(d.id().toString()))
                .forEach(d -> {
                    DriverHandler handler = driverService.createHandler(d.id());
                    BridgeConfig bridgeConfig = handler
                            .behaviour(BridgeConfig.class);
                    Collection<BridgeDescription> bridgeDescriptions = bridgeConfig
                            .getBridges();
                    Iterator<BridgeDescription> it = bridgeDescriptions
                            .iterator();
                    while (it.hasNext()) {
                        BridgeDescription sw = it.next();
                        if (sw.bridgeName().name().equals(VtnConfig.DEFAULT_BRIDGE_NAME)) {
                            List<Port> ports = deviceService.getPorts(sw.deviceId());
                            ports.stream()
                                    .filter(p -> p.annotations().value(AnnotationKeys.PORT_NAME)
                                            .equalsIgnoreCase(tunnelName))
                                    .forEach(p -> {
                                l2ForwardService.programTunnelOut(sw.deviceId(),
                                                                  segmentationId, p.number(),
                                                                  dstMac, type, ipAddress);
                            });
                            break;
                        }
                    }
                });
    }

    private class InnerDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            Device device = event.subject();
            if (Device.Type.CONTROLLER == device.type()) {
                if (DeviceEvent.Type.DEVICE_ADDED == event.type()) {
                    onControllerDetected(device);
                }
                if (DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED == event.type()) {
                    if (deviceService.isAvailable(device.id())) {
                        onControllerDetected(device);
                    } else {
                        onControllerVanished(device);
                    }
                }
            } else if (Device.Type.SWITCH == device.type()) {
                if (DeviceEvent.Type.DEVICE_ADDED == event.type()) {
                    onOvsDetected(device);
                }
                if (DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED == event.type()) {
                    if (deviceService.isAvailable(device.id())) {
                        onOvsDetected(device);
                    } else {
                        onOvsVanished(device);
                    }
                }
            } else {
                log.info("Do nothing for this device type");
            }
        }
    }

    private class InnerHostListener implements HostListener {

        @Override
        public void event(HostEvent event) {
            Host host = event.subject();
            if (HostEvent.Type.HOST_ADDED == event.type()) {
                onHostDetected(host);
            } else if (HostEvent.Type.HOST_REMOVED == event.type()) {
                onHostVanished(host);
            } else if (HostEvent.Type.HOST_UPDATED == event.type()) {
                onHostVanished(host);
                onHostDetected(host);
            }
        }

    }

    // Local Host Ports of Network.
    private class NetworkOfLocalHostPorts {
        private final Map<TenantNetworkId, Set<PortNumber>> networkOfLocalHostPorts =
                                      new HashMap<TenantNetworkId, Set<PortNumber>>();

        public Map<TenantNetworkId, Set<PortNumber>> getNetworkOfLocalHostPorts() {
            return networkOfLocalHostPorts;
        }
    }

    private void programGroupTable(DeviceId deviceId, ApplicationId appid,
                                   PortNumber portNumber, Iterable<Device> devices, Objective.Operation type) {
        if (type.equals(Objective.Operation.REMOVE)) {
            return;
        }

        List<GroupBucket> buckets = Lists.newArrayList();
        Sets.newHashSet(devices)
        .stream()
        .filter(d -> d.type() == Device.Type.CONTROLLER)
        .filter(d -> !deviceId.equals(d.id()))
        .forEach(d -> {
                    String ipAddress = d.annotations()
                             .value(CONTROLLER_IP_KEY);
                    Ip4Address dst = Ip4Address.valueOf(ipAddress);
                    Builder builder = DefaultTrafficTreatment.builder();

                    DriverHandler handler = driverService.createHandler(deviceId);
                    ExtensionTreatmentResolver resolver =  handler.behaviour(ExtensionTreatmentResolver.class);
                    ExtensionTreatment treatment = resolver.getExtensionInstruction(NICIRA_SET_TUNNEL_DST.type());
                    try {
                        treatment.setPropertyValue("tunnelDst", dst);
                    } catch (Exception e) {
                       log.error("Failed to get extension instruction to set tunnel dst {}", deviceId);
                    }

                    builder.extension(treatment, deviceId);
                    builder.setOutput(portNumber);
                    GroupBucket bucket = DefaultGroupBucket
                            .createAllGroupBucket(builder.build());
                    buckets.add(bucket);
                 });
        final GroupKey key = new DefaultGroupKey(APP_ID.getBytes());
        GroupDescription groupDescription = new DefaultGroupDescription(deviceId,
                                                                        GroupDescription.Type.ALL,
                                                                        new GroupBuckets(buckets),
                                                                        key,
                                                                        L2ForwardServiceImpl.GROUP_ID,
                                                                        appid);
        groupService.addGroup(groupDescription);
    }

    private class VtnL3EventListener implements VtnRscListener {
        @Override
        public void event(VtnRscEvent event) {
            VtnRscEventFeedback l3Feedback = event.subject();
            if (VtnRscEvent.Type.ROUTER_INTERFACE_PUT == event.type()) {
                onRouterInterfaceDetected(l3Feedback);
            } else
                if (VtnRscEvent.Type.ROUTER_INTERFACE_DELETE == event.type()) {
                onRouterInterfaceVanished(l3Feedback);
            } else if (VtnRscEvent.Type.FLOATINGIP_BIND == event.type()) {
                onFloatingIpDetected(l3Feedback);
            } else if (VtnRscEvent.Type.FLOATINGIP_UNBIND == event.type()) {
                onFloatingIpVanished(l3Feedback);
            }
        }

    }

    @Override
    public void onRouterInterfaceDetected(VtnRscEventFeedback l3Feedback) {
        Objective.Operation operation = Objective.Operation.ADD;
        RouterInterface routerInf = l3Feedback.routerInterface();
        VirtualPort gwPort = virtualPortService.getPort(routerInf.portId());
        vPortStore.put(gwPort.portId(), gwPort);
        Iterable<RouterInterface> interfaces = routerInterfaceService
                .getRouterInterfaces();
        Set<RouterInterface> interfacesSet = Sets.newHashSet(interfaces).stream()
                .filter(r -> r.tenantId().equals(routerInf.tenantId()))
                .filter(r -> r.routerId().equals(routerInf.routerId()))
                .collect(Collectors.toSet());
        TenantRouter tenantRouter = TenantRouter
                .tenantRouter(routerInf.tenantId(), routerInf.routerId());
        if (routerInfFlagOfTenantRouter.get(tenantRouter) != null) {
            programRouterInterface(routerInf, operation);
        } else {
            if (interfacesSet.size() >= SUBNET_NUM) {
                programInterfacesSet(interfacesSet, operation);
            }
        }
    }

    @Override
    public void onRouterInterfaceVanished(VtnRscEventFeedback l3Feedback) {
        Objective.Operation operation = Objective.Operation.REMOVE;
        RouterInterface routerInf = l3Feedback.routerInterface();
        Iterable<RouterInterface> interfaces = routerInterfaceService
                .getRouterInterfaces();
        Set<RouterInterface> interfacesSet = Sets.newHashSet(interfaces)
                .stream().filter(r -> r.tenantId().equals(routerInf.tenantId()))
                .collect(Collectors.toSet());
        TenantRouter tenantRouter = TenantRouter
                .tenantRouter(routerInf.tenantId(), routerInf.routerId());
        if (routerInfFlagOfTenantRouter.get(tenantRouter) != null) {
            programRouterInterface(routerInf, operation);
            if (interfacesSet.size() == 1) {
                routerInfFlagOfTenantRouter.remove(tenantRouter);
                interfacesSet.stream().forEach(r -> {
                    programRouterInterface(r, operation);
                });
            }
        }
        VirtualPort gwPort = virtualPortService.getPort(routerInf.portId());
        if (gwPort == null) {
            gwPort = VtnData.getPort(vPortStore, routerInf.portId());
        }
        vPortStore.remove(gwPort.portId());
    }

    @Override
    public void onFloatingIpDetected(VtnRscEventFeedback l3Feedback) {
        programFloatingIpEvent(l3Feedback, VtnRscEvent.Type.FLOATINGIP_BIND);
    }

    @Override
    public void onFloatingIpVanished(VtnRscEventFeedback l3Feedback) {
        programFloatingIpEvent(l3Feedback, VtnRscEvent.Type.FLOATINGIP_UNBIND);
    }

    private void programInterfacesSet(Set<RouterInterface> interfacesSet,
                                      Objective.Operation operation) {
        int subnetVmNum = 0;
        for (RouterInterface r : interfacesSet) {
            // Get all the host of the subnet
            Map<HostId, Host> hosts = hostsOfSubnet.get(r.subnetId());
            if (hosts != null && hosts.size() > 0) {
                subnetVmNum++;
                if (subnetVmNum >= SUBNET_NUM) {
                    TenantRouter tenantRouter = TenantRouter
                            .tenantRouter(r.tenantId(), r.routerId());
                    routerInfFlagOfTenantRouter.put(tenantRouter, true);
                    interfacesSet.stream().forEach(f -> {
                        programRouterInterface(f, operation);
                    });
                    break;
                }
            }
        }
    }

    private void programRouterInterface(RouterInterface routerInf,
                                        Objective.Operation operation) {
        TenantRouter tenantRouter = TenantRouter
                .tenantRouter(routerInf.tenantId(), routerInf.routerId());
        SegmentationId l3vni = vtnRscService.getL3vni(tenantRouter);
        // Get all the host of the subnet
        Map<HostId, Host> hosts = hostsOfSubnet.get(routerInf.subnetId());
        hosts.values().stream().forEach(h -> {
            applyEastWestL3Flows(h, l3vni, operation);
        });
    }

    private void applyEastWestL3Flows(Host h, SegmentationId l3vni,
                                      Objective.Operation operation) {
        if (!mastershipService.isLocalMaster(h.location().deviceId())) {
            log.debug("not master device:{}", h.location().deviceId());
            return;
        }
        String ifaceId = h.annotations().value(IFACEID);
        VirtualPort hPort = virtualPortService
                .getPort(VirtualPortId.portId(ifaceId));
        if (hPort == null) {
            hPort = VtnData.getPort(vPortStore, VirtualPortId.portId(ifaceId));
        }
        IpAddress srcIp = null;
        IpAddress srcGwIp = null;
        MacAddress srcVmGwMac = null;
        SubnetId srcSubnetId = null;
        Iterator<FixedIp> srcIps = hPort.fixedIps().iterator();
        if (srcIps.hasNext()) {
            FixedIp fixedIp = srcIps.next();
            srcIp = fixedIp.ip();
            srcSubnetId = fixedIp.subnetId();
            srcGwIp = subnetService.getSubnet(srcSubnetId).gatewayIp();
            FixedIp fixedGwIp = FixedIp.fixedIp(srcSubnetId, srcGwIp);
            VirtualPort gwPort = virtualPortService.getPort(fixedGwIp);
            if (gwPort == null) {
                gwPort = VtnData.getPort(vPortStore, fixedGwIp);
            }
            srcVmGwMac = gwPort.macAddress();
        }
        TenantNetwork network = tenantNetworkService
                .getNetwork(hPort.networkId());
        // Classifier rules
        classifierService
                .programL3InPortClassifierRules(h.location().deviceId(),
                                                h.location().port(), h.mac(),
                                                srcVmGwMac, l3vni, operation);
        classifierService.programArpClassifierRules(h.location().deviceId(),
                                                    h.location().port(), srcGwIp,
                                                    network.segmentationId(),
                                                    operation);
        // Arp rules
        if (operation == Objective.Operation.ADD) {
            DriverHandler handler = driverService.createHandler(h.location().deviceId());
            arpService.programArpRules(handler, h.location().deviceId(), srcGwIp,
                                       network.segmentationId(), srcVmGwMac,
                                       operation);
        }
        Iterable<Device> devices = deviceService.getAvailableDevices();
        IpAddress srcArpIp = srcIp;
        MacAddress srcArpGwMac = srcVmGwMac;
        Sets.newHashSet(devices).stream()
                .filter(d -> Device.Type.SWITCH == d.type()).forEach(d -> {
                    // L3FWD rules
                    l3ForwardService.programRouteRules(d.id(), l3vni, srcArpIp,
                                                       network.segmentationId(),
                                                       srcArpGwMac, h.mac(),
                                                       operation);
                });
    }

    private void programFloatingIpEvent(VtnRscEventFeedback l3Feedback,
                                       VtnRscEvent.Type type) {
        FloatingIp floaingIp = l3Feedback.floatingIp();
        if (floaingIp != null) {
            VirtualPortId vmPortId = floaingIp.portId();
            VirtualPort vmPort = virtualPortService.getPort(vmPortId);
            VirtualPort fipPort = virtualPortService
                    .getPort(floaingIp.networkId(), floaingIp.floatingIp());
            if (vmPort == null) {
                vmPort = VtnData.getPort(vPortStore, vmPortId);
            }
            if (fipPort == null) {
                fipPort = VtnData.getPort(vPortStore, floaingIp.networkId(),
                                          floaingIp.floatingIp());
            }
            Set<Host> hostSet = hostService.getHostsByMac(vmPort.macAddress());
            Host host = null;
            for (Host h : hostSet) {
                String ifaceid = h.annotations().value(IFACEID);
                if (ifaceid != null && ifaceid.equals(vmPortId.portId())) {
                    host = h;
                    break;
                }
            }
            if (host != null && vmPort != null && fipPort != null) {
                DeviceId deviceId = host.location().deviceId();
                Port exPort = exPortOfDevice.get(deviceId);
                TenantRouter tenantRouter = TenantRouter
                        .tenantRouter(floaingIp.tenantId(), floaingIp.routerId());
                SegmentationId l3vni = vtnRscService.getL3vni(tenantRouter);
                // Floating ip BIND
                if (type == VtnRscEvent.Type.FLOATINGIP_BIND) {
                    vPortStore.put(fipPort.portId(), fipPort);
                    applyNorthSouthL3Flows(deviceId, host, vmPort, fipPort,
                                           floaingIp, l3vni, exPort,
                                           Objective.Operation.ADD);
                } else if (type == VtnRscEvent.Type.FLOATINGIP_UNBIND) {
                    // Floating ip UNBIND
                    applyNorthSouthL3Flows(deviceId, host, vmPort, fipPort,
                                           floaingIp, l3vni, exPort,
                                           Objective.Operation.REMOVE);
                    vPortStore.remove(fipPort.portId());
                }
            }
        }
    }

    private void applyNorthSouthL3Flows(DeviceId deviceId, Host host,
                                        VirtualPort vmPort, VirtualPort fipPort,
                                        FloatingIp floatingIp,
                                        SegmentationId l3vni, Port exPort,
                                        Objective.Operation operation) {
        if (!mastershipService.isLocalMaster(deviceId)) {
            log.debug("not master device:{}", deviceId);
            return;
        }
        List gwIpMac = getGwIpAndMac(vmPort);
        IpAddress dstVmGwIp = (IpAddress) gwIpMac.get(0);
        MacAddress dstVmGwMac = (MacAddress) gwIpMac.get(1);
        List fGwIpMac = getGwIpAndMac(fipPort);
        MacAddress fGwMac = (MacAddress) fGwIpMac.get(1);
        TenantNetwork vmNetwork = tenantNetworkService
                .getNetwork(vmPort.networkId());
        TenantNetwork fipNetwork = tenantNetworkService
                .getNetwork(fipPort.networkId());
        // L3 downlink traffic flow
        MacAddress exPortMac = MacAddress.valueOf(exPort.annotations()
                                                  .value(AnnotationKeys.PORT_MAC));
        classifierService.programArpClassifierRules(deviceId, floatingIp.floatingIp(),
                                                    fipNetwork.segmentationId(),
                                                    operation);
        classifierService.programL3ExPortClassifierRules(deviceId, exPort.number(),
                                                         floatingIp.floatingIp(), operation);
        DriverHandler handler = driverService.createHandler(deviceId);
        arpService.programArpRules(handler, deviceId, floatingIp.floatingIp(),
                                         fipNetwork.segmentationId(), exPortMac,
                                         operation);
        dnatService.programRules(deviceId, floatingIp.floatingIp(),
                                     fGwMac, floatingIp.fixedIp(),
                                     l3vni, operation);
        l3ForwardService
                .programRouteRules(deviceId, l3vni, floatingIp.fixedIp(),
                                   vmNetwork.segmentationId(), dstVmGwMac,
                                   vmPort.macAddress(), operation);

        // L3 uplink traffic flow
        classifierService.programL3InPortClassifierRules(deviceId,
                                                         host.location().port(),
                                                         host.mac(), dstVmGwMac,
                                                         l3vni, operation);
        snatService.programRules(deviceId, l3vni, floatingIp.fixedIp(),
                                     fGwMac, exPortMac,
                                     floatingIp.floatingIp(),
                                     fipNetwork.segmentationId(), operation);
        classifierService.programArpClassifierRules(deviceId, host.location().port(),
                                                    dstVmGwIp, vmNetwork.segmentationId(),
                                                    operation);
        if (operation == Objective.Operation.ADD) {
            arpService.programArpRules(handler, deviceId, dstVmGwIp,
                                       vmNetwork.segmentationId(), dstVmGwMac,
                                       operation);
            l2ForwardService.programLocalOut(deviceId,
                                             fipNetwork.segmentationId(),
                                             exPort.number(), fGwMac, operation);
        }
    }

    private Port getExPort(DeviceId deviceId) {
        List<Port> ports = deviceService.getPorts(deviceId);
        Port exPort = null;
        for (Port port : ports) {
            String portName = port.annotations().value(AnnotationKeys.PORT_NAME);
            Versioned<String> exPortVersioned = exPortMap.get(EX_PORT_KEY);
            if (portName != null && exPortVersioned != null && portName.
                    equals(exPortVersioned.value())) {
                exPort = port;
                break;
            }
        }
        return exPort;
    }

    private List getGwIpAndMac(VirtualPort port) {
        List list = new ArrayList();
        MacAddress gwMac = null;
        SubnetId subnetId = null;
        IpAddress gwIp = null;
        Iterator<FixedIp> fixips = port.fixedIps().iterator();
        if (fixips.hasNext()) {
            FixedIp fixip = fixips.next();
            subnetId = fixip.subnetId();
            gwIp = subnetService.getSubnet(subnetId).gatewayIp();
            FixedIp fixedGwIp = FixedIp.fixedIp(fixip.subnetId(), gwIp);
            VirtualPort gwPort = virtualPortService.getPort(fixedGwIp);
            if (gwPort == null) {
                gwPort = VtnData.getPort(vPortStore, fixedGwIp);
            }
            gwMac = gwPort.macAddress();
        }
        list.add(gwIp);
        list.add(gwMac);
        return list;
    }

    private void applyHostMonitoredL3Rules(Host host,
                                           Objective.Operation operation) {
        String ifaceId = host.annotations().value(IFACEID);
        DeviceId deviceId = host.location().deviceId();
        VirtualPortId portId = VirtualPortId.portId(ifaceId);
        VirtualPort port = virtualPortService.getPort(portId);
        if (port == null) {
            port = VtnData.getPort(vPortStore, portId);
        }
        TenantId tenantId = port.tenantId();
        Port exPort = exPortOfDevice.get(deviceId);
        Iterator<FixedIp> fixips = port.fixedIps().iterator();
        SubnetId sid = null;
        IpAddress hostIp = null;
        if (fixips.hasNext()) {
            FixedIp fixip = fixips.next();
            sid = fixip.subnetId();
            hostIp = fixip.ip();
        }
        final SubnetId subnetId = sid;
        // L3 internal network access to each other
        Iterable<RouterInterface> interfaces = routerInterfaceService
                .getRouterInterfaces();
        Set<RouterInterface> hostInterfaces = Sets.newHashSet(interfaces)
                .stream().filter(r -> r.tenantId().equals(tenantId))
                .filter(r -> r.subnetId().equals(subnetId))
                .collect(Collectors.toSet());
        hostInterfaces.stream().forEach(routerInf -> {
            Set<RouterInterface> interfacesSet = Sets.newHashSet(interfaces)
                    .stream().filter(r -> r.tenantId().equals(tenantId))
                    .filter(r -> r.routerId().equals(routerInf.routerId()))
                    .collect(Collectors.toSet());
            long count = interfacesSet.stream()
                    .filter(r -> !r.subnetId().equals(subnetId)).count();
            if (count > 0) {
                TenantRouter tenantRouter = TenantRouter
                        .tenantRouter(routerInf.tenantId(), routerInf.routerId());
                SegmentationId l3vni = vtnRscService.getL3vni(tenantRouter);
                if (operation == Objective.Operation.ADD) {
                    if (routerInfFlagOfTenantRouter.get(tenantRouter) != null) {
                        applyEastWestL3Flows(host, l3vni, operation);
                    } else {
                        if (interfacesSet.size() > 1) {
                            programInterfacesSet(interfacesSet, operation);
                        }
                    }
                } else if (operation == Objective.Operation.REMOVE) {
                    if (routerInfFlagOfTenantRouter.get(tenantRouter) != null) {
                        applyEastWestL3Flows(host, l3vni, operation);
                    }
                }
            }
        });
        // L3 external and internal network access to each other
        FloatingIp floatingIp = null;
        Iterable<FloatingIp> floatingIps = floatingIpService.getFloatingIps();
        Set<FloatingIp> floatingIpSet = Sets.newHashSet(floatingIps).stream()
                .filter(f -> f.tenantId().equals(tenantId))
                .collect(Collectors.toSet());
        for (FloatingIp f : floatingIpSet) {
            IpAddress fixedIp = f.fixedIp();
            if (fixedIp.equals(hostIp)) {
                floatingIp = f;
                break;
            }
        }
        if (floatingIp != null) {
            TenantRouter tenantRouter = TenantRouter
                    .tenantRouter(floatingIp.tenantId(), floatingIp.routerId());
            SegmentationId l3vni = vtnRscService.getL3vni(tenantRouter);
            VirtualPort fipPort = virtualPortService
                    .getPort(floatingIp.networkId(), floatingIp.floatingIp());
            if (fipPort == null) {
                fipPort = VtnData.getPort(vPortStore, floatingIp.networkId(),
                                          floatingIp.floatingIp());
            }
            applyNorthSouthL3Flows(deviceId, host, port, fipPort, floatingIp,
                                   l3vni, exPort, operation);
        }
    }

    public static void setExPortName(String name) {
        exPortMap.put(EX_PORT_KEY, name);
    }
}
