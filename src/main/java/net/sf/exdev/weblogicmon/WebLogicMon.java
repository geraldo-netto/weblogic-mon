package net.sf.exdev.weblogicmon;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class WebLogicMon {

	private MBeanServerConnection connection;
	private JMXConnector connector;
	private ObjectName service;

	public synchronized String getDate() {
		return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
	}

	public WebLogicMon(String address, int port, String user, String password) throws IOException {

		try {
			service = new ObjectName(
					"com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");

		} catch (MalformedObjectNameException malformedObject) {
			throw new AssertionError(malformedObject.getMessage());
		}

		String protocol = "t3";
		String resource = "/jndi/";
		String uri = "weblogic.management.mbeanservers.domainruntime";

		Map<String, String> map = new HashMap<String, String>(4);
		map.put("java.naming.security.principal", user);
		map.put("java.naming.security.credentials", password);
		map.put("jmx.remote.protocol.provider.pkgs", "weblogic.management.remote");

		JMXServiceURL jmxService = new JMXServiceURL(protocol, address, port, resource + uri);
		connector = JMXConnectorFactory.connect(jmxService, map);
		connection = connector.getMBeanServerConnection();
	}

	public ObjectName[] getServerRuntimes() throws Exception {
		// (String) connection.getAttribute(arrayOfObjectName[j], "State");
		return (ObjectName[]) connection.getAttribute(service, "ServerRuntimes");
	}

	public void threadPoolRuntime(ObjectName[] runtime) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currObject : runtime) {
			String str = (String) connection.getAttribute(currObject, "Name");
			String host = (String) connection.getAttribute(currObject, "ListenAddress");
			ObjectName threadPool = (ObjectName) connection.getAttribute(currObject, "ThreadPoolRuntime");
			System.out.println(getDate() + ";" + host + ";" + str + ";"
					+ connection.getAttribute(threadPool, "CompletedRequestCount") + ";"
					+ connection.getAttribute(threadPool, "ExecuteThreadTotalCount") + ";"
					+ connection.getAttribute(threadPool, "ExecuteThreadIdleCount") + ";"
					+ connection.getAttribute(threadPool, "HoggingThreadCount") + ";"
					+ connection.getAttribute(threadPool, "PendingUserRequestCount") + ";"
					+ connection.getAttribute(threadPool, "QueueLength") + ";"
					+ connection.getAttribute(threadPool, "StandbyThreadCount") + ";"
					+ connection.getAttribute(threadPool, "Throughput"));
		}
	}

	public void getJvmRuntime(ObjectName[] runtime) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currObject : runtime) {
			String str = (String) connection.getAttribute(currObject, "Name");
			String host = (String) connection.getAttribute(currObject, "ListenAddress");
			ObjectName jvmRT = (ObjectName) connection.getAttribute(currObject, "JVMRuntime");
			System.out.println(
					getDate() + ";" + host + ";" + str + ";" + connection.getAttribute(jvmRT, "HeapFreeCurrent") + ";"
							+ connection.getAttribute(jvmRT, "HeapFreePercent") + ";"
							+ connection.getAttribute(jvmRT, "HeapSizeCurrent") + ";"
							+ connection.getAttribute(jvmRT, "HeapSizeMax"));
		}
	}

	public void getJmsRuntime(ObjectName[] runtime) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {

		for (ObjectName currObject : runtime) {
			String str = (String) connection.getAttribute(currObject, "Name");
			String host = (String) connection.getAttribute(currObject, "ListenAddress");
			ObjectName jmsRT = (ObjectName) connection.getAttribute(currObject, "JMSRuntime");
			ObjectName[] jmsSrv = (ObjectName[]) connection.getAttribute(jmsRT, "JMSServers");

			for (ObjectName currJMSSrv : jmsSrv) {
				ObjectName[] destinations = (ObjectName[]) connection.getAttribute(currJMSSrv, "Destinations");

				for (ObjectName currDestination : destinations) {
					System.out.println(
							getDate() + ";" + host + ";" + str + ";" + connection.getAttribute(currJMSSrv, "Name") + ";"
									+ connection.getAttribute(currDestination, "Name") + ";"
									+ connection.getAttribute(currDestination, "MessagesCurrentCount") + ";"
									+ connection.getAttribute(currDestination, "MessagesPendingCount") + ";"
									+ connection.getAttribute(currDestination, "MessagesHighCount") + ";"
									+ connection.getAttribute(currDestination, "MessagesReceivedCount"));
				}
			}
		}
	}

	public void getJdbcRuntime(ObjectName[] runtime) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException, MalformedObjectNameException {
		int i = runtime.length;
		for (int j = 0; j < i; j++) {
			String str = (String) connection.getAttribute(runtime[j], "Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) connection.getAttribute(new ObjectName(
					"com.bea:Name=" + str + ",ServerRuntime=" + str + ",Location=" + str + ",Type=JDBCServiceRuntime"),
					"JDBCDataSourceRuntimeMBeans");
			String host = (String) connection.getAttribute(runtime[j], "ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				System.out.println(getDate() + ";" + host + ";" + str + ";"
						+ (String) connection.getAttribute(arrayOfObjectName2[m], "Name") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "ActiveConnectionsCurrentCount") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitSecondsHighCount") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitingForConnectionCurrentCount") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitingForConnectionFailureTotal") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitingForConnectionTotal") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitingForConnectionHighCount"));
			}
		}
	}

	public void getServletData(ObjectName[] runtime) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		int i = runtime.length;
		for (int j = 0; j < i; j++) {
			// String str1 = (String) connection.getAttribute(arrayOfObjectName1[j],"Name");
			// String host = (String)
			// connection.getAttribute(arrayOfObjectName1[j],"ListenAddress");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) connection.getAttribute(runtime[j], "ApplicationRuntimes");

			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				System.out.println(
						"Application name: " + (String) connection.getAttribute(arrayOfObjectName2[m], "Name"));
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) connection.getAttribute(arrayOfObjectName2[m],
						"ComponentRuntimes");
				int n = arrayOfObjectName3.length;
				for (int i1 = 0; i1 < n; i1++) {
					System.out.println(
							"Component name: " + (String) connection.getAttribute(arrayOfObjectName3[i1], "Name"));
					String str2 = (String) connection.getAttribute(arrayOfObjectName3[i1], "Type");
					if ("WebAppComponentRuntime".equals(str2)) {
						ObjectName[] arrayOfObjectName4 = (ObjectName[]) connection.getAttribute(arrayOfObjectName3[i1],
								"Servlets");
						int i2 = arrayOfObjectName4.length;
						for (int i3 = 0; i3 < i2; i3++) {
							System.out.println("Servlet name: "
									+ (String) connection.getAttribute(arrayOfObjectName4[i3], "Name"));
							System.out.println("Servlet context path: "
									+ (String) connection.getAttribute(arrayOfObjectName4[i3], "ContextPath"));
							System.out.println("Invocation Total Count : "
									+ connection.getAttribute(arrayOfObjectName4[i3], "InvocationTotalCount"));
						}
					}
				}
			}
		}
	}

	public void getEJBData(ObjectName[] runtime) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		int i = runtime.length;
		for (int j = 0; j < i; j++) {
			String str1 = (String) connection.getAttribute(runtime[j], "Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) connection.getAttribute(runtime[j], "ApplicationRuntimes");
			String host = (String) connection.getAttribute(runtime[j], "ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) connection.getAttribute(arrayOfObjectName2[m],
						"ComponentRuntimes");
				int n = arrayOfObjectName3.length;
				for (int i1 = 0; i1 < n; i1++) {
					String str2 = (String) connection.getAttribute(arrayOfObjectName3[i1], "Type");

					if ("EJBComponentRuntime".equals(str2)) {
						ObjectName[] arrayOfObjectName4 = (ObjectName[]) connection.getAttribute(arrayOfObjectName3[i1],
								"EJBRuntimes");
						int i2 = arrayOfObjectName4.length;
						for (int i3 = 0; i3 < i2; i3++) {
							ObjectName localObjectName = (ObjectName) connection.getAttribute(arrayOfObjectName4[i3],
									"PoolRuntime");

							System.out.println(getDate() + ";" + host + ";" + str1 + ";"
									+ (String) connection.getAttribute(arrayOfObjectName2[m], "Name") + ";"
									+ (String) connection.getAttribute(localObjectName, "Name") + ";"
									+ connection.getAttribute(localObjectName, "AccessTotalCount") + ";"
									+ connection.getAttribute(localObjectName, "MissTotalCount") + ";"
									+ connection.getAttribute(localObjectName, "DestroyedTotalCount") + ";"
									+ connection.getAttribute(localObjectName, "PooledBeansCurrentCount") + ";"
									+ connection.getAttribute(localObjectName, "BeansInUseCurrentCount") + ";"
									+ connection.getAttribute(localObjectName, "WaiterCurrentCount") + ";"
									+ connection.getAttribute(localObjectName, "TimeoutTotalCount"));
						}
					}
				}
			}
		}
	}

	public void getWeb(ObjectName[] runtime) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		int i = runtime.length;

		for (int j = 0; j < i; j++) {
			String str1 = (String) connection.getAttribute(runtime[j], "Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) connection.getAttribute(runtime[j], "ApplicationRuntimes");
			String host = (String) connection.getAttribute(runtime[j], "ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) connection.getAttribute(arrayOfObjectName2[m],
						"WorkManagerRuntimes");

				int n = arrayOfObjectName3.length;
				for (int i1 = 0; i1 < n; i1++) {
					System.out.println(getDate() + ";" + host + ";" + str1 + ";"
							+ (String) connection.getAttribute(arrayOfObjectName2[m], "Name") + ";"
							+ (String) connection.getAttribute(arrayOfObjectName3[i1], "Name") + ";"
							+ Integer.parseInt(
									(String) connection.getAttribute(arrayOfObjectName3[i1], "PendingRequests"))
							+ ";"
							+ Integer.parseInt(
									(String) connection.getAttribute(arrayOfObjectName3[i1], "CompletedRequests"))
							+ ";" + Integer.parseInt(
									(String) connection.getAttribute(arrayOfObjectName3[i1], "StuckThreadCount")));
				}

				ObjectName[] arrayOfObjectName4 = (ObjectName[]) connection.getAttribute(arrayOfObjectName2[m],
						"ComponentRuntimes");
				n = arrayOfObjectName4.length;
				for (int i2 = 0; i2 < n; i2++) {
					String str2 = (String) connection.getAttribute(arrayOfObjectName4[i2], "Type");

					if ("WebAppComponentRuntime".equals(str2)) {
						System.out.println(getDate() + ";" + host + ";" + str1 + ";"
								+ (String) connection.getAttribute(arrayOfObjectName2[m], "Name") + ";"
								+ (String) connection.getAttribute(arrayOfObjectName4[i2], "ComponentName") + ";"
								+ Integer.parseInt((String) connection.getAttribute(arrayOfObjectName4[i2],
										"OpenSessionsCurrentCount"))
								+ ";" + Integer.parseInt((String) connection.getAttribute(arrayOfObjectName4[i2],
										"SessionsOpenedTotalCount")));
					}
				}
			}
		}
	}

	public void getCluster(ObjectName[] runtime) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName localObjectName1 : runtime) {
			String str = (String) connection.getAttribute(localObjectName1, "Name");
			String host = (String) connection.getAttribute(localObjectName1, "ListenAddress");
			ObjectName localObjectName2 = (ObjectName) connection.getAttribute(localObjectName1, "ClusterRuntime");
			if (localObjectName2 != null) {
				System.out.println(getDate() + ";" + host + ";" + str + ";"
						+ (String) connection.getAttribute(localObjectName2, "Name") + ";"
						+ Integer.parseInt((String) connection.getAttribute(localObjectName2, "ResendRequestsCount"))
						+ ";"
						+ Integer.parseInt(
								(String) connection.getAttribute(localObjectName2, "ForeignFragmentsDroppedCount"))
						+ ";"
						+ Integer.parseInt((String) connection.getAttribute(localObjectName2, "FragmentsReceivedCount"))
						+ ";"
						+ Integer.parseInt((String) connection.getAttribute(localObjectName2, "FragmentsSentCount"))
						+ ";" + connection.getAttribute(localObjectName2, "MulticastMessagesLostCount"));
			}
		}
	}

	public boolean close() {
		try {
			connector.close();
			return true;

		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.out.println("Usage: java WebLogicMon.jar adm-host adm-port adm-username adm-password");
			System.exit(0);
		}

		String address = args[0];
		int port = Integer.parseInt(args[1]);
		String user = args[2];
		String password = args[3];

		WebLogicMon webLogicMon = new WebLogicMon(address, port, user, password);
		ObjectName[] runtime = webLogicMon.getServerRuntimes();
		webLogicMon.threadPoolRuntime(runtime);
		webLogicMon.getJvmRuntime(runtime);
		webLogicMon.getJdbcRuntime(runtime);
		webLogicMon.getJmsRuntime(runtime);
		webLogicMon.getEJBData(runtime);
		webLogicMon.getWeb(runtime);
		webLogicMon.getCluster(runtime);
		webLogicMon.close();
	}

}
