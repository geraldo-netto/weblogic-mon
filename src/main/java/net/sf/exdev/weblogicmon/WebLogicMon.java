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
		// (String) getValue(serverMBean[j], "State");
		ObjectName service = new ObjectName(
				"com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");

		return (ObjectName[]) getValue(service, "ServerRuntimes");
	}

	private void getThreadPoolInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currMBean : serverMBean) {
			String server = (String) getValue(currMBean, "Name");
			String host = (String) getValue(currMBean, "ListenAddress");
			ObjectName threadPoolMBean = (ObjectName) getValue(currMBean, "ThreadPoolRuntime");
			System.out.println(getDate() + ";" + host + ";" + server + ";"
					+ getValue(threadPoolMBean, "CompletedRequestCount") + ";"
					+ getValue(threadPoolMBean, "ExecuteThreadTotalCount") + ";"
					+ getValue(threadPoolMBean, "ExecuteThreadIdleCount") + ";"
					+ getValue(threadPoolMBean, "HoggingThreadCount") + ";"
					+ getValue(threadPoolMBean, "PendingUserRequestCount") + ";"
					+ getValue(threadPoolMBean, "QueueLength") + ";" + getValue(threadPoolMBean, "StandbyThreadCount")
					+ ";" + getValue(threadPoolMBean, "Throughput"));
		}
	}

	private void getJVMInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currMBean : serverMBean) {
			String server = (String) getValue(currMBean, "Name");
			String host = (String) getValue(currMBean, "ListenAddress");
			ObjectName jvmMBean = (ObjectName) getValue(currMBean, "JVMRuntime");
			System.out.println(getDate() + ";" + host + ";" + server + ";" + getValue(jvmMBean, "HeapFreeCurrent") + ";"
					+ getValue(jvmMBean, "HeapFreePercent") + ";" + getValue(jvmMBean, "HeapSizeCurrent") + ";"
					+ getValue(jvmMBean, "HeapSizeMax"));
		}
	}

	private void getJMSInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currMBean : serverMBean) {
			String server = (String) getValue(currMBean, "Name");
			String host = (String) getValue(currMBean, "ListenAddress");
			ObjectName jms = (ObjectName) getValue(currMBean, "JMSRuntime");
			ObjectName[] jmsSrvs = (ObjectName[]) getValue(jms, "JMSServers");

			for (ObjectName currJMSSrv : jmsSrvs) {
				ObjectName[] destinationsMBean = (ObjectName[]) getValue(currJMSSrv, "Destinations");

				for (ObjectName currDestination : destinationsMBean) {
					System.out.println(getDate() + ";" + host + ";" + server + ";" + getValue(currJMSSrv, "Name") + ";"
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
		for (ObjectName currMBean : serverMBean) {
			String server = (String) getValue(currMBean, "Name");
			String host = (String) getValue(currMBean, "ListenAddress");

			ObjectName[] jdbcsMBean = (ObjectName[]) getValue(new ObjectName("com.bea:Name=" + server
					+ ",ServerRuntime=" + server + ",Location=" + server + ",Type=JDBCServiceRuntime"),
					"JDBCDataSourceRuntimeMBeans");
			for (ObjectName currJDBC : jdbcsMBean) {
				System.out.println(getDate() + ";" + host + ";" + server + ";" + (String) getValue(currJDBC, "Name")
						+ ";" + getValue(currJDBC, "ActiveConnectionsCurrentCount") + ";"
						+ getValue(currJDBC, "WaitSecondsHighCount") + ";"
						+ getValue(currJDBC, "WaitingForConnectionCurrentCount") + ";"
						+ getValue(currJDBC, "WaitingForConnectionFailureTotal") + ";"
						+ getValue(currJDBC, "WaitingForConnectionTotal") + ";"
						+ getValue(currJDBC, "WaitingForConnectionHighCount"));
			}
		}
	}

	private void getServletInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currMBean : serverMBean) {
			ObjectName[] appsMBean = (ObjectName[]) getValue(currMBean, "ApplicationRuntimes");
			for (ObjectName currApp : appsMBean) {
				System.out.println("Application name: " + (String) getValue(currApp, "Name"));

				ObjectName[] componentsMBean = (ObjectName[]) getValue(currApp, "ComponentRuntimes");
				for (ObjectName currComponent : componentsMBean) {
					System.out.println("Component: " + (String) getValue(currComponent, "Name"));

					String type = (String) getValue(currComponent, "Type");
					if ("WebAppComponentRuntime".equals(type)) {
						ObjectName[] servletsMBean = (ObjectName[]) getValue(currComponent, "Servlets");
						for (ObjectName currServlet : servletsMBean) {
							System.out.println("Servlet: " + (String) getValue(currServlet, "Name"));
							System.out.println("Servlet path: " + (String) getValue(currServlet, "ContextPath"));
							System.out.println("Invocation Count: " + getValue(currServlet, "InvocationTotalCount"));
						}
					}
				}
			}
		}
	}

	private void getEJBInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currMBean : serverMBean) {
			String server = (String) getValue(currMBean, "Name");
			String host = (String) getValue(currMBean, "ListenAddress");

			ObjectName[] appsMBean = (ObjectName[]) getValue(currMBean, "ApplicationRuntimes");
			for (ObjectName currApp : appsMBean) {

				ObjectName[] componentsMBean = (ObjectName[]) getValue(currApp, "ComponentRuntimes");
				for (ObjectName currComponent : componentsMBean) {
					String type = (String) getValue(currComponent, "Type");

					if ("EJBComponentRuntime".equals(type)) {
						ObjectName[] ejbsMBean = (ObjectName[]) getValue(currComponent, "EJBRuntimes");
						for (ObjectName currEJB : ejbsMBean) {
							ObjectName pool = (ObjectName) getValue(currEJB, "PoolRuntime");

							System.out.println(getDate() + ";" + host + ";" + server + ";"
									+ (String) getValue(currApp, "Name") + ";" + (String) getValue(pool, "Name") + ";"
									+ getValue(pool, "AccessTotalCount") + ";" + getValue(pool, "MissTotalCount") + ";"
									+ getValue(pool, "DestroyedTotalCount") + ";"
									+ getValue(pool, "PooledBeansCurrentCount") + ";"
									+ getValue(pool, "BeansInUseCurrentCount") + ";"
									+ getValue(pool, "WaiterCurrentCount") + ";" + getValue(pool, "TimeoutTotalCount"));
						}
					}
				}
			}
		}
	}

	private void getWebInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currMBean : serverMBean) {
			String server = (String) getValue(currMBean, "Name");
			String host = (String) getValue(currMBean, "ListenAddress");

			ObjectName[] appsMBean = (ObjectName[]) getValue(currMBean, "ApplicationRuntimes");
			for (ObjectName currApp : appsMBean) {
				ObjectName[] workManagersMBean = (ObjectName[]) getValue(currApp, "WorkManagerRuntimes");
				for (ObjectName currWorkMan : workManagersMBean) {
					System.out.println(getDate() + ";" + host + ";" + server + ";" + (String) getValue(currApp, "Name")
							+ ";" + (String) getValue(currWorkMan, "Name") + ";"
							+ Integer.parseInt((String) getValue(currWorkMan, "PendingRequests")) + ";"
							+ Integer.parseInt((String) getValue(currWorkMan, "CompletedRequests")) + ";"
							+ Integer.parseInt((String) getValue(currWorkMan, "StuckThreadCount")));
				}

				ObjectName[] componentsMBean = (ObjectName[]) getValue(currApp, "ComponentRuntimes");
				for (ObjectName currComponent : componentsMBean) {
					String type = (String) getValue(currComponent, "Type");

					if ("WebAppComponentRuntime".equals(type)) {
						System.out.println(getDate() + ";" + host + ";" + server + ";"
								+ (String) getValue(currApp, "Name") + ";"
								+ (String) getValue(currComponent, "ComponentName") + ";"
								+ Integer.parseInt((String) getValue(currComponent, "OpenSessionsCurrentCount")) + ";"
								+ Integer.parseInt((String) getValue(currComponent, "SessionsOpenedTotalCount")));
					}
				}
			}
		}
	}

	private void getClusterInfo(ObjectName[] serverMBean) throws MBeanException, AttributeNotFoundException,
			InstanceNotFoundException, ReflectionException, IOException {
		for (ObjectName currMBean : serverMBean) {
			String server = (String) getValue(currMBean, "Name");
			String host = (String) getValue(currMBean, "ListenAddress");
			ObjectName clusterMBean = (ObjectName) getValue(currMBean, "ClusterRuntime");
			if (clusterMBean != null) {
				System.out.println(getDate() + ";" + host + ";" + server + ";" + (String) getValue(clusterMBean, "Name")
						+ ";" + Integer.parseInt((String) getValue(clusterMBean, "ResendRequestsCount")) + ";"
						+ Integer.parseInt((String) getValue(clusterMBean, "ForeignFragmentsDroppedCount")) + ";"
						+ Integer.parseInt((String) getValue(clusterMBean, "FragmentsReceivedCount")) + ";"
						+ Integer.parseInt((String) getValue(clusterMBean, "FragmentsSentCount")) + ";"
						+ getValue(clusterMBean, "MulticastMessagesLostCount"));
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
