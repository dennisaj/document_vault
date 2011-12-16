package us.paperlesstech

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.amazonaws.services.sqs.model.DeleteMessageRequest
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import grails.converters.JSON
import java.util.concurrent.Executors
import org.springframework.beans.factory.InitializingBean
import us.paperlesstech.nimble.User

class ActivityLogService implements InitializingBean {
	static transactional = false
	private static final executorService = Executors.newCachedThreadPool()

	def authService
	private AWSCredentials credentials
	def grailsApplication
	boolean loggingEnabled
	def requestService
	private String queue
	private int logBatchSize

	/**
	 * Initializes the object after Spring has injected the beans.  This should only be called by Spring.
	 */
	void afterPropertiesSet() {
		loggingEnabled = grailsApplication.config.document_vault.activity_log.enabled
		queue = grailsApplication.config.document_vault.aws.sqs.logQueue
		assert queue
		logBatchSize = grailsApplication.config.document_vault.aws.sqs.logBatchSize
		assert logBatchSize
		String accessKey = grailsApplication.config.document_vault.aws.credentials.accessKey
		assert accessKey
		String secretKey = grailsApplication.config.document_vault.aws.credentials.secretKey
		assert secretKey
		credentials = new BasicAWSCredentials(accessKey, secretKey)
	}

	/**
	 * Queues a log entry from the given information. params will be altered so this should not be the actual params.
	 *
	 * @param controller The name of the requested controller
	 * @param action the name of the action (defaults to "index")
	 * @param status the http response code
	 * @param params the map of parameters the user sent.  documentId, pageNumber, and lines will be removed
	 */
	void addLog(String controller, String action, int status, Map params = [:]) {
		action = action ?: "index"
		def documentId = params.remove("documentId")
		def document = documentId ? Document.load(documentId as Long) : null
		def pageNumber = params.remove("pageNumber")
		// Don't log signature line or document notes data
		params.remove("lines")
		params.remove("notes")
		params.remove("password")

		def activityLog = newActivityLog()
		activityLog.action = "$controller:$action"
		activityLog.delegate = authService.delegateUser
		activityLog.document = document
		activityLog.userAgent = requestService.getHeader("User-Agent")
		activityLog.ip = requestService.getRemoteAddr()
		activityLog.user = authService.authenticatedUser
		activityLog.pageNumber = pageNumber
		activityLog.params = params.toString()
		activityLog.status = status
		activityLog.uri = requestService.getRequestURI()

		def map = activityLog.asMap()

		sendMessage(map)
	}

	ActivityLog createFromJson(String input) {
		def json = JSON.parse(input)

		def al = newActivityLog()
		def tenant = json.getInt('tenant')
		DomainTenantMap domainTenantMap = DomainTenantMap.findByMappedTenantId(tenant)
		assert domainTenantMap

		domainTenantMap.withThisTenant {
			al.action = json.action
			if (!json.isNull('delegate')) {
				al.delegate = User.load(json.getLong('delegate'))
			}
			if (!json.isNull('document')) {
				al.document = Document.load(json.getLong('document'))
			}
			al.ip = json.ip
			if (!json.isNull('pageNumber')) {
				al.pageNumber = json.getString('pageNumber')
			}
			if (!json.isNull('params')) {
				al.params = json.getString('params')
			}
			al.status = json.status
			al.uri = json.uri
			if (!json.isNull('user')) {
				al.user = User.load(json.getLong('user'))
			}
			al.userAgent = json.userAgent

			al.save(flush: true)
		}

		al
	}

	ActivityLog newActivityLog() {
		new ActivityLog()
	}

	/**
	 * Sends a message to the Amazon Simple Queue Service (SQS)
	 *
	 * @param activityLogMap the activity log entry to be sent
	 */
	void sendMessage(Map activityLogMap) {
		if(!loggingEnabled) {
			return
		}

		String body = (activityLogMap as JSON).toString()
		def message = new SendMessageRequest(queue, body)
		def client = new AmazonSQSAsyncClient(credentials, executorService)
		client.sendMessage(message)
	}

	/**
	 * Retrieves any pending requests from the queue and writes them to the database
	 */
	void writeQueuedLogs() {
		if(!loggingEnabled) {
			return
		}

		def client = new AmazonSQSAsyncClient(credentials, executorService)
		def request = new ReceiveMessageRequest(queue)
		request.setMaxNumberOfMessages(logBatchSize)

		// Loop until there are no more messages to pull
		while (true) {
			def messages = client.receiveMessage(request).messages
			if (!messages) {
				log.debug 'All messages written'
				return
			}

			ActivityLog.withTransaction {
				for (Message m in messages) {
					createFromJson(m.body)

					// TODO change this to a synchronous batch delete once the Java SDK has been updated
					def deleteRequest = new DeleteMessageRequest(queue, m.receiptHandle)
					client.deleteMessageAsync(deleteRequest)
				}
			}

			log.info "Wrote ${messages.size()} log messages"
		}
	}
}
