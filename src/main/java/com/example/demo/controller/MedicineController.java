package com.example.demo.controller;

import com.example.demo.model.Medicine;
import com.example.demo.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    public List<Medicine> getAllMedicines() {
        return medicineService.getAllMedicines();
    }

    @GetMapping("/search")
    public List<Medicine> searchMedicines(@RequestParam String keyword) {
        return medicineService.searchMedicines(keyword);
    }

    @PostMapping
    public Medicine addMedicine(@RequestBody Medicine medicine) {
        return medicineService.saveMedicine(medicine);
    }

    @PutMapping("/{id}/stock")
    public Medicine updateStock(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> payload) {
        return medicineService.updateStock(id, payload.get("stock"));
    }

    @DeleteMapping("/{id}")
    public void deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
    }
}
