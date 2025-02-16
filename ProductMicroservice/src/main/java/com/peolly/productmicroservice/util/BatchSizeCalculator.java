package com.peolly.productmicroservice.util;

import org.springframework.stereotype.Component;

@Component
public class BatchSizeCalculator {
    /**
     * Determines the optimal batch size for database operations based on memory, database connections,
     * and concurrency constraints.
     *
     * @return the computed batch size.
     */
    public int getBatchSize() {
        long availableMemory = 500 * 1024 * 1024; // 500 MB
        long avgObjectSize = 1024 * 10;          // Средний размер объекта 10 KB
        int maxDbConnections = 50;              // Максимум 50 подключений к БД
        int expectedConcurrentThreads = 10;      // 10 потоков одновременно
        int maxAllowedBatchSize = 1000;         // Ограничение батча в 100 записей

        int batchSize = calculateBatchSize(availableMemory, avgObjectSize, maxDbConnections, expectedConcurrentThreads, maxAllowedBatchSize);
        return batchSize;
    }

    /**
     * Calculates the batch size based on available memory, database connection limits,
     * concurrent threads, and a maximum batch size constraint.
     *
     * @param availableMemory         the available memory in bytes.
     * @param avgObjectSize           the estimated size of each object in bytes.
     * @param maxDbConnections        the maximum allowed database connections.
     * @param expectedConcurrentThreads the expected number of concurrent threads.
     * @param maxAllowedBatchSize     the upper limit for batch size.
     * @return the optimal batch size.
     */
    private int calculateBatchSize(long availableMemory, long avgObjectSize,
                                         int maxDbConnections, int expectedConcurrentThreads, 
                                         int maxAllowedBatchSize) {
        int memoryBasedBatchSize = (int) (availableMemory / avgObjectSize);
        int dbConnectionBasedBatchSize = maxDbConnections / expectedConcurrentThreads;
        return Math.min(Math.min(memoryBasedBatchSize, dbConnectionBasedBatchSize), maxAllowedBatchSize);
    }
}
