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

	public WebLogicMon(String address, int port, String user, String password) throws IOException {
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

	private synchronized String getDate() {
		return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
	}

	private Object getValue(ObjectName mbean, String attribute) throws AttributeNotFoundException,
			InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		return connection.getAttribute(mbean, attribute);
	}

	private ObjectName[] getServerRuntimes() throws MalformedObjectNameException, MBeanException,
			AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
		// (String) getValue(arrayOfObjectName[j], "State");
		ObjectName service = new ObjectName(
				"com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");

		return (ObjectName[]) getValue(service, "ServerRuntimes");
	}

	private void getThreadPoolInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currObject : serverMBean) {
			String str = (String) getValue(currObject, "Name");
			String host = (String) getValue(currObject, "ListenAddress");
			ObjectName threadPool = (ObjectName) getValue(currObject, "ThreadPoolRuntime");
			System.out.println(getDate() + ";" + host + ";" + str + ";" + getValue(threadPool, "CompletedRequestCount")
					+ ";" + getValue(threadPool, "ExecuteThreadTotalCount") + ";"
					+ getValue(threadPool, "ExecuteThreadIdleCount") + ";" + getValue(threadPool, "HoggingThreadCount")
					+ ";" + getValue(threadPool, "PendingUserRequestCount") + ";" + getValue(threadPool, "QueueLength")
					+ ";" + getValue(threadPool, "StandbyThreadCount") + ";" + getValue(threadPool, "Throughput"));
		}
	}

	private void getJVMInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currObject : serverMBean) {
			String str = (String) getValue(currObject, "Name");
			String host = (String) getValue(currObject, "ListenAddress");
			ObjectName jvmRT = (ObjectName) getValue(currObject, "JVMRuntime");
			System.out.println(getDate() + ";" + host + ";" + str + ";" + getValue(jvmRT, "HeapFreeCurrent") + ";"
					+ getValue(jvmRT, "HeapFreePercent") + ";" + getValue(jvmRT, "HeapSizeCurrent") + ";"
					+ getValue(jvmRT, "HeapSizeMax"));
		}
	}

	private void getJMSInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {

		for (ObjectName currObject : serverMBean) {
			String str = (String) getValue(currObject, "Name");
			String host = (String) getValue(currObject, "ListenAddress");
			ObjectName jmsRT = (ObjectName) getValue(currObject, "JMSRuntime");
			ObjectName[] jmsSrv = (ObjectName[]) getValue(jmsRT, "JMSServers");

			for (ObjectName currJMSSrv : jmsSrv) {
				ObjectName[] destinations = (ObjectName[]) getValue(currJMSSrv, "Destinations");

				for (ObjectName currDestination : destinations) {
					System.out.println(getDate() + ";" + host + ";" + str + ";" + getValue(currJMSSrv, "Name") + ";"
							+ getValue(currDestination, "Name") + ";"
							+ getValue(currDestination, "MessagesCurrentCount") + ";"
							+ getValue(currDestination, "MessagesPendingCount") + ";"
							+ getValue(currDestination, "MessagesHighCount") + ";"
							+ getValue(currDestination, "MessagesReceivedCount"));
				}
			}
		}
	}

	private void getJDBCInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException, MalformedObjectNameException {
		int i = serverMBean.length;
		for (int j = 0; j < i; j++) {
			String str = (String) getValue(serverMBean[j], "Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) getValue(new ObjectName(
					"com.bea:Name=" + str + ",ServerRuntime=" + str + ",Location=" + str + ",Type=JDBCServiceRuntime"),
					"JDBCDataSourceRuntimeMBeans");
			String host = (String) getValue(serverMBean[j], "ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				System.out.println(
						getDate() + ";" + host + ";" + str + ";" + (String) getValue(arrayOfObjectName2[m], "Name")
								+ ";" + getValue(arrayOfObjectName2[m], "ActiveConnectionsCurrentCount") + ";"
								+ getValue(arrayOfObjectName2[m], "WaitSecondsHighCount") + ";"
								+ getValue(arrayOfObjectName2[m], "WaitingForConnectionCurrentCount") + ";"
								+ getValue(arrayOfObjectName2[m], "WaitingForConnectionFailureTotal") + ";"
								+ getValue(arrayOfObjectName2[m], "WaitingForConnectionTotal") + ";"
								+ getValue(arrayOfObjectName2[m], "WaitingForConnectionHighCount"));
			}
		}
	}

	private void getServletInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		int i = serverMBean.length;
		for (int j = 0; j < i; j++) {
			// String str1 = (String) getValue(arrayOfObjectName1[j],"Name");
			// String host = (String) getValue(arrayOfObjectName1[j],"ListenAddress");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) getValue(serverMBean[j], "ApplicationRuntimes");

			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				System.out.println("Application name: " + (String) getValue(arrayOfObjectName2[m], "Name"));
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) getValue(arrayOfObjectName2[m], "ComponentRuntimes");
				int n = arrayOfObjectName3.length;
				for (int i1 = 0; i1 < n; i1++) {
					System.out.println("Component name: " + (String) getValue(arrayOfObjectName3[i1], "Name"));
					String str2 = (String) getValue(arrayOfObjectName3[i1], "Type");
					if ("WebAppComponentRuntime".equals(str2)) {
						ObjectName[] arrayOfObjectName4 = (ObjectName[]) getValue(arrayOfObjectName3[i1], "Servlets");
						int i2 = arrayOfObjectName4.length;
						for (int i3 = 0; i3 < i2; i3++) {
							System.out.println("Servlet name: " + (String) getValue(arrayOfObjectName4[i3], "Name"));
							System.out.println("Servlet context path: "
									+ (String) getValue(arrayOfObjectName4[i3], "ContextPath"));
							System.out.println("Invocation Total Count : "
									+ getValue(arrayOfObjectName4[i3], "InvocationTotalCount"));
						}
					}
				}
			}
		}
	}

	private void getEJBInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		int i = serverMBean.length;
		for (int j = 0; j < i; j++) {
			String str1 = (String) getValue(serverMBean[j], "Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) getValue(serverMBean[j], "ApplicationRuntimes");
			String host = (String) getValue(serverMBean[j], "ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) getValue(arrayOfObjectName2[m], "ComponentRuntimes");
				int n = arrayOfObjectName3.length;
				for (int i1 = 0; i1 < n; i1++) {
					String str2 = (String) getValue(arrayOfObjectName3[i1], "Type");

					if ("EJBComponentRuntime".equals(str2)) {
						ObjectName[] arrayOfObjectName4 = (ObjectName[]) getValue(arrayOfObjectName3[i1],
								"EJBRuntimes");
						int i2 = arrayOfObjectName4.length;
						for (int i3 = 0; i3 < i2; i3++) {
							ObjectName localObjectName = (ObjectName) getValue(arrayOfObjectName4[i3], "PoolRuntime");

							System.out.println(getDate() + ";" + host + ";" + str1 + ";"
									+ (String) getValue(arrayOfObjectName2[m], "Name") + ";"
									+ (String) getValue(localObjectName, "Name") + ";"
									+ getValue(localObjectName, "AccessTotalCount") + ";"
									+ getValue(localObjectName, "MissTotalCount") + ";"
									+ getValue(localObjectName, "DestroyedTotalCount") + ";"
									+ getValue(localObjectName, "PooledBeansCurrentCount") + ";"
									+ getValue(localObjectName, "BeansInUseCurrentCount") + ";"
									+ getValue(localObjectName, "WaiterCurrentCount") + ";"
									+ getValue(localObjectName, "TimeoutTotalCount"));
						}
					}
				}
			}
		}
	}

	private void getWebInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		int i = serverMBean.length;

		for (int j = 0; j < i; j++) {
			String str1 = (String) getValue(serverMBean[j], "Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) getValue(serverMBean[j], "ApplicationRuntimes");
			String host = (String) getValue(serverMBean[j], "ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) getValue(arrayOfObjectName2[m], "WorkManagerRuntimes");

				int n = arrayOfObjectName3.length;
				for (int i1 = 0; i1 < n; i1++) {
					System.out.println(getDate() + ";" + host + ";" + str1 + ";"
							+ (String) getValue(arrayOfObjectName2[m], "Name") + ";"
							+ (String) getValue(arrayOfObjectName3[i1], "Name") + ";"
							+ Integer.parseInt((String) getValue(arrayOfObjectName3[i1], "PendingRequests")) + ";"
							+ Integer.parseInt((String) getValue(arrayOfObjectName3[i1], "CompletedRequests")) + ";"
							+ Integer.parseInt((String) getValue(arrayOfObjectName3[i1], "StuckThreadCount")));
				}

				ObjectName[] arrayOfObjectName4 = (ObjectName[]) getValue(arrayOfObjectName2[m], "ComponentRuntimes");
				n = arrayOfObjectName4.length;
				for (int i2 = 0; i2 < n; i2++) {
					String str2 = (String) getValue(arrayOfObjectName4[i2], "Type");

					if ("WebAppComponentRuntime".equals(str2)) {
						System.out.println(getDate() + ";" + host + ";" + str1 + ";"
								+ (String) getValue(arrayOfObjectName2[m], "Name") + ";"
								+ (String) getValue(arrayOfObjectName4[i2], "ComponentName") + ";"
								+ Integer
										.parseInt((String) getValue(arrayOfObjectName4[i2], "OpenSessionsCurrentCount"))
								+ ";" + Integer.parseInt(
										(String) getValue(arrayOfObjectName4[i2], "SessionsOpenedTotalCount")));
					}
				}
			}
		}
	}

	private void getClusterInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName localObjectName1 : serverMBean) {
			String str = (String) getValue(localObjectName1, "Name");
			String host = (String) getValue(localObjectName1, "ListenAddress");
			ObjectName localObjectName2 = (ObjectName) getValue(localObjectName1, "ClusterRuntime");
			if (localObjectName2 != null) {
				System.out.println(
						getDate() + ";" + host + ";" + str + ";" + (String) getValue(localObjectName2, "Name") + ";"
								+ Integer.parseInt((String) getValue(localObjectName2, "ResendRequestsCount")) + ";"
								+ Integer.parseInt((String) getValue(localObjectName2, "ForeignFragmentsDroppedCount"))
								+ ";" + Integer.parseInt((String) getValue(localObjectName2, "FragmentsReceivedCount"))
								+ ";" + Integer.parseInt((String) getValue(localObjectName2, "FragmentsSentCount"))
								+ ";" + getValue(localObjectName2, "MulticastMessagesLostCount"));
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
		ObjectName[] serverMBean = webLogicMon.getServerRuntimes();
		webLogicMon.getThreadPoolInfo(serverMBean);
		webLogicMon.getJVMInfo(serverMBean);
		webLogicMon.getJDBCInfo(serverMBean);
		webLogicMon.getServletInfo(serverMBean);
		webLogicMon.getJMSInfo(serverMBean);
		webLogicMon.getEJBInfo(serverMBean);
		webLogicMon.getWebInfo(serverMBean);
		webLogicMon.getClusterInfo(serverMBean);
		webLogicMon.close();
	}

}
