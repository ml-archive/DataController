package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSource.SourceType;
import com.fuzz.datacontroller.source.DataSourceStorage.DataSourceParams;
import com.fuzz.datacontroller.source.ListBasedDataSourceContainer;
import com.fuzz.datacontroller.source.MemoryDataSource;
import com.fuzz.datacontroller.source.TreeMapSingleTypeDataSourceContainer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class DefaultDataSourceContainerTest {

    @Test
    public void test_treeMapDataSourceContainer() {
        TreeMapSingleTypeDataSourceContainer<String> treeMapDataSourceContainer
                = new TreeMapSingleTypeDataSourceContainer<>();

        MemoryDataSource<String> memoryDataSource = new MemoryDataSource<>();
        treeMapDataSourceContainer.registerDataSource(memoryDataSource);
        treeMapDataSourceContainer.registerDataSource(new MockDataSource<String>() {
            @Override
            public SourceType getSourceType() {
                return SourceType.DISK;
            }
        });
        treeMapDataSourceContainer.registerDataSource(new MockDataSource<String>() {
            @Override
            public SourceType getSourceType() {
                return SourceType.NETWORK;
            }
        });

        assertNotNull(treeMapDataSourceContainer.getDataSource(
                new DataSourceParams(SourceType.MEMORY)));

        assertEquals(3, treeMapDataSourceContainer.sources().size());

        treeMapDataSourceContainer.deregisterDataSource(memoryDataSource);
        assertEquals(2, treeMapDataSourceContainer.sources().size());
    }

    @Test
    public void test_ListBasedDataSourceContainer() {
        ListBasedDataSourceContainer<String> listBasedDataSourceContainer
                = new ListBasedDataSourceContainer<>();

        MockDataSource<String> mockDataSource = new MockDataSource<String>() {
            @Override
            public SourceType getSourceType() {
                return SourceType.DISK;
            }
        };
        listBasedDataSourceContainer.registerDataSource(mockDataSource);
        listBasedDataSourceContainer.registerDataSource(new MockDataSource<String>() {
            @Override
            public SourceType getSourceType() {
                return SourceType.DISK;
            }
        });

        assertEquals(2, listBasedDataSourceContainer.sources().size());

        boolean failed = false;
        try {
            listBasedDataSourceContainer.getDataSource(new DataSourceParams(SourceType.MEMORY));
        } catch (ArrayIndexOutOfBoundsException e) {
            failed = true;
        }
        assertTrue(failed);

        DataSource<String> dataSource = listBasedDataSourceContainer.getDataSource(new DataSourceParams(0));
        assertEquals(dataSource, mockDataSource);

        assertNotEquals(listBasedDataSourceContainer.getDataSource(new DataSourceParams(1)), mockDataSource);

        listBasedDataSourceContainer.deregisterDataSource(mockDataSource);
        assertEquals(1, listBasedDataSourceContainer.sources().size());
    }
}
