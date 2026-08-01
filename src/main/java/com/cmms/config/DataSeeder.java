package com.cmms.config;

import com.cmms.entity.*;
import com.cmms.enums.*;
import com.cmms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Seeds the database with random sample data (locations, technicians, assets,
 * work orders, maintenance schedules, spare parts) plus login users, so the
 * project can be exercised end-to-end right after startup.
 *
 * Controlled by cmms.seed.enabled in application.properties. Idempotent -
 * skips seeding if users already exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final TechnicianRepository technicianRepository;
    private final AssetRepository assetRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final SparePartRepository sparePartRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${cmms.seed.enabled:true}")
    private boolean seedEnabled;

    private final Random random = new Random();

    private static final String[] LOCATION_NAMES = {
            "Plant A - Building 1", "Plant A - Building 2", "Plant B - Warehouse",
            "Plant C - Assembly Line", "Distribution Center North", "Distribution Center South"
    };

    private static final String[] TECH_FIRST_NAMES = {
            "John", "Maria", "David", "Priya", "Ahmed", "Sara", "Chen", "Olga", "Carlos", "Emily"
    };

    private static final String[] TECH_LAST_NAMES = {
            "Smith", "Garcia", "Patel", "Khan", "Johnson", "Müller", "Rossi", "Kim", "Silva", "Brown"
    };

    private static final String[] SPECIALIZATIONS = {
            "Electrical", "Mechanical", "HVAC", "Plumbing", "Instrumentation", "General Maintenance"
    };

    private static final String[] ASSET_CATEGORIES = {
            "Machinery", "HVAC", "Electrical Panel", "Conveyor", "Pump", "Compressor", "Generator", "Forklift"
    };

    private static final String[] MANUFACTURERS = {
            "Haas", "Siemens", "ABB", "Atlas Copco", "Caterpillar", "Bosch", "Schneider Electric", "Toyota"
    };

    private static final String[] WORK_ORDER_TITLES = {
            "Replace worn bearing", "Fix coolant leak", "Calibrate sensor", "Inspect belt tension",
            "Replace air filter", "Repair electrical fault", "Lubricate moving parts",
            "Replace hydraulic hose", "Fix unusual vibration", "Software/firmware update"
    };

    private static final String[] PART_NAMES = {
            "Ball Bearing 6205", "Hydraulic Hose 1/2in", "Air Filter Type-B", "V-Belt A42",
            "Coolant Pump Seal", "Pressure Sensor PT100", "Motor Contactor 25A", "O-Ring Kit",
            "Timing Belt", "Grease Cartridge 400g"
    };

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("CMMS data seeding is disabled (cmms.seed.enabled=false)");
            return;
        }

        if (userRepository.count() > 0) {
            log.info("CMMS data already present - skipping seed");
            return;
        }

        log.info("Seeding CMMS sample data...");

        seedUsers();
        List<Location> locations = seedLocations();
        List<Technician> technicians = seedTechnicians();
        List<Asset> assets = seedAssets(locations);
        seedWorkOrders(assets, technicians);
        seedMaintenanceSchedules(assets);
        seedSpareParts();

        log.info("CMMS sample data seeding complete.");
        log.info("Login with username='admin', password='admin123' (ROLE_ADMIN)");
        log.info("Login with username='tech1', password='tech123' (ROLE_TECHNICIAN)");
    }

    private void seedUsers() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@cmms.com");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        User tech1 = new User();
        tech1.setUsername("tech1");
        tech1.setPassword(passwordEncoder.encode("tech123"));
        tech1.setEmail("tech1@cmms.com");
        tech1.setRole(Role.TECHNICIAN);
        tech1.setEnabled(true);
        userRepository.save(tech1);

        User tech2 = new User();
        tech2.setUsername("tech2");
        tech2.setPassword(passwordEncoder.encode("tech123"));
        tech2.setEmail("tech2@cmms.com");
        tech2.setRole(Role.TECHNICIAN);
        tech2.setEnabled(true);
        userRepository.save(tech2);
    }

    private List<Location> seedLocations() {
        return locationRepository.saveAll(
                List.of(LOCATION_NAMES).stream().map(name -> {
                    Location loc = new Location();
                    loc.setName(name);
                    loc.setDescription("Auto-generated sample location: " + name);
                    return loc;
                }).toList()
        );
    }

    private List<Technician> seedTechnicians() {
        return technicianRepository.saveAll(
                List.of(0, 1, 2, 3, 4, 5).stream().map(i -> {
                    String first = TECH_FIRST_NAMES[random.nextInt(TECH_FIRST_NAMES.length)];
                    String last = TECH_LAST_NAMES[random.nextInt(TECH_LAST_NAMES.length)];
                    Technician t = new Technician();
                    t.setName(first + " " + last);
                    t.setEmail((first + "." + last + i + "@cmms.com").toLowerCase());
                    t.setPhone(randomPhone());
                    t.setSpecialization(SPECIALIZATIONS[random.nextInt(SPECIALIZATIONS.length)]);
                    return t;
                }).toList()
        );
    }

    private List<Asset> seedAssets(List<Location> locations) {
        return assetRepository.saveAll(
                List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).stream().map(i -> {
                    Asset asset = new Asset();
                    String category = ASSET_CATEGORIES[random.nextInt(ASSET_CATEGORIES.length)];
                    asset.setName(category + " Unit " + (i + 1));
                    asset.setAssetCode(String.format("AST-%03d", i + 1));
                    asset.setCategory(category);
                    asset.setManufacturer(MANUFACTURERS[random.nextInt(MANUFACTURERS.length)]);
                    asset.setModel("MDL-" + (100 + random.nextInt(900)));
                    asset.setSerialNumber("SN-" + (100000 + random.nextInt(900000)));
                    asset.setPurchaseDate(LocalDate.now().minusDays(30L + random.nextInt(1500)));
                    asset.setStatus(AssetStatus.values()[random.nextInt(AssetStatus.values().length)]);
                    asset.setLocation(locations.get(random.nextInt(locations.size())));
                    return asset;
                }).toList()
        );
    }

    private void seedWorkOrders(List<Asset> assets, List<Technician> technicians) {
        List<WorkOrder> orders = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).stream().map(i -> {
            WorkOrder wo = new WorkOrder();
            wo.setTitle(WORK_ORDER_TITLES[random.nextInt(WORK_ORDER_TITLES.length)]);
            wo.setDescription("Auto-generated sample work order describing an issue that needs attention.");
            wo.setAsset(assets.get(random.nextInt(assets.size())));
            wo.setTechnician(technicians.get(random.nextInt(technicians.size())));
            wo.setType(WorkOrderType.values()[random.nextInt(WorkOrderType.values().length)]);
            wo.setPriority(WorkOrderPriority.values()[random.nextInt(WorkOrderPriority.values().length)]);
            wo.setStatus(WorkOrderStatus.values()[random.nextInt(WorkOrderStatus.values().length)]);
            wo.setDueDate(LocalDate.now().plusDays(random.nextInt(30) - 5));
            if (wo.getStatus() == WorkOrderStatus.COMPLETED) {
                wo.setCompletedDate(LocalDateTime.now().minusDays(random.nextInt(10)));
            }
            return wo;
        }).toList();

        workOrderRepository.saveAll(orders);
    }

    private void seedMaintenanceSchedules(List<Asset> assets) {
        List<MaintenanceSchedule> schedules = assets.stream().limit(6).map(asset -> {
            MaintenanceSchedule ms = new MaintenanceSchedule();
            ms.setAsset(asset);
            ms.setFrequency(MaintenanceFrequency.values()[random.nextInt(MaintenanceFrequency.values().length)]);
            ms.setDescription("Routine preventive maintenance for " + asset.getName());
            ms.setLastMaintenanceDate(LocalDate.now().minusDays(20L + random.nextInt(60)));
            ms.setNextMaintenanceDate(LocalDate.now().plusDays(random.nextInt(45) - 10));
            ms.setActive(true);
            return ms;
        }).toList();

        maintenanceScheduleRepository.saveAll(schedules);
    }

    private void seedSpareParts() {
        List<SparePart> parts = List.of(PART_NAMES).stream().map(name -> {
            SparePart sp = new SparePart();
            sp.setName(name);
            sp.setPartNumber("PN-" + (1000 + random.nextInt(9000)));
            sp.setQuantityInStock(random.nextInt(20));
            sp.setReorderLevel(5);
            sp.setUnitCost(Math.round((5 + random.nextDouble() * 200) * 100.0) / 100.0);
            return sp;
        }).toList();

        sparePartRepository.saveAll(parts);
    }

    private String randomPhone() {
        return String.format("555-%04d", random.nextInt(10000));
    }
}
