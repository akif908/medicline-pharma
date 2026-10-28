package com.example.demo.service.impl;

import com.example.demo.model.Medicine;
import com.example.demo.repository.MedicineRepository;
import com.example.demo.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MedicineServiceImpl — thread-safe medicine operations.
 *
 * Thread safety strategy:
 *  - A ConcurrentHashMap<medicineId, ReentrantLock> provides per-medicine
 *    locks so that concurrent orders for *different* medicines don't block
 *    each other, while concurrent orders for the *same* medicine are
 *    serialised to prevent overselling (race conditions on stock).
 *
 *  - The global `stockUpdateLock` (synchronized method) guards the
 *    admin stock-override operation, which is a rarer but heavier write.
 */
@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    // Per-medicine ReentrantLocks — prevents overselling under concurrent load
    private final ConcurrentHashMap<Long, ReentrantLock> medicineLocks = new ConcurrentHashMap<>();

    // Helper: get-or-create the lock for a given medicine ID
    private ReentrantLock getLockFor(Long medicineId) {
        return medicineLocks.computeIfAbsent(medicineId, id -> new ReentrantLock(true)); // fair lock
    }

    @Override
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    @Override
    public List<Medicine> searchMedicines(String keyword) {
        return medicineRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    @Override
    public Medicine saveMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    /**
     * Thread-safe stock reduction using per-medicine ReentrantLock.
     * Multiple threads placing orders concurrently for the same medicine
     * will queue here — only one reduces stock at a time, preventing
     * negative stock / overselling.
     */
    @Override
    @Transactional
    public boolean reduceStock(Long medicineId, int quantity) {
        ReentrantLock lock = getLockFor(medicineId);
        lock.lock(); // acquire the lock for this specific medicine
        try {
            Optional<Medicine> opt = medicineRepository.findById(medicineId);
            if (opt.isPresent()) {
                Medicine m = opt.get();
                if (m.getStock() >= quantity) {
                    m.setStock(m.getStock() - quantity);
                    medicineRepository.save(m);
                    System.out.printf("[%s] Stock reduced for medicine %d: %d → %d%n",
                            Thread.currentThread().getName(), medicineId,
                            m.getStock() + quantity, m.getStock());
                    return true;
                }
                System.out.printf("[%s] Insufficient stock for medicine %d (has %d, need %d)%n",
                        Thread.currentThread().getName(), medicineId, m.getStock(), quantity);
            }
            return false;
        } finally {
            lock.unlock(); // always release, even on exception
        }
    }

    /**
     * Admin stock override — synchronized on the service instance level
     * since this is an infrequent admin-only operation.
     */
    @Override
    @Transactional
    public synchronized Medicine updateStock(Long medicineId, int newStock) {
        Optional<Medicine> opt = medicineRepository.findById(medicineId);
        if (opt.isPresent()) {
            Medicine m = opt.get();
            int old = m.getStock();
            m.setStock(newStock);
            Medicine saved = medicineRepository.save(m);
            System.out.printf("[%s] Admin stock override for medicine %d: %d → %d%n",
                    Thread.currentThread().getName(), medicineId, old, newStock);
            return saved;
        }
        return null;
    }

    @Override
    public void deleteMedicine(Long id) {
        // Clean up the lock entry when medicine is deleted
        medicineLocks.remove(id);
        medicineRepository.deleteById(id);
    }
}
