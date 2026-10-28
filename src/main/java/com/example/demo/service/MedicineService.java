package com.example.demo.service;

import com.example.demo.model.Medicine;
import java.util.List;
import java.util.Optional;

public interface MedicineService {
    List<Medicine> getAllMedicines();
    List<Medicine> searchMedicines(String keyword);
    Optional<Medicine> getMedicineById(Long id);
    Medicine saveMedicine(Medicine medicine);
    boolean reduceStock(Long medicineId, int quantity);
    Medicine updateStock(Long medicineId, int newStock);
    void deleteMedicine(Long id);
}
