package org.sagebionetworks.template.repo.kinesis;

import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.template.repo.kinesis.firehose.GlueTableDescriptor;
import org.sagebionetworks.template.repo.kinesis.firehose.KinesisFirehoseConfig;
import org.sagebionetworks.template.repo.kinesis.firehose.KinesisFirehoseConfigValidator;
import org.sagebionetworks.template.repo.kinesis.firehose.KinesisFirehoseStreamDescriptor;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class KinesisFirehoseConfigValidatorTest {
	
	@Mock
	KinesisFirehoseConfig mockConfig;
	
	@InjectMocks
	KinesisFirehoseConfigValidator validator;
	
	@Test
	public void testWithNoTables() {
		
		KinesisFirehoseStreamDescriptor stream = new KinesisFirehoseStreamDescriptor();
		stream.setConvertToParquet(false);
		
		when(mockConfig.getStreamDescriptors()).thenReturn(Collections.singleton(stream));
		
		validator.validate();
	}
	
	@Test
	public void testWithTablesRef() {
		
		GlueTableDescriptor table = new GlueTableDescriptor();
		
		table.setName("someTableRef");
		table.setColumns(ImmutableMap.of("someColumn", "string"));
		
		KinesisFirehoseStreamDescriptor stream = new KinesisFirehoseStreamDescriptor();
		stream.setConvertToParquet(true);
		stream.setTableName("someTableRef");
		
		when(mockConfig.getGlueTableDescriptors()).thenReturn(Collections.singleton(table));
		when(mockConfig.getStreamDescriptors()).thenReturn(Collections.singleton(stream));
		
		validator.validate();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testWithEmptyColumnDef() {
		
		GlueTableDescriptor table = new GlueTableDescriptor();
		
		table.setName("someTableRef");
		
		when(mockConfig.getGlueTableDescriptors()).thenReturn(Collections.singleton(table));
		
		validator.validate();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testWithMissingTableName() {
		
		KinesisFirehoseStreamDescriptor stream = new KinesisFirehoseStreamDescriptor();
		stream.setConvertToParquet(true);
		stream.setTableName(null);
		
		when(mockConfig.getStreamDescriptors()).thenReturn(Collections.singleton(stream));
		
		validator.validate();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testWithEmptyTableName() {
		
		KinesisFirehoseStreamDescriptor stream = new KinesisFirehoseStreamDescriptor();
		stream.setConvertToParquet(true);
		stream.setTableName("");
		
		when(mockConfig.getStreamDescriptors()).thenReturn(Collections.singleton(stream));
		
		validator.validate();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testWithNonExistingTableRef() {
		
		KinesisFirehoseStreamDescriptor stream = new KinesisFirehoseStreamDescriptor();
		stream.setConvertToParquet(true);
		stream.setTableName("someTable");
		
		when(mockConfig.getStreamDescriptors()).thenReturn(Collections.singleton(stream));
		
		validator.validate();
	}
		
	

}
