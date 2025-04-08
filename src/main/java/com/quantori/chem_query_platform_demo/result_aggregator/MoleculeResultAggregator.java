package com.quantori.chem_query_platform_demo.result_aggregator;

import com.quantori.chem_query_platform_demo.serde.MoleculeAnalysisTaskSerDe;
import com.quantori.qdp.core.task.model.ResultAggregator;
import com.quantori.qdp.core.task.model.StreamTaskResult;
import com.quantori.qdp.core.task.model.DataProvider;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code DemoResultAggregator} is an implementation of {@link ResultAggregator}
 * used within an Akka Stream pipeline managed by {@link com.quantori.qdp.core.task.service.StreamTaskService}.
 * <p>
 * This class is responsible for aggregating results by counting the number of
 * molecules processed in the stream, typically from an uploaded SDF file.
 * Each invocation of {@link #consume(DataProvider.Data)} increments an internal atomic counter.
 * <p>
 * The result is exposed via {@link #getResult()}, which returns a {@link ValidationResult}
 * encapsulating the total count.
 * <p>
 * This aggregator is designed to be thread-safe and can be used safely
 * in concurrent streaming environments.
 *
 * @see MoleculeAnalysisTaskSerDe
 *
 * <h3>Thread Safety:</h3>
 * This class is thread-safe due to the use of {@link AtomicInteger} for tracking counts.
 */
@NoArgsConstructor
public class MoleculeResultAggregator implements ResultAggregator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoleculeResultAggregator.class);

    private final AtomicInteger amountOfMolecules = new AtomicInteger(0);

    /**
     * Consumes the provided data from the Akka stream, and counts the number of molecules
     * present in the uploaded SDF file using the file path provided during construction.
     * <p>
     * The result is stored internally using an atomic counter and logged.
     *
     * @param data Data object provided by the stream (not used directly).
     * @throws RuntimeException if the file cannot be read or processed.
     */
    @Override
    public void consume(DataProvider.Data data) {
        amountOfMolecules.addAndGet(1);
    }

    @Override
    public StreamTaskResult getResult() {
        return new ValidationResult(amountOfMolecules.get());
    }

    @Override
    public double getPercent() {
        return 0;
    }

    @Override
    public void taskCompleted(boolean successful) {
        ValidationResult result = (ValidationResult) getResult();

        LOGGER.info("Amount of molecules in file: {}", result.amount);
    }

    record ValidationResult(int amount)
            implements StreamTaskResult {
    }
}
