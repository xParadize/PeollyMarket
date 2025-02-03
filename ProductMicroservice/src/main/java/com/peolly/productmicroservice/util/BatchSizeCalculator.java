package com.peolly.productmicroservice.util;

import org.springframework.stereotype.Component;

@Component
public class BatchSizeCalculator {
    public int getBatchSize() {
        long availableMemory = 500 * 1024 * 1024; // 500 MB
        long avgObjectSize = 1024 * 10;          // Средний размер объекта 10 KB
        int maxDbConnections = 50;              // Максимум 50 подключений к БД
        int expectedConcurrentThreads = 10;      // 10 потоков одновременно
        int maxAllowedBatchSize = 1000;         // Ограничение батча в 100 записей

        int batchSize = calculateBatchSize(availableMemory, avgObjectSize, maxDbConnections, expectedConcurrentThreads, maxAllowedBatchSize);
        return batchSize;
    }

    private int calculateBatchSize(long availableMemory, long avgObjectSize,
                                         int maxDbConnections, int expectedConcurrentThreads, 
                                         int maxAllowedBatchSize) {
        int memoryBasedBatchSize = (int) (availableMemory / avgObjectSize);
        int dbConnectionBasedBatchSize = maxDbConnections / expectedConcurrentThreads;
        return Math.min(Math.min(memoryBasedBatchSize, dbConnectionBasedBatchSize), maxAllowedBatchSize);
    }
}
