# ⚡ RadTech — Realistic Industrial Electricity & Technology

**RadTech** is a modern, high-performance industrial engineering mod for **Minecraft 1.21.1 (NeoForge)**. It brings authentic electrical engineering concepts into the game: realistic dual-voltage power grids, catenary overhead wires with sag physics, overcurrent cable melting, circuit breakers, heavy multiblock structures, automated quarries, and rechargeable power tools.

---

## 🌟 Key Features

### ⚡ 1. Dual-Tier Voltage Grid
- **Industrial Voltage (380 V)**: Powers workshops, crushing mills, stamping presses, and factory batteries.
- **Distribution High Voltage (10,000 V / 10 kV)**: Transports massive power over long distances with minimal amperage.
- **Transformers**: Heavy step-up (380 V → 10 kV) and step-down (10 kV → 380 V) substations with 95% efficiency.

### 🔌 2. Overhead Wires & Physical Cables
- **Catenary Suspension Wires**: Hang overhead power lines between Wall/Ceiling Connectors up to 16 blocks apart with realistic natural sag!
- **Wire Cutters**: Right-click to cycle wire sag or cut connections instantly.
- **4 Conductor Materials**:
  - **Tin**: 16 A capacity (entry-tier)
  - **Copper**: 30 A capacity (workhorse industrial standard)
  - **Aluminum**: 66 A capacity (high-throughput transmission)
  - **Iron**: 120 A capacity (heavy-duty substation grade)
- **Insulated & Bare Variants**: Bare wires are cheap to roll but dangerous — beware of electrocution!
- **Real Electrical Hazards**: Exceeding a wire's rated amperage causes overheating, smoke, spark bursts, and catastrophic burnouts!

### 🛡️ 3. Grid Protection & Monitoring
- **Circuit Breaker**: Protects factory circuits against destructive overloads. Features adjustable trip threshold (16A, 32A, 48A, 64A, 128A) and a physical reset switch.
- **Panel Meter**: Wall-mounted digital voltmeter and ammeter for real-time diagnostics.
- **Alarm Siren**: Emits audible warnings and red alert strobes when systems are overloaded.

### 🏭 4. Heavy Industrial Machinery
- **Crusher**: Pulverizes raw metals and ores into fine dust with **2× yield** doubling efficiency.
- **Press**: Heavy hydraulic press that stamps metal ingots into dense industrial plates.
- **Rolling Mill**: High-precision rolling station that extrudes metal plates into spools of electrical wire.

### 🔋 5. Power Generation & Energy Storage
- **Thermal Generator**: Solid-fuel boiler generating clean 380V power at 12 A (4.56 kW).
- **Large Thermal Power Station**: 3×3 multiblock industrial boiler with 4 fuel bays, generating 48 A (18.2 kW) with +50% combustion efficiency!
- **Solar Panels**: Eco-friendly rooftop generation with real day/weather sensors.
- **Lead-Acid Battery (4 MWh)**: Reliable storage with real electrolyte thermal physics (overheating & boil-off hazard if stressed above 32 A).
- **Lithium-Ion Battery (16 MWh)**: Advanced power storage capable of massive 512 A bursts without risk of explosion.
- **Charging Slots**: Built-in charge (`⚡+`) and discharge (`⚡−`) slots to recharge your tools and gadgets directly in battery GUIs.

### 🏗️ 6. Multiblock Industrial Quarry
- Automated large-scale mining rig with a 3×3 reinforced casing foundation.
- Automatically clears an 11×11 area downwards tier-by-tier all the way to bedrock.
- Directly feeds mined ores into top chests/inventories or stores them in an internal 18-slot buffer.

### 🛠️ 7. Rechargeable Electric Tools
- **Electric Drill**: 250,000 Wh battery capacity. Features instant toggleable modes (**1×1 precision** and **3×3 tunnel digger** via `Shift + Right Click`).
- **Electric Chainsaw**: High-rpm woodcutter capable of felling entire trees in seconds (`Shift + Mine`) and dealing devastating weapon damage.

---

## 📖 Getting Started Guide

1. **Prospect for Ores**: Mine Tin, Aluminum, Lithium, Neodymium, Titanium, and Tungsten deep underground.
2. **Craft a Thermal Generator & Connectors**: Place the generator, connect bare or insulated wire coils to your connectors.
3. **Build a Crusher**: Double your raw ore output into dust and smelt it in a furnace.
4. **Stamp Plates & Roll Coils**: Craft a **Press** to make plates, then feed plates into a **Rolling Mill** to produce wire coils effortlessly.
5. **Protect your Grid**: Install a **Circuit Breaker** between your battery bank and high-drain machines to prevent wire melt-down!

---

## 🔌 Compatibility & Translations

- **Recipe Viewers**: Full native support for **JEI (Just Enough Items)** and **REI (Roughly Enough Items)**.
- **Recipe Book**: Integrated into vanilla crafting recipe books.
- **Languages**: Full translation into **English (`en_us`)** and **Ukrainian (`uk_ua`)**.
- **Performance**: Zero-allocation client wire renderer, cached $O(1)$ recipe engine, and throttled chunk updates for maximum server TPS.

---

## 📦 Requirements

- **Minecraft**: `1.21.1`
- **Mod Loader**: `NeoForge 21.1.241+`
- **Dependency**: [Kotlin for Forge (KFF)](https://modrinth.com/mod/kotlin-for-forge)
