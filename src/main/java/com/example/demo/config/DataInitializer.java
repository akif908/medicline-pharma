package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with sample data on startup
 * so the application is immediately usable.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final DoctorRepository doctorRepository;
        private final MedicineRepository medicineRepository;
        private final com.example.demo.repository.DoctorSlotRepository doctorSlotRepository;
        private final com.example.demo.repository.AppointmentRepository appointmentRepository;
        private final com.example.demo.repository.NotificationRepository notificationRepository;
        private final com.example.demo.repository.MedicalHistoryRepository medicalHistoryRepository;
        private final com.example.demo.repository.ConsultationRepository consultationRepository;
        private final com.example.demo.repository.PrescriptionRepository prescriptionRepository;
        private final com.example.demo.repository.SupplierRepository supplierRepository;

        private String currentCategory = "General";

        @Override
        public void run(String... args) {
                // Only seed users/doctors if empty
                if (userRepository.count() == 0) {

                        // ---- Create Users ----
                        createUser("Admin", "admin@mediclinepharma.com", "admin123", Role.ADMIN);
                        createUser("Elena Rodriguez", "elena@mediclinepharma.com", "password", Role.PATIENT);
                        createUser("Marcus Wright", "marcus@mediclinepharma.com", "password", Role.PATIENT);

                        User docUser1 = createUser("Dr. Julian Vance", "julian@mediclinepharma.com", "password",
                                        Role.DOCTOR);
                        User docUser2 = createUser("Dr. Elena Rodriguez", "drelenr@mediclinepharma.com", "password",
                                        Role.DOCTOR);
                        User docUser3 = createUser("Dr. Marcus Thorne", "marcus.t@mediclinepharma.com", "password",
                                        Role.DOCTOR);
                        User docUser4 = createUser("Dr. Sarah Jenkins", "sarah@mediclinepharma.com", "password",
                                        Role.DOCTOR);

                        // ---- Create Doctors ----
                        createDoctor(docUser1, "Neurology Specialist", 12,
                                        "Expert in neurological disorders and brain health.");
                        createDoctor(docUser2, "Cardiology", 15,
                                        "Specializing in heart disease and cardiovascular health.");
                        createDoctor(docUser3, "Pediatrics", 8,
                                        "Dedicated to children's health and development.");
                        createDoctor(docUser4, "Dermatology", 10,
                                        "Skin care specialist with expertise in clinical dermatology.");

                        // Removed slot creation from here as it's moved below
                }

                // ---- Refresh Slots on every startup ----
                // Always ensure fresh upcoming slots are available for each doctor
                java.util.List<DoctorSlot> existingSlots = doctorSlotRepository.findAll();
                java.time.LocalDate today = java.time.LocalDate.now();
                int deleted = 0;
                for (DoctorSlot s : existingSlots) {
                        if (!s.isBooked() && (s.getDate().isBefore(today) || s.getDate().equals(today))) {
                                doctorSlotRepository.delete(s);
                                deleted++;
                        }
                }
                if (deleted > 0) {
                        doctorSlotRepository.flush();
                        System.out.println("🧹 Purged " + deleted + " old unbooked slots.");
                }

                if (doctorRepository.count() > 0) {
                        java.util.List<Doctor> docs = doctorRepository.findAll();
                        if (docs.size() >= 4) {
                                // Group slots by doctor ID to determine who needs more
                                java.util.Map<Long, Long> slotCounts = existingSlots.stream()
                                                .filter(s -> !s.isBooked() && s.getDate().isAfter(today))
                                                .collect(java.util.stream.Collectors.groupingBy(
                                                                s -> s.getDoctor().getId(),
                                                                java.util.stream.Collectors.counting()));

                                boolean seeded = false;
                                java.time.LocalDate tomorrow = today.plusDays(1);
                                java.time.LocalDate dayAfter = today.plusDays(2);

                                for (Doctor doc : docs) {
                                        long count = slotCounts.getOrDefault(doc.getId(), 0L);
                                        if (count < 2) {
                                                createSlot(doc, tomorrow, java.time.LocalTime.of(9, 0),
                                                                java.time.LocalTime.of(9, 30));
                                                createSlot(doc, tomorrow, java.time.LocalTime.of(10, 0),
                                                                java.time.LocalTime.of(10, 30));
                                                createSlot(doc, dayAfter, java.time.LocalTime.of(11, 0),
                                                                java.time.LocalTime.of(11, 30));
                                                seeded = true;
                                        }
                                }

                                if (seeded) {
                                        System.out.println(
                                                        "✅ Seeded fresh doctor slots for " + docs.size() + " doctors.");
                                }
                        }
                }

                // ---- Sync missing doctor notifications for PENDING appointments ----
                java.util.List<Appointment> pendingAppts = appointmentRepository.findAll().stream()
                                .filter(a -> "PENDING".equals(a.getStatus()))
                                .collect(java.util.stream.Collectors.toList());
                for (Appointment appt : pendingAppts) {
                        User doctorUser = appt.getDoctor().getUser();
                        // Check if this doctor already has a notification for this appointment
                        boolean hasNotif = notificationRepository.findByUserOrderByCreatedAtDesc(doctorUser)
                                        .stream()
                                        .anyMatch(n -> n.getMessage().contains(appt.getPatient().getName())
                                                        && n.getMessage().contains(
                                                                        appt.getAppointmentDate().toString()));
                        if (!hasNotif) {
                                Notification n = new Notification();
                                n.setUser(doctorUser);
                                n.setMessage("New appointment request from " + appt.getPatient().getName()
                                                + " for " + appt.getAppointmentDate()
                                                + " at " + appt.getStartTime());
                                notificationRepository.save(n);
                                System.out.println("✅ Created missing notification for Dr. " + doctorUser.getName());
                        }
                }

                // ---- Create Medicines ----
                if (medicineRepository.count() == 0
                                || medicineRepository.findAll().stream()
                                                .noneMatch(m -> m.getName().contains("NeuroZen Forte"))) {
                        // Add new real medicines if they don't exist
                        createMedicine("Amoxicillin 500mg", 12.50, 50,
                                        "Penicillin antibiotic used to treat bacterial infections.",
                                        "/medicine-pics/amoxicillin.jfif");
                        createMedicine("Ibuprofen 400mg", 8.99, 120,
                                        "NSAID used to reduce fever and treat pain or inflammation.",
                                        "/medicine-pics/advance ibuprofen.webp");
                        createMedicine("Paracetamol 500mg", 5.50, 200,
                                        "Pain reliever and a fever reducer used to treat headaches.",
                                        "/medicine-pics/paracetamol.webp");
                        createMedicine("Aspirin 325mg", 6.20, 85,
                                        "Salicylate used to treat pain, and reduce fever or inflammation.",
                                        "/medicine-pics/aspirin.jpg");

                        // ============================================================
                        // MEDICINES SEED — createMedicine() format
                        // Paste into Cursor and say:
                        // "Call all these createMedicine() methods in my seeder/init file"
                        // ============================================================

                        // ─── ANALGESICS ─────────────────────────────────────────────
                        createMedicine("Paracetamol 500mg", 5.50, 200,
                                        "Pain reliever and a fever reducer used to treat headaches and mild to moderate pain.",
                                        "/medicine-pics/paracetamol.webp");
                        createMedicine("Ibuprofen 400mg", 8.99, 120,
                                        "NSAID used to reduce fever and treat pain or inflammation caused by arthritis or injury.",
                                        "/medicine-pics/advance ibuprofen.webp");
                        createMedicine("Aspirin 325mg", 6.20, 85,
                                        "Salicylate used to treat pain, reduce fever or inflammation, and prevent blood clots.",
                                        "/medicine-pics/aspirin.jpg");
                        createMedicine("Diclofenac 50mg", 11.80, 90,
                                        "Anti-inflammatory NSAID for arthritis, sprains, and menstrual pain. Available as tablet or gel.",
                                        "https://upload.wikimedia.org/wikipedia/commons/6/6f/Diclofenac_Gel.jpg");
                        createMedicine("Tramadol 50mg", 14.50, 60,
                                        "Opioid-like analgesic for moderate to severe pain acting on the central nervous system.",
                                        "https://upload.wikimedia.org/wikipedia/commons/0/09/Tramadol_50_mg_capsules.jpg");
                        createMedicine("Naproxen 500mg", 9.40, 95,
                                        "NSAID for long-lasting relief of arthritis, gout, menstrual cramps, and muscle aches.",
                                        "https://upload.wikimedia.org/wikipedia/commons/b/b0/Naproxen_tablets.jpg");

                        // ─── ANTIBIOTICS ────────────────────────────────────────────
                        createMedicine("Amoxicillin 500mg", 12.50, 50,
                                        "Penicillin antibiotic used to treat bacterial infections of the ear, throat, chest, and urinary tract.",
                                        "/medicine-pics/amoxicillin.jfif");
                        createMedicine("Azithromycin 500mg", 18.75, 70,
                                        "Macrolide antibiotic for respiratory infections, skin infections, and sexually transmitted diseases.",
                                        "https://upload.wikimedia.org/wikipedia/commons/7/7e/Azithromycin.jpg");
                        createMedicine("Ciprofloxacin 500mg", 15.00, 85,
                                        "Fluoroquinolone antibiotic for urinary tract, gastrointestinal, and respiratory infections.",
                                        "https://upload.wikimedia.org/wikipedia/commons/8/80/Ciprofloxacin_500mg_Tablets.jpg");
                        createMedicine("Metronidazole 400mg", 9.30, 95,
                                        "Antibiotic and antiprotozoal for anaerobic infections, H. pylori, and amoebic dysentery.",
                                        "https://upload.wikimedia.org/wikipedia/commons/a/a4/Metronidazole.jpg");
                        createMedicine("Doxycycline 100mg", 13.40, 75,
                                        "Tetracycline antibiotic for malaria prophylaxis, acne, chlamydia, and Lyme disease.",
                                        "https://upload.wikimedia.org/wikipedia/commons/3/30/Doxycycline_100mg_capsules.jpg");
                        createMedicine("Cephalexin 500mg", 14.20, 65,
                                        "First-generation cephalosporin antibiotic for skin, bone, ear, and urinary tract infections.",
                                        "https://upload.wikimedia.org/wikipedia/commons/c/c9/Cephalexin_capsules.jpg");
                        createMedicine("Clindamycin 300mg", 16.80, 55,
                                        "Lincosamide antibiotic for serious anaerobic and gram-positive bacterial infections.",
                                        "https://upload.wikimedia.org/wikipedia/commons/c/c5/Clindamycin_capsules.jpg");

                        // ─── ANTIHYPERTENSIVES ──────────────────────────────────────
                        createMedicine("Amlodipine 5mg", 10.20, 110,
                                        "Calcium channel blocker for hypertension and angina. Relaxes blood vessels to reduce heart workload.",
                                        "https://upload.wikimedia.org/wikipedia/commons/2/29/Amlodipine_5_mg_tablets.jpg");
                        createMedicine("Lisinopril 10mg", 11.50, 100,
                                        "ACE inhibitor for hypertension and heart failure. Also protects kidneys in diabetic patients.",
                                        "https://upload.wikimedia.org/wikipedia/commons/b/b3/Lisinopril_10mg.jpg");
                        createMedicine("Atenolol 50mg", 9.80, 88,
                                        "Beta-blocker for hypertension, angina, and arrhythmia. Slows heart rate and lowers blood pressure.",
                                        "https://upload.wikimedia.org/wikipedia/commons/d/d8/Atenolol_50mg_Tablets.jpg");
                        createMedicine("Losartan 50mg", 13.60, 92,
                                        "ARB antihypertensive for high blood pressure, diabetic nephropathy, and heart failure prevention.",
                                        "https://upload.wikimedia.org/wikipedia/commons/5/52/Losartan_50_mg_film_coated_tablet.jpg");
                        createMedicine("Furosemide 40mg", 7.90, 130,
                                        "Loop diuretic for fluid retention and edema caused by heart failure, liver, or kidney disease.",
                                        "https://upload.wikimedia.org/wikipedia/commons/c/ca/Furosemide40mg.jpg");
                        createMedicine("Hydrochlorothiazide 25mg", 8.10, 105,
                                        "Thiazide diuretic for hypertension and edema. Often combined with other antihypertensives.",
                                        "https://upload.wikimedia.org/wikipedia/commons/h/hc/Hydrochlorothiazide_25mg.jpg");

                        // ─── ANTIDIABETICS ──────────────────────────────────────────
                        createMedicine("Metformin 500mg", 8.50, 140,
                                        "First-line oral antidiabetic for type 2 diabetes. Reduces hepatic glucose output and improves insulin sensitivity.",
                                        "https://upload.wikimedia.org/wikipedia/commons/5/51/Metformin_500mg_Tablets.jpg");
                        createMedicine("Glibenclamide 5mg", 10.10, 70,
                                        "Sulfonylurea that stimulates insulin secretion from pancreatic beta cells for type 2 diabetes.",
                                        "https://upload.wikimedia.org/wikipedia/commons/e/e5/Glibenclamide.jpg");
                        createMedicine("Insulin Glargine 100IU/ml", 45.00, 30,
                                        "Long-acting basal insulin for type 1 and type 2 diabetes. Provides steady 24-hour glucose control.",
                                        "https://upload.wikimedia.org/wikipedia/commons/9/9b/Insulin_pen.jpg");
                        createMedicine("Sitagliptin 100mg", 32.50, 45,
                                        "DPP-4 inhibitor for type 2 diabetes. Stimulates insulin release and reduces glucagon when blood sugar is high.",
                                        "https://upload.wikimedia.org/wikipedia/commons/s/si/Sitagliptin_tablets.jpg");

                        // ─── GASTROINTESTINAL ───────────────────────────────────────
                        createMedicine("Omeprazole 20mg", 9.00, 160,
                                        "Proton pump inhibitor for GERD, peptic ulcer disease, and H. pylori eradication therapy.",
                                        "https://upload.wikimedia.org/wikipedia/commons/9/9e/Omeprazole_20_mg_capsules.jpg");
                        createMedicine("Ondansetron 4mg", 16.00, 65,
                                        "Antiemetic that blocks serotonin receptors to prevent nausea and vomiting after chemotherapy or surgery.",
                                        "https://upload.wikimedia.org/wikipedia/commons/0/04/Ondansetron_4mg_tablets.jpg");
                        createMedicine("Loperamide 2mg", 6.50, 110,
                                        "Antidiarrheal that slows intestinal movement. Used for acute and traveler's diarrhea.",
                                        "https://upload.wikimedia.org/wikipedia/commons/b/b4/Loperamide_2mg_capsules.jpg");
                        createMedicine("Bisacodyl 5mg", 5.80, 95,
                                        "Stimulant laxative for constipation relief and bowel preparation before medical procedures.",
                                        "https://upload.wikimedia.org/wikipedia/commons/3/31/Bisacodyl_Tablets.jpg");
                        createMedicine("Pantoprazole 40mg", 11.20, 88,
                                        "Proton pump inhibitor for erosive esophagitis, Zollinger-Ellison syndrome, and GERD.",
                                        "https://upload.wikimedia.org/wikipedia/commons/p/pa/Pantoprazole_40mg.jpg");

                        // ─── RESPIRATORY ────────────────────────────────────────────
                        createMedicine("Salbutamol 100mcg Inhaler", 22.00, 55,
                                        "Short-acting bronchodilator for rapid relief of asthma and COPD bronchospasm. 200 doses per inhaler.",
                                        "https://upload.wikimedia.org/wikipedia/commons/4/4d/Salbutamol_Inhaler.jpg");
                        createMedicine("Budesonide 200mcg Inhaler", 34.00, 40,
                                        "Inhaled corticosteroid for daily asthma prevention and COPD management. Reduces airway inflammation.",
                                        "https://upload.wikimedia.org/wikipedia/commons/b/bd/Budesonide_Inhaler.jpg");
                        createMedicine("Cetirizine 10mg", 7.20, 130,
                                        "Second-generation antihistamine for allergic rhinitis, hay fever, and urticaria. Low sedation.",
                                        "https://upload.wikimedia.org/wikipedia/commons/5/53/Cetirizine_Hydrochloride_10mg_Tablets.jpg");
                        createMedicine("Loratadine 10mg", 6.90, 120,
                                        "Non-sedating antihistamine for seasonal allergic rhinitis and chronic urticaria. Once daily dosing.",
                                        "https://upload.wikimedia.org/wikipedia/commons/l/la/Loratadine_10_mg_tablets.jpg");
                        createMedicine("Montelukast 10mg", 19.50, 68,
                                        "Leukotriene antagonist for daily prevention of asthma symptoms and seasonal allergic rhinitis.",
                                        "https://upload.wikimedia.org/wikipedia/commons/m/m3/Montelukast_Sodium_Tablets.jpg");

                        // ─── CARDIOVASCULAR ─────────────────────────────────────────
                        createMedicine("Atorvastatin 20mg", 14.80, 105,
                                        "Statin for lowering LDL cholesterol and reducing risk of heart attack and stroke.",
                                        "https://upload.wikimedia.org/wikipedia/commons/a/a0/Atorvastatin_20_mg_Tablets.jpg");
                        createMedicine("Warfarin 5mg", 8.40, 75,
                                        "Anticoagulant to prevent blood clots in DVT, pulmonary embolism, and atrial fibrillation.",
                                        "https://upload.wikimedia.org/wikipedia/commons/w/w1/Warfarin_tablets.jpg");
                        createMedicine("Clopidogrel 75mg", 17.30, 80,
                                        "Antiplatelet agent for prevention of heart attack and stroke in high-risk cardiovascular patients.",
                                        "https://upload.wikimedia.org/wikipedia/commons/c/cf/Clopidogrel_75mg.jpg");
                        createMedicine("Nitroglycerin 0.5mg Sublingual", 12.00, 45,
                                        "Rapid-onset nitrate for immediate relief of acute angina. Dilates coronary arteries within minutes.",
                                        "https://upload.wikimedia.org/wikipedia/commons/n/nb/Nitroglycerin_tablets.jpg");
                        createMedicine("Digoxin 0.25mg", 9.70, 58,
                                        "Cardiac glycoside for heart failure and atrial fibrillation. Strengthens heart contractions and slows rate.",
                                        "https://upload.wikimedia.org/wikipedia/commons/d/dc/Digoxin_tablets.jpg");

                        // ─── CNS / NEUROLOGICAL ─────────────────────────────────────
                        createMedicine("Sertraline 50mg", 21.00, 72,
                                        "SSRI antidepressant for depression, OCD, PTSD, panic disorder, and social anxiety disorder.",
                                        "https://upload.wikimedia.org/wikipedia/commons/s/se/Sertraline_50_mg_tablets.jpg");
                        createMedicine("Fluoxetine 20mg", 19.80, 78,
                                        "SSRI for major depressive disorder, bulimia nervosa, OCD, and panic disorder. Once daily dosing.",
                                        "https://upload.wikimedia.org/wikipedia/commons/f/f7/Fluoxetine_20mg_capsules.jpg");
                        createMedicine("Diazepam 5mg", 11.00, 55,
                                        "Benzodiazepine for short-term anxiety, muscle spasm, alcohol withdrawal, and seizure adjunct.",
                                        "https://upload.wikimedia.org/wikipedia/commons/d/d1/Diazepam_5mg_Tablets.jpg");
                        createMedicine("Phenytoin 100mg", 13.50, 50,
                                        "Antiepileptic for tonic-clonic and partial seizures. Requires therapeutic drug monitoring.",
                                        "https://upload.wikimedia.org/wikipedia/commons/p/ph/Phenytoin_capsules.jpg");
                        createMedicine("Carbamazepine 200mg", 12.80, 62,
                                        "Anticonvulsant for epilepsy, trigeminal neuralgia, and bipolar disorder mood stabilization.",
                                        "https://upload.wikimedia.org/wikipedia/commons/c/cb/Carbamazepine_200mg_tablets.jpg");

                        // ─── ENDOCRINE / HORMONES ───────────────────────────────────
                        createMedicine("Levothyroxine 50mcg", 10.50, 115,
                                        "Synthetic thyroid hormone for hypothyroidism. Take fasting in the morning for optimal absorption.",
                                        "https://upload.wikimedia.org/wikipedia/commons/l/lv/Levothyroxine_tablets.jpg");
                        createMedicine("Prednisolone 5mg", 9.60, 98,
                                        "Oral corticosteroid for autoimmune conditions, severe asthma, allergies, and inflammatory diseases.",
                                        "https://upload.wikimedia.org/wikipedia/commons/p/pr/Prednisolone_tablets.jpg");
                        createMedicine("Hydrocortisone 10mg", 8.90, 80,
                                        "Corticosteroid for adrenal insufficiency and inflammatory conditions. Natural cortisol replacement.",
                                        "https://upload.wikimedia.org/wikipedia/commons/h/hd/Hydrocortisone_tablets.jpg");

                        // ─── ANTIMALARIALS / ANTIPARASITICS ─────────────────────────
                        createMedicine("Artemether/Lumefantrine 20/120mg", 28.00, 40,
                                        "Artemisinin combination therapy for uncomplicated Plasmodium falciparum malaria. Full 6-dose course.",
                                        "https://upload.wikimedia.org/wikipedia/commons/a/a6/Coartem.jpg");
                        createMedicine("Chloroquine 250mg", 11.20, 60,
                                        "Aminoquinoline antimalarial for treatment and prophylaxis of malaria and rheumatoid arthritis.",
                                        "https://upload.wikimedia.org/wikipedia/commons/c/c0/Chloroquine.jpg");
                        createMedicine("Albendazole 400mg", 8.70, 85,
                                        "Broad-spectrum anthelmintic for intestinal worm infections, neurocysticercosis, and hydatid disease.",
                                        "https://upload.wikimedia.org/wikipedia/commons/a/ab/Albendazole_400mg.jpg");

                        // ─── ANTIVIRALS ──────────────────────────────────────────────
                        createMedicine("Acyclovir 400mg", 15.60, 65,
                                        "Antiviral for herpes simplex (oral and genital), herpes zoster, and varicella infections.",
                                        "https://upload.wikimedia.org/wikipedia/commons/a/a8/Aciclovir_tablets.jpg");
                        createMedicine("Oseltamivir 75mg", 38.00, 35,
                                        "Neuraminidase inhibitor for influenza A and B treatment and prophylaxis. 5-day treatment course.",
                                        "https://upload.wikimedia.org/wikipedia/commons/o/o8/Tamiflu.jpg");

                        // ─── SUPPLEMENTS ────────────────────────────────────────────
                        createMedicine("Vitamin D3 1000IU — 60 Capsules", 12.90, 180,
                                        "Cholecalciferol for vitamin D deficiency, bone health, immune support, and calcium absorption.",
                                        "https://upload.wikimedia.org/wikipedia/commons/v/v4/Vitamin_D_supplement.jpg");
                        createMedicine("Ferrous Sulfate 200mg", 7.40, 140,
                                        "Iron supplement for iron deficiency anemia and pregnancy supplementation. Take with vitamin C.",
                                        "https://upload.wikimedia.org/wikipedia/commons/f/f2/Ferrous_sulfate_tablets.jpg");
                        createMedicine("Folic Acid 5mg", 5.20, 200,
                                        "B-vitamin for megaloblastic anemia prevention and neural tube defect prevention during pregnancy.",
                                        "https://upload.wikimedia.org/wikipedia/commons/f/fc/Folic_acid_tablets.jpg");
                        createMedicine("Omega-3 Fish Oil 1000mg — 90 Softgels", 24.99, 90,
                                        "EPA and DHA supplement for heart health, triglyceride reduction, and anti-inflammatory support.",
                                        "/medicine-pics/omega-3 fish oil.webp");
                        createMedicine("Vitamin C 500mg — 60 Tablets", 8.00, 210,
                                        "Ascorbic acid for immune support, collagen synthesis, iron absorption, and antioxidant protection.",
                                        "https://upload.wikimedia.org/wikipedia/commons/v/vc/Vitamin_C_supplement_tablets.jpg");
                        createMedicine("Multi-Vitamin Complex — 60 Capsules", 18.50, 100,
                                        "Daily multivitamin with vitamins A, B-complex, C, D, E, K, and essential minerals for general health.",
                                        "/medicine-pics/multivitamin.jfif");

                        // ─── ANTIFUNGAL / DERMATOLOGY ───────────────────────────────
                        createMedicine("Clotrimazole 1% Cream 20g", 9.10, 95,
                                        "Topical antifungal for tinea pedis, ringworm, and vaginal candidiasis. Apply 2 to 3 times daily.",
                                        "https://upload.wikimedia.org/wikipedia/commons/c/c3/Clotrimazole_cream.jpg");
                        createMedicine("Betamethasone 0.1% Cream 15g", 11.30, 78,
                                        "Potent topical corticosteroid for eczema, psoriasis, and contact dermatitis. Apply thinly once daily.",
                                        "https://upload.wikimedia.org/wikipedia/commons/b/b8/Betamethasone_cream.jpg");
                        createMedicine("Tretinoin 0.025% Cream 20g", 23.50, 50,
                                        "Topical retinoid for acne vulgaris and photoaging. Increases skin cell turnover. Apply at night only.",
                                        "https://upload.wikimedia.org/wikipedia/commons/r/r4/Tretinoin_cream.jpg");
                        createMedicine("Fluconazole 150mg", 13.70, 72,
                                        "Systemic antifungal for vaginal candidiasis, oral thrush, and cryptococcal meningitis. Single dose for yeast.",
                                        "https://upload.wikimedia.org/wikipedia/commons/f/fl/Fluconazole_capsules.jpg");

                        // ─── SPECIALTY MEDICINES ────────────────────────────────────
                        createMedicine("NeuroZen Forte", 42.00, 50,
                                        "Focus & Neural Clarity — 60 Capsules",
                                        "/medicine-pics/neurozan.jpg");
                        createMedicine("Vitalis Serum", 29.50, 3,
                                        "Complex Vitamin B12 — 30ml Bottle",
                                        "/medicine-pics/Vitalis Serum.jfif");
                        createMedicine("CardioSafe 50mg", 18.20, 40,
                                        "Heart Rate Regulator — 30 Tablets",
                                        "/medicine-pics/CARDIOSAFE.webp");
                        createMedicine("AeroBreath Duo", 55.00, 10,
                                        "Advanced Inhalation Relief — 2 Inhalers",
                                        "/medicine-pics/Aerochamber.webp");
                        createMedicine("Smart Thermometer", 35.00, 15,
                                        "Digital Smart Infrared Thermometer — Fast and Accurate Reading",
                                        "/medicine-pics/smart thermometer.webp");

                }

                // Ensure specific products are seeded even if the main block didn't run
                if (medicineRepository.findAll().stream().noneMatch(m -> m.getName().contains("Smart Thermometer"))) {
                        createMedicine("Smart Thermometer", 35.00, 15,
                                        "Digital Smart Infrared Thermometer — Fast and Accurate Reading",
                                        "/medicine-pics/smart thermometer.webp");
                        System.out.println("✅ Seeded missing Smart Thermometer.");
                }

                // --- Aggressive update of image URLs for existing medicines ---
                medicineRepository.findAll().forEach(m -> {
                        boolean updated = false;
                        String currentUrl = m.getImageUrl() != null ? m.getImageUrl() : "";

                        if (m.getName().contains("Amoxicillin")
                                        && !"/medicine-pics/amoxicillin.jfif".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/amoxicillin.jfif");
                                updated = true;
                        } else if (m.getName().contains("Ibuprofen")
                                        && !"/medicine-pics/advance ibuprofen.webp".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/advance ibuprofen.webp");
                                updated = true;
                        } else if (m.getName().contains("Paracetamol")
                                        && !"/medicine-pics/paracetamol.webp".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/paracetamol.webp");
                                updated = true;
                        } else if (m.getName().contains("Aspirin")
                                        && !"/medicine-pics/aspirin.jpg".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/aspirin.jpg");
                                updated = true;
                        } else if (m.getName().contains("NeuroZen")
                                        && !"/medicine-pics/neurozan.jpg".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/neurozan.jpg");
                                updated = true;
                        } else if (m.getName().contains("Vitalis")
                                        && !"/medicine-pics/Vitalis Serum.jfif".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/Vitalis Serum.jfif");
                                updated = true;
                        } else if (m.getName().contains("CardioSafe")
                                        && !"/medicine-pics/CARDIOSAFE.webp".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/CARDIOSAFE.webp");
                                updated = true;
                        } else if (m.getName().contains("AeroBreath")
                                        && !"/medicine-pics/Aerochamber.webp".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/Aerochamber.webp");
                                updated = true;
                        } else if (m.getName().contains("Omega-3")
                                        && !"/medicine-pics/omega-3 fish oil.webp".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/omega-3 fish oil.webp");
                                updated = true;
                        } else if (m.getName().contains("Multi-Vitamin")
                                        && !"/medicine-pics/multivitamin.jfif".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/multivitamin.jfif");
                                updated = true;
                        } else if (m.getName().contains("Thermometer")
                                        && !"/medicine-pics/smart thermometer.webp".equals(currentUrl)) {
                                m.setImageUrl("/medicine-pics/smart thermometer.webp");
                                updated = true;
                        }
                        if (updated)
                                medicineRepository.save(m);
                });

                // Assign categories to medicines
                medicineRepository.findAll().forEach(m -> {
                        if (m.getCategory() == null || m.getCategory().isEmpty() || "General".equals(m.getCategory())) {
                                String name = m.getName().toLowerCase();
                                if (name.contains("paracetamol") || name.contains("ibuprofen") || name.contains("aspirin") || name.contains("diclofenac") || name.contains("tramadol") || name.contains("naproxen")) {
                                        m.setCategory("Analgesics");
                                } else if (name.contains("amoxicillin") || name.contains("azithromycin") || name.contains("ciprofloxacin") || name.contains("metronidazole") || name.contains("doxycycline") || name.contains("cephalexin") || name.contains("clindamycin")) {
                                        m.setCategory("Antibiotics");
                                } else if (name.contains("amlodipine") || name.contains("lisinopril") || name.contains("atenolol") || name.contains("losartan") || name.contains("furosemide") || name.contains("hydrochlorothiazide")) {
                                        m.setCategory("Antihypertensives");
                                } else if (name.contains("metformin") || name.contains("glibenclamide") || name.contains("insulin") || name.contains("sitagliptin")) {
                                        m.setCategory("Antidiabetics");
                                } else if (name.contains("omeprazole") || name.contains("ondansetron") || name.contains("loperamide") || name.contains("bisacodyl") || name.contains("pantoprazole")) {
                                        m.setCategory("Gastrointestinal");
                                } else if (name.contains("salbutamol") || name.contains("budesonide") || name.contains("cetirizine") || name.contains("loratadine") || name.contains("montelukast") || name.contains("aerobreath")) {
                                        m.setCategory("Respiratory");
                                } else if (name.contains("atorvastatin") || name.contains("warfarin") || name.contains("clopidogrel") || name.contains("nitroglycerin") || name.contains("digoxin") || name.contains("cardiosafe")) {
                                        m.setCategory("Cardiovascular");
                                } else if (name.contains("sertraline") || name.contains("fluoxetine") || name.contains("diazepam") || name.contains("phenytoin") || name.contains("carbamazepine") || name.contains("neurozen")) {
                                        m.setCategory("Neurological");
                                } else if (name.contains("levothyroxine") || name.contains("prednisolone") || name.contains("hydrocortisone")) {
                                        m.setCategory("Hormones");
                                } else if (name.contains("artemether") || name.contains("chloroquine") || name.contains("albendazole")) {
                                        m.setCategory("Antiparasitics");
                                } else if (name.contains("acyclovir") || name.contains("oseltamivir")) {
                                        m.setCategory("Antivirals");
                                } else if (name.contains("vitamin") || name.contains("ferrous") || name.contains("folic") || name.contains("omega") || name.contains("vitalis")) {
                                        m.setCategory("Supplements");
                                } else if (name.contains("clotrimazole") || name.contains("betamethasone") || name.contains("tretinoin") || name.contains("fluconazole")) {
                                        m.setCategory("Dermatology");
                                } else {
                                        m.setCategory("General");
                                }
                                medicineRepository.save(m);
                        }
                });

                // ---- Ensure every doctor has at least one consultation ----
                java.util.List<User> patients = userRepository.findAll().stream()
                                .filter(u -> u.getRole() == Role.PATIENT)
                                .collect(java.util.stream.Collectors.toList());
                java.util.List<Doctor> doctors = doctorRepository.findAll();
                if (!patients.isEmpty() && !doctors.isEmpty()) {
                        User patient1 = patients.get(0);
                        User patient2 = patients.size() > 1 ? patients.get(1) : patients.get(0);
                        Doctor doc1 = doctors.get(0);
                        Doctor doc2 = doctors.size() > 1 ? doctors.get(1) : doctors.get(0);
                        Doctor doc3 = doctors.size() > 2 ? doctors.get(2) : doctors.get(0);
                        Doctor doc4 = doctors.size() > 3 ? doctors.get(3) : doctors.get(0);

                        // Create any missing consultations for doctors without any
                        java.util.List<Consultation> existingConsults = consultationRepository.findAll();
                        boolean added = false;

                        if (existingConsults.stream().noneMatch(c -> c.getDoctor().getId().equals(doc1.getId()))) {
                                Consultation c = new Consultation();
                                c.setPatient(patient1);
                                c.setDoctor(doc1);
                                c.setStatus("CLOSED");
                                consultationRepository.save(c);
                                createPrescription(c, doc1,
                                                "Amoxicillin 500mg — 1 capsule 3 times daily for 7 days\nIbuprofen 400mg — as needed for pain");
                                createMedicalHistory(patient1, doc1, c, "Acute sinusitis with moderate fever.",
                                                "Rest and hydration.", "Full antibiotic course prescribed.");
                                added = true;
                        }
                        if (existingConsults.stream().noneMatch(c -> c.getDoctor().getId().equals(doc2.getId()))) {
                                Consultation c = new Consultation();
                                c.setPatient(patient1);
                                c.setDoctor(doc2);
                                c.setStatus("CLOSED");
                                consultationRepository.save(c);
                                createPrescription(c, doc2,
                                                "Metformin 500mg — 1 tablet twice daily\nLisinopril 10mg — 1 tablet daily");
                                createMedicalHistory(patient1, doc2, c,
                                                "Type 2 diabetes mellitus, hypertension. HbA1c: 7.2%, BP: 135/85.",
                                                "Follow up in 3 months.", "Diet and exercise counseling provided.");
                                added = true;
                        }
                        if (existingConsults.stream().noneMatch(c -> c.getDoctor().getId().equals(doc3.getId()))) {
                                Consultation c = new Consultation();
                                c.setPatient(patient2);
                                c.setDoctor(doc3);
                                c.setStatus("CLOSED");
                                consultationRepository.save(c);
                                createPrescription(c, doc3,
                                                "Cetirizine 10mg — 1 tablet daily\nMontelukast 10mg — 1 tablet at bedtime");
                                createMedicalHistory(patient2, doc3, c,
                                                "Seasonal allergic rhinitis with mild conjunctivitis.",
                                                "Consider allergy testing.", "Nasal saline rinse recommended.");
                                added = true;
                        }
                        if (existingConsults.stream().noneMatch(c -> c.getDoctor().getId().equals(doc4.getId()))) {
                                Consultation c = new Consultation();
                                c.setPatient(patient1);
                                c.setDoctor(doc4);
                                c.setStatus("CLOSED");
                                consultationRepository.save(c);
                                createPrescription(c, doc4,
                                                "Clotrimazole 1% Cream — apply twice daily\nHydrocortisone 10mg — apply as needed");
                                createMedicalHistory(patient1, doc4, c,
                                                "Mild contact dermatitis on forearms due to new laundry detergent.",
                                                "Discontinue irritant. Follow up in 2 weeks.",
                                                "Patch testing recommended if reactions recur.");
                                added = true;
                        }
                        if (added)
                                System.out.println("✅ Seeded missing consultations for doctors.");
                }

                if (supplierRepository.count() == 0) {
                        createSupplier("Global Pharma Supplies", "John Davis", "contact@globalpharma.com", "+1-555-0199", "123 Health Ave, New York, NY");
                        createSupplier("MediCore Distribution", "Sarah Connor", "info@medicore.com", "+1-555-0144", "456 Wellness Blvd, Chicago, IL");
                        createSupplier("Apex BioLabs", "David Chen", "sales@apexbio.com", "+1-555-0188", "789 Research Rd, San Francisco, CA");
                        System.out.println("✅ Seeded initial suppliers.");
                }

                System.out.println("✅ Database checked and images synchronized.");
        }

        private User createUser(String name, String email, String password, Role role) {
                User u = new User();
                u.setName(name);
                u.setEmail(email);
                u.setPassword(password);
                u.setRole(role);
                return userRepository.save(u);
        }

        private Doctor createDoctor(User user, String spec, int exp, String bio) {
                Doctor d = new Doctor();
                d.setUser(user);
                d.setSpecialization(spec);
                d.setExperience(exp);
                d.setBio(bio);
                return doctorRepository.save(d);
        }

        private void createSlot(Doctor doctor, java.time.LocalDate date, java.time.LocalTime start,
                        java.time.LocalTime end) {
                com.example.demo.model.DoctorSlot slot = new com.example.demo.model.DoctorSlot();
                slot.setDoctor(doctor);
                slot.setDate(date);
                slot.setStartTime(start);
                slot.setEndTime(end);
                slot.setBooked(false);
                doctorSlotRepository.save(slot);
        }

        private void createMedicine(String name, double price, int stock, String desc, String imageUrl) {
                Medicine m = new Medicine();
                m.setName(name);
                m.setPrice(price);
                m.setStock(stock);
                m.setCategory(currentCategory);
                m.setDescription(desc);
                m.setImageUrl(imageUrl);
                medicineRepository.save(m);
        }

        private void createSupplier(String name, String contact, String email, String phone, String address) {
                Supplier s = new Supplier();
                s.setName(name);
                s.setContactPerson(contact);
                s.setEmail(email);
                s.setPhone(phone);
                s.setAddress(address);
                supplierRepository.save(s);
        }

        private void createPrescription(Consultation consultation, Doctor doctor, String content) {
                Prescription p = new Prescription();
                p.setConsultation(consultation);
                p.setDoctor(doctor);
                p.setContent(content);
                prescriptionRepository.save(p);
        }

        private void createMedicalHistory(User patient, Doctor doctor, Consultation consultation,
                        String diagnosis, String notes, String prescriptionText) {
                MedicalHistory h = new MedicalHistory();
                h.setPatient(patient);
                h.setDoctor(doctor);
                h.setConsultation(consultation);
                h.setDiagnosis(diagnosis);
                h.setNotes(notes);
                h.setPrescription(prescriptionText);
                medicalHistoryRepository.save(h);
        }
}
