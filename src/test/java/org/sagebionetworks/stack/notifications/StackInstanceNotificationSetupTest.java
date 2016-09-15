package org.sagebionetworks.stack.notifications;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import org.sagebionetworks.stack.config.InputConfiguration;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.Subscription;
import org.sagebionetworks.factory.MockAmazonClientFactory;
import org.sagebionetworks.stack.Constants;
import org.sagebionetworks.stack.GeneratedResources;
import org.sagebionetworks.stack.TestHelper;

public class StackInstanceNotificationSetupTest {
	
	AmazonSNSClient mockClient;
	MockAmazonClientFactory factory = new MockAmazonClientFactory();
	InputConfiguration config;
	GeneratedResources resources;
	StackInstanceNotificationSetup setup;
	
	@Before
	public void before() throws IOException{
		config = TestHelper.createTestConfig("dev");
		mockClient = factory.createSNSClient();
		resources = new GeneratedResources();
		setup = new StackInstanceNotificationSetup(factory, config, resources);
	}
	
	@Test
	public void testSetupNotificationTopics() {
		String topicArn = "arn:123";
		String protocol = Constants.TOPIC_SUBSCRIBE_PROTOCOL_EMAIL;
		String endpoint = config.getRDSAlertSubscriptionEndpoint();
		
		CreateTopicRequest expectedTopic = new CreateTopicRequest();
		expectedTopic.setName(config.getRDSAlertTopicName());
		CreateTopicResult expectedResult = new CreateTopicResult().withTopicArn(topicArn);
		when(mockClient.createTopic(expectedTopic)).thenReturn(expectedResult);
		
		ListSubscriptionsByTopicRequest tRequest = new ListSubscriptionsByTopicRequest().withTopicArn(topicArn);
		Subscription expected = new Subscription().withEndpoint(endpoint).withProtocol(protocol);
		ListSubscriptionsByTopicResult result = new ListSubscriptionsByTopicResult().withSubscriptions(expected);
		when(mockClient.listSubscriptionsByTopic(tRequest)).thenReturn(result);
		
		// Make the call
		setup.setupResources();

		verify(mockClient, times(1)).createTopic(expectedTopic);
		
		// Make sure it was set the resources
		assertEquals("The expected topic was not set in the resoruces",expectedResult.getTopicArn(), resources.getStackInstanceNotificationTopicArn());
	}
}