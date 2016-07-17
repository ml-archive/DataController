package com.fuzz.datacontroller.test;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionManager;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robolectric.RuntimeEnvironment;

/**
 * Description: The main rule for tests.
 */
public class DBFlowTestRule implements TestRule {

    public static DBFlowTestRule create() {
        return new DBFlowTestRule();
    }

    private DBFlowTestRule() {
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                FlowManager.init(new FlowConfig.Builder(RuntimeEnvironment.application)
                        .addDatabaseConfig(new DatabaseConfig.Builder(TestDatabase.class)
                                .transactionManagerCreator(new DatabaseConfig.TransactionManagerCreator() {
                                    @Override
                                    public BaseTransactionManager createManager(
                                            DatabaseDefinition databaseDefinition) {
                                        return new DefaultTransactionManager(
                                                new ImmediateTransactionQueue(),
                                                databaseDefinition);
                                    }
                                }).build())
                        .build());
                try {
                    base.evaluate();
                } finally {
                    FlowManager.reset();
                    FlowManager.destroy();
                }
            }
        };
    }

    class ImmediateTransactionQueue implements ITransactionQueue {

        @Override
        public void add(Transaction transaction) {
            transaction.executeSync();
        }

        @Override
        public void cancel(Transaction transaction) {
        }

        @Override
        public void startIfNotAlive() {
        }

        @Override
        public void cancel(String name) {
        }

        @Override
        public void quit() {
        }
    }
}