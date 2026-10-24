package com.rekora.app.data.repository

import com.rekora.app.data.local.dao.AiConversationDao
import com.rekora.app.data.local.dao.ComponentDao
import com.rekora.app.data.local.entity.AiConversationEntity
import com.rekora.app.data.local.entity.AiMessageEntity
import com.rekora.app.data.mapper.toDomain
import com.rekora.app.data.mapper.toEntity
import com.rekora.app.domain.model.*
import com.rekora.app.domain.repository.AiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val aiConversationDao: AiConversationDao,
    private val componentDao: ComponentDao
) : AiRepository {

    override fun getConversationsForUser(userId: String): Flow<List<AiConversation>> {
        return aiConversationDao.getConversationsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getConversation(conversationId: String): Flow<AiConversation?> {
        return combine(
            aiConversationDao.observeConversationById(conversationId),
            aiConversationDao.getMessagesForConversation(conversationId)
        ) { conversationEntity, messageEntities ->
            conversationEntity?.toDomain(messages = messageEntities.map { it.toDomain() })
        }
    }

    override fun getMessages(conversationId: String): Flow<List<AiMessage>> {
        return aiConversationDao.getMessagesForConversation(conversationId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun createConversation(title: String, userId: String): String {
        val conversationId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = AiConversationEntity(
            id = conversationId,
            userId = userId,
            title = title.ifBlank { "New Engineering Consultation" },
            hasBlueprint = false,
            blueprintId = null,
            createdAt = now,
            updatedAt = now
        )
        aiConversationDao.insertConversation(entity)

        // Seed initial friendly AI greeting
        val initialMessage = AiMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            content = "Greetings! I'm your REKORA AI Project Engineer. Tell me about your engineering project idea, problem statement, or target specifications. I can help with architecture design, component selection from campus inventory, BOM cost optimization, CAD/fabrication planning, and validation protocols.",
            role = AiRole.ASSISTANT.name,
            timestamp = now
        )
        aiConversationDao.insertMessage(initialMessage)

        return conversationId
    }

    override fun sendMessage(conversationId: String, userMessage: String): Flow<AiMessage> = flow {
        val now = System.currentTimeMillis()
        val userMsgId = UUID.randomUUID().toString()

        // 1. Persist user message
        val userEntity = AiMessageEntity(
            id = userMsgId,
            conversationId = conversationId,
            content = userMessage,
            role = AiRole.USER.name,
            timestamp = now
        )
        aiConversationDao.insertMessage(userEntity)

        // Update conversation timestamp
        val conv = aiConversationDao.getConversationById(conversationId)
        if (conv != null) {
            aiConversationDao.updateConversation(conv.copy(updatedAt = now))
        }

        // 2. Fetch context
        val history = aiConversationDao.getMessagesForConversation(conversationId).first()

        // 3. Generate structured engineering response
        val assistantResponseContent = AiEngineeringEngine.generateResponse(userMessage, history)
        val assistantMsgId = UUID.randomUUID().toString()

        // 4. Stream response in progressive stages for realistic AI interaction
        val stages = assistantResponseContent.split("\n\n")
        val buffer = StringBuilder()

        for (i in stages.indices) {
            if (i > 0) buffer.append("\n\n")
            buffer.append(stages[i])

            emit(
                AiMessage(
                    id = assistantMsgId,
                    conversationId = conversationId,
                    content = buffer.toString(),
                    role = AiRole.ASSISTANT,
                    isStreaming = (i < stages.size - 1),
                    timestamp = System.currentTimeMillis()
                )
            )
            delay(120) // Realistic streaming pace
        }

        // 5. Persist final assistant response
        val assistantEntity = AiMessageEntity(
            id = assistantMsgId,
            conversationId = conversationId,
            content = assistantResponseContent,
            role = AiRole.ASSISTANT.name,
            timestamp = System.currentTimeMillis()
        )
        aiConversationDao.insertMessage(assistantEntity)

        // Emit finalized state
        emit(
            AiMessage(
                id = assistantMsgId,
                conversationId = conversationId,
                content = assistantResponseContent,
                role = AiRole.ASSISTANT,
                isStreaming = false,
                timestamp = assistantEntity.timestamp
            )
        )
    }

    override suspend fun generateBlueprint(conversationId: String): ProjectBlueprint {
        val conversation = aiConversationDao.getConversationById(conversationId)
        val messages = aiConversationDao.getMessagesForConversation(conversationId).first()
        val allText = messages.joinToString(" ") { it.content }

        val blueprint = AiEngineeringEngine.createBlueprint(
            conversationId = conversationId,
            userId = conversation?.userId ?: "default-user",
            corpus = allText
        )

        // Update conversation with blueprint flag
        if (conversation != null) {
            aiConversationDao.updateConversation(
                conversation.copy(
                    hasBlueprint = true,
                    blueprintId = blueprint.id,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        return blueprint
    }
}

/**
 * Robust offline/dev-mode AI Project Engineer reasoning engine.
 * Generates structured, production-grade engineering design analysis.
 */
object AiEngineeringEngine {

    fun generateResponse(userPrompt: String, history: List<AiMessageEntity>): String {
        val lower = userPrompt.lowercase()

        return when {
            // Robotics / Drones / Rovers
            lower.contains("drone") || lower.contains("quadcopter") || lower.contains("rover") ||
                    lower.contains("robot") || lower.contains("arm") || lower.contains("thruster") -> {
                buildRoboticsAnalysis(userPrompt)
            }

            // IoT / Environmental / Agriculture / Sensors
            lower.contains("iot") || lower.contains("sensor") || lower.contains("irrigation") ||
                    lower.contains("water") || lower.contains("weather") || lower.contains("air") ||
                    lower.contains("soil") || lower.contains("monitoring") -> {
                buildIotAnalysis(userPrompt)
            }

            // Computer Vision / AI / Waste / Cameras
            lower.contains("vision") || lower.contains("camera") || lower.contains("detection") ||
                    lower.contains("classify") || lower.contains("waste") || lower.contains("opencv") -> {
                buildVisionAnalysis(userPrompt)
            }

            // Renewable / Solar / Battery / Power
            lower.contains("solar") || lower.contains("battery") || lower.contains("power") ||
                    lower.contains("energy") || lower.contains("bms") || lower.contains("mppt") -> {
                buildRenewableAnalysis(userPrompt)
            }

            // CAD / 3D Printing / Mechanical
            lower.contains("cad") || lower.contains("print") || lower.contains("fabricat") ||
                    lower.contains("tolerance") || lower.contains("material") || lower.contains("pla") ||
                    lower.contains("petg") -> {
                buildMechanicalAnalysis(userPrompt)
            }

            // General Engineering Inquiry
            else -> {
                buildComprehensiveEngineeringResponse(userPrompt)
            }
        }
    }

    private fun buildRoboticsAnalysis(query: String): String {
        return """
### Autonomous Robotic System — Engineering Assessment

**1. Problem Decomposition & Functional Scope**
Your inquiry focuses on: *"$query"*.
In robotics system design, success depends on tight closed-loop coupling between kinematics, motor torque budgets, sensory latency, and thermal dissipation under continuous duty.

**2. Recommended System Architecture**
- **Compute Tier**: Microcontroller (ESP32 / STM32F4) dedicated to real-time deterministic PWM motor control, PID loops, and safety watchdog timers, interfaced via UART/SPI to an optional single-board computer (Raspberry Pi 4) for high-level SLAM or computer vision.
- **Actuation Tier**: All-metal gear servos (TowerPro MG996R) or NEMA 17 stepper motors with DRV8825 drivers for anti-backlash positioning.
- **Power Architecture**: 3S LiPo (11.1V nominal) with dual buck regulators (step down to 5V 5A for digital logic, and 7.4V rail for high-torque servo rails) to isolate inductive motor kickback.

**3. Bill of Materials (BOM) Recommendations**
- **MCU**: ESP32-WROOM-32D Development Board (₹480)
- **Actuator**: TowerPro MG996R Digital High-Torque Servo (₹420 × 2)
- **Motor Driver**: L298N Dual H-Bridge Motor Driver Module (₹180)
- **Power**: 11.1V 3S 2200mAh 30C LiPo Pack (₹1,650)
- **Regulation**: LM2596 High-Efficiency Buck Converter (₹90)
*Estimated Subtotal: ₹3,240 (All components readily available in REKORA lab stock).*

**4. Mechanical & CAD Fabrication Guidelines**
- **Material**: Carbon-fiber PETG or ABS for structural motor brackets to resist servo heat deflection (glass transition > 75°C).
- **Print Parameters**: 0.20mm layer height, 4 perimeters (walls), and 40% gyroid infill for isotropic rigidity.
- **Fasteners**: Standardize on M3 stainless steel machine screws with brass heat-set threaded inserts (OD 4.2mm, depth 4.0mm).

**5. Validation & Safety Protocols**
- Measure locked-rotor stall current on bench power supply before connecting LiPo.
- Add flyback diodes across inductive loads and 1000µF low-ESR electrolytic capacitors at motor driver VIN.
- Implement soft-stop firmware limits and a hardware latching emergency kill-switch.
        """.trimIndent()
    }

    private fun buildIotAnalysis(query: String): String {
        return """
### Edge IoT & Environmental Sensing — Engineering Assessment

**1. Functional Requirements & Boundary Conditions**
Target project focus: *"$query"*.
For field deployment, minimizing average quiescent current through deep sleep duty cycles and preventing sensor corrosion are paramount engineering priorities.

**2. System Architecture & Signal Path**
- **Controller**: ESP32 Dual-Core running FreeRTOS with ULP (Ultra-Low Power) co-processor waking periodically every 5–15 minutes.
- **Sensor Array**:
  - Capacitive Soil Moisture Sensor (Corrosion-resistant 555 timer oscillating topology, avoid resistive PCB traces).
  - BME280 for calibrated temperature, relative humidity, and barometric pressure over I2C bus (0x76/0x77).
- **Telemetry Protocol**: MQTT over TLS to cloud dashboard with local flash (SPIFFS/LittleFS) circular buffering during network dropouts.

**3. Hardware BOM & Lab Availability**
- **Core Node**: ESP32 NodeMCU Wi-Fi + BLE (₹480)
- **Atmospheric Sensor**: Bosch BME280 Breakout (₹380)
- **Subsurface Sensor**: Capacitive Soil Moisture Sensor v1.2 (₹120)
- **Energy Storage**: 2× 18650 Li-ion Cells (3400mAh, ₹350 each) + TP4056 with protection IC (₹45)
- **Solar Harvesting**: 5V 2W Monocrystalline Solar Panel (₹320)
*Estimated Subtotal: ₹1,745.*

**4. Enclosure & Environmental Protection**
- 3D print in UV-resistant ASA or PETG with a Stevenson screen ventilated baffle geometry for accurate atmospheric readings.
- Conformal coat all exposed PCB traces with silicone-based conformal spray (MG Chemicals 422B).
- Cable gland entries rated IP65 with silicone O-rings.

**5. Firmware & Validation Benchmarks**
- Verify sleep current ≤ 15µA in ESP32 deep sleep mode.
- Measure battery self-discharge vs solar replenishment curves under overcast simulations.
        """.trimIndent()
    }

    private fun buildVisionAnalysis(query: String): String {
        return """
### Edge Computer Vision & Classification — Engineering Assessment

**1. Objectives & Compute Budget**
Project application: *"$query"*.
Edge computer vision requires balancing inference frame rates, model quantization, and power envelope constraints on embedded accelerators.

**2. Machine Vision Architecture**
- **Vision Pipeline**: OV2640 / Raspberry Pi Camera v2 → OpenCV preprocessing (resize to 224×224 / 320×320, normalize uint8 to float32) → INT8 Quantized MobileNet-SSD / YOLOv8n running on TensorFlow Lite runtime.
- **Inference Target**:
  - For basic detection (<5 FPS): ESP32-CAM module with 4MB PSRAM.
  - For real-time autonomous sorting (15–30 FPS): Raspberry Pi 4 Model B (4GB) or Coral USB Accelerator.

**3. Component Selection & BOM**
- **Compute Unit**: ESP32-CAM Development Board with OV2640 2MP Camera (₹650) OR Raspberry Pi 4 (₹4,800)
- **Lighting**: Ring LED Illuminator with constant-current driver (₹220) to eliminate ambient light variance
- **Actuation for Sorting**: SG90 Micro Servo (₹120) or 12V Solenoid Push-Pull Actuator (₹380)
*Lab Stock Available for immediate checkout or 1-week student rental.*

**4. Mechanical Integration & Optics**
- Mount camera at a fixed focal distance (typically 150mm–250mm) perpendicular to conveyor or collection chute.
- CAD fabricate a shaded inspection shroud with 3D printed matte black PLA to block external glare and reflections.

**5. Validation & Metrics**
- Compute Confusion Matrix on a test set of at least 100 labeled validation frames.
- Target Precision ≥ 92%, Recall ≥ 90%, and inference latency under 80ms per frame.
        """.trimIndent()
    }

    private fun buildRenewableAnalysis(query: String): String {
        return """
### Renewable Energy & Power Systems — Engineering Assessment

**1. Engineering Constraints & Load Profiling**
Focus domain: *"$query"*.
Power harvesting requires precise matching of source internal impedance to load, dynamic Maximum Power Point Tracking (MPPT), and battery safety margins.

**2. Power Train Architecture**
- **Source**: Monocrystalline Photovoltaic Panel (10W–50W, open circuit voltage Voc ~ 21V).
- **MPPT Stage**: Synchronous Buck Topology with Perturb and Observe (P&O) tracking firmware maintaining efficiency > 94%.
- **Battery Management (BMS)**: Multi-cell balancing BMS with dual N-channel MOSFET protection for over-voltage (4.25V), under-voltage (2.8V), and over-current trip.

**3. Recommended Lab Hardware BOM**
- **Controller**: Arduino Nano or STM32 Blue Pill for high-frequency PWM switching (₹350)
- **Sensors**: INA219 High-Side DC Voltage & Current Sensor with I2C (₹160)
- **Switching**: IRFZ44N Logic-Level N-MOSFET + IR2104 Gate Driver (₹140)
- **Energy Buffer**: 12V 7Ah Sealed Lead Acid (₹950) or 3S 18650 Li-ion pack (₹1,200)
*Total Estimated BOM: ₹2,800.*

**4. Validation & Bench Testing**
- Sweep I-V curve with an electronic dummy load under controlled 1000 W/m² halogen illumination.
- Thermal imaging inspection on switching MOSFETs under peak current draw to verify junction temperature stays below 65°C.
        """.trimIndent()
    }

    private fun buildMechanicalAnalysis(query: String): String {
        return """
### Mechanical Prototyping & CAD Fabrication — Engineering Assessment

**1. Design Intent & Structural Criteria**
Topic: *"$query"*.
Fabrication in rapid prototyping demands strict consideration of anisotropic layer adhesion, shrink rates, draft angles, and press-fit tolerances.

**2. Material Selection Matrix**
- **PLA**: Ideal for non-load-bearing enclosures, visual prototypes, and rapid dimensional checks. Heat deflection temp: 55°C.
- **PETG**: Excellent impact resistance, ductile failure mode, and chemical resilience. Suitable for gear housings and outdoor assemblies. Heat deflection: 75°C.
- **Nylon / CF-PETG**: Optimal for gears, sliding linkages, and high-stress robotic joints requiring self-lubrication and high tensile strength.

**3. Tolerance & Fit Standards for FDM 3D Printing**
- **Press-fit (Interference)**: CAD shaft diameter = hole diameter + 0.05mm.
- **Close sliding fit (Bearings/Pins)**: CAD clearance = nominal diameter + 0.20mm.
- **Free running fit (Hinges/Linkages)**: CAD clearance = nominal diameter + 0.40mm.
- **Vertical Holes**: Always oversize hole diameters by +0.3mm to compensate for filament squish.

**4. Next Steps via REKORA Fabrication Hub**
You can submit your STL / STEP CAD file directly through the REKORA Fabrication tab for campus 3D printing and CNC milling with 24-48h turnaround!
        """.trimIndent()
    }

    private fun buildComprehensiveEngineeringResponse(query: String): String {
        return """
### REKORA Project Engineering Consultation

**1. Problem Clarification & Target Specifications**
Based on: *"$query"*.
To build an industry-standard engineering demonstrator, let's establish a structured engineering workflow:
1. Define formal inputs, outputs, and performance constraints.
2. Formulate modular hardware/firmware subsystem blocks.
3. Select commercial-off-the-shelf (COTS) components readily available in our campus inventory.
4. Establish bench testing and safety acceptance criteria.

**2. Proposed Architecture & Implementation Roadmap**
- **Compute & Control**: Dedicated microcontroller with low-latency peripherals (I2C/SPI/UART/PWM).
- **Power Management**: Regulated multi-rail power distribution with reverse polarity and overcurrent protection.
- **Physical Construction**: Parametric 3D printed mechanical chassis with modular mounting standoffs.
- **Firmware**: Structured state machine or FreeRTOS task architecture for deterministic operation.

**3. Next Step Recommendations**
- Ask for a **Detailed Bill of Materials** to see specific part prices and lab stock.
- Or tap **"Generate Project Blueprint"** to produce an exportable system engineering specification complete with milestones, wiring diagrams, and validation checklists!
        """.trimIndent()
    }

    fun createBlueprint(conversationId: String, userId: String, corpus: String): ProjectBlueprint {
        val lower = corpus.lowercase()
        val title: String
        val objective: String
        val domain: EngineeringDomain
        val components: List<BomItem>

        when {
            lower.contains("drone") || lower.contains("quadcopter") || lower.contains("rover") || lower.contains("robot") -> {
                title = "Autonomous Multi-Terrain Robotics Platform"
                objective = "Design and validate an autonomous robotic system featuring closed-loop PID motion control, obstacle avoidance telemetry, and edge computational guidance."
                domain = EngineeringDomain.ROBOTICS
                components = listOf(
                    BomItem(id = UUID.randomUUID().toString(), name = "ESP32-WROOM-32D Development Board", quantity = 1, unitPrice = 480.0, totalPrice = 480.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "TowerPro MG996R Metal-Gear Servos", quantity = 4, unitPrice = 420.0, totalPrice = 1680.0, sourcingChoice = SourcingChoice.RENT),
                    BomItem(id = UUID.randomUUID().toString(), name = "Dual H-Bridge Motor Driver (L298N)", quantity = 2, unitPrice = 180.0, totalPrice = 360.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "11.1V 3S 2200mAh 30C LiPo Battery", quantity = 1, unitPrice = 1650.0, totalPrice = 1650.0, sourcingChoice = SourcingChoice.RENT),
                    BomItem(id = UUID.randomUUID().toString(), name = "HC-SR04 Ultrasonic Distance Sensor", quantity = 3, unitPrice = 90.0, totalPrice = 270.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "3D Printed Structural Chassis (PETG)", quantity = 1, unitPrice = 650.0, totalPrice = 650.0, sourcingChoice = SourcingChoice.FABRICATE)
                )
            }
            lower.contains("irrigation") || lower.contains("soil") || lower.contains("weather") || lower.contains("iot") -> {
                title = "Solar-Backed Smart Micro-Climatic Monitoring Node"
                objective = "Deploy an ultra-low-power edge environmental telemetry node monitoring ambient atmospheric and subsurface parameters with sub-50mA duty cycles."
                domain = EngineeringDomain.IOT
                components = listOf(
                    BomItem(id = UUID.randomUUID().toString(), name = "ESP32 NodeMCU Wi-Fi + BLE Dev Board", quantity = 1, unitPrice = 480.0, totalPrice = 480.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "Bosch BME280 Digital Barometric & RH Sensor", quantity = 1, unitPrice = 380.0, totalPrice = 380.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "Capacitive Soil Moisture Probe v1.2", quantity = 2, unitPrice = 120.0, totalPrice = 240.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "18650 Li-ion 3400mAh High-Capacity Cell", quantity = 2, unitPrice = 350.0, totalPrice = 700.0, sourcingChoice = SourcingChoice.RENT),
                    BomItem(id = UUID.randomUUID().toString(), name = "10W Monocrystalline PV Module", quantity = 1, unitPrice = 850.0, totalPrice = 850.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "IP67 Weatherproof Ventilated Enclosure", quantity = 1, unitPrice = 450.0, totalPrice = 450.0, sourcingChoice = SourcingChoice.FABRICATE)
                )
            }
            else -> {
                title = "Rapid Engineering Hardware Demonstrator"
                objective = "Synthesize an integrated hardware-software demonstrator addressing physical domain sensing, local decision inference, and closed-loop actuation."
                domain = EngineeringDomain.GENERAL
                components = listOf(
                    BomItem(id = UUID.randomUUID().toString(), name = "ESP32-WROOM-32D Development Board", quantity = 1, unitPrice = 480.0, totalPrice = 480.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "Sensory Interface Module (I2C/SPI)", quantity = 2, unitPrice = 250.0, totalPrice = 500.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "Regulated 5V/12V Power Distribution Board", quantity = 1, unitPrice = 320.0, totalPrice = 320.0, sourcingChoice = SourcingChoice.BUY),
                    BomItem(id = UUID.randomUUID().toString(), name = "Actuator Subsystem with Driver Stage", quantity = 1, unitPrice = 750.0, totalPrice = 750.0, sourcingChoice = SourcingChoice.RENT),
                    BomItem(id = UUID.randomUUID().toString(), name = "Parametric 3D Printed Mechanical Frame", quantity = 1, unitPrice = 500.0, totalPrice = 500.0, sourcingChoice = SourcingChoice.FABRICATE)
                )
            }
        }

        val totalCost = components.sumOf { it.totalPrice }

        return ProjectBlueprint(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            userId = userId,
            title = title,
            objective = objective,
            assumptions = listOf(
                "Access to campus electronics lab soldering station and multimeter.",
                "Target operational ambient temperature: 0°C to 50°C.",
                "Primary power source calibrated for minimum 8-hour continuous deployment."
            ),
            functionalRequirements = listOf(
                "Telemetry update rate: >= 1 Hz for active kinematics, <= 15 min for environmental sleep nodes.",
                "Power conversion efficiency >= 88% across voltage regulation stages.",
                "Failsafe hardware cutoff triggered on thermal excursion or communication timeout."
            ),
            proposedArchitecture = "Microcontroller logic core executing periodic tasks with DMA hardware peripherals, driving isolated power stages via optocouplers/MOSFET drivers, reporting metrics over MQTT.",
            components = components,
            estimatedCost = PriceRange(min = totalCost * 0.9, max = totalCost * 1.15, currency = "INR"),
            dependencies = listOf(
                "ESP-IDF or Arduino Core for ESP32",
                "Autodesk Fusion 360 / FreeCAD for chassis refinement",
                "REKORA 3D Printing Hub (PETG/PLA material)"
            ),
            milestones = listOf(
                "Milestone 1: Subsystem breadboard bench validation & sensor calibration (Week 1)",
                "Milestone 2: CAD mechanical integration and FDM chassis printing (Week 2)",
                "Milestone 3: Closed-loop firmware integration and safety limit testing (Week 3)",
                "Milestone 4: Continuous 24h reliability stress run and documentation (Week 4)"
            ),
            validationCriteria = listOf(
                "Current consumption profile conforms to battery lifespan model within 10% error margin.",
                "Sensor measurement noise within datasheet standard deviation under loaded operation.",
                "Chassis withstands normal vibrational shock without fastener loosening."
            ),
            risks = listOf(
                "Back-EMF inductive voltage spikes damaging MCU GPIO lines -> Solved with flyback diodes & optoisolation.",
                "Thermal throttling of voltage regulator under peak actuator load -> Solved with heat sink and switching regulator."
            ),
            openQuestions = listOf(
                "Would you prefer onboard display telemetry (0.96 inch I2C OLED) or web dashboard only?",
                "Do you have existing 18650 cells to reduce initial BOM expenditure?"
            ),
            createdAt = System.currentTimeMillis()
        )
    }
}
