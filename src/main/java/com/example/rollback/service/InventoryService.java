package com.example.rollback.service;

import com.example.rollback.domain.Inventory;
import com.example.rollback.repository.InventoryRepository;
import com.example.rollback.util.ContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    
    private String getGuid() {
        return ContextHolder.getCurrentContext().getString("guid");
    }
    
    // 전체 재고 목록 조회
    public List<Inventory> getAllInventory() {
        log.info("[GUID: {}] 전체 재고 목록 조회 요청", getGuid());
        return inventoryRepository.findAll();
    }
    
    // 상품명으로 재고 조회
    public Optional<Inventory> findByProductName(String productName) {
        log.info("[GUID: {}] 재고 조회 요청 - 상품: {}", getGuid(), productName);
        return inventoryRepository.findByProductName(productName);
    }
    
    // 재고 충분 여부 확인
    public boolean hasEnoughStock(String productName, int quantity) {
        log.info("[GUID: {}] 재고 확인 요청 - 상품: {}, 수량: {}", getGuid(), productName, quantity);
        boolean hasStock = inventoryRepository.hasEnoughStock(productName, quantity);
        log.info("[GUID: {}] 재고 확인 결과 - 상품: {}, 수량: {}, 충분함: {}", getGuid(), productName, quantity, hasStock);
        return hasStock;
    }
    
    // 재고 예약 (트랜잭션)
    @Transactional
    public Inventory reserveStock(String productName, int quantity) {
        log.info("[GUID: {}] 재고 예약 시작 - 상품: {}, 수량: {}", getGuid(), productName, quantity);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductName(productName);
        if (inventoryOpt.isEmpty()) {
            throw new IllegalArgumentException("재고를 찾을 수 없습니다: " + productName);
        }
        
        Inventory inventory = inventoryOpt.get();
        int availableStock = inventory.getAvailableStock();
        
        if (availableStock < quantity) {
            String errorMsg = String.format("재고 부족: %s (요청: %d, 가용: %d)", 
                productName, quantity, availableStock);
            log.error("[GUID: {}] {}", getGuid(), errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        
        // 데이터베이스에서 예약 처리
        int affectedRows = inventoryRepository.reserveStock(inventory.getId(), quantity);
        if (affectedRows == 0) {
            throw new IllegalStateException("재고 예약에 실패했습니다: " + productName);
        }
        
        // 업데이트된 재고 정보 다시 조회
        Inventory updatedInventory = inventoryRepository.findById(inventory.getId())
            .orElseThrow(() -> new IllegalStateException("재고 정보를 다시 조회할 수 없습니다"));
        
        
        log.info("[GUID: {}] 재고 예약 완료 - 상품: {}, 수량: {}, 남은 가용 재고: {}", 
            getGuid(), productName, quantity, updatedInventory.getAvailableStock());
        
        return updatedInventory;
    }
    
    // 재고 예약 해제 (트랜잭션)
    @Transactional
    public Inventory releaseReservation(String productName, int quantity) {
        log.info("[GUID: {}] 재고 예약 해제 시작 - 상품: {}, 수량: {}", getGuid(), productName, quantity);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductName(productName);
        if (inventoryOpt.isEmpty()) {
            throw new IllegalArgumentException("재고를 찾을 수 없습니다: " + productName);
        }
        
        Inventory inventory = inventoryOpt.get();
        
        // 데이터베이스에서 예약 해제 처리
        int affectedRows = inventoryRepository.releaseReservation(inventory.getId(), quantity);
        if (affectedRows == 0) {
            throw new IllegalStateException("재고 예약 해제에 실패했습니다: " + productName);
        }
        
        // 업데이트된 재고 정보 다시 조회
        Inventory updatedInventory = inventoryRepository.findById(inventory.getId())
            .orElseThrow(() -> new IllegalStateException("재고 정보를 다시 조회할 수 없습니다"));
        
        log.info("[GUID: {}] 재고 예약 해제 완료 - 상품: {}, 수량: {}, 남은 예약 재고: {}", 
            getGuid(), productName, quantity, updatedInventory.getReservedStock());
        
        return updatedInventory;
    }
    
    // 실제 재고 차감 (주문 확정 시) (트랜잭션)
    @Transactional
    public Inventory deductStock(String productName, int quantity) {
        log.info("[GUID: {}] 실제 재고 차감 시작 - 상품: {}, 수량: {}", getGuid(), productName, quantity);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductName(productName);
        if (inventoryOpt.isEmpty()) {
            throw new IllegalArgumentException("재고를 찾을 수 없습니다: " + productName);
        }
        
        Inventory inventory = inventoryOpt.get();
        
        // 데이터베이스에서 재고 차감 처리
        int affectedRows = inventoryRepository.deductStock(inventory.getId(), quantity);
        if (affectedRows == 0) {
            throw new IllegalStateException("재고 차감에 실패했습니다: " + productName);
        }
        
        // 업데이트된 재고 정보 다시 조회
        Inventory updatedInventory = inventoryRepository.findById(inventory.getId())
            .orElseThrow(() -> new IllegalStateException("재고 정보를 다시 조회할 수 없습니다"));
        
        log.info("[GUID: {}] 실제 재고 차감 완료 - 상품: {}, 수량: {}, 현재 재고: {}", 
            getGuid(), productName, quantity, updatedInventory.getCurrentStock());
        
        return updatedInventory;
    }
    

    // 재고 부족 목록 조회
    public List<Inventory> getLowStockItems() {
        log.info("[GUID: {}] 재고 부족 목록 조회 요청", getGuid());
        List<Inventory> lowStockItems = inventoryRepository.findLowStockItems();
        log.info("[GUID: {}] 재고 부 shortage 목록 조회 완료 - {}개 항목", getGuid(), lowStockItems.size());
        return lowStockItems;
    }
    
    // 새로운 재고 항목 생성 (트랜잭션)
    @Transactional
    public Inventory createInventory(String productName, int currentStock, int minStockLevel) {
        log.info("[GUID: {}] 신규 재고 생성 시작 - 상품: {}, 초기 재고: {}, 최소 재고: {}", 
            getGuid(), productName, currentStock, minStockLevel);
        
        // 이미 존재하는 상품인지 확인
        Optional<Inventory> existing = inventoryRepository.findByProductName(productName);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 상품입니다: " + productName);
        }
        
        Inventory inventory = Inventory.create(productName, currentStock, minStockLevel);
        int affectedRows = inventoryRepository.save(inventory);
        
        if (affectedRows == 0) {
            throw new IllegalStateException("재고 생성에 실패했습니다: " + productName);
        }
        
        // 생성된 재고 정보 다시 조회
        Inventory createdInventory = inventoryRepository.findById(inventory.getId())
            .orElseThrow(() -> new IllegalStateException("생성된 재고 정보를 조회할 수 없습니다"));
        
        log.info("[GUID: {}] 신규 재고 생성 완료 - 상품: {}, ID: {}", getGuid(), productName, createdInventory.getId());
        
        return createdInventory;
    }
}