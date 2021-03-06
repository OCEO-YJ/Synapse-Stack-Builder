package org.sagebionetworks.template.repo.kinesis;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.template.Constants.GLUE_DATABASE_NAME;
import static org.sagebionetworks.template.Constants.KINESIS_FIREHOSE_STREAM_DESCRIPTORS;
import static org.sagebionetworks.template.Constants.PROPERTY_KEY_INSTANCE;
import static org.sagebionetworks.template.Constants.PROPERTY_KEY_STACK;
import static org.sagebionetworks.template.repo.kinesis.firehose.KinesisFirehoseVelocityContextProvider.GLUE_DB_SUFFIX;

import java.util.Collections;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.template.config.RepoConfiguration;
import org.sagebionetworks.template.repo.kinesis.firehose.GlueTableDescriptor;
import org.sagebionetworks.template.repo.kinesis.firehose.KinesisFirehoseConfig;
import org.sagebionetworks.template.repo.kinesis.firehose.KinesisFirehoseStreamDescriptor;
import org.sagebionetworks.template.repo.kinesis.firehose.KinesisFirehoseVelocityContextProvider;

import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class KinesisFirehoseVelocityContextProviderTest {

	@Mock
	VelocityContext mockContext;

	@Mock
	RepoConfiguration mockRepoConfig;

	@Mock
	KinesisFirehoseConfig mockConfig;

	@Mock
	KinesisFirehoseStreamDescriptor mockStream;
	
	@Mock
	KinesisFirehoseStreamDescriptor mockStream2;

	@Mock
	GlueTableDescriptor mockTable;

	@InjectMocks
	KinesisFirehoseVelocityContextProvider contextProvider;

	private String testStack = "TestStack";
	private String testInstance = "TestInstance";
	private Set<KinesisFirehoseStreamDescriptor> testStreams;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		testStreams = Collections.singleton(mockStream);
		when(mockStream.isDevOnly()).thenReturn(false);
		when(mockRepoConfig.getProperty(PROPERTY_KEY_STACK)).thenReturn(testStack);
		when(mockRepoConfig.getProperty(PROPERTY_KEY_INSTANCE)).thenReturn(testInstance);
		when(mockStream.getTableDescriptor()).thenReturn(mockTable);
		when(mockConfig.getStreamDescriptors()).thenReturn(testStreams);
	}

	@Test
	public void testAddToContext() {
		
		contextProvider.addToContext(mockContext);
		
		verify(mockContext).put(GLUE_DATABASE_NAME, (testStack + testInstance + GLUE_DB_SUFFIX));
		verify(mockContext).put(KINESIS_FIREHOSE_STREAM_DESCRIPTORS, testStreams);
	}
	
	@Test
	public void testAddToContextWithoutDevOnlyStreamsInProd() {
		String prodStack = "Prod";
		
		when(mockRepoConfig.getProperty(PROPERTY_KEY_STACK)).thenReturn(prodStack);
		when(mockStream.isDevOnly()).thenReturn(true);
		
		contextProvider.addToContext(mockContext);
		
		verify(mockContext).put(KINESIS_FIREHOSE_STREAM_DESCRIPTORS, Collections.emptySet());
	}
	
	@Test
	public void testAddToContextWithDevOnlyStreams() {
		
		// A stack different than prod
		String devStack = "someOtherStack";
		
		when(mockRepoConfig.getProperty(PROPERTY_KEY_STACK)).thenReturn(devStack);
		when(mockStream.isDevOnly()).thenReturn(true);
		
		contextProvider.addToContext(mockContext);
		
		verify(mockContext).put(KINESIS_FIREHOSE_STREAM_DESCRIPTORS, Collections.singleton(mockStream));
	}
	
	@Test
	public void testAddToContextWithDevOnlyStreamsMixed() {
		
		// A stack different than prod
		String prodStack = "someOtherStack";
		
		// Second stream is dev only
		when(mockStream2.isDevOnly()).thenReturn(true);
		
		Set<KinesisFirehoseStreamDescriptor> streams = ImmutableSet.of(mockStream, mockStream2);
		
		when(mockConfig.getStreamDescriptors()).thenReturn(streams);
		when(mockRepoConfig.getProperty(PROPERTY_KEY_STACK)).thenReturn(prodStack);
		
		contextProvider.addToContext(mockContext);
		
		verify(mockContext).put(KINESIS_FIREHOSE_STREAM_DESCRIPTORS, streams);
	}
	
	@Test
	public void testAddToContextWithoutDevOnlyStreamsMixed() {
		
		String prodStack = "prod";
		
		// Second stream is dev only
		when(mockStream2.isDevOnly()).thenReturn(true);
		
		Set<KinesisFirehoseStreamDescriptor> streams = ImmutableSet.of(mockStream, mockStream2);
		
		when(mockConfig.getStreamDescriptors()).thenReturn(streams);
		when(mockRepoConfig.getProperty(PROPERTY_KEY_STACK)).thenReturn(prodStack);
		
		contextProvider.addToContext(mockContext);
		
		verify(mockContext).put(KINESIS_FIREHOSE_STREAM_DESCRIPTORS, Collections.singleton(mockStream));
	}
	
	@Test
	public void testParameterizedTableName() {
		
		String originalTableName = "TestTable";
		
		when(mockTable.getName()).thenReturn(originalTableName);
		
		contextProvider.addToContext(mockContext);
		
		verify(mockTable).setName((testStack + testInstance + originalTableName));
	}
}
